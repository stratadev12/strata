package com.strata.ui.theme;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

/**
 * Custom typeface support.
 *
 * <p>Minecraft renders TTF natively through the font-provider system, so a custom
 * face needs no extra rendering machinery: the glyphs go through the same text path
 * as everything else, which keeps them correctly ordered inside the retained-mode
 * GUI state. Selection happens per-Component via {@code Style#withFont}.
 *
 * <p>26.1 takes a {@link FontDescription} rather than a bare Identifier.
 */
public final class Fonts {

    /** Resolves to assets/strata/font/main.json. */
    public static final Identifier MAIN_ID = Identifier.fromNamespaceAndPath("strata", "main");

    private static final FontDescription MAIN = new FontDescription.Resource(MAIN_ID);

    private Fonts() {
    }

    /** Literal text in Strata's typeface. */
    public static MutableComponent text(String literal) {
        return Component.literal(literal).withStyle(style -> style.withFont(MAIN));
    }

    /** Restyle an existing component into Strata's typeface. */
    public static MutableComponent styled(MutableComponent component) {
        return component.withStyle(style -> style.withFont(MAIN));
    }
}
