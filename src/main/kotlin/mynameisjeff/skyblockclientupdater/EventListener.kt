package mynameisjeff.skyblockclientupdater

import mynameisjeff.skyblockclientupdater.config.Config
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiOptions
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.LogManager
import java.security.SecureRandom
import kotlin.math.abs

class EventListener {
    private val logger = LogManager.getLogger("SkyClientUpdater (EventListener)")
    private var buttonId = generateButtonId()

    @SubscribeEvent
    fun onGuiInitialized(event: GuiScreenEvent.InitGuiEvent) {
        if (event.gui !is GuiOptions || !Config.showButtonOnEscapeMenu) return
        if (event.buttonList.any { it.id == buttonId }) buttonId = generateButtonId(event.buttonList)
        event.buttonList.add(GuiButton(buttonId, 2, 2, 100, 20, "SkyClient Updater"))
    }

    @SubscribeEvent
    fun onGuiAction(event: GuiScreenEvent.ActionPerformedEvent) {
        if (event.gui !is GuiOptions || !Config.showButtonOnEscapeMenu) return
        if (event.button.id == buttonId) Config.openGui()
    }

    private fun generateButtonId(buttonList: List<GuiButton> = listOf()): Int {
        var buttonId = abs(SecureRandom.getInstanceStrong().nextInt())
        logger.info("Generating a secure button ID for the SkyClientUpdater button. (currently: $buttonId)")
        if (buttonList.any { it.id == buttonId }) buttonId = generateButtonId(buttonList)
        logger.info("Valid button ID found. ($buttonId)")
        return buttonId
    }
}