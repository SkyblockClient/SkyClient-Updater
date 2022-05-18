package mynameisjeff.skyblockclientupdater

import kotlinx.serialization.json.Json
import mynameisjeff.skyblockclientupdater.utils.UpdateChecker
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
    const val VERSION = "@VERSION@"

    val accentColor = Color(67, 184, 0)

    val mc: Minecraft get() = Minecraft.getMinecraft()

    /* buggy but probably more performant :blamelily:
    val mc: Minecraft by lazy {
        Minecraft.getMinecraft()
    }*/

    val json = Json {
        ignoreUnknownKeys = true
    }

    @Mod.EventHandler
    fun on(event: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(EventListener())
        MinecraftForge.EVENT_BUS.register(UpdateChecker)

        val progress = ProgressManager.push("SkyClient Updater", 5)
        progress.step("Downloading helper utility")
        UpdateChecker.downloadHelperTask()
        progress.step("Discovering mods")
        UpdateChecker.getValidModFiles()
        progress.step("Fetching latest commit ID")
        UpdateChecker.updateLatestCommitId()
        progress.step("Fetching latest versions")
        UpdateChecker.getLatestMods()
        progress.step("Comparing versions")
        UpdateChecker.getUpdateCandidates()
        ProgressManager.pop(progress)
    }

}
