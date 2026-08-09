package com.strata.module.impl.dojo;

import com.strata.module.Category;
import com.strata.module.Module;
import com.strata.module.setting.BoolSetting;
import com.strata.module.setting.EnumSetting;
import com.strata.module.setting.NumberSetting;

/**
 * Crimson Isle Dojo macro.
 *
 * <p>Currently a skeleton: settings and registration only, no automation yet.
 *
 * <p>When the automation lands it must drive vanilla input paths (key and mouse state)
 * only. Strata never constructs or sends packets, so the traffic Hypixel sees is
 * identical to a human playing.
 */
public class DojoModule extends Module {

    public enum Test {
        FORCE,
        STAMINA,
        MASTERY,
        DISCIPLINE,
        SWIFTNESS,
        CONTROL
    }

    private final EnumSetting<Test> test =
            new EnumSetting<>("Test", Test.FORCE);

    private final NumberSetting reactionDelay =
            new NumberSetting("Reaction delay", 120, 0, 500, 10);

    private final NumberSetting delayJitter =
            new NumberSetting("Delay jitter", 40, 0, 200, 5);

    private final BoolSetting pauseOnPlayer =
            new BoolSetting("Pause when a player is near", true);

    private final BoolSetting showOverlay =
            new BoolSetting("Show status overlay", true);

    public DojoModule() {
        super("Dojo", Category.MISC);
        addSettings(test, reactionDelay, delayJitter, pauseOnPlayer, showOverlay);
        describe("Automates the Crimson Isle Dojo tests.");
    }

    @Override
    public void onTick() {
        // Automation not implemented yet.
    }
}
