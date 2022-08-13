package mynameisjeff.skyblockclientupdater.config

import gg.essential.api.EssentialAPI
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.Property
import gg.essential.vigilance.data.PropertyType
import mynameisjeff.skyblockclientupdater.UpdateChecker
import mynameisjeff.skyblockclientupdater.gui.screens.ModUpdateScreen
import java.io.File

object Config : Vigilant(File(UpdateChecker.taskDir, "config.toml"), "SkyClient Updater") {
    @Property(
        type = PropertyType.SWITCH,
        name = "Show Button on Settings Menu",
        description = "Show the button on the settings menu",
        category = "General"
    )
    var showButtonOnEscapeMenu = true

    @Property(
        type = PropertyType.BUTTON,
        name = "Check for Updates",
        description = "Check for updates again. This might take a while after clicking this button.",
        category = "General"
    )
    fun checkForUpdates() {
        UpdateChecker.reset()
        if (UpdateChecker.INSTANCE.needsUpdate.isNotEmpty()) {
            EssentialAPI.getGuiUtil().openScreen(ModUpdateScreen(UpdateChecker.INSTANCE.needsUpdate))
        }
    }

    @Property(
        type = PropertyType.BUTTON,
        name = "Reset Ignored Updates",
        description = "Reset the ignored updates list. This might take a while after clicking this button.",
        category = "General"
    )
    fun resetIgnoredUpdates() {
        UpdateChecker.ignoredJson.delete()
        checkForUpdates()
    }

    init {
        initialize()
    }
}