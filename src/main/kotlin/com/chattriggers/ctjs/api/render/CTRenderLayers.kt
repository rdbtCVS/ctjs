package com.chattriggers.ctjs.api.render

import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.BufferAllocator

object CTRenderLayers {
    val DEFAULT = RenderLayer.of(
        "ctjs/default",
        1024,
        CTRenderPipelines.DEFAULT,
        RenderLayer.MultiPhaseParameters.builder()
            .build(false)
    )
}