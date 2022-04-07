package mynameisjeff.skyblockclientupdater.gui.screens

import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.universal.ChatColor
import gg.essential.api.EssentialAPI
import net.minecraft.client.gui.GuiMainMenu
import mynameisjeff.skyblockclientupdater.utils.UpdateChecker
import mynameisjeff.skyblockclientupdater.gui.elements.SexyButton
import net.minecraftforge.fml.common.FMLCommonHandler
import java.awt.Color
import java.io.File

class UpdateSummaryScreen(
    private val successfulUpdate: HashSet<Triple<File, String, String>>,
    private val failedUpdate: HashSet<Triple<File, String, String>>
): BaseScreen(
    useContentContainer = true
) {
    val typeDivider = UIBlock(Color(31, 31, 31)).constrain {
        x = CenterConstraint()
        width = 2.pixels()
        height = RelativeConstraint()
    } childOf contentContainer

    val headerText = UIText("Attempted to update your mods.\nUpdated mods will load upon restart.").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    }.setTextScale(1.25f.pixels()) childOf headerContainer

    val successfulContainer = UIContainer().constrain {
        width = 50.percent()
        height = RelativeConstraint()
    } childOf contentContainer
    val failedContainer = UIContainer().constrain {
        x = SiblingConstraint()
        width = 50.percent()
        height = RelativeConstraint()
    } childOf contentContainer

    val successfulHeaderText = UIText("${ChatColor.UNDERLINE}Successful").constrain {
        x = CenterConstraint()
        y = 5.pixels()
        color = Color.GREEN.toConstraint()
    }.setTextScale(1.5f.pixels()) childOf successfulContainer
    val successfulList = ScrollComponent("None").constrain {
        y = 20.pixels()
        width = RelativeConstraint()
        height = RelativeConstraint()
        color = Color.GREEN.toConstraint()
    } childOf successfulContainer
    val failedHeaderText = UIText("${ChatColor.UNDERLINE}Failed").constrain {
        x = CenterConstraint()
        y = 5.pixels()
        color = Color.RED.toConstraint()
    }.setTextScale(1.5f.pixels()) childOf failedContainer
    val failedList = ScrollComponent("None").constrain {
        y = 20.pixels()
        width = RelativeConstraint()
        height = RelativeConstraint()
        color = Color.GREEN.toConstraint()
    } childOf failedContainer

    private val buttonContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ChildBasedSizeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf footerContainer
    private val quitButton = SexyButton(
        text = "Quit",
        outlineColor = Color.RED,
        primary = false
    ).constrain {
        width = 50.pixels()
        height = 20.pixels()
    }.onMouseClick {
        FMLCommonHandler.instance().exitJava(0, false)
    } childOf buttonContainer
    private val continueButton = SexyButton(
        text = "Continue to MC"
    ).constrain {
        x = SiblingConstraint()
        width = 150.pixels()
        height = 20.pixels()
    }.onMouseClick {
        UpdateChecker.ignoreUpdates();
        EssentialAPI.getGuiUtil().openScreen(GuiMainMenu())
    } childOf buttonContainer

    init {
        for (update in successfulUpdate) {
            UIText(update.second).constrain {
                x = CenterConstraint()
                y = if (successfulUpdate.indexOf(update) == 0) 2.pixels() else SiblingConstraint(2f)
            } childOf successfulList
        }

        for (update in failedUpdate) {
            UIText(update.second).constrain {
                x = CenterConstraint()
                y = if (failedUpdate.indexOf(update) == 0) 2.pixels() else SiblingConstraint(2f)
            } childOf failedList
        }
    }
}