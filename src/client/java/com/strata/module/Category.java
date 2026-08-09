package com.strata.module;

/** Top-level grouping. Each value is a row in the ClickGUI sidebar, in this order. */
public enum Category {
    COMBAT("Combat"),
    MINING("Mining"),
    FARMING("Farming"),
    RENDER("Render"),
    MISC("Misc"),
    FAILSAFE("Failsafe");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
