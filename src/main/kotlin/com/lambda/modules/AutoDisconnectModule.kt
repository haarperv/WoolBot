package com.lambda.modules

import com.lambda.JalvaPlugins
import com.lambda.client.module.Category
import com.lambda.client.plugin.api.PluginModule
import com.lambda.client.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * This is a module. First set properties then settings then add listener.
 * **/
internal object AutoDisconnectModule : PluginModule(
    name = "AutoDisconnect",
    category = Category.MISC,
    description = "Disconnects when a certain life threshold is hit",
    pluginMain = JalvaPlugins
) {
    private val heartLimit by setting("Heart Limit", 10, 1..20, 1)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            val playerHearts = mc.player.health
            if(playerHearts <= heartLimit.toFloat() && !mc.isSingleplayer){
                world.sendQuittingDisconnectingPacket()
            }
        }
    }
}