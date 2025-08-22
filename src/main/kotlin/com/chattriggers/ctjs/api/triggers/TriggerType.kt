package com.chattriggers.ctjs.api.triggers

import com.chattriggers.ctjs.internal.engine.JSLoader

sealed interface ITriggerType {
    val name: String

    fun triggerAll(vararg args: Any?) {
        JSLoader.exec(this, args)
    }
}

enum class TriggerType : ITriggerType {
    RENDER_OVERLAY,

    CHAT,
    ACTION_BAR,
    MESSAGE_SENT,
}

data class CustomTriggerType(override val name: String) : ITriggerType
