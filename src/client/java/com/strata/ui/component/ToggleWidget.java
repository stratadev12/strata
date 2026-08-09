package com.strata.ui.component;

import com.strata.module.setting.BoolSetting;
import com.strata.ui.render.UiRenderer;
import com.strata.ui.theme.Theme;

/** A label with a sliding pill switch, bound to a {@link BoolSetting}. */
public class ToggleWidget extends Widget {

    private static final float TRACK_WIDTH = 28F;
    private static final float TRACK_HEIGHT = 15F;
    private static final float KNOB_INSET = 2.5F;

    private final BoolSetting setting;

    /** 0 = off, 1 = on. Animated so the knob slides rather than jumps. */
    private float progress;

    public ToggleWidget(BoolSetting setting) {
        this.setting = setting;
        this.progress = setting.value() ? 1F : 0F;
        this.height = Theme.ROW_HEIGHT;
    }

    @Override
    public void draw(UiRenderer ui, int mouseX, int mouseY, float delta) {
        progress = approach(progress, setting.value() ? 1F : 0F, delta);

        boolean hovered = isOver(mouseX, mouseY);
        ui.text(setting.name(), x, y + (height - 11F) / 2F, 12F,
                hovered ? Theme.TEXT : Theme.TEXT_MUTED);

        float trackX = x + width - TRACK_WIDTH;
        float trackY = y + (height - TRACK_HEIGHT) / 2F;

        int trackColour = blend(Theme.CARD_ACTIVE, Theme.ACCENT, progress);
        ui.rect(trackX, trackY, TRACK_WIDTH, TRACK_HEIGHT, Theme.RADIUS_PILL, trackColour);

        float knobDiameter = TRACK_HEIGHT - KNOB_INSET * 2F;
        float knobTravel = TRACK_WIDTH - knobDiameter - KNOB_INSET * 2F;
        float knobX = trackX + KNOB_INSET + knobTravel * progress;
        ui.rect(knobX, trackY + KNOB_INSET, knobDiameter, knobDiameter,
                Theme.RADIUS_PILL, Theme.TEXT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isOver(mouseX, mouseY)) {
            return false;
        }
        setting.toggle();
        return true;
    }

    /** Linear interpolation in 0xAARRGGBB space, per channel. */
    static int blend(int from, int to, float t) {
        t = Math.max(0F, Math.min(1F, t));
        int a = lerpChannel(from >>> 24, to >>> 24, t);
        int r = lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, t);
        int g = lerpChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, t);
        int b = lerpChannel(from & 0xFF, to & 0xFF, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerpChannel(int from, int to, float t) {
        return Math.round(from + (to - from) * t);
    }
}
