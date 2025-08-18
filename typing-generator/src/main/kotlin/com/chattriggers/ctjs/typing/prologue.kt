package com.chattriggers.ctjs.typing

val manualRoots = setOf(
    "java.awt.Color",
    "java.util.ArrayList",
    "java.util.HashMap",
    "gg.essential.universal.UKeyboard",
    "net.minecraft.util.Hand",
    "org.lwjgl.opengl.GL11",
    "org.lwjgl.opengl.GL12",
    "org.lwjgl.opengl.GL13",
    "org.lwjgl.opengl.GL14",
    "org.lwjgl.opengl.GL15",
    "org.lwjgl.opengl.GL20",
    "org.lwjgl.opengl.GL21",
    "org.lwjgl.opengl.GL30",
    "org.lwjgl.opengl.GL31",
    "org.lwjgl.opengl.GL32",
    "org.lwjgl.opengl.GL33",
    "org.lwjgl.opengl.GL40",
    "org.lwjgl.opengl.GL41",
    "org.lwjgl.opengl.GL42",
    "org.lwjgl.opengl.GL43",
    "org.lwjgl.opengl.GL44",
    "org.lwjgl.opengl.GL45",
    "org.spongepowered.asm.mixin.injection.callback.CallbackInfo",
)

private val providedTypes = mutableMapOf(
    "Keyboard" to "gg.essential.universal.UKeyboard",
    "Hand" to "net.minecraft.util.Hand",

    "FileLib" to "com.chattriggers.ctjs.api.FileLib",
    "CancellableEvent" to "com.chattriggers.ctjs.api.triggers.CancellableEvent",
    "Config" to "com.chattriggers.ctjs.api.Config",
    "TriggerRegister" to "com.chattriggers.ctjs.engine.Register",
    "Thread" to "com.chattriggers.ctjs.engine.WrappedThread",
    "Priority" to "com.chattriggers.ctjs.api.triggers.Trigger\$Priority",
    "ChatTriggers" to "com.chattriggers.ctjs.CTJS",
    "Console" to "com.chattriggers.ctjs.engine.Console",

    "GL11" to "org.lwjgl.opengl.GL11",
    "GL12" to "org.lwjgl.opengl.GL12",
    "GL13" to "org.lwjgl.opengl.GL13",
    "GL14" to "org.lwjgl.opengl.GL14",
    "GL15" to "org.lwjgl.opengl.GL15",
    "GL20" to "org.lwjgl.opengl.GL20",
    "GL21" to "org.lwjgl.opengl.GL21",
    "GL30" to "org.lwjgl.opengl.GL30",
    "GL31" to "org.lwjgl.opengl.GL31",
    "GL32" to "org.lwjgl.opengl.GL32",
    "GL33" to "org.lwjgl.opengl.GL33",
    "GL40" to "org.lwjgl.opengl.GL40",
    "GL41" to "org.lwjgl.opengl.GL41",
    "GL42" to "org.lwjgl.opengl.GL42",
    "GL43" to "org.lwjgl.opengl.GL43",
    "GL44" to "org.lwjgl.opengl.GL44",
    "GL45" to "org.lwjgl.opengl.GL45",
)

val prologue = """
    /// <reference no-default-lib="true" />
    /// <reference lib="es2015" />
    export {};
    
    declare interface Number {
      easeOut(to: number, speed: number, jump: number): number;
      easeColor(to: number, speed: number, jump: number): java.awt.Color;
    }

    declare global {
      const Java: {
        /**
         * Returns the Java Class or Package given by name. If you want to
         * enforce the name is a class, use Java.class() instead.
         */
        type(name: string): java.lang.Package | java.lang.Class<any>;
  
        /**
         * Returns the Java Class given by `className`. Throws an error if the
         * name is not a valid class name.
         */
        class(className: string): java.lang.Class<any>;
      };

      /**
       * Runs `func` in a Java synchronized() block with `lock` as the synchronizer
       */
      function sync(func: () => void, lock: unknown): void;
  
      /**
       * Runs `func` after `delayInMs` milliseconds. A new thread is spawned to accomplish
       * this, which means this function is asynchronous. If you want to avoid the Thread
       * instantiation, use `Client.scheduleTask(delayInTicks, func)`.
       */
      function setTimeout(func: () => void, delayInMs: number): void;

      const ArrayList: typeof java.util.ArrayList;
      interface ArrayList<T> extends java.util.ArrayList<T> {}
      const HashMap: typeof java.util.HashMap;
      interface HashMap<K, V> extends java.util.HashMap<K, V> {}
      
${providedTypes.entries.joinToString("") { (name, type) ->
"const $name: typeof $type;\ninterface $name extends $type {}\n"
}.prependIndent("      ")}

      /**
       * Registers a new trigger and returns it.
       */
      function register<T extends keyof RegisterTypes>(
        name: T, 
        cb: (...args: Parameters<RegisterTypes[T]>) => void,
      ): ReturnType<RegisterTypes[T]>;

      /**
       * Cancels the given event
       */
      function cancel(event: CancellableEvent | org.spongepowered.asm.mixin.injection.callback.CallbackInfo): void;

      /**
       * Creates a custom trigger. `name` can be used as the first argument of a
       * subsequent call to `register`. Returns an object that can be used to
       * invoke the trigger.
       */
      function createCustomTrigger(name: string): { trigger(...args: unknown[]) };
      
      function easeOut(start: number, finish: number, speed: number, jump?: number): number;
      function easeColor(start: number, finish: number, speed: number, jump?: number): java.awt.Color;

      function print(message: string, color?: java.awt.Color): void;
      function println(message: string, color?: java.awt.Color, end?: string): void;

      const console: {
        assert(condition: boolean, message: string): void;
        clear(): void;
        count(label?: string): void;
        debug(args: unknown[]): void;
        dir(obj: object): void;
        dirxml(obj: object): void;
        error(...args: unknown[]): void;
        group(...args: unknown[]): void;
        groupCollapsed(...args: unknown[]): void;
        groupEnd(...args: unknown[]): void;
        info(...args: unknown[]): void;
        log(...args: unknown[]): void;
        table(data: object, columns?: string[]): void;
        time(label?: string): void;
        timeEnd(label?: string): void;
        trace(...args: unknown[]): void;
        warn(...args: unknown[]): void;
      };
""".trimIndent()
