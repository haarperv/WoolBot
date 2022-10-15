package com.lambda

import com.lambda.client.LambdaMod
import com.lambda.client.plugin.api.Plugin
import com.lambda.client.util.threads.BackgroundJob
import com.lambda.modules.WoolModule

internal object WoolBot : Plugin() {

    override fun onLoad() {
        // Load any modules, commands, or HUD elements here
        modules.add(WoolModule)
    }

    override fun onUnload() {
        // Here you can unregister threads etc...
    }
}