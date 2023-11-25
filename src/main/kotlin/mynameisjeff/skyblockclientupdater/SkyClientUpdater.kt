package mynameisjeff.skyblockclientupdater

import cc.polyfrost.oneconfig.utils.commands.CommandManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.serializersModuleOf
import mynameisjeff.skyblockclientupdater.command.Command
import mynameisjeff.skyblockclientupdater.config.Config
import mynameisjeff.skyblockclientupdater.data.FileSerializer
import mynameisjeff.skyblockclientupdater.utils.ssl.FixSSL
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.ProgressManager
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.awt.Color

@Mod(
    name = "@NAME@",
    version = SkyClientUpdater.VERSION,
    modid = "@ID@",
    clientSideOnly = true,
    modLanguageAdapter = "cc.polyfrost.oneconfig.utils.KotlinLanguageAdapter"
) object SkyClientUpdater {
    const val VERSION = "@VER"

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
        CommandManager.register(Command)
        Config.preload()

        val progress = ProgressManager.push("SkyClient Updater", 6)
        progress.step("Fixing Let's Encrypt SSL")
        FixSSL.fixup()
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
