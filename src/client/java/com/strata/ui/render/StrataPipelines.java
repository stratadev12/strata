package com.strata.ui.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom render pipelines.
 *
 * <p>Corner radii are compile-time {@code #define}s rather than uniforms, because
 * {@link net.minecraft.client.renderer.state.gui.GuiElementRenderState} has no hook
 * for setting per-draw uniforms. Each distinct corner combination therefore compiles
 * its own pipeline, hence the cache -- a UI realistically uses a handful.
 */
public final class StrataPipelines {

    private static final Map<Corners, RenderPipeline> ROUNDED = new HashMap<>();

    private StrataPipelines() {
    }

    public static RenderPipeline rounded(Corners corners) {
        return ROUNDED.computeIfAbsent(corners, StrataPipelines::buildRounded);
    }

    private static RenderPipeline buildRounded(Corners corners) {
        return RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath("strata", "pipeline/rounded_rect_" + corners.key()))
                .withVertexShader(Identifier.fromNamespaceAndPath("strata", "core/rounded_rect"))
                .withFragmentShader(Identifier.fromNamespaceAndPath("strata", "core/rounded_rect"))
                .withShaderDefine("R_TL", (float) corners.topLeft())
                .withShaderDefine("R_TR", (float) corners.topRight())
                .withShaderDefine("R_BR", (float) corners.bottomRight())
                .withShaderDefine("R_BL", (float) corners.bottomLeft())
                // Uniform blocks must be declared here as well as in the GLSL, or the
                // engine refuses to bind them and the shader reads undefined memory.
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                // Straight (non-premultiplied) alpha, matching what the fragment
                // shader emits. Without this the default state is used and the
                // antialiased edge blends as if premultiplied, giving a light halo.
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withCull(false)
                .build();
    }
}
