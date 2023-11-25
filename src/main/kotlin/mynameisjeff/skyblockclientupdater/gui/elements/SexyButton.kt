package mynameisjeff.skyblockclientupdater.gui.elements

import cc.polyfrost.oneconfig.libs.elementa.components.UIBlock
import cc.polyfrost.oneconfig.libs.elementa.components.UIContainer
import cc.polyfrost.oneconfig.libs.elementa.components.UIText
import cc.polyfrost.oneconfig.libs.elementa.constraints.CenterConstraint
import cc.polyfrost.oneconfig.libs.elementa.constraints.RelativeConstraint
import cc.polyfrost.oneconfig.libs.elementa.constraints.animation.Animations
import cc.polyfrost.oneconfig.libs.elementa.dsl.childOf
import cc.polyfrost.oneconfig.libs.elementa.dsl.constrain
import cc.polyfrost.oneconfig.libs.elementa.dsl.effect
import cc.polyfrost.oneconfig.libs.elementa.effects.OutlineEffect
import cc.polyfrost.oneconfig.libs.universal.USound
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