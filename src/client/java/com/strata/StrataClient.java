package com.strata;

import com.strata.module.ModuleManager;
import com.strata.ui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrataClient implements ClientModInitializer {

    public static final String MOD_ID = "strata";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ModuleManager moduleManager;

    /** Edge-detection state so holding the key does not reopen the screen every tick. */
    private boolean guiKeyWasDown;

    public static ModuleManager modules() {
        return moduleManager;
    }

    @Override
    public void onInitializeClient() {
        moduleManager = new ModuleManager(
                FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".properties"));

        moduleManager.register(new com.strata.module.impl.dojo.DojoModule());

        // Load after registration: config is keyed by module id.
        moduleManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);

        LOGGER.info("Strata initialised ({} modules)", moduleManager.all().size());
    }

    private void onEndTick(Minecraft client) {
        pollGuiKey(client);
        moduleManager.onTick();
    }

    /**
     * Polled via GLFW rather than a registered KeyMapping: this is a temporary
     * binding for the prototype, and polling avoids depending on the keybinding
     * module before the GUI settles.
     */
    private void pollGuiKey(Minecraft client) {
        if (client.getWindow() == null) {
            return;
        }
        boolean down = GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (down && !guiKeyWasDown && client.screen == null) {
            client.setScreen(new ClickGuiScreen());
        }
        guiKeyWasDown = down;
    }
}
