package com.strata.module.setting;

public class BoolSetting extends Setting<Boolean> {

    public BoolSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public boolean value() {
        return get();
    }

    public void toggle() {
        set(!get());
    }

    @Override
    public String serialize() {
        return Boolean.toString(get());
    }

    @Override
    public void deserialize(String raw) {
        if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) {
            set(Boolean.parseBoolean(raw));
        }
    }
}
