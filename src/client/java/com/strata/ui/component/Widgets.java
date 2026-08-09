package com.strata.ui.component;

import com.strata.module.setting.BoolSetting;
import com.strata.module.setting.EnumSetting;
import com.strata.module.setting.NumberSetting;
import com.strata.module.setting.Setting;

/**
 * Maps a {@link Setting} to the widget that edits it.
 *
 * <p>This is the join that makes the module system pay off: a new module declares
 * settings and the GUI builds itself. Adding a new setting type means adding one case
 * here and nothing else.
 */
public final class Widgets {

    private Widgets() {
    }

    /** @return null when the setting type has no widget yet, so callers can skip it. */
    public static Widget forSetting(Setting<?> setting) {
        if (setting instanceof BoolSetting bool) {
            return new ToggleWidget(bool);
        }
        if (setting instanceof NumberSetting number) {
            return new SliderWidget(number);
        }
        if (setting instanceof EnumSetting<?> enumSetting) {
            return new DropdownWidget(enumSetting);
        }
        return null;
    }
}
