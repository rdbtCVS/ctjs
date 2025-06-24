package com.chattriggers.ctjs.api.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.VertexFormats

object CTRenderPipelines {
    val DEFAULT = RenderPipeline.builder(
        RenderPipelines.POSITION_COLOR_SNIPPET
    )
        .withLocation("ctjs/default_pipeline")
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
        .withBlend(BlendFunction.PANORAMA)
        .withCull(false)
        .build()
}