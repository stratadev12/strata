package com.strata.ui.render.nanovg;

import com.strata.StrataClient;
import com.strata.ui.render.Corners;
import com.strata.ui.render.UiRenderer;
import net.minecraft.client.Minecraft;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NanoVG-backed implementation of {@link UiRenderer}.
 *
 * <p>Minecraft 26.1 routes all drawing through Blaze3D, which leaves GL state that
 * NanoVG does not expect. The reset in {@link #beginFrame} is mandatory, not defensive
 * -- in particular {@code glBindSampler(0, 0)}, without which the glyph atlas becomes
 * an incomplete texture and text renders as nothing at all, silently.
 */
public final class NanoVgRenderer implements UiRenderer {

    public static final NanoVgRenderer INSTANCE = new NanoVgRenderer();

    private static final String FONT = "manrope";
    private static final String FONT_PATH = "/assets/strata/font/manrope-medium.ttf";

    private long context;
    private boolean initialised;
    private boolean failed;

    /** Must outlive the font inside NanoVG, which keeps a pointer to it. */
    @SuppressWarnings("unused")
    private ByteBuffer fontData;

    private final NVGColor color = NVGColor.create();
    private float guiScale = 1.0F;

    private final Map<String, Integer> images = new HashMap<>();
    /** Retained so the off-heap buffers backing decoded images are never collected. */
    private final List<ByteBuffer> imageData = new ArrayList<>();

    private NanoVgRenderer() {
    }

    // ---- lifecycle ----

    private boolean ensureInitialised() {
        if (initialised) {
            return true;
        }
        if (failed) {
            return false;
        }
        try {
            context = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_STENCIL_STROKES);
            if (context == 0L) {
                throw new IllegalStateException("nvgCreate returned NULL");
            }
            fontData = readResource(FONT_PATH);
            if (NanoVG.nvgCreateFontMem(context, FONT, fontData, false) == -1) {
                throw new IllegalStateException("nvgCreateFontMem failed");
            }
            StrataClient.LOGGER.info("[nanovg] renderer ready");
            initialised = true;
            return true;
        } catch (Throwable t) {
            StrataClient.LOGGER.error("[nanovg] init failed; NanoVG UI disabled", t);
            failed = true;
            return false;
        }
    }

    /** @return true if a frame was opened and must be closed with {@link #endFrame}. */
    public boolean beginFrame() {
        if (!ensureInitialised()) {
            return false;
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) {
            return false;
        }
        int fbWidth = client.getWindow().getWidth();
        int fbHeight = client.getWindow().getHeight();
        if (fbWidth <= 0 || fbHeight <= 0) {
            return false;
        }
        guiScale = (float) client.getWindow().getGuiScale();

        // Blaze3D leaves state NanoVG cannot cope with. Each of these matters:
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, fbWidth, fbHeight);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        // A bound sampler overrides the texture's own parameters. NanoVG's glyph atlas
        // has no mipmaps, so Blaze3D's sampler makes it incomplete -> invisible text.
        GL33.glBindSampler(0, 0);
        // Blaze3D uploads textures through buffer objects; a bound one would make the
        // glyph upload read from it instead of NanoVG's own memory.
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
        GL20.glUseProgram(0);
        GL30.glBindVertexArray(0);

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        NanoVG.nvgBeginFrame(context, fbWidth, fbHeight, 1.0F);
        // Work in GUI units so coordinates match Screen#width / Screen#height.
        NanoVG.nvgScale(context, guiScale, guiScale);
        return true;
    }

    public void endFrame() {
        NanoVG.nvgEndFrame(context);
    }

    // ---- UiRenderer ----

    @Override
    public void rect(float x, float y, float width, float height, Corners radii, int argb) {
        NanoVG.nvgBeginPath(context);
        // Clamp so a "pill" radius cannot exceed half the shorter side.
        float limit = Math.min(width, height) * 0.5F;
        NanoVG.nvgRoundedRectVarying(context, x, y, width, height,
                Math.min(radii.topLeft(), limit),
                Math.min(radii.topRight(), limit),
                Math.min(radii.bottomRight(), limit),
                Math.min(radii.bottomLeft(), limit));
        NanoVG.nvgFillColor(context, argb(argb));
        NanoVG.nvgFill(context);
    }

    @Override
    public void text(String text, float x, float y, float size, int argb) {
        NanoVG.nvgFontSize(context, size);
        NanoVG.nvgFontFace(context, FONT);
        // Top-left origin, so callers never have to reason about baselines.
        NanoVG.nvgTextAlign(context, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_TOP);
        NanoVG.nvgFillColor(context, argb(argb));
        NanoVG.nvgText(context, x, y, text);
    }

    @Override
    public float textWidth(String text, float size) {
        NanoVG.nvgFontSize(context, size);
        NanoVG.nvgFontFace(context, FONT);
        return NanoVG.nvgTextBounds(context, 0, 0, text, (float[]) null);
    }

    @Override
    public float lineHeight(float size) {
        NanoVG.nvgFontSize(context, size);
        NanoVG.nvgFontFace(context, FONT);
        float[] ascender = new float[1];
        float[] descender = new float[1];
        float[] lineHeight = new float[1];
        NanoVG.nvgTextMetrics(context, ascender, descender, lineHeight);
        return lineHeight[0];
    }

    @Override
    public void line(float x1, float y1, float x2, float y2, float thickness, int argb) {
        NanoVG.nvgBeginPath(context);
        NanoVG.nvgMoveTo(context, x1, y1);
        NanoVG.nvgLineTo(context, x2, y2);
        NanoVG.nvgStrokeWidth(context, thickness);
        NanoVG.nvgLineCap(context, NanoVG.NVG_ROUND);
        NanoVG.nvgStrokeColor(context, argb(argb));
        NanoVG.nvgStroke(context);
    }

    @Override
    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int argb) {
        NanoVG.nvgBeginPath(context);
        NanoVG.nvgMoveTo(context, x1, y1);
        NanoVG.nvgLineTo(context, x2, y2);
        NanoVG.nvgLineTo(context, x3, y3);
        NanoVG.nvgClosePath(context);
        NanoVG.nvgFillColor(context, argb(argb));
        NanoVG.nvgFill(context);
    }

    @Override
    public void image(String name, float x, float y, float width, float height, float alpha) {
        int handle = imageHandle(name);
        if (handle == -1) {
            return;
        }
        NVGPaint paint = NanoVG.nvgImagePattern(context, x, y, width, height, 0F, handle, alpha,
                NVGPaint.create());
        NanoVG.nvgBeginPath(context);
        NanoVG.nvgRect(context, x, y, width, height);
        NanoVG.nvgFillPaint(context, paint);
        NanoVG.nvgFill(context);
    }

    /** @return the NanoVG image handle, or -1 if it could not be loaded. */
    private int imageHandle(String name) {
        Integer cached = images.get(name);
        if (cached != null) {
            return cached;
        }
        int handle = -1;
        try {
            // Kept off-heap and retained: NanoVG decodes from this buffer.
            ByteBuffer data = readResource("/assets/strata/textures/" + name + ".png");
            imageData.add(data);
            handle = NanoVG.nvgCreateImageMem(context, 0, data);
            if (handle == 0) {
                handle = -1;
                StrataClient.LOGGER.error("[nanovg] could not decode image '{}'", name);
            }
        } catch (IOException e) {
            StrataClient.LOGGER.error("[nanovg] missing image '{}'", name, e);
        }
        images.put(name, handle);
        return handle;
    }

    @Override
    public void clip(float x, float y, float width, float height) {
        NanoVG.nvgScissor(context, x, y, width, height);
    }

    @Override
    public void clearClip() {
        NanoVG.nvgResetScissor(context);
    }

    // ---- helpers ----

    private NVGColor argb(int argb) {
        return color
                .r(((argb >> 16) & 0xFF) / 255F)
                .g(((argb >> 8) & 0xFF) / 255F)
                .b((argb & 0xFF) / 255F)
                .a(((argb >>> 24) & 0xFF) / 255F);
    }

    private static ByteBuffer readResource(String path) throws IOException {
        try (InputStream in = NanoVgRenderer.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("missing resource: " + path);
            }
            byte[] bytes = in.readAllBytes();
            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            return buffer.put(bytes).flip();
        }
    }
}
