package com.strata.ui.component;

import com.strata.module.Module;
import com.strata.module.setting.Setting;
import com.strata.ui.render.Corners;
import com.strata.ui.render.UiRenderer;
import com.strata.ui.theme.Theme;

import java.util.ArrayList;
import java.util.List;

/**
 * One module: a header row with an enable switch, and its settings underneath when
 * expanded.
 *
 * <p>The settings list is built from {@link Widgets#forSetting}, so a module that
 * declares new settings gets new controls without any change here.
 */
public class ModulePanel extends Widget {

    private static final float HEADER_HEIGHT = 26F;
    private static final float INNER_PADDING = 8F;

    private final Module module;
    private final List<Widget> settingWidgets = new ArrayList<>();

    private boolean expanded;
    private float enableProgress;

    public ModulePanel(Module module) {
        this.module = module;
        for (Setting<?> setting : module.settings()) {
            Widget widget = Widgets.forSetting(setting);
            if (widget != null) {
                settingWidgets.add(widget);
            }
        }
        this.enableProgress = module.isEnabled() ? 1F : 0F;
    }

    /** Recomputes child positions and this panel's own height. */
    @Override
    public void layout(float x, float y, float width, float height) {
        super.layout(x, y, width, height);

        float cursor = y + HEADER_HEIGHT;
        if (expanded) {
            cursor += INNER_PADDING / 2F;
            for (Widget widget : settingWidgets) {
                widget.layout(x + INNER_PADDING, cursor, width - INNER_PADDING * 2F, widget.height());
                cursor += widget.height();
            }
            cursor += INNER_PADDING / 2F;
        }
        this.height = cursor - y;
    }

    @Override
    public void draw(UiRenderer ui, int mouseX, int mouseY, float delta) {
        enableProgress = approach(enableProgress, module.isEnabled() ? 1F : 0F, delta);

        boolean headerHovered = mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + HEADER_HEIGHT;

        int background = expanded ? Theme.CARD_HOVER : (headerHovered ? Theme.CARD_HOVER : Theme.CARD);
        ui.rect(x, y, width, height, Theme.RADIUS_CARD, background);

        // Accent bar on the left edge, fading in with the enabled state.
        if (enableProgress > 0.01F) {
            int accent = ToggleWidget.blend(Theme.CARD, Theme.ACCENT, enableProgress);
            ui.rect(x, y + 5F, 3F, HEADER_HEIGHT - 10F, Corners.right(2), accent);
        }

        ui.text(module.name(), x + INNER_PADDING + 4F, y + (HEADER_HEIGHT - 12F) / 2F, 13F,
                module.isEnabled() ? Theme.TEXT : Theme.TEXT_MUTED);

        drawSwitch(ui, x + width - 34F, y + (HEADER_HEIGHT - 15F) / 2F);

        if (expanded) {
            for (Widget widget : settingWidgets) {
                widget.draw(ui, mouseX, mouseY, delta);
            }
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Children first: an expanded slider overlaps nothing, but it must win over
        // the panel's own expand/collapse behaviour.
        if (expanded) {
            for (Widget widget : settingWidgets) {
                if (widget.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        boolean onHeader = mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
        if (!onHeader) {
            return false;
        }

        // The switch occupies the right edge; the rest of the header expands.
        if (mouseX >= x + width - 38F) {
            if (button == 0) {
                module.toggle();
                return true;
            }
            return false;
        }

        if (button == 0 && !settingWidgets.isEmpty()) {
            expanded = !expanded;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = false;
        for (Widget widget : settingWidgets) {
            handled |= widget.mouseReleased(mouseX, mouseY, button);
        }
        return handled;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        boolean handled = false;
        for (Widget widget : settingWidgets) {
            handled |= widget.mouseDragged(mouseX, mouseY, button);
        }
        return handled;
    }
}
