package mynameisjeff.skyblockclientupdater.gui.screens

import cc.polyfrost.oneconfig.utils.dsl.tick
import cc.polyfrost.oneconfig.libs.elementa.components.UIBlock
import cc.polyfrost.oneconfig.libs.elementa.components.UIContainer
import cc.polyfrost.oneconfig.libs.elementa.components.UIText
import cc.polyfrost.oneconfig.libs.elementa.components.Window
import cc.polyfrost.oneconfig.libs.elementa.constraints.CenterConstraint
import cc.polyfrost.oneconfig.libs.elementa.constraints.ChildBasedSizeConstraint
import cc.polyfrost.oneconfig.libs.elementa.constraints.SiblingConstraint
import cc.polyfrost.oneconfig.libs.elementa.constraints.animation.Animations
import cc.polyfrost.oneconfig.libs.elementa.dsl.*
import cc.polyfrost.oneconfig.libs.elementa.effects.OutlineEffect
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.launch
import mynameisjeff.skyblockclientupdater.SkyClientUpdater
import mynameisjeff.skyblockclientupdater.gui.elements.SexyButton
import mynameisjeff.skyblockclientupdater.UpdateChecker
import mynameisjeff.skyblockclientupdater.data.UpdateMod
import net.minecraft.client.gui.GuiMainMenu
import org.apache.logging.log4j.LogManager
import java.awt.Color
import java.io.File

class DownloadProgressScreen(
    private val updating: HashSet<UpdateMod>
) : BaseScreen(
    useContentContainer = true
) {
    private val successfullyUpdated = mutableSetOf<UpdateMod>()
    private val failedUpdated = mutableSetOf<UpdateMod>()

    private var exited = false
    private var watchingFile: File? = null
    private var lastProgress: Int = -1
    private var progress: Int = -1

    val headerText = UIText("Updating your mods...").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    }.setTextScale(1.25f.pixels()) childOf headerContainer

    val currentlyUpdatingText = UIText().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf contentContainer
    val progressBarContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = SiblingConstraint(5f)
        width = 200.pixels()
        height = 10.pixels()
    } effect OutlineEffect(Color.BLACK, 2f) childOf contentContainer
    val progressBar = UIBlock(SkyClientUpdater.accentColor).constrain {
        width = 1.pixel()
        height = 10.pixels()
    } childOf progressBarContainer

    private val buttonContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ChildBasedSizeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf footerContainer
    private val cancelButton = SexyButton(
        text = "Cancel",
        outlineColor = Color.RED,
        primary = false
    ).constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = 150.pixels()
        height = 20.pixels()
    }.onMouseClick {
        exited = true
    } childOf buttonContainer

    init {
        cancelButton.setFloating(true)
        SkyClientUpdater.IO.launch {
            try {
                val directory = File(File(SkyClientUpdater.mc.mcDataDir, "skyclientupdater"), "updates")
                directory.mkdirs()
                for (update in updating) {
                    val jarName = update.name
                    val file = File(directory, jarName)
                    downloadUpdate(update, file)
                    if (!failedUpdated.contains(update)) {
                        UpdateChecker.INSTANCE.deleteFileOnShutdown(update.file, jarName)
                        successfullyUpdated.add(update)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private suspend fun downloadUpdate(update: UpdateMod, file: File) {
        try {
            currentlyUpdatingText.setText(update.name)
            val logger = LogManager.getLogger("SkyClientUpdater (Update Downloader)")
            progressBar.constrain {
                width = 0.pixels()
            }

            val urlName = update.updateURL.replace(" ", "%20")
            val url = Url(urlName)

            watchingFile = file
            lastProgress = 0
            progress = 0
            val download = SkyClientUpdater.client.get(url) {
                expectSuccess = false
                timeout {
                    connectTimeoutMillis = null
                    requestTimeoutMillis = null
                    socketTimeoutMillis = null
                }
                onDownload { bytesSentTotal, contentLength ->
                    progress = ((((bytesSentTotal * 0.000001) / (contentLength * 0.000001))) * 200).toInt()
                }
            }
            if (download.status != HttpStatusCode.OK) {
                failedUpdated.add(update)
                println("$url returned status code ${download.status}")
                watchingFile = null
                lastProgress = -1
                progress = -1
                return
            }

            update.file.parentFile.mkdirs()
            if (!update.file.exists() && !update.file.createNewFile()) {
                failedUpdated.add(update)
                logger.error("Couldn't create update directory/file for ${update.name}.")
                watchingFile = null
                lastProgress = -1
                progress = -1
                return
            }
            val bodyAsChannel = download.bodyAsChannel()
            if (bodyAsChannel.copyTo(file.writeChannel()) == 0L) {
                failedUpdated.add(update)
                logger.error("Couldn't download update for ${update.name}.")
                watchingFile = null
                lastProgress = -1
                progress = -1
                return
            }
            Thread.sleep(1000)
            watchingFile = null
            lastProgress = -1
            progress = -1
        } catch (ex: Exception) {
            ex.printStackTrace()
            failedUpdated.add(update)
        }
    }

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)
        watchingFile?.let {
            if (lastProgress != -1) {
                progress.let { size ->
                    if (size != lastProgress) {
                        lastProgress = size
                        progressBar.animate {
                            setWidthAnimation(
                                Animations.OUT_EXP,
                                0.1f,
                                size.pixels()
                            )
                        }
                    }
                }
            }
        }
        when {
            exited -> {
                val directory = File(File(SkyClientUpdater.mc.mcDataDir, "skyclientupdater"), "updates")
                if (directory.exists()) {
                    directory.listFiles()?.let {
                        for (file in it) {
                            file.delete()
                        }
                    }
                }
                displayScreen(GuiMainMenu())
            }
            successfullyUpdated.size + failedUpdated.size == updating.size -> {
                tick(5) {
                    displayScreen(
                        UpdateSummaryScreen(
                            successfullyUpdated as HashSet<UpdateMod>,
                            failedUpdated as HashSet<UpdateMod>
                        )
                    )
                }
            }
        }
    }
}