package mynameisjeff.skyblockclientupdater.gui.screens

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
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