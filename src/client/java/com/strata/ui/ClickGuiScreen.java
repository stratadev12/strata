package com.strata.ui;

import com.strata.StrataClient;
import com.strata.module.Category;
import com.strata.module.Module;
import com.strata.module.setting.Setting;
import com.strata.ui.component.DropdownWidget;
import com.strata.ui.component.ModuleCard;
import com.strata.ui.component.Widget;
import com.strata.ui.component.Widgets;
import com.strata.ui.render.Corners;
import com.strata.ui.render.UiRenderer;
import com.strata.ui.render.nanovg.NanoVgDrawable;
import com.strata.ui.theme.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The ClickGUI: sidebar of categories, grid of module tiles, and a settings view.
 *
 * <p>Still a real {@link Screen} so vanilla owns input and lifecycle; only painting is
 * diverted to NanoVG. All contents derive from the registered modules and their
 * settings, so adding a module changes nothing in this file.
 */
public class ClickGuiScreen extends Screen implements NanoVgDrawable {

    /** Target window size in GUI units; clamped down on small screens. */
    private static final float PANEL_WIDTH = 560F;
    private static final float PANEL_HEIGHT = 330F;
    private static final float MIN_MARGIN = 12F;

    private static final float HEADER_HEIGHT = 30F;
    private static final float SIDEBAR_WIDTH = 96F;
    private static final float PAD = 10F;
    private static final float GRID_GAP = 8F;
    /** Column count is derived from this, so the grid reflows as the panel resizes. */
    private static final float MIN_CARD_WIDTH = 128F;
    private static final float CARD_HEIGHT = 74F;
    private static final float SIDEBAR_ROW = 22F;

    private final List<ModuleCard> cards = new ArrayList<>();

    private Category active = Category.COMBAT;
    private String search = "";
    private boolean searchFocused;

    /** Non-null when showing a single module's settings instead of the grid. */
    private Module detail;
    private final List<Widget> detailWidgets = new ArrayList<>();

    private float scroll;
    private float maxScroll;

    // Recomputed each frame; input handlers read these so hit-testing matches drawing.
    private float px, py, pw, ph;
    private float contentX, contentY, contentW, contentH;

    public ClickGuiScreen() {
        super(Component.literal("Strata"));
        for (Module module : StrataClient.modules().all()) {
            cards.add(new ModuleCard(module));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Vanilla blurs and dims behind screens; we want the world sharp. */
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    private List<ModuleCard> visibleCards() {
        String query = search.trim().toLowerCase(Locale.ROOT);
        List<ModuleCard> result = new ArrayList<>();
        for (ModuleCard card : cards) {
            Module module = card.module();
            boolean matchesCategory = query.isEmpty() && module.category() == active;
            boolean matchesSearch = !query.isEmpty()
                    && (module.name().toLowerCase(Locale.ROOT).contains(query)
                    || module.description().toLowerCase(Locale.ROOT).contains(query));
            if (matchesCategory || matchesSearch) {
                result.add(card);
            }
        }
        return result;
    }

    // ---- drawing ----

    @Override
    public void drawNanoVg(UiRenderer ui, int mouseX, int mouseY, float delta) {
        pw = Math.min(PANEL_WIDTH, this.width - MIN_MARGIN * 2);
        ph = Math.min(PANEL_HEIGHT, this.height - MIN_MARGIN * 2);
        px = (this.width - pw) / 2F;
        py = (this.height - ph) / 2F;

        ui.rect(px, py, pw, ph, Theme.RADIUS_PANEL, Theme.PANEL);

        drawHeader(ui, mouseX, mouseY);
        drawSidebar(ui, mouseX, mouseY);

        contentX = px + SIDEBAR_WIDTH + PAD;
        contentY = py + HEADER_HEIGHT + PAD;
        contentW = pw - SIDEBAR_WIDTH - PAD * 2;
        contentH = ph - HEADER_HEIGHT - PAD * 2;

        if (detail != null) {
            drawDetail(ui, mouseX, mouseY, delta);
        } else {
            drawGrid(ui, mouseX, mouseY, delta);
        }
    }

    private void drawHeader(UiRenderer ui, int mouseX, int mouseY) {
        ui.rect(px, py, pw, HEADER_HEIGHT, Corners.top(Theme.RADIUS_PANEL), Theme.PANEL_HEADER);

        // Logo is 598x398, so hold its 1.5:1 aspect rather than stretching it.
        float logoHeight = HEADER_HEIGHT - 12F;
        float logoWidth = logoHeight * (598F / 398F);
        ui.image("logo", px + PAD, py + 6F, logoWidth, logoHeight);
        ui.text("Strata", px + PAD + logoWidth + 8F, py + (HEADER_HEIGHT - 13F) / 2F, 15F, Theme.TEXT);

        // Search field, right-aligned with room for the close button.
        float searchW = Math.min(260F, pw * 0.35F);
        float searchX = px + pw - searchW - 40F;
        float searchY = py + 7F;
        float searchH = HEADER_HEIGHT - 14F;

        boolean hovered = mouseX >= searchX && mouseX <= searchX + searchW
                && mouseY >= searchY && mouseY <= searchY + searchH;
        ui.rect(searchX, searchY, searchW, searchH, Theme.RADIUS_PILL,
                searchFocused ? Theme.CARD_ACTIVE : (hovered ? Theme.CARD_HOVER : Theme.CARD));

        String shown = search.isEmpty() && !searchFocused ? "Search modules…" : search;
        int colour = search.isEmpty() && !searchFocused ? Theme.TEXT_FAINT : Theme.TEXT;
        ui.clip(searchX + 8F, searchY, searchW - 16F, searchH);
        ui.text(shown, searchX + 10F, searchY + (searchH - 10F) / 2F, 11F, colour);
        if (searchFocused) {
            float caretX = searchX + 10F + ui.textWidth(search, 11F) + 1F;
            ui.rect(caretX, searchY + 4F, 1F, searchH - 8F, 0, Theme.ACCENT);
        }
        ui.clearClip();

        // Close button, drawn as two strokes rather than a glyph.
        float closeCx = px + pw - 20F;
        float closeCy = py + HEADER_HEIGHT / 2F;
        float arm = 4.5F;
        boolean closeHovered = mouseX >= closeCx - 10F && mouseX <= closeCx + 10F
                && mouseY >= closeCy - 10F && mouseY <= closeCy + 10F;
        int closeColour = closeHovered ? Theme.TEXT : Theme.TEXT_MUTED;
        ui.line(closeCx - arm, closeCy - arm, closeCx + arm, closeCy + arm, 1.6F, closeColour);
        ui.line(closeCx + arm, closeCy - arm, closeCx - arm, closeCy + arm, 1.6F, closeColour);
    }

    private void drawSidebar(UiRenderer ui, int mouseX, int mouseY) {
        float x = px + 6F;
        float y = py + HEADER_HEIGHT + 8F;
        float w = SIDEBAR_WIDTH - 12F;

        for (Category category : Category.values()) {
            boolean isActive = detail == null && category == active && search.isEmpty();
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + SIDEBAR_ROW;

            if (isActive) {
                ui.rect(x, y, w, SIDEBAR_ROW, Theme.RADIUS_CARD, Theme.ACCENT_WASH);
                ui.rect(x, y + 6F, 3F, SIDEBAR_ROW - 12F, Corners.right(2), Theme.ACCENT);
            } else if (hovered) {
                ui.rect(x, y, w, SIDEBAR_ROW, Theme.RADIUS_CARD, Theme.CARD);
            }

            ui.text(category.displayName(), x + 12F, y + (SIDEBAR_ROW - 11F) / 2F, 12F,
                    isActive ? Theme.ACCENT : (hovered ? Theme.TEXT : Theme.TEXT_MUTED));
            y += SIDEBAR_ROW + 2F;
        }

        // Pinned to the bottom of the sidebar.
        float customizeY = py + ph - SIDEBAR_ROW - 8F;
        boolean customizeHovered = mouseX >= x && mouseX <= x + w
                && mouseY >= customizeY && mouseY <= customizeY + SIDEBAR_ROW;
        ui.rect(x, customizeY, w, SIDEBAR_ROW, Theme.RADIUS_CARD,
                customizeHovered ? Theme.CARD_HOVER : Theme.CARD);
        ui.text("Customize", x + 12F, customizeY + (SIDEBAR_ROW - 11F) / 2F, 12F,
                customizeHovered ? Theme.TEXT : Theme.TEXT_MUTED);

        // Divider between sidebar and content.
        ui.rect(px + SIDEBAR_WIDTH, py + HEADER_HEIGHT, 1F, ph - HEADER_HEIGHT, 0, Theme.OUTLINE);
    }

    private void drawGrid(UiRenderer ui, int mouseX, int mouseY, float delta) {
        List<ModuleCard> visible = visibleCards();
        if (visible.isEmpty()) {
            ui.text(search.isEmpty() ? "No modules in this category" : "No modules match “" + search + "”",
                    contentX, contentY + 4F, 12F, Theme.TEXT_FAINT);
            maxScroll = 0F;
            return;
        }

        // Reflow rather than fixing a column count, so the grid stays sane at any
        // panel size or GUI scale.
        int columns = Math.max(1, (int) ((contentW + GRID_GAP) / (MIN_CARD_WIDTH + GRID_GAP)));
        float cardWidth = (contentW - GRID_GAP * (columns - 1)) / columns;
        float cursorY = contentY - scroll;

        for (int i = 0; i < visible.size(); i++) {
            int column = i % columns;
            float cardX = contentX + column * (cardWidth + GRID_GAP);
            float cardY = cursorY + (i / columns) * (CARD_HEIGHT + GRID_GAP);
            visible.get(i).layout(cardX, cardY, cardWidth, CARD_HEIGHT);
        }

        int rows = (visible.size() + columns - 1) / columns;
        float totalHeight = rows * (CARD_HEIGHT + GRID_GAP) - GRID_GAP;
        maxScroll = Math.max(0F, totalHeight - contentH);
        scroll = Math.min(scroll, maxScroll);

        ui.clip(contentX, contentY, contentW + 6F, contentH);
        for (ModuleCard card : visible) {
            card.draw(ui, mouseX, mouseY, delta);
        }
        ui.clearClip();
    }

    private void drawDetail(UiRenderer ui, int mouseX, int mouseY, float delta) {
        boolean backHovered = mouseX >= contentX && mouseX <= contentX + 60F
                && mouseY >= contentY && mouseY <= contentY + 18F;
        ui.text("← Back", contentX, contentY + 3F, 12F, backHovered ? Theme.TEXT : Theme.TEXT_MUTED);
        ui.text(detail.name(), contentX + 80F, contentY, 15F, Theme.TEXT);

        float y = contentY + 30F;
        ui.clip(contentX, y, contentW + 6F, contentH - 30F);
        float cursor = y - scroll;
        for (Widget widget : detailWidgets) {
            widget.layout(contentX, cursor, contentW - 8F, widget.height());
            widget.draw(ui, mouseX, mouseY, delta);
            cursor += widget.height() + 2F;
        }
        ui.clearClip();

        // Popups draw last and unclipped, so an open drop-down list is not cut off by
        // the scroll region or hidden behind the widgets below it.
        for (Widget widget : detailWidgets) {
            widget.drawOverlay(ui, mouseX, mouseY, delta);
        }

        float total = (cursor + scroll) - y;
        maxScroll = Math.max(0F, total - (contentH - 30F));
    }

    // ---- input ----

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();

        // Close button.
        float closeCx = px + pw - 20F;
        float closeCy = py + HEADER_HEIGHT / 2F;
        if (mx >= closeCx - 10F && mx <= closeCx + 10F && my >= closeCy - 10F && my <= closeCy + 10F) {
            onClose();
            return true;
        }

        // Search field.
        float searchW = Math.min(260F, pw * 0.35F);
        float searchX = px + pw - searchW - 40F;
        searchFocused = mx >= searchX && mx <= searchX + searchW
                && my >= py + 7F && my <= py + HEADER_HEIGHT - 7F;
        if (searchFocused) {
            return true;
        }

        if (detail != null) {
            if (mx >= contentX && mx <= contentX + 60F && my >= contentY && my <= contentY + 18F) {
                detail = null;
                detailWidgets.clear();
                scroll = 0F;
                return true;
            }
            // An open drop-down overlaps the widgets beneath it, so it must be offered
            // the click before anything it is covering.
            for (Widget widget : detailWidgets) {
                if (widget instanceof DropdownWidget dropdown && dropdown.isOpen()
                        && dropdown.mouseClicked(mx, my, event.button())) {
                    return true;
                }
            }
            for (Widget widget : detailWidgets) {
                if (widget.mouseClicked(mx, my, event.button())) {
                    return true;
                }
            }
            return true;
        }

        if (handleSidebarClick(mx, my)) {
            return true;
        }

        for (ModuleCard card : visibleCards()) {
            if (card.mouseClicked(mx, my, event.button())) {
                // The card toggles itself when the switch was hit; otherwise open it.
                if (!card.isOnSwitch(mx, my) && !card.module().settings().isEmpty()) {
                    openDetail(card.module());
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void openDetail(Module module) {
        detail = module;
        detailWidgets.clear();
        for (Setting<?> setting : module.settings()) {
            Widget widget = Widgets.forSetting(setting);
            if (widget != null) {
                detailWidgets.add(widget);
            }
        }
        scroll = 0F;
    }

    private boolean handleSidebarClick(double mx, double my) {
        float x = px + 6F;
        float w = SIDEBAR_WIDTH - 12F;
        if (mx < x || mx > x + w) {
            return false;
        }

        float y = py + HEADER_HEIGHT + 8F;
        for (Category category : Category.values()) {
            if (my >= y && my <= y + SIDEBAR_ROW) {
                active = category;
                search = "";
                searchFocused = false;
                scroll = 0F;
                return true;
            }
            y += SIDEBAR_ROW + 2F;
        }

        float customizeY = py + ph - SIDEBAR_ROW - 8F;
        if (my >= customizeY && my <= customizeY + SIDEBAR_ROW) {
            // Reserved for HUD layout editing.
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (Widget widget : detailWidgets) {
            widget.mouseReleased(event.x(), event.y(), event.button());
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        for (Widget widget : detailWidgets) {
            if (widget.mouseDragged(event.x(), event.y(), event.button())) {
                return true;
            }
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0F) {
            scroll = Math.max(0F, Math.min(maxScroll, scroll - (float) scrollY * 20F));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!searchFocused) {
            return super.charTyped(event);
        }
        search += (char) event.codepoint();
        scroll = 0F;
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searchFocused) {
            if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
                if (!search.isEmpty()) {
                    search = search.substring(0, search.length() - 1);
                }
                return true;
            }
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                search = "";
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        StrataClient.modules().save();
        super.onClose();
    }
}
