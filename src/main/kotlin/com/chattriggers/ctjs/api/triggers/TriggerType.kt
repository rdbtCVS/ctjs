package com.chattriggers.ctjs.api.triggers

import com.chattriggers.ctjs.internal.engine.JSLoader

sealed interface ITriggerType {
    val name: String

    fun triggerAll(vararg args: Any?) {
        JSLoader.exec(this, args)
    }
}

data class CustomTriggerType(override val name: String) : ITriggerType
