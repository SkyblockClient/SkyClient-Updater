package mynameisjeff.skyblockclientupdater.gui.screens

import cc.polyfrost.oneconfig.libs.elementa.components.ScrollComponent
import cc.polyfrost.oneconfig.libs.elementa.components.UIContainer
import cc.polyfrost.oneconfig.libs.elementa.components.UIText
import cc.polyfrost.oneconfig.libs.elementa.constraints.CenterConstraint
import cc.polyfrost.oneconfig.libs.elementa.constraints.ChildBasedSizeConstraint
import cc.polyfrost.oneconfig.libs.elementa.constraints.RelativeConstraint
import cc.polyfrost.oneconfig.libs.elementa.constraints.SiblingConstraint
import cc.polyfrost.oneconfig.libs.elementa.dsl.childOf
import cc.polyfrost.oneconfig.libs.elementa.dsl.constrain
import cc.polyfrost.oneconfig.libs.elementa.dsl.pixels
import mynameisjeff.skyblockclientupdater.UpdateChecker
import mynameisjeff.skyblockclientupdater.data.UpdateMod
import mynameisjeff.skyblockclientupdater.gui.elements.ModUpdateComponent
import mynameisjeff.skyblockclientupdater.gui.elements.SexyButton
import java.awt.Color

class ModUpdateScreen(
    private val needsUpdate: HashSet<UpdateMod>
) : BaseScreen(
    useContentContainer = true
) {
    private val updating = needsUpdate.toMutableSet()

    val headerText = UIText("Some of your mods are outdated. Do you want to update?").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    }.setTextScale(1.25f.pixels()) childOf headerContainer

    private val updateScroller = ScrollComponent("There are no new updates. :)").constrain {
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
        displayScreen(DownloadProgressScreen(updating as HashSet<UpdateMod>))
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
        UpdateChecker.INSTANCE.ignoreUpdates()
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
