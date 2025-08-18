package com.chattriggers.ctjs.internal.listeners

import com.chattriggers.ctjs.internal.utils.Initializer
import gg.essential.universal.UMinecraft
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

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
    }

    fun addTask(delay: Int, callback: () -> Unit) {
        synchronized(tasks) {
            tasks.add(Task(delay, callback))
        }
    }
}
