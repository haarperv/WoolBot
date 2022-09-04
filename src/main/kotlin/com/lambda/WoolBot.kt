package com.lambda

import com.lambda.client.LambdaMod
import com.lambda.client.plugin.api.Plugin
import com.lambda.client.util.threads.BackgroundJob
import com.lambda.modules.WoolModule

internal object WoolBot : Plugin() {

    override fun onLoad() {
        // Load any modules, commands, or HUD elements here
        modules.add(WoolModule)
        bgJobs.add(BackgroundJob("ExampleJob", 10000L) { LambdaMod.LOG.info("Hello its me the BackgroundJob of your example plugin.") })
    }

    override fun onUnload() {
        // Here you can unregister threads etc...
    }
}