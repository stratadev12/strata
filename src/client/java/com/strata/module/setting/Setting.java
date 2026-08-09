package com.strata.module.setting;

import java.util.function.BooleanSupplier;

/**
 * A single configurable value on a {@link com.strata.module.Module}.
 *
 * <p>Settings are the contract between modules and the GUI: a module declares what
 * it needs, and the ClickGUI reflects over the declared settings to build widgets.
 * Adding a new module therefore requires no UI code.
 *
 * <p>Persistence goes through {@link #serialize()} / {@link #deserialize(String)} so
 * that config files stay human-readable and tolerant of settings being added or
 * removed between versions.
 */
public abstract class Setting<T> {

    private final String name;
    private final String id;
    private final T defaultValue;
    private T value;

    /** Controls conditional visibility, e.g. hide a slider unless a toggle is on. */
    private BooleanSupplier visibleWhen = () -> true;
    private String description = "";

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.id = name.toLowerCase().replaceAll("[^a-z0-9]+", "_");
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String name() {
        return name;
    }

    /** Stable key used in the config file. Derived from the name, so renaming resets it. */
    public String id() {
        return id;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        this.value = newValue;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public void reset() {
        this.value = defaultValue;
    }

    public boolean visible() {
        return visibleWhen.getAsBoolean();
    }

    public Setting<T> visibleWhen(BooleanSupplier condition) {
        this.visibleWhen = condition;
        return this;
    }

    public String description() {
        return description;
    }

    public Setting<T> describe(String description) {
        this.description = description;
        return this;
    }

    public abstract String serialize();

    /** Must not throw on malformed input; fall back to the current value instead. */
    public abstract void deserialize(String raw);
}
