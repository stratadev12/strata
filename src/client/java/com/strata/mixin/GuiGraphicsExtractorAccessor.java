package com.strata.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 26.1's GUI is retained-mode: custom geometry is submitted by handing a
 * {@link net.minecraft.client.renderer.state.gui.GuiElementRenderState} to
 * {@link GuiRenderState#addGuiElement}. {@code GuiGraphicsExtractor} holds the only
 * reachable {@code GuiRenderState} in a private field with no getter, so an accessor
 * is the sole way to draw anything the vanilla helper methods do not already cover.
 */
@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {

    @Accessor("guiRenderState")
    GuiRenderState strata$guiRenderState();
}
