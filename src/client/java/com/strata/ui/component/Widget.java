package com.strata.ui.component;

import com.strata.ui.render.UiRenderer;

/**
 * Base for every interactive element.
 *
 * <p>Layout is assigned by the parent rather than computed by the widget, so a widget
 * never needs to know where it sits in the tree. Input handlers return true when they
 * consume the event, which is how the parent decides whether to keep dispatching.
 */
public abstract class Widget {

    protected float x;
    protected float y;
    protected float width;
    protected float height;

    private boolean visible = true;

    public void layout(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float height() {
        return height;
    }

    public boolean visible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public abstract void draw(UiRenderer ui, int mouseX, int mouseY, float delta);

    /**
     * Drawn after every sibling and outside the parent's clip rectangle, so popups
     * (drop-down lists, tooltips) can overlap widgets below them instead of being
     * clipped by the scroll region.
     */
    public void drawOverlay(UiRenderer ui, int mouseX, int mouseY, float delta) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    protected boolean isOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    /**
     * Eases a 0..1 animation value towards a target. Frame-rate independent enough for
     * UI, and keeps hover/toggle transitions from snapping.
     */
    protected static float approach(float current, float target, float delta) {
        float speed = Math.min(1F, delta * 0.35F);
        return current + (target - current) * speed;
    }
}
