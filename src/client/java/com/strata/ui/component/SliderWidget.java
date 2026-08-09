package com.strata.ui.component;

import com.strata.module.setting.NumberSetting;
import com.strata.ui.render.UiRenderer;
import com.strata.ui.theme.Theme;

/** A labelled track with a draggable knob, bound to a {@link NumberSetting}. */
public class SliderWidget extends Widget {

    private static final float TRACK_HEIGHT = 4F;
    private static final float KNOB_RADIUS = 5F;
    private static final float LABEL_HEIGHT = 13F;

    private final NumberSetting setting;
    private boolean dragging;

    public SliderWidget(NumberSetting setting) {
        this.setting = setting;
        this.height = Theme.ROW_HEIGHT + 8F;
    }

    @Override
    public void draw(UiRenderer ui, int mouseX, int mouseY, float delta) {
        boolean hovered = isOver(mouseX, mouseY);

        ui.text(setting.name(), x, y, 12F, hovered || dragging ? Theme.TEXT : Theme.TEXT_MUTED);

        String value = format(setting.value());
        ui.text(value, x + width - ui.textWidth(value, 12F), y, 12F, Theme.TEXT_MUTED);

        float trackY = y + LABEL_HEIGHT + 6F;
        // Inset by the knob radius so the knob never overhangs the track ends.
        float trackX = x + KNOB_RADIUS;
        float trackWidth = width - KNOB_RADIUS * 2F;

        ui.rect(trackX, trackY, trackWidth, TRACK_HEIGHT, Theme.RADIUS_PILL, Theme.CARD_ACTIVE);

        float fraction = (float) setting.fraction();
        ui.rect(trackX, trackY, trackWidth * fraction, TRACK_HEIGHT, Theme.RADIUS_PILL, Theme.ACCENT);

        float knobX = trackX + trackWidth * fraction;
        ui.rect(knobX - KNOB_RADIUS, trackY + TRACK_HEIGHT / 2F - KNOB_RADIUS,
                KNOB_RADIUS * 2F, KNOB_RADIUS * 2F, Theme.RADIUS_PILL, Theme.TEXT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isOver(mouseX, mouseY)) {
            return false;
        }
        dragging = true;
        applyFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (!dragging) {
            return false;
        }
        applyFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!dragging) {
            return false;
        }
        dragging = false;
        return true;
    }

    private void applyFromMouse(double mouseX) {
        float trackX = x + KNOB_RADIUS;
        float trackWidth = width - KNOB_RADIUS * 2F;
        if (trackWidth <= 0) {
            return;
        }
        setting.setFraction((mouseX - trackX) / trackWidth);
    }

    /** Drops the decimal point for whole-number settings, which is most of them. */
    private String format(double value) {
        return setting.step() >= 1.0
                ? Integer.toString((int) Math.round(value))
                : String.format("%.2f", value);
    }
}
