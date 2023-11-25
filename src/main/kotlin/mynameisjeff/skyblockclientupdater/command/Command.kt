package mynameisjeff.skyblockclientupdater.command

import cc.polyfrost.oneconfig.utils.commands.annotations.Command
import cc.polyfrost.oneconfig.utils.commands.annotations.Main
import mynameisjeff.skyblockclientupdater.config.Config

@Command("skyclientupdater", aliases = ["skyblockclientupdater", "scu"])
object Command {

    @Main
    private fun main() {
        Config.openGui()
    }
}