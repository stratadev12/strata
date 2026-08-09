package com.strata.ui.theme;

/**
 * Single source of truth for colours and metrics.
 *
 * <p>Palette is "mountain": granite blacks and greys, snow whites, and a restrained
 * glacial blue used only for state that matters (enabled, selected, focused).
 * Surfaces are deliberately translucent so the world reads through them -- there is
 * no background blur, so the panel's own alpha is what separates it from the scene.
 */
public final class Theme {

    private Theme() {
    }

    // 0xAARRGGBB

    // --- surfaces (translucent, darkest at the back) ---
    /** Main panel body. ~83% opaque. */
    public static final int PANEL = 0xD40E1013;
    /** Header / title bar, slightly denser than the body. */
    public static final int PANEL_HEADER = 0xE216191E;
    /** Raised surface: cards, rows, list items. */
    public static final int CARD = 0xB81C2026;
    public static final int CARD_HOVER = 0xC8262C34;
    public static final int CARD_ACTIVE = 0xD4303740;
    /** Popups float over the world, so they need to be near-opaque to stay readable. */
    public static final int POPUP = 0xFA14171B;

    /** Hairline separators. White at low alpha reads as a lit granite edge. */
    public static final int OUTLINE = 0x24FFFFFF;
    public static final int OUTLINE_STRONG = 0x3DFFFFFF;

    // --- accent: glacial blue, used sparingly ---
    public static final int ACCENT = 0xFF8FC1E3;
    public static final int ACCENT_DIM = 0xFF5D839F;
    /** Wash behind a selected row. */
    public static final int ACCENT_WASH = 0x2E8FC1E3;

    // --- text ---
    public static final int TEXT = 0xFFEFF3F6;
    public static final int TEXT_MUTED = 0xFF98A2AC;
    public static final int TEXT_FAINT = 0xFF616A73;
    /** Text drawn on top of an accent fill. */
    public static final int TEXT_ON_ACCENT = 0xFF0C1116;

    // --- metrics (GUI units) ---
    // Sharp-edged by design. Every surface reads its radius from here, so the whole
    // UI switches between sharp and rounded by changing these three numbers.
    public static final int RADIUS_PANEL = 0;
    public static final int RADIUS_CARD = 0;
    /** Kept as a distinct constant so switches/tabs can be re-rounded independently. */
    public static final int RADIUS_PILL = 0;

    public static final int PADDING = 10;
    public static final int GAP = 6;
    public static final int ROW_HEIGHT = 22;
    public static final int HEADER_HEIGHT = 26;
}
