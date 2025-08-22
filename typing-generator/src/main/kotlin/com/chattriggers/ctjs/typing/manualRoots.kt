package com.chattriggers.ctjs.typing

import org.jsoup.Jsoup

object ManualRoots {
    private const val FABRIC_API_URL = "https://maven.fabricmc.net/docs/fabric-api-0.132.0+1.21.8/allclasses-index.html"
    private const val YARN_URL = "https://maven.fabricmc.net/docs/yarn-1.21.8+build.1/allclasses-index.html"

    val roots = mutableSetOf(
        "java.awt.Color",
        "java.util.ArrayList",
        "java.util.HashMap",
        "gg.essential.universal.UKeyboard",
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

    private fun collectClasses(url: String) {
        val doc = Jsoup.connect(url).get()
        val links = doc.getElementsByClass("col-first")
        links.forEach { link ->
            val a  = link.getElementsByTag("a")
            if (a.isEmpty()) return@forEach
            val href = a.first()?.attr("href") ?: return@forEach
            roots += href.replace("/", ".").replace(".html", "").trim()
        }
    }

    init {
        collectClasses(YARN_URL)
        collectClasses(FABRIC_API_URL)
    }
}


