package com.strata.ui.render.nanovg;

import com.strata.ui.render.UiRenderer;

/**
 * Implemented by screens that draw themselves through NanoVG rather than Minecraft's
 * retained-mode GUI state.
 *
 * <p>Screens remain real {@code Screen} subclasses so vanilla still handles input,
 * focus and lifecycle; only the painting is diverted.
 */
public interface NanoVgDrawable {

    /** Coordinates are in GUI units, matching {@code Screen#width}/{@code height}. */
    void drawNanoVg(UiRenderer renderer, int mouseX, int mouseY, float delta);
}
