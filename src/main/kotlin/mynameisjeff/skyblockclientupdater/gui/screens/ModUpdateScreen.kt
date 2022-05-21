package mynameisjeff.skyblockclientupdater.gui.screens

import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import mynameisjeff.skyblockclientupdater.gui.elements.ModUpdateComponent
import mynameisjeff.skyblockclientupdater.gui.elements.SexyButton
import mynameisjeff.skyblockclientupdater.UpdateChecker
import java.awt.Color
import java.io.File

class ModUpdateScreen(
    private val needsUpdate: HashSet<Triple<File, String, String>>
) : BaseScreen(
    useContentContainer = true
) {
    private val updating = needsUpdate.toMutableSet()

    val headerText = UIText("Some of your mods are outdated. Do you want to update?").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    }.setTextScale(1.25f.pixels()) childOf headerContainer

    private val updateScroller = ScrollComponent("There are no updates... How are you seeing this menu?!").constrain {
        width = RelativeConstraint()
        height = RelativeConstraint()
    } childOf contentContainer

    private val buttonContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ChildBasedSizeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf footerContainer
    val updateButton = SexyButton("Update").constrain {
        width = 150.pixels()
        height = 20.pixels()
    }.onMouseClick {
        displayScreen(DownloadProgressScreen(updating as HashSet<Triple<File, String, String>>))
    } childOf buttonContainer
    val exitButton = SexyButton(
        text = "Main Menu",
        outlineColor = Color.RED,
        primary = false
    ).constrain {
        width = 150.pixels()
        height = 20.pixels()
        x = SiblingConstraint(7.5f)
    }.onMouseClick {
        UpdateChecker.ignoreUpdates()
        restorePreviousScreen()
    } childOf buttonContainer

    init {
        updateButton.setFloating(true)
        exitButton.setFloating(true)
        for (update in needsUpdate) {
            ModUpdateComponent(update, updating).constrain {
                x = CenterConstraint()
                y = if (needsUpdate.indexOf(update) == 0) 5.pixels() else SiblingConstraint(1f)
            } childOf updateScroller
        }
    }
}