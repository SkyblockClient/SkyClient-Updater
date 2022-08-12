package mynameisjeff.skyblockclientupdater.command

import gg.essential.api.EssentialAPI
import gg.essential.api.commands.DefaultHandler
import mynameisjeff.skyblockclientupdater.config.Config

object Command : gg.essential.api.commands.Command("skyblockclientupdater") {
    override val commandAliases = setOf(Alias("skyclientupdater"), Alias("scu"))

    @DefaultHandler
    fun default() {
        EssentialAPI.getGuiUtil().openScreen(Config.gui())
    }
}