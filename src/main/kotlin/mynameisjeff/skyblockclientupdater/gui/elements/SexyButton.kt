package mynameisjeff.skyblockclientupdater.gui.elements

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.USound
import mynameisjeff.skyblockclientupdater.SkyClientUpdater
import java.awt.Color

class SexyButton(
    text: String,
    outlineColor: Color = SkyClientUpdater.accentColor,
    primary: Boolean = true,
    playClickSound: Boolean = true
) : UIContainer() {
    private val background = UIBlock(if (primary) Color(15, 15, 15) else Color(21, 21, 21)).constrain {
        width = RelativeConstraint()
        height = RelativeConstraint()
    } effect OutlineEffect(Color(0, 0, 0, 0), 1f) childOf this
    private val textComponent = UIText(text).constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf background

    init {
        val outlineEffect = background.effects[0] as OutlineEffect
        onMouseEnter {
            outlineEffect::color.animate(Animations.OUT_EXP, 1f, outlineColor)
        }.onMouseLeave {
            outlineEffect::color.animate(Animations.OUT_EXP, 1f, Color(0, 0, 0, 0))
        }.onMouseClick {
            if (playClickSound) {
                USound.playButtonPress()
            }
        }
    }
}