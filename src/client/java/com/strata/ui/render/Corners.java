package com.strata.ui.render;

/** Per-corner radii in pixels. Doubles as the pipeline cache key. */
public record Corners(int topLeft, int topRight, int bottomRight, int bottomLeft) {

    public static Corners all(int radius) {
        return new Corners(radius, radius, radius, radius);
    }

    public static Corners none() {
        return new Corners(0, 0, 0, 0);
    }

    /** Rounded along the top edge only -- headers, tab strips, first list row. */
    public static Corners top(int radius) {
        return new Corners(radius, radius, 0, 0);
    }

    /** Rounded along the bottom edge only -- footers, last list row. */
    public static Corners bottom(int radius) {
        return new Corners(0, 0, radius, radius);
    }

    public static Corners left(int radius) {
        return new Corners(radius, 0, 0, radius);
    }

    public static Corners right(int radius) {
        return new Corners(0, radius, radius, 0);
    }

    /** Stable, filesystem-safe suffix for the pipeline identifier. */
    public String key() {
        return topLeft + "_" + topRight + "_" + bottomRight + "_" + bottomLeft;
    }
}
