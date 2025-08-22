package com.chattriggers.ctjs.internal.listeners

import com.chattriggers.ctjs.api.triggers.CancellableEvent
import com.chattriggers.ctjs.api.triggers.TriggerType
import com.chattriggers.ctjs.internal.utils.Initializer
import gg.essential.universal.UMinecraft
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
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

        HudElementRegistry.addLast(Identifier.of("chattriggers", "render_overlay")) { ctx, tickCounter ->
            TriggerType.RENDER_OVERLAY.triggerAll(ctx, tickCounter)
        }

        ClientReceiveMessageEvents.ALLOW_CHAT.register { message, _, _, _, _ ->
            val event = CancellableEvent()
            TriggerType.CHAT.triggerAll(message, event)

            !event.isCancelled()
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            val event = CancellableEvent()
            (if (overlay) TriggerType.ACTION_BAR else TriggerType.CHAT)
                .triggerAll(message, event)

            !event.isCancelled()
        }

        ClientSendMessageEvents.ALLOW_CHAT.register { message ->
            val event = CancellableEvent()
            TriggerType.MESSAGE_SENT.triggerAll(message, false, event)

            !event.isCancelled()
        }

        ClientSendMessageEvents.ALLOW_COMMAND.register { message ->
            val event = CancellableEvent()
            TriggerType.MESSAGE_SENT.triggerAll(message, true, event)

            !event.isCancelled()
        }
    }

    fun addTask(delay: Int, callback: () -> Unit) {
        synchronized(tasks) {
            tasks.add(Task(delay, callback))
        }
    }
}
