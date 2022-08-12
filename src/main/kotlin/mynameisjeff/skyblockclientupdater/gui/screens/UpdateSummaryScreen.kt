package mynameisjeff.skyblockclientupdater.gui.screens

import gg.essential.api.EssentialAPI
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
import mynameisjeff.skyblockclientupdater.UpdateChecker
import mynameisjeff.skyblockclientupdater.data.UpdateMod
import mynameisjeff.skyblockclientupdater.gui.elements.SexyButton
import net.minecraftforge.fml.common.FMLCommonHandler
import java.awt.Color

class UpdateSummaryScreen(
    private val successfulUpdate: HashSet<UpdateMod>,
    private val failedUpdate: HashSet<UpdateMod>
): BaseScreen(
    useContentContainer = true
) {
    val typeDivider = UIBlock(Color(31, 31, 31)).constrain {
        x = CenterConstraint()
        width = 2.pixels()
        height = RelativeConstraint()
    } childOf contentContainer

    val headerText = UIText("Attempted to update your mods. Updated mods will load the next time you close and re-launch Minecraft.").constrain {
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
        text = "Close Minecraft now",
        outlineColor = Color.RED
    ).constrain {
        width = 150.pixels()
        height = 20.pixels()
    }.onMouseClick {
        FMLCommonHandler.instance().exitJava(0, false)
    } childOf buttonContainer
    private val continueButton = SexyButton(
        text = "Continue without closing"
    ).constrain {
        x = SiblingConstraint(7f)
        width = 150.pixels()
        height = 20.pixels()
    }.onMouseClick {
        UpdateChecker.INSTANCE.ignoreUpdates()
        EssentialAPI.getGuiUtil().openScreen(lastNonSCUScreen)
    } childOf buttonContainer

    init {
        quitButton.setFloating(true)
        continueButton.setFloating(true)
        for (update in successfulUpdate) {
            UIText(update.name).constrain {
                x = CenterConstraint()
                y = if (successfulUpdate.indexOf(update) == 0) 2.pixels() else SiblingConstraint(2f)
            } childOf successfulList
        }

        for (update in failedUpdate) {
            UIText(update.name).constrain {
                x = CenterConstraint()
                y = if (failedUpdate.indexOf(update) == 0) 2.pixels() else SiblingConstraint(2f)
            } childOf failedList
        }
    }
}