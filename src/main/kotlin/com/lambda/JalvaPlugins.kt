package com.lambda

import com.lambda.client.plugin.api.Plugin
import com.lambda.modules.AutoDisconnectModule
import com.lambda.modules.ControlModule
import com.lambda.modules.WoolModule

internal object JalvaPlugins : Plugin() {

    override fun onLoad() {
        // Load any modules, commands, or HUD elements here
        modules.add(WoolModule)
        modules.add(ControlModule)
        modules.add(AutoDisconnectModule)
    }

    override fun onUnload() {
        // Here you can unregister threads etc...
    }
}