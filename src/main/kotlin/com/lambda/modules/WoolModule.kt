package com.lambda.modules
import baritone.api.pathing.goals.GoalNear
import baritone.api.pathing.goals.GoalXZ
import com.lambda.WoolBot
import com.lambda.classes.worker.Job
import com.lambda.classes.worker.utils.JobTracker
import com.lambda.classes.worker.utils.JobUtils
import com.lambda.client.commons.extension.floorToInt
import com.lambda.client.event.SafeClientEvent
import com.lambda.client.module.Category
import com.lambda.client.plugin.api.PluginModule
import com.lambda.client.util.BaritoneUtils
import com.lambda.client.util.Wrapper.world
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import com.lambda.enums.EJobEvents
import com.lambda.enums.EWorkerStatus
import com.lambda.enums.EWorkerType
import com.lambda.events.StartPathingEvent
import com.lambda.events.StopPathingEvent
import com.lambda.events.UpdatePathingEvent
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.passive.EntitySheep
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemShears
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * This is a module. First set properties then settings then add listener.
 * **/
internal object WoolModule : PluginModule(
    name = "WoolModule",
    category = Category.MISC,
    description = "Bot that collects wool automagically for servers like 2b.",
    pluginMain = WoolBot
) {
    private val jUtils = JobUtils()
    private val bUtils = com.lambda.utils.BaritoneUtils()
    private var sheepArray: MutableList<EntitySheep> = mutableListOf()

    //private var sheepMap: MutableMap<Entity,Boolean> = mutableMapOf()
    private val debug by setting("Debug", false)
    private var currentSlot = -1
    private var sheepIterator = 0


    init {
        onEnable {  //Loads a list of sheep in the loaded area
            try {
                loadSheep()
            }
            catch(e: ArrayIndexOutOfBoundsException){
                MessageSendHelper.sendChatMessage("Error loading nearby sheep. Try again later")
            }
        }
        onDisable {  //Unloads the sheep list
            try {
                unloadSheep()
            }
            catch(e: ArrayIndexOutOfBoundsException){
                MessageSendHelper.sendChatMessage("Error unloading nearby sheep. Try again later")
            }
        }
        safeListener<TickEvent.ClientTickEvent> {
            val currentSheep = sheepArray[sheepIterator]
            if (bUtils.status == EWorkerStatus.IDLE) {
                if (!currentSheep.sheared) {
                    if (debug) MessageSendHelper.sendChatMessage("Pathing towards $currentSheep")
                    baritoneMove(currentSheep)
                    bUtils.pathingGoalCheck()
                    if (debug) MessageSendHelper.sendChatMessage("Finished pathing")
                    useShears(sheepArray[sheepIterator])
                    pickNearestItem()
                    bUtils.pathingGoalCheck()
                }
            }
            if (sheepIterator == sheepArray.size - 1) {
                sheepIterator = 0
            } else {
                sheepIterator += 1
            }
        }


        safeListener<StartPathingEvent> {
            val job = JobTracker(Job(
                type = EWorkerType.BARITONE,
                goal = it.goal,
                entity = this.player
            ))
            jUtils.addJob(job)
        }

        safeListener<StopPathingEvent> {
            jUtils.currentJob()?.run {
                this.job.end()
            }
        }
        safeListener<UpdatePathingEvent> {
            jUtils.currentJob()?.run {
                if (this.isStuck()) {
                    this.job.emitEvent(EJobEvents.JOB_STUCK)
                }
            }
        }
    }


    private fun loadSheep() { //loads the sheep in the sheepArray<Entity>
        for (entity in world?.loadedEntityList!!){
            if (entity is EntitySheep){
                sheepArray += entity
            }
        }
    }

    private fun unloadSheep() { //clears the sheepArray and the sheepMap.
        sheepArray.clear()
    }

    private fun baritoneMove(currentSheep : Entity){ //Checks the last block position of the sheep and pathfinds near it (1 block radius).
        BaritoneUtils.primary?.customGoalProcess?.setGoalAndPath( //This should be done with a Baritone Job, making it more straight-forward, since this is run every "countdown" seconds and can override the last pathfinding.
            GoalNear(BlockPos(currentSheep.posX,currentSheep.posY,currentSheep.posZ),1))
    }

    private fun pickNearestItem(){
        for (entityItem in world?.loadedEntityList!!) {
            if (entityItem is EntityItem && mc.player.getDistance(entityItem)<=16.0){
                BaritoneUtils.primary?.customGoalProcess?.setGoalAndPath(
                    GoalXZ(entityItem.posX.floorToInt(), entityItem.posZ.floorToInt()))
            }
        }
    }
    private fun SafeClientEvent.findShears() { //-Self explanatory.
        for (i in 0..8) {
            val stack = player.inventory.getStackInSlot(i)
            if (stack == ItemStack.EMPTY || stack.item is ItemBlock) continue
            if (isShears(i)) {
                currentSlot = i
            }
        }
    }
    private fun SafeClientEvent.isShears(i: Int): Boolean { //-Self explanatory.
        val stack = player.inventory.getStackInSlot(i)
        val tag = stack.item
        return tag is ItemShears
    }

    private fun SafeClientEvent.useShears(currentSheep: Entity) {
        val originalSlot = player.inventory.currentItem
        findShears()
        if (debug) MessageSendHelper.sendChatMessage("Trying to shear $currentSheep")
        playerController.interactWithEntity(player, currentSheep, EnumHand.MAIN_HAND)
        player.inventory.currentItem = originalSlot
    }
}
