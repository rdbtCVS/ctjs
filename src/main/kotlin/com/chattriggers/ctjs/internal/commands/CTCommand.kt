package com.chattriggers.ctjs.internal.commands

import com.chattriggers.ctjs.CTJS
import com.chattriggers.ctjs.api.Config
import com.chattriggers.ctjs.api.FileLib
import com.chattriggers.ctjs.engine.Console
import com.chattriggers.ctjs.engine.printTraceToConsole
import com.chattriggers.ctjs.internal.engine.module.ModuleManager
import com.chattriggers.ctjs.internal.listeners.ClientListener
import com.chattriggers.ctjs.internal.utils.Initializer
import com.chattriggers.ctjs.internal.utils.onExecute
import com.chattriggers.ctjs.internal.utils.toVersion
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import java.io.File
import java.io.IOException
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

internal object CTCommand : Initializer {
    private val mc = MinecraftClient.getInstance()

    override fun init() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            register(dispatcher)
        }
    }

    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        val command = literal("ct")
            .then(literal("load").onExecute { CTJS.load() })
            .then(literal("unload").onExecute { CTJS.unload() })
            .then(literal("files").onExecute { openFileLocation() })
            .then(
                literal("delete")
                    .then(argument("module", ModuleArgumentType)
                        .onExecute {
                            val module = ModuleArgumentType.getModule(it, "module")
                            if (ModuleManager.deleteModule(module)) {
                                mc.player?.sendMessage(Text.of("&aDeleted $module"), false)
                            } else mc.player?.sendMessage(Text.of("&cFailed to delete $module"), false)
                        })
            )
            .then(literal("console").onExecute { Console.show() })
            .then(literal("config").onExecute { ClientListener.addTask(0, {
                mc.setScreen(Config.gui()!!)
            }) })
            .then(
                literal("simulate")
                    .then(
                        argument("message", StringArgumentType.greedyString())
                            .onExecute { mc.networkHandler?.sendChatMessage(StringArgumentType.getString(it, "message")) }
                    )
            )

        dispatcher.register(command)
    }

    private fun openFileLocation() {
        try {
            FileLib.open(ModuleManager.modulesFolder)
        } catch (exception: IOException) {
            exception.printTraceToConsole()
            mc.player?.sendMessage(Text.of("&cCould not open file location"), false)
        }
    }

    private object ModuleArgumentType : ArgumentType<String> {
        override fun parse(reader: StringReader): String {
            val string = reader.readUnquotedString()
            val modules = ModuleManager.cachedModules.map { it.name }

            return modules.find {
                it.equals(string, ignoreCase = true)
            } ?: throw SimpleCommandExceptionType(Text.literal("No modules found with name \"$string\""))
                .createWithContext(reader)
        }

        override fun <S : Any?> listSuggestions(
            context: CommandContext<S>?,
            builder: SuggestionsBuilder?
        ): CompletableFuture<Suggestions> {
            return CommandSource.suggestMatching(ModuleManager.cachedModules.map { it.name }, builder)
        }

        fun getModule(ctx: CommandContext<FabricClientCommandSource>, module: String): String {
            return ctx.getArgument(module, String::class.java)
        }
    }
}
