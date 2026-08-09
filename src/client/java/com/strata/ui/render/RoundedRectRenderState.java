package com.strata.ui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;

/**
 * A single anti-aliased rounded quad, submitted straight into the retained-mode GUI
 * render state.
 *
 * <p>Emits UVs spanning 0..1 so the fragment shader can recover the quad's pixel size
 * from {@code fwidth(uv)}; see {@code rounded_rect.fsh}.
 */
public record RoundedRectRenderState(
        RenderPipeline pipeline,
        Matrix3x2f pose,
        float x0,
        float y0,
        float x1,
        float y1,
        int color,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds
) implements GuiElementRenderState {

    @Override
    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(pose, x0, y0).setUv(0.0F, 0.0F).setColor(color);
        consumer.addVertexWith2DPose(pose, x0, y1).setUv(0.0F, 1.0F).setColor(color);
        consumer.addVertexWith2DPose(pose, x1, y1).setUv(1.0F, 1.0F).setColor(color);
        consumer.addVertexWith2DPose(pose, x1, y0).setUv(1.0F, 0.0F).setColor(color);
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }
}
