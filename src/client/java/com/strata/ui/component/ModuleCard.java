package com.strata.ui.component;

import com.strata.module.Module;
import com.strata.ui.render.UiRenderer;
import com.strata.ui.theme.Theme;

/**
 * A module as a grid tile: name, description, and an enable switch.
 *
 * <p>Settings are not shown here -- a tile is a summary. Opening a module's settings is
 * a separate view, so the grid stays a uniform scannable shape.
 */
public class ModuleCard extends Widget {

    private final Module module;

    private float enableProgress;
    private float hoverProgress;

    public ModuleCard(Module module) {
        this.module = module;
        this.enableProgress = module.isEnabled() ? 1F : 0F;
    }

    public Module module() {
        return module;
    }

    @Override
    public void draw(UiRenderer ui, int mouseX, int mouseY, float delta) {
        boolean hovered = isOver(mouseX, mouseY);
        enableProgress = approach(enableProgress, module.isEnabled() ? 1F : 0F, delta);
        hoverProgress = approach(hoverProgress, hovered ? 1F : 0F, delta);

        int background = ToggleWidget.blend(Theme.CARD, Theme.CARD_HOVER, hoverProgress);
        ui.rect(x, y, width, height, Theme.RADIUS_CARD, background);

        // Enabled tiles pick up a faint accent wash so state reads at a glance.
        if (enableProgress > 0.01F) {
            int wash = ToggleWidget.blend(0x00000000, Theme.ACCENT_WASH, enableProgress);
            ui.rect(x, y, width, height, Theme.RADIUS_CARD, wash);
        }

        float textWidth = width - 24F;
        ui.text(fit(ui, module.name(), textWidth, 13F), x + 12F, y + 12F, 13F,
                module.isEnabled() ? Theme.TEXT : Theme.TEXT_MUTED);

        String description = module.description();
        if (!description.isEmpty()) {
            ui.text(fit(ui, description, textWidth, 11F), x + 12F, y + 30F, 11F, Theme.TEXT_FAINT);
        }

        drawSwitch(ui, x + width - 38F, y + height - 24F);

        String hint = module.settings().isEmpty() ? "" : "settings →";
        if (!hint.isEmpty()) {
            ui.text(hint, x + 12F, y + height - 20F, 11F,
                    hovered ? Theme.ACCENT : Theme.TEXT_FAINT);
        }
    }

    private void drawSwitch(UiRenderer ui, float trackX, float trackY) {
        float trackWidth = 26F;
        float trackHeight = 15F;
        float inset = 2.5F;

        ui.rect(trackX, trackY, trackWidth, trackHeight, Theme.RADIUS_PILL,
                ToggleWidget.blend(Theme.CARD_ACTIVE, Theme.ACCENT, enableProgress));

        float knob = trackHeight - inset * 2F;
        float travel = trackWidth - knob - inset * 2F;
        ui.rect(trackX + inset + travel * enableProgress, trackY + inset, knob, knob,
                Theme.RADIUS_PILL, Theme.TEXT);
    }

    /**
     * Truncates with an ellipsis so long text degrades gracefully instead of being
     * sliced mid-glyph by a clip rectangle.
     */
    private static String fit(UiRenderer ui, String text, float maxWidth, float size) {
        if (ui.textWidth(text, size) <= maxWidth) {
            return text;
        }
        int end = text.length();
        while (end > 0 && ui.textWidth(text.substring(0, end) + "…", size) > maxWidth) {
            end--;
        }
        return text.substring(0, Math.max(0, end)).stripTrailing() + "…";
    }

    /** True when the click landed on the switch rather than the tile body. */
    public boolean isOnSwitch(double mouseX, double mouseY) {
        return mouseX >= x + width - 42F && mouseX <= x + width - 8F
                && mouseY >= y + height - 28F && mouseY <= y + height - 5F;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isOver(mouseX, mouseY)) {
            return false;
        }
        if (isOnSwitch(mouseX, mouseY)) {
            module.toggle();
        }
        // Tile-body clicks are handled by the parent, which owns view switching.
        return true;
    }
}
