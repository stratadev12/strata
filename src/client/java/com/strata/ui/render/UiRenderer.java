package com.strata.ui.render;

/**
 * Everything the UI is allowed to draw with.
 *
 * <p>Deliberately expressed in primitives and measurements only -- no Minecraft and no
 * NanoVG types cross this boundary. Widgets code against this, so the backend can be
 * replaced without touching a single widget.
 */
public interface UiRenderer {

    void rect(float x, float y, float width, float height, Corners radii, int argb);

    default void rect(float x, float y, float width, float height, int radius, int argb) {
        rect(x, y, width, height, Corners.all(radius), argb);
    }

    /** Draws with the baseline-independent top-left origin, like every other coordinate. */
    void text(String text, float x, float y, float size, int argb);

    float textWidth(String text, float size);

    float lineHeight(float size);

    /**
     * Icons are drawn as geometry rather than glyphs: the UI font carries no symbol
     * coverage, so characters like ✕ and ▼ render as tofu boxes.
     */
    void line(float x1, float y1, float x2, float y2, float thickness, int argb);

    void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int argb);

    /**
     * Draws a texture from {@code assets/strata/textures/<name>.png}, stretched to the
     * given box. Loaded lazily and cached by name.
     */
    void image(String name, float x, float y, float width, float height, float alpha);

    default void image(String name, float x, float y, float width, float height) {
        image(name, x, y, width, height, 1.0F);
    }

    /** Clips subsequent drawing to this rectangle until {@link #clearClip()}. */
    void clip(float x, float y, float width, float height);

    void clearClip();
}
