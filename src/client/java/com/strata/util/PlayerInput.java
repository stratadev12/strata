package com.strata.util;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import java.util.HashSet;
import java.util.Set;

/**
 * Drives the player through vanilla's own key state.
 *
 * <p>Setting {@link KeyMapping#setDown} makes Minecraft's normal tick logic produce the
 * movement and attacks, so the packets the server receives are identical to a human
 * playing. Nothing here constructs or sends a packet, which is a hard requirement:
 * anything Hypixel would not see from a vanilla client is a ban risk.
 *
 * <p>Every key pressed is tracked so {@link #releaseAll()} can guarantee cleanup. A
 * macro that dies mid-tick while holding W would otherwise walk the player into a
 * wall indefinitely.
 */
public final class PlayerInput {

    private static final Set<KeyMapping> held = new HashSet<>();

    private PlayerInput() {
    }

    private static Options options() {
        Minecraft client = Minecraft.getInstance();
        return client == null ? null : client.options;
    }

    public static void set(KeyMapping key, boolean down) {
        if (key == null) {
            return;
        }
        key.setDown(down);
        if (down) {
            held.add(key);
        } else {
            held.remove(key);
        }
    }

    /** Releases every key this class pressed. Safe to call repeatedly. */
    public static void releaseAll() {
        for (KeyMapping key : held) {
            key.setDown(false);
        }
        held.clear();
    }

    public static boolean anyHeld() {
        return !held.isEmpty();
    }

    // ---- named helpers, so callers never touch Options directly ----

    public static void forward(boolean down) {
        Options o = options();
        if (o != null) {
            set(o.keyUp, down);
        }
    }

    public static void back(boolean down) {
        Options o = options();
        if (o != null) {
            set(o.keyDown, down);
        }
    }

    public static void left(boolean down) {
        Options o = options();
        if (o != null) {
            set(o.keyLeft, down);
        }
    }

    public static void right(boolean down) {
        Options o = options();
        if (o != null) {
            set(o.keyRight, down);
        }
    }

    public static void jump(boolean down) {
        Options o = options();
        if (o != null) {
            set(o.keyJump, down);
        }
    }

    public static void sneak(boolean down) {
        Options o = options();
        if (o != null) {
            set(o.keyShift, down);
        }
    }

    public static void sprint(boolean down) {
        Options o = options();
        if (o != null) {
            set(o.keySprint, down);
        }
    }

    public static void attack(boolean down) {
        Options o = options();
        if (o != null) {
            set(o.keyAttack, down);
        }
    }

    public static void use(boolean down) {
        Options o = options();
        if (o != null) {
            set(o.keyUse, down);
        }
    }

    /** Stops all movement but leaves the player's look direction alone. */
    public static void stopMoving() {
        forward(false);
        back(false);
        left(false);
        right(false);
        jump(false);
        sprint(false);
    }
}
