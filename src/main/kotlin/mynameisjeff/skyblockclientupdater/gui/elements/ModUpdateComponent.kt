package mynameisjeff.skyblockclientupdater.gui.elements

import cc.polyfrost.oneconfig.libs.elementa.components.UIContainer
import cc.polyfrost.oneconfig.libs.elementa.components.UIText
import cc.polyfrost.oneconfig.libs.elementa.constraints.ChildBasedSizeConstraint
import cc.polyfrost.oneconfig.libs.elementa.constraints.SiblingConstraint
import cc.polyfrost.oneconfig.libs.elementa.constraints.animation.Animations
import cc.polyfrost.oneconfig.libs.elementa.dsl.animate
import cc.polyfrost.oneconfig.libs.elementa.dsl.childOf
import cc.polyfrost.oneconfig.libs.elementa.dsl.constrain
import cc.polyfrost.oneconfig.libs.elementa.dsl.toConstraint
import cc.polyfrost.oneconfig.libs.universal.ChatColor
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
                    newFileText.setText("${ChatColor.GREEN}${ChatColor.STRIKETHROUGH}${update.name} ${ChatColor.WHITE}(skipping)")
                    updating.remove(update)
                }
                UpdateMod.Type.TEMP_DISABLE -> {
                    update.type = UpdateMod.Type.DISABLE
                    seperatorText.animate { setColorAnimation(Animations.OUT_EXP, 1f, Color(245, 66, 66).toConstraint()) }
                    newFileText.setText("${ChatColor.RED}${ChatColor.STRIKETHROUGH}${update.name} ${ChatColor.WHITE}(ignored)")
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
                    newFileText.setText("${ChatColor.GREEN}${update.name} ${ChatColor.WHITE}(skip)")
                }
                UpdateMod.Type.TEMP_DISABLE -> {
                    newFileText.setText("${ChatColor.RED}${update.name} ${ChatColor.WHITE}(ignore)")
                }
                UpdateMod.Type.DISABLE -> {
                    newFileText.setText("${ChatColor.GREEN}${update.name}")
                }
            }
        }.onMouseLeave {
            when (update.type) {
                UpdateMod.Type.UPDATING -> {
                    newFileText.setText("${ChatColor.GREEN}${update.name}")
                }
                UpdateMod.Type.TEMP_DISABLE -> {
                    newFileText.setText("${ChatColor.GREEN}${ChatColor.STRIKETHROUGH}${update.name} ${ChatColor.WHITE}(skipping)")
                }
                UpdateMod.Type.DISABLE -> {
                    newFileText.setText("${ChatColor.RED}${ChatColor.STRIKETHROUGH}${update.name} ${ChatColor.WHITE}(ignored)")
                }
            }
        }
    }
}