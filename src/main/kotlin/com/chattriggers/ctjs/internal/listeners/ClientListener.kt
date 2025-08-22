package com.chattriggers.ctjs.internal.listeners

import com.chattriggers.ctjs.api.triggers.TriggerType
import com.chattriggers.ctjs.internal.utils.Initializer
import gg.essential.universal.UMinecraft
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.util.Identifier

object ClientListener : Initializer {
    private val tasks = mutableListOf<Task>()

    class Task(var delay: Int, val callback: () -> Unit)

    override fun init() {
        ClientTickEvents.START_CLIENT_TICK.register {
            synchronized(tasks) {
                tasks.removeAll {
                    if (it.delay-- <= 0) {
                        UMinecraft.getMinecraft().submit(it.callback)
                        true
                    } else false
                }
            }
        }

        HudElementRegistry.addFirst(Identifier.of("chattriggers", "render_overlay")) { ctx, tickCounter ->
            TriggerType.RENDER_OVERLAY.triggerAll(ctx, tickCounter)
        }
    }

    fun addTask(delay: Int, callback: () -> Unit) {
        synchronized(tasks) {
            tasks.add(Task(delay, callback))
        }
    }
}
