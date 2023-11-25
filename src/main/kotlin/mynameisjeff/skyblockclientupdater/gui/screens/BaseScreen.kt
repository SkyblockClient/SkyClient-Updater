package mynameisjeff.skyblockclientupdater.gui.screens

import cc.polyfrost.oneconfig.libs.elementa.ElementaVersion
import cc.polyfrost.oneconfig.libs.elementa.WindowScreen
import cc.polyfrost.oneconfig.libs.elementa.components.UIBlock
import cc.polyfrost.oneconfig.libs.elementa.components.UIContainer
import cc.polyfrost.oneconfig.libs.elementa.constraints.FillConstraint
import cc.polyfrost.oneconfig.libs.elementa.constraints.RelativeConstraint
import cc.polyfrost.oneconfig.libs.elementa.constraints.SiblingConstraint
import cc.polyfrost.oneconfig.libs.elementa.dsl.childOf
import cc.polyfrost.oneconfig.libs.elementa.dsl.constrain
import cc.polyfrost.oneconfig.libs.elementa.dsl.percent
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import java.awt.Color

abstract class BaseScreen(
    val useContentContainer: Boolean
) : WindowScreen(
    version = ElementaVersion.V2,
    drawDefaultBackground = false,
    restoreCurrentGuiOnClose = true
) {
    companion object {
        var lastNonSCUScreen: GuiScreen? = null
        private set
    }
    init {
        Minecraft.getMinecraft().currentScreen.let {
            if (it !is BaseScreen) {
                lastNonSCUScreen = it
            }
        }
    }
    private val background = UIBlock(Color(31, 31, 31)).constrain {
        width = RelativeConstraint()
        height = RelativeConstraint()
    } childOf window
    private val outerContainer = UIContainer().constrain {
        width = RelativeConstraint()
        height = RelativeConstraint()
    } childOf window

    val headerContainer = UIContainer().constrain {
        width = RelativeConstraint()
        height = 12.5f.percent()
    } childOf outerContainer
    val bodyContainer = UIContainer().constrain {
        y = SiblingConstraint()
        width = RelativeConstraint()
        height = 75.percent()
    } childOf outerContainer
    val contentContainer = UIBlock(Color(19, 19, 19)).constrain {
        width = FillConstraint()
        height = RelativeConstraint()
    }.also {
        if (!useContentContainer) it.hide()
    } childOf bodyContainer
    val footerContainer = UIContainer().constrain {
        y = SiblingConstraint()
        width = RelativeConstraint()
        height = 12.5f.percent()
    } childOf outerContainer
}