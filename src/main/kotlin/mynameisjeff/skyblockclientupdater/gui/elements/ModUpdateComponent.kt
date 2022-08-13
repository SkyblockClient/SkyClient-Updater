package mynameisjeff.skyblockclientupdater.gui.elements

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.toConstraint
import gg.essential.universal.ChatColor
import mynameisjeff.skyblockclientupdater.SkyClientUpdater
import mynameisjeff.skyblockclientupdater.UpdateChecker
import mynameisjeff.skyblockclientupdater.data.UpdateMod
import java.awt.Color

class ModUpdateComponent(
    update: UpdateMod,
    updating: MutableSet<UpdateMod>
) : UIContainer() {

    val oldFileText = UIText(update.file.name).constrain {
        color = Color(179, 0, 0).toConstraint()
    } childOf this
    val seperatorContainer = UIContainer().constrain {
        x = SiblingConstraint(2f)
    } childOf this
    val seperatorText = UIText("${ChatColor.BOLD}\u279C").constrain {
        color = Color(66, 245, 93).toConstraint()
    } childOf seperatorContainer
    val newFileText = UIText("${ChatColor.GREEN}${update.name}").constrain {
        x = SiblingConstraint(2f)
        color = SkyClientUpdater.accentColor.toConstraint()
    } childOf this

    init {
        seperatorContainer.constrain {
            width = seperatorText.constraints.width
            height = seperatorText.constraints.height
        }

        constrain {
            width = ChildBasedSizeConstraint()
            height = newFileText.constraints.height
        }.onMouseClick {
            when (update.type) {
                UpdateMod.Type.UPDATING -> {
                    update.type = UpdateMod.Type.TEMP_DISABLE
                    seperatorText.animate { setColorAnimation(Animations.OUT_EXP, 1f, Color(245, 99, 99).toConstraint()) }
                    newFileText.setText("skip: ${ChatColor.GREEN}${ChatColor.STRIKETHROUGH}${update.name}")
                    updating.remove(update)
                }
                UpdateMod.Type.TEMP_DISABLE -> {
                    update.type = UpdateMod.Type.DISABLE
                    seperatorText.animate { setColorAnimation(Animations.OUT_EXP, 1f, Color(245, 66, 66).toConstraint()) }
                    newFileText.setText("perm disabled: ${ChatColor.RED}${ChatColor.STRIKETHROUGH}${update.name}")
                    UpdateChecker.INSTANCE.ignored.add(update)
                    UpdateChecker.INSTANCE.writeIgnoredJson()
                }
                UpdateMod.Type.DISABLE -> {
                    update.type = UpdateMod.Type.UPDATING
                    seperatorText.animate { setColorAnimation(Animations.OUT_EXP, 1f, Color(66, 245, 93).toConstraint()) }
                    newFileText.setText("${ChatColor.GREEN}${update.name}")
                    updating.add(update)
                    UpdateChecker.INSTANCE.ignored.remove(update)
                    UpdateChecker.INSTANCE.writeIgnoredJson()
                }
            }
        }.onMouseEnter {
            when (update.type) {
                UpdateMod.Type.UPDATING -> {
                    seperatorText.animate { setColorAnimation(Animations.OUT_EXP, 1f, Color(245, 99, 99).toConstraint()) }
                    newFileText.setText("skip: ${ChatColor.GREEN}${ChatColor.STRIKETHROUGH}${update.name}")
                }
                UpdateMod.Type.TEMP_DISABLE -> {
                    seperatorText.animate { setColorAnimation(Animations.OUT_EXP, 1f, Color(245, 66, 66).toConstraint()) }
                    newFileText.setText("perm disabled: ${ChatColor.RED}${ChatColor.STRIKETHROUGH}${update.name}")
                }
                UpdateMod.Type.DISABLE -> {
                    seperatorText.animate { setColorAnimation(Animations.OUT_EXP, 1f, Color(66, 245, 93).toConstraint()) }
                    newFileText.setText("${ChatColor.GREEN}${update.name}")
                }
            }
        }.onMouseLeave {
            when (update.type) {
                UpdateMod.Type.UPDATING -> {
                    seperatorText.animate { setColorAnimation(Animations.OUT_EXP, 1f, Color(66, 245, 93).toConstraint()) }
                    newFileText.setText("${ChatColor.GREEN}${update.name}")
                }
                UpdateMod.Type.TEMP_DISABLE -> {
                    seperatorText.animate { setColorAnimation(Animations.OUT_EXP, 1f, Color(245, 99, 99).toConstraint()) }
                    newFileText.setText("skip: ${ChatColor.GREEN}${ChatColor.STRIKETHROUGH}${update.name}")
                }
                UpdateMod.Type.DISABLE -> {
                    seperatorText.animate { setColorAnimation(Animations.OUT_EXP, 1f, Color(245, 66, 66).toConstraint()) }
                    newFileText.setText("perm disabled: ${ChatColor.RED}${ChatColor.STRIKETHROUGH}${update.name}")
                }
            }
        }
    }
}