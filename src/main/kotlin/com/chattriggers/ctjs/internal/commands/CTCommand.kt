package com.chattriggers.ctjs.internal.commands

import com.chattriggers.ctjs.CTJS
import com.chattriggers.ctjs.api.Config
import com.chattriggers.ctjs.api.FileLib
import com.chattriggers.ctjs.engine.Console
import com.chattriggers.ctjs.engine.printTraceToConsole
import com.chattriggers.ctjs.internal.engine.module.ModuleManager
import com.chattriggers.ctjs.internal.listeners.ClientListener
import com.chattriggers.ctjs.internal.utils.Initializer
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
    private const val idFixed = 90123 // ID for dumped chat
    private var idFixedOffset = -1 // ID offset (increments)

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
                literal("import")
                    .then(argument("module", StringArgumentType.string())
                        .onExecute { import(StringArgumentType.getString(it, "module")) })
            )
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
            .onExecute { mc.player?.sendMessage(Text.of(getUsage()), false) }

        dispatcher.register(command)
    }

    private fun import(moduleName: String) {
        if (ModuleManager.cachedModules.any { it.name.equals(moduleName, ignoreCase = true) }) {
            mc.player?.sendMessage(Text.of("&cModule $moduleName is already installed!"), false)
        } else {
            mc.player?.sendMessage(Text.of("&cImporting $moduleName..."), false)
            thread {
                val (module, dependencies) = ModuleManager.importModule(moduleName)
                if (module == null) {
                    mc.player?.sendMessage(Text.of("&cUnable to import module $moduleName"), false)
                    return@thread
                }

                val allModules = listOf(module) + dependencies
                val modVersion = CTJS.MOD_VERSION.toVersion()
                allModules.forEach {
                    val version = it.targetModVersion ?: return@forEach
                    if (version.majorVersion < modVersion.majorVersion)
                        ModuleManager.tryReportOldVersion(it)
                }

                mc.player?.sendMessage(Text.of("&aSuccessfully imported ${module.metadata.name ?: module.name}"), false)
                if (Config.moduleImportHelp && module.metadata.helpMessage != null) {
                    mc.player?.sendMessage(Text.of(module.metadata.helpMessage.toString().take(383)), false)
                }
            }
        }
    }

    private fun getUsage() = """
        &b&m${"-".repeat(20)}
        &c/ct load &7- &oReloads all of the ChatTriggers modules.
        &c/ct import <module> &7- &oImports a module.
        &c/ct delete <module> &7- &oDeletes a module.
        &c/ct files &7- &oOpens the ChatTriggers folder.
        &c/ct modules &7- &oOpens the modules GUI.
        &c/ct console [language] &7- &oOpens the ChatTriggers console.
        &c/ct simulate <message> &7- &oSimulates a received chat message.
        &c/ct dump &7- &oDumps previous chat messages into chat.
        &c/ct settings &7- &oOpens the ChatTriggers settings.
        &c/ct migrate <input> [output]&7 - &oMigrate a module from version 2.X to 3.X 
        &c/ct &7- &oDisplays this help dialog.
        &b&m${"-".repeat(20)}
    """.trimIndent()

    private fun openFileLocation() {
        try {
            FileLib.open(ModuleManager.modulesFolder)
        } catch (exception: IOException) {
            exception.printTraceToConsole()
            mc.player?.sendMessage(Text.of("&cCould not open file location"), false)
        }
    }

    private class FileArgumentType(private val relativeTo: File) : ArgumentType<File> {
        override fun parse(reader: StringReader): File {
            val isquoted = StringReader.isQuotedStringStart(reader.peek())
            val path = if (isquoted) {
                reader.readQuotedString()
            } else reader.readStringUntilOrEof(' ')
            return File(relativeTo, path)
        }

        override fun getExamples(): MutableCollection<String> {
            return mutableListOf(
                "/foo/bar/baz",
                "C:\\foo\\bar\\baz",
                "\"/path/with/spaces in the name\"",
            )
        }

        // Copy and pasted from StringReader, but doesn't throw on EOF
        fun StringReader.readStringUntilOrEof(terminator: Char): String {
            val result = StringBuilder()
            var escaped = false
            while (canRead()) {
                val c = read()
                when {
                    escaped -> {
                        escaped = if (c == terminator || c == '\\') {
                            result.append(c)
                            false
                        } else {
                            cursor -= 1
                            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape()
                                .createWithContext(this, c.toString())
                        }
                    }
                    c == '\\' -> escaped = true
                    c == terminator -> {
                        cursor -= 1
                        return result.toString()
                    }
                    else -> result.append(c)
                }
            }

            return result.toString()
        }

        companion object {
            fun getFile(ctx: CommandContext<*>, name: String) = ctx.getArgument(name, File::class.java)
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
