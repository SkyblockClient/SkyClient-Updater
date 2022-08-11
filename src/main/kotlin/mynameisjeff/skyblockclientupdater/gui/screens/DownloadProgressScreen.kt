package mynameisjeff.skyblockclientupdater.gui.screens

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UMatrixStack
import mynameisjeff.skyblockclientupdater.SkyClientUpdater
import mynameisjeff.skyblockclientupdater.gui.elements.SexyButton
import mynameisjeff.skyblockclientupdater.utils.TickTask
import mynameisjeff.skyblockclientupdater.UpdateChecker
import mynameisjeff.skyblockclientupdater.data.UpdateMod
import net.minecraft.client.gui.GuiMainMenu
import org.apache.logging.log4j.LogManager
import java.awt.Color
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class DownloadProgressScreen(
    private val updating: HashSet<UpdateMod>
) : BaseScreen(
    useContentContainer = true
) {
    private val successfullyUpdated = mutableSetOf<UpdateMod>()
    private val failedUpdated = mutableSetOf<UpdateMod>()

    private var exited = false

    private var watchingFile: File? = null
    private var currentProgress: Long = -1
    private var totalSize: Long = -1

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
        thread(name = "SkyClient Updater Thread") {
            try {
                val directory = File(File(SkyClientUpdater.mc.mcDataDir, "skyclientupdater"), "updates")
                directory.mkdirs()
                for (update in updating) {
                    val jarName = update.name
                    val file = File(directory, jarName)
                    downloadUpdate(update, file)
                    if (!failedUpdated.contains(update)) {
                        UpdateChecker.deleteFileOnShutdown(update.file, jarName)
                        successfullyUpdated.add(update)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun downloadUpdate(update: UpdateMod, file: File) {
        try {
            currentlyUpdatingText.setText(update.name)
            val logger = LogManager.getLogger("SkyClientUpdater (Update Downloader)")
            progressBar.constrain {
                width = 0.pixels()
            }
            val st = URL(update.updateURL).openConnection() as HttpURLConnection
            st.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2"
            )
            st.connect()
            if (st.responseCode != HttpURLConnection.HTTP_OK) {
                failedUpdated.add(update)
                logger.error(update.updateURL + " returned status code " + st.responseCode)
                return
            }
            update.file.parentFile.mkdirs()
            if (!update.file.exists() && !update.file.createNewFile()) {
                failedUpdated.add(update)
                logger.error("Couldn't create update directory/file for ${update.name}.")
                return
            }

            watchingFile = file
            currentProgress = 0
            totalSize = st.contentLengthLong
            st.inputStream.use { stream ->
                BufferedInputStream(stream).use { bufferedStream ->
                    FileOutputStream(file).use { output ->
                        val data = ByteArray(1024)
                        var count: Int
                        while (bufferedStream.read(data, 0, 1024).also { count = it } != -1) {
                            output.write(data, 0, count)
                        }
                    }
                }
            }
            Thread.sleep(1000)
            watchingFile = null
            currentProgress = -1
            totalSize = -1
        } catch (ex: Exception) {
            ex.printStackTrace()
            failedUpdated.add(update)
        }
    }

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)
        watchingFile?.let { file ->
            if (currentProgress != -1L && totalSize != -1L) {
                file.length().let { size ->
                    if (size != currentProgress) {
                        currentProgress = size
                        progressBar.animate {
                            setWidthAnimation(
                                Animations.OUT_EXP,
                                0.1f,
                                ((((size * 0.000001) / (totalSize * 0.000001))) * 200).pixels()
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
                TickTask(5) {
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