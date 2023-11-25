package mynameisjeff.skyblockclientupdater.config

import cc.polyfrost.oneconfig.config.annotations.Button
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.migration.VigilanceMigrator
import cc.polyfrost.oneconfig.utils.gui.GuiUtils
import mynameisjeff.skyblockclientupdater.UpdateChecker
import mynameisjeff.skyblockclientupdater.gui.screens.ModUpdateScreen
import java.io.File

object Config : cc.polyfrost.oneconfig.config.Config(Mod("SkyClient Updater", ModType.UTIL_QOL, "/SkyClient.png", VigilanceMigrator(File(UpdateChecker.taskDir, "config.toml").absolutePath)), "SkyClientUpdater/config.json") {
    @Switch(
        name = "Show Button on Settings Menu",
        description = "Show the button on the settings menu",
        category = "General"
    )
    var showButtonOnEscapeMenu = true

    @Switch(
        name = "Enable SkyClient Beta",
        description = "Enable beta versions of SkyClient mods.",
        category = "General"
    )
    var enableBeta = false

    @Button(
        name = "Check for Updates",
        description = "Check for updates again. This might take a while after clicking this button.",
        text = "Check",
        category = "General"
    )
    fun checkForUpdates() {
        UpdateChecker.reset()
        if (UpdateChecker.INSTANCE.needsUpdate.isNotEmpty()) {
            GuiUtils.displayScreen(ModUpdateScreen(UpdateChecker.INSTANCE.needsUpdate))
        }
    }

    @Button(
        name = "Reset Ignored Updates",
        description = "Reset the ignored updates list. This might take a while after clicking this button.",
        text = "Reset",
        category = "General"
    )
    fun resetIgnoredUpdates() {
        UpdateChecker.ignoredJson.delete()
        checkForUpdates()
    }

    init {
        initialize()
        addListener("enableBeta") {
            UpdateChecker.reset()
            if (UpdateChecker.INSTANCE.needsUpdate.isNotEmpty()) {
                GuiUtils.displayScreen(ModUpdateScreen(UpdateChecker.INSTANCE.needsUpdate))
            }
        }
    }
}