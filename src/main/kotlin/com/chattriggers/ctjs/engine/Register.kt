package com.chattriggers.ctjs.engine

import com.chattriggers.ctjs.api.triggers.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
object Register {
    private val methodMap = Register::class.java.methods.filter {
        it.name.startsWith("register") && it.name.length > "register".length
    }.associateBy {
        it.name.lowercase().drop("register".length)
    }
    private val customTriggers = mutableSetOf<CustomTriggerType>()

    internal fun clearCustomTriggers() = customTriggers.clear()

    /**
     * Helper method to register a trigger.
     *
     * @param triggerType the type of trigger
     * @param method the actual method to callback when the event is fired
     * @return The trigger for additional modification
     */
    @JvmStatic
    fun register(triggerType: String, method: Any): Trigger {
        val type = triggerType.lowercase()

        methodMap[type]?.let { return it.invoke(this, method) as Trigger }

        val customType = CustomTriggerType(type)
        if (customType in customTriggers)
            return RegularTrigger(method, customType)

        throw NoSuchMethodException("No trigger type named '$triggerType'")
    }

    @JvmStatic
    fun createCustomTrigger(name: String): Any {
        val customType = CustomTriggerType(name.lowercase())
        require(customType !in customTriggers) { "Cannot register duplicate custom trigger \"$name\"" }
        customTriggers.add(customType)

        return object {
            fun trigger(vararg args: Any?) = customType.triggerAll(*args)
        }
    }

    @JvmStatic
    fun registerRenderOverlay(method: Any): Trigger {
        return RegularTrigger(method, TriggerType.RENDER_OVERLAY)
    }
}
