package com.strata.ui.component;

import com.strata.module.setting.EnumSetting;
import com.strata.ui.render.UiRenderer;
import com.strata.ui.theme.Theme;

/**
 * Drop-down selector for an {@link EnumSetting}.
 *
 * <p>The open list is painted in {@link #drawOverlay}, which the parent calls after
 * every other widget and outside its clip rectangle. Without that the list would be
 * clipped by the scroll region and hidden behind the widgets it overlaps.
 */
public class DropdownWidget extends Widget {

    private static final float CHIP_WIDTH = 104F;
    private static final float OPTION_HEIGHT = 18F;

    private final EnumSetting<?> setting;
    private boolean open;

    public DropdownWidget(EnumSetting<?> setting) {
        this.setting = setting;
        this.height = Theme.ROW_HEIGHT;
    }

    private float chipX() {
        return x + width - CHIP_WIDTH;
    }

    private float chipY() {
        return y + 3F;
    }

    private float chipHeight() {
        return height - 6F;
    }

    private float listHeight() {
        return setting.options().length * OPTION_HEIGHT;
    }

    @Override
    public void draw(UiRenderer ui, int mouseX, int mouseY, float delta) {
        boolean hovered = isOver(mouseX, mouseY);

        ui.text(setting.name(), x, y + (height - 11F) / 2F, 12F,
                hovered || open ? Theme.TEXT : Theme.TEXT_MUTED);

        float cx = chipX();
        float cy = chipY();
        float ch = chipHeight();

        boolean chipHovered = mouseX >= cx && mouseX <= cx + CHIP_WIDTH
                && mouseY >= cy && mouseY <= cy + ch;

        ui.rect(cx, cy, CHIP_WIDTH, ch, Theme.RADIUS_CARD,
                open ? Theme.CARD_ACTIVE : (chipHovered ? Theme.CARD_HOVER : Theme.CARD));

        ui.text(pretty(setting.get().name()), cx + 8F, cy + (ch - 11F) / 2F, 12F, Theme.ACCENT);

        // Caret drawn as a triangle: the UI font has no glyph for ▼/▲, which would
        // otherwise render as a tofu box. Flips to indicate open state.
        float caretCx = cx + CHIP_WIDTH - 12F;
        float caretCy = cy + ch / 2F;
        float half = 3.5F;
        if (open) {
            ui.triangle(caretCx - half, caretCy + 2F, caretCx + half, caretCy + 2F,
                    caretCx, caretCy - 2.5F, Theme.TEXT_MUTED);
        } else {
            ui.triangle(caretCx - half, caretCy - 2F, caretCx + half, caretCy - 2F,
                    caretCx, caretCy + 2.5F, Theme.TEXT_MUTED);
        }
    }

    @Override
    public void drawOverlay(UiRenderer ui, int mouseX, int mouseY, float delta) {
        if (!open) {
            return;
        }
        float cx = chipX();
        float listY = chipY() + chipHeight() + 2F;

        // Near-opaque: an open list sits over the world, and the translucent panel
        // colour leaves it unreadable.
        ui.rect(cx, listY, CHIP_WIDTH, listHeight(), Theme.RADIUS_CARD, Theme.POPUP);

        Enum<?>[] options = setting.options();
        for (int i = 0; i < options.length; i++) {
            float rowY = listY + i * OPTION_HEIGHT;
            boolean selected = options[i] == setting.get();
            boolean hovered = mouseX >= cx && mouseX <= cx + CHIP_WIDTH
                    && mouseY >= rowY && mouseY <= rowY + OPTION_HEIGHT;

            if (hovered) {
                ui.rect(cx, rowY, CHIP_WIDTH, OPTION_HEIGHT, Theme.RADIUS_CARD, Theme.CARD_HOVER);
            } else if (selected) {
                ui.rect(cx, rowY, CHIP_WIDTH, OPTION_HEIGHT, Theme.RADIUS_CARD, Theme.ACCENT_WASH);
            }

            ui.text(pretty(options[i].name()), cx + 8F, rowY + (OPTION_HEIGHT - 11F) / 2F, 12F,
                    selected ? Theme.ACCENT : (hovered ? Theme.TEXT : Theme.TEXT_MUTED));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        float cx = chipX();
        float cy = chipY();
        float ch = chipHeight();

        if (mouseX >= cx && mouseX <= cx + CHIP_WIDTH && mouseY >= cy && mouseY <= cy + ch) {
            open = !open;
            return true;
        }

        if (open) {
            float listY = cy + ch + 2F;
            if (mouseX >= cx && mouseX <= cx + CHIP_WIDTH
                    && mouseY >= listY && mouseY <= listY + listHeight()) {
                int index = (int) ((mouseY - listY) / OPTION_HEIGHT);
                Enum<?>[] options = setting.options();
                if (index >= 0 && index < options.length) {
                    selectByName(options[index].name());
                }
                open = false;
                return true;
            }
            // Any click elsewhere dismisses, but does not get swallowed.
            open = false;
        }
        return false;
    }

    /** Routed through the setting's own deserialize so the generic stays contained. */
    private void selectByName(String name) {
        setting.deserialize(name);
    }

    public boolean isOpen() {
        return open;
    }

    /** SCREAMING_SNAKE_CASE reads badly in a UI; render it as Title Case. */
    private static String pretty(String constant) {
        String[] parts = constant.toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(part.charAt(0))).append(part, 1, part.length());
        }
        return out.toString();
    }
}
