package com.strata.module.setting;

/** A double-backed slider. {@code step} drives both snapping and label precision. */
public class NumberSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, double defaultValue, double min, double max) {
        this(name, defaultValue, min, max, 1.0);
    }

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public double step() {
        return step;
    }

    public double value() {
        return get();
    }

    public int intValue() {
        return (int) Math.round(get());
    }

    @Override
    public void set(Double newValue) {
        double clamped = Math.max(min, Math.min(max, newValue));
        double snapped = min + Math.round((clamped - min) / step) * step;
        super.set(Math.max(min, Math.min(max, snapped)));
    }

    /** 0..1 position of the handle, for the slider widget. */
    public double fraction() {
        return max - min == 0 ? 0 : (get() - min) / (max - min);
    }

    public void setFraction(double fraction) {
        set(min + fraction * (max - min));
    }

    @Override
    public String serialize() {
        return Double.toString(get());
    }

    @Override
    public void deserialize(String raw) {
        try {
            set(Double.parseDouble(raw));
        } catch (NumberFormatException ignored) {
            // keep current value
        }
    }
}
