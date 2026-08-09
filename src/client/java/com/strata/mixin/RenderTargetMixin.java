package com.strata.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.strata.ui.render.nanovg.NanoVgDrawable;
import com.strata.ui.render.nanovg.NanoVgRenderer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NanoVG draw hook.
 *
 * <p>26.1's {@link RenderTarget} exposes no framebuffer id -- it is abstracted behind
 * {@code GpuTexture}, and the raw GL handles live in the backend-internal
 * {@code com.mojang.blaze3d.opengl} classes. So Blaze3D cannot tell us which
 * framebuffer to target.
 *
 * <p>The one place a raw GL draw reliably lands is immediately after the finished frame
 * is blitted to the default framebuffer, just before the buffer swap. Injecting at
 * {@code GameRenderer#render} TAIL instead produces no visible output at all.
 */
@Mixin(RenderTarget.class)
public class RenderTargetMixin {

    @Inject(method = "blitToScreen", at = @At("TAIL"))
    private void strata$drawNanoVg(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getMainRenderTarget() != (Object) this) {
            return;
        }
        if (!(client.screen instanceof NanoVgDrawable drawable)) {
            return;
        }

        double scale = client.getWindow().getGuiScale();
        int mouseX = (int) (client.mouseHandler.xpos() / scale);
        int mouseY = (int) (client.mouseHandler.ypos() / scale);

        NanoVgRenderer renderer = NanoVgRenderer.INSTANCE;
        if (!renderer.beginFrame()) {
            return;
        }
        try {
            drawable.drawNanoVg(renderer, mouseX, mouseY, client.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        } catch (Throwable t) {
            com.strata.StrataClient.LOGGER.error("[nanovg] draw threw", t);
        } finally {
            renderer.endFrame();
        }
    }
}
