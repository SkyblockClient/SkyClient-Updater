package mynameisjeff.skyblockclientupdater

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.serializersModuleOf
import mynameisjeff.skyblockclientupdater.command.Command
import mynameisjeff.skyblockclientupdater.config.Config
import mynameisjeff.skyblockclientupdater.data.FileSerializer
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.ProgressManager
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.awt.Color

@Mod(
    name = "SkyClient Updater",
    version = SkyClientUpdater.VERSION,
    modid = "skyblockclientupdater",
    clientSideOnly = true,
    modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter"
) object SkyClientUpdater {
    const val VERSION = "1.3.2"

    val accentColor = Color(67, 184, 0)

    val mc: Minecraft
    get() = Minecraft.getMinecraft()

    val json = Json {
        serializersModule = serializersModuleOf(FileSerializer())
        ignoreUnknownKeys = true
    }

    @Mod.EventHandler
    fun on(event: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(EventListener())
        MinecraftForge.EVENT_BUS.register(UpdateChecker.INSTANCE)
        Command.register()
        Config.preload()

        val progress = ProgressManager.push("SkyClient Updater", 5)
        progress.step("Downloading helper utility")
        UpdateChecker.INSTANCE.downloadHelperTask()
        progress.step("Discovering mods")
        UpdateChecker.INSTANCE.getValidModFiles()
        progress.step("Fetching latest commit ID")
        UpdateChecker.INSTANCE.updateLatestCommitId()
        progress.step("Fetching latest versions")
        UpdateChecker.INSTANCE.getLatestMods()
        progress.step("Comparing versions")
        UpdateChecker.INSTANCE.getUpdateCandidates()
        ProgressManager.pop(progress)
    }

}
