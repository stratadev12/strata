package com.strata.module;

import com.strata.StrataClient;
import com.strata.module.setting.Setting;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Owns every {@link Module}: registration, event dispatch and config persistence.
 *
 * <p>Config is a flat {@code moduleId.key=value} properties file. That format is
 * deliberately dumb: unknown keys are ignored and missing keys keep their defaults,
 * so adding or removing settings between versions never corrupts a user's config.
 */
public class ModuleManager {

    private final Map<String, Module> modules = new LinkedHashMap<>();
    private final Path configPath;

    public ModuleManager(Path configPath) {
        this.configPath = configPath;
    }

    public void register(Module module) {
        Module previous = modules.put(module.id(), module);
        if (previous != null) {
            throw new IllegalStateException("Duplicate module id: " + module.id());
        }
    }

    public Module byId(String id) {
        return modules.get(id);
    }

    public List<Module> all() {
        return new ArrayList<>(modules.values());
    }

    public List<Module> byCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules.values()) {
            if (module.category() == category) {
                result.add(module);
            }
        }
        return result;
    }

    // ---- dispatch ----

    /**
     * One module throwing must not stop the others or crash the client, so each
     * tick is isolated and a failure disables the offending module.
     */
    public void onTick() {
        for (Module module : modules.values()) {
            if (!module.isEnabled()) {
                continue;
            }
            try {
                module.onTick();
            } catch (Throwable t) {
                StrataClient.LOGGER.error("Module '{}' threw during tick; disabling it.", module.id(), t);
                try {
                    module.setEnabled(false);
                } catch (Throwable ignored) {
                    module.setEnabledSilently(false);
                }
            }
        }
    }

    /** @return true if some module claimed the key. */
    public boolean onKeyPress(int keyCode) {
        if (keyCode == Module.KEY_UNBOUND) {
            return false;
        }
        boolean handled = false;
        for (Module module : modules.values()) {
            if (module.keyCode() == keyCode) {
                module.toggle();
                handled = true;
            }
        }
        return handled;
    }

    // ---- persistence ----

    public void save() {
        Properties props = new Properties();
        for (Module module : modules.values()) {
            String prefix = module.id() + ".";
            props.setProperty(prefix + "enabled", Boolean.toString(module.isEnabled()));
            props.setProperty(prefix + "keybind", Integer.toString(module.keyCode()));
            for (Setting<?> setting : module.settings()) {
                props.setProperty(prefix + setting.id(), setting.serialize());
            }
        }
        try {
            Path parent = configPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                props.store(writer, "strata config");
            }
        } catch (IOException e) {
            StrataClient.LOGGER.error("Failed to save config to {}", configPath, e);
        }
    }

    public void load() {
        if (!Files.exists(configPath)) {
            return;
        }
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(configPath)) {
            props.load(reader);
        } catch (IOException e) {
            StrataClient.LOGGER.error("Failed to read config from {}; using defaults.", configPath, e);
            return;
        }

        for (Module module : modules.values()) {
            String prefix = module.id() + ".";

            String enabled = props.getProperty(prefix + "enabled");
            if (enabled != null) {
                // Silent: the world is not loaded yet, so onEnable() must not run here.
                module.setEnabledSilently(Boolean.parseBoolean(enabled));
            }

            String keybind = props.getProperty(prefix + "keybind");
            if (keybind != null) {
                try {
                    module.setKeyCode(Integer.parseInt(keybind.trim()));
                } catch (NumberFormatException ignored) {
                    // leave unbound
                }
            }

            for (Setting<?> setting : module.settings()) {
                String raw = props.getProperty(prefix + setting.id());
                if (raw != null) {
                    setting.deserialize(raw);
                }
            }
        }
    }
}
