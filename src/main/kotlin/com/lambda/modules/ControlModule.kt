package com.lambda.modules

import com.lambda.JalvaPlugins
import com.lambda.client.module.Category
import com.lambda.client.plugin.api.PluginModule
import com.lambda.client.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * This is a module. First set properties then settings then add listener.
 * **/
internal object ControlModule : PluginModule(
    name = "ControlModule",
    category = Category.PLAYER,
    description = "Module that controls your camera's yaw and pitch. Perfect for flying.",
    pluginMain = JalvaPlugins
) {
    private val pitch by setting("Pitch", 0, -90..90, 1)
    private val yaw by setting("Yaw", 0, -180..180, 1)
    private val togglePitchYaw by setting("Toggle", false)
    init {
        onEnable {
            mc.player.rotationYaw=yaw.toFloat()
            mc.player.rotationPitch=pitch.toFloat()
        }
        safeListener<TickEvent.ClientTickEvent>{
            if (togglePitchYaw){
                mc.player.rotationYaw=yaw.toFloat()
                mc.player.rotationPitch=pitch.toFloat()
            }
        }
    }
}