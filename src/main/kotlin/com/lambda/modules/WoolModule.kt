package com.lambda.modules

import baritone.api.pathing.goals.GoalNear
import com.lambda.WoolBot
import com.lambda.client.event.SafeClientEvent
import com.lambda.client.module.Category
import com.lambda.client.plugin.api.PluginModule
import com.lambda.client.util.BaritoneUtils
import com.lambda.client.util.TickTimer
import com.lambda.client.util.Wrapper.world
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import net.minecraft.entity.Entity
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
    private var sheepArray : MutableList<Entity> = mutableListOf()
    private var sheepMap: MutableMap<Entity,Boolean> = mutableMapOf()
    private val timer = TickTimer()
    private val countdown by setting("Timer", 1, 1..30, 1)
    private val debug by setting("Debug", false)
    private var currentSlot = -1
    private var sheepIterator = 0

    init {
        onEnable {  //Loads a list of sheep in the loaded area
            loadSheep()
        }
        onDisable {  //Unloads the sheep list
            unloadSheep()
        }
        safeListener<TickEvent.ClientTickEvent> {
            findShears()
            useShears()
            if (timer.tick(countdown*1000)){ //
                val currentSheep = sheepArray[sheepIterator] //Makes every "countdown" seconds the baritone bot move to the next sheep in the list. To be honest, It would be better to check the nearest sheep and path to it instead.
                if (sheepMap[currentSheep]==false){ //Check in the sheepMap if the sheep has been sheared by the bot.
                    if (debug) MessageSendHelper.sendChatMessage("Pathing towards $currentSheep")
                    baritoneMove(currentSheep)
                }
            }
            if(sheepIterator == sheepArray.size-1){ //When baritone has searched through all the sheep, set to false the flag of recently sheared, for all of them. Maybe a tag could be made for all sheep with mixins: recentlySheared, and this code would be much more efficient.
                sheepIterator = 0
                for(sheep in sheepArray){
                    sheepMap[sheep]=false
                }
            }else{
                sheepIterator += 1
            }
        }
    }

    private fun loadSheep() { //loads the sheep in the sheepArray<Entity> and sheepMap<Entity,boolean> the boolean is for recently sheared sheep.
        for (entity in world?.loadedEntityList!!){
            if (entity is EntitySheep){
                sheepArray += entity
                sheepMap[entity] = false
            }
        }
    }

    private fun unloadSheep() { //clears the sheepArray and the sheepMap.
        sheepArray.clear()
        sheepMap.clear()
    }

    private fun baritoneMove(currentSheep : Entity){ //Checks the last block position of the sheep and pathfinds near it (1 block radius).
        BaritoneUtils.primary?.customGoalProcess?.setGoalAndPath( //This should be done with a Baritone Job, making it more straight-forward, since this is run every "countdown" seconds and can override the last pathfinding.
            GoalNear(BlockPos(currentSheep.posX,currentSheep.posY,currentSheep.posZ),1))
    }

    private fun SafeClientEvent.findShears() { //Self explanatory.
        for (i in 0..8) {
            val stack = player.inventory.getStackInSlot(i)
            if (stack == ItemStack.EMPTY || stack.item is ItemBlock) continue
            if (isShears(i)) {
                currentSlot = i
            }
        }
    }
    private fun SafeClientEvent.isShears(i: Int): Boolean { //Self explanatory.
        val stack = player.inventory.getStackInSlot(i)
        val tag = stack.item
        return tag is ItemShears
    }

    private fun SafeClientEvent.useShears() { //Tries to shear every tick the sheep around the player and sets their recentlySheared flag to true.
        val originalSlot = player.inventory.currentItem //This shouldn't be run every tick for every nearby sheep while pathfinding.
        for (entity in world.loadedEntityList) {
            if (entity is EntitySheep && player.getDistance(entity) <= 1.5 && sheepMap[entity]==false) {
                playerController.interactWithEntity(player, entity, EnumHand.MAIN_HAND)
                sheepMap[entity]=true
            }
        }
        player.inventory.currentItem = originalSlot
    }
}

