package com.strata.module.setting;

/** A dropdown backed by an enum constant. */
public class EnumSetting<E extends Enum<E>> extends Setting<E> {

    private final Class<E> type;

    public EnumSetting(String name, E defaultValue) {
        super(name, defaultValue);
        this.type = defaultValue.getDeclaringClass();
    }

    public E[] options() {
        return type.getEnumConstants();
    }

    public void cycle() {
        E[] values = options();
        set(values[(get().ordinal() + 1) % values.length]);
    }

    @Override
    public String serialize() {
        return get().name();
    }

    @Override
    public void deserialize(String raw) {
        try {
            set(Enum.valueOf(type, raw));
        } catch (IllegalArgumentException ignored) {
            // enum constant was renamed or removed; keep current value
        }
    }
}
