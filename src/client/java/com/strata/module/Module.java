package com.strata.module;

import com.strata.module.setting.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for every feature in the mod.
 *
 * <p>A subclass declares its settings in the constructor and overrides whichever
 * lifecycle hooks it needs. Registration, config persistence, keybinding and GUI
 * rendering are all handled generically by {@link ModuleManager} and the ClickGUI,
 * so a new module never touches UI or config code.
 */
public abstract class Module {

    /** Unbound keybind sentinel. Matches GLFW_KEY_UNKNOWN. */
    public static final int KEY_UNBOUND = -1;

    private final String name;
    private final String id;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();

    private boolean enabled;
    private int keyCode = KEY_UNBOUND;
    private String description = "";

    protected Module(String name, Category category, Setting<?>... settings) {
        this.name = name;
        this.id = name.toLowerCase().replaceAll("[^a-z0-9]+", "_");
        this.category = category;
        Collections.addAll(this.settings, settings);
    }

    public String name() {
        return name;
    }

    /** Stable key used in the config file and for lookups. */
    public String id() {
        return id;
    }

    public Category category() {
        return category;
    }

    public List<Setting<?>> settings() {
        return Collections.unmodifiableList(settings);
    }

    protected void addSettings(Setting<?>... extra) {
        Collections.addAll(this.settings, extra);
    }

    public String description() {
        return description;
    }

    protected Module describe(String description) {
        this.description = description;
        return this;
    }

    public int keyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    /**
     * Idempotent: re-setting the current state does nothing, so callers do not need
     * to guard against double-enable.
     */
    public void setEnabled(boolean value) {
        if (this.enabled == value) {
            return;
        }
        this.enabled = value;
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /**
     * Force the flag without firing hooks. Used during config load, where modules
     * must not act before the world exists.
     */
    void setEnabledSilently(boolean value) {
        this.enabled = value;
    }

    // ---- lifecycle hooks, all optional ----

    public void onEnable() {
    }

    public void onDisable() {
    }

    /** Called once per client tick while enabled. */
    public void onTick() {
    }
}
