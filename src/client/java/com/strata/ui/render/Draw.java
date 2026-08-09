package com.strata.ui.render;

import com.strata.mixin.GuiGraphicsExtractorAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.joml.Matrix3x2f;

/** Drawing helpers on top of the retained-mode GUI state. */
public final class Draw {

    private Draw() {
    }

    public static void roundedRect(GuiGraphicsExtractor graphics, float x, float y,
                                   float width, float height, int radius, int argb) {
        roundedRect(graphics, x, y, width, height, Corners.all(radius), argb);
    }

    public static void roundedRect(GuiGraphicsExtractor graphics, float x, float y,
                                   float width, float height, Corners corners, int argb) {
        GuiRenderState state = ((GuiGraphicsExtractorAccessor) graphics).strata$guiRenderState();

        // pose() returns a live stack, so snapshot it -- the element is drawn later.
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());

        ScreenRectangle bounds = new ScreenRectangle(
                (int) Math.floor(x), (int) Math.floor(y),
                (int) Math.ceil(width), (int) Math.ceil(height));

        state.addGuiElement(new RoundedRectRenderState(
                StrataPipelines.rounded(corners),
                pose,
                x, y, x + width, y + height,
                argb,
                null,
                bounds));
    }
}
