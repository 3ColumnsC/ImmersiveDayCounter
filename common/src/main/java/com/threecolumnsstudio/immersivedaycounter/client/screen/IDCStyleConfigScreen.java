package com.threecolumnsstudio.immersivedaycounter.client.screen;

import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class IDCStyleConfigScreen extends Screen {

    private static final int WIDGET_WIDTH = 300;
    private static final int WIDGET_HEIGHT = 20;
    private static final int ROW_SPACING = 6;
    private static final int INITIAL_SCROLL_HEIGHT = 120;
    private static final int BUTTON_WIDTH = 100;
    private static final int COLOR_ROW_SPACING = 6;

    public enum StyleTarget {
        CENTER("idc.config.display_center"),
        HOTBAR("idc.config.display_hotbar"),
        CORNER("idc.config.display_corner");

        private final String titleKey;

        StyleTarget(String titleKey) {
            this.titleKey = titleKey;
        }

        String titleKey() {
            return this.titleKey;
        }
    }

    private final Screen parent;
    private final StyleTarget target;
    private final HeaderAndFooterLayout layout;
    private final TabManager tabManager;

    private TabNavigationBar tabNavigationBar;
    private ScrollableLayout scrollArea;

    private boolean enabled;
    private boolean permanent;
    private double durationSeconds;
    private double fadeSeconds;
    private boolean background;
    private String textColor;
    private boolean specialDayText;
    private String specialDayTextColor;
    private IDCConfig.DisplayContent content;
    private IDCConfig.DayTimeFormat dayTimeFormat;
    private IDCConfig.Position position;

    public IDCStyleConfigScreen(Screen parent, StyleTarget target) {
        super(Component.translatable(target.titleKey()));
        this.parent = parent;
        this.target = target;
        this.layout = new HeaderAndFooterLayout(this);
        this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
        readConfig();
    }

    private IDCConfig.Style style() {
        return switch (this.target) {
            case CENTER -> IDCConfig.get().display.center;
            case HOTBAR -> IDCConfig.get().display.hotbar;
            case CORNER -> IDCConfig.get().display.corner;
        };
    }

    private IDCConfig.CornerStyle cornerStyle() {
        return this.target == StyleTarget.CORNER ? IDCConfig.get().display.corner : null;
    }

    private void readConfig() {
        IDCConfig.Style style = style();
        this.enabled = style.enabled;
        this.permanent = style.permanent;
        this.durationSeconds = style.durationSeconds;
        this.fadeSeconds = style.fadeSeconds;
        this.background = Boolean.TRUE.equals(style.background);
        this.textColor = style.textColor;
        this.specialDayText = style.specialDayText;
        this.specialDayTextColor = style.specialDayTextColor;
        this.content = style.content;
        this.dayTimeFormat = style.dayTimeFormat;
        IDCConfig.CornerStyle corner = cornerStyle();
        if (corner != null) {
            this.position = corner.position;
        }
    }

    @Override
    protected void init() {
        this.scrollArea = new ScrollableLayout(this.minecraft, buildContentGrid(), INITIAL_SCROLL_HEIGHT);

        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width)
            .addTabs(new ScrollConfigTab(Component.translatable(this.target.titleKey()), this.scrollArea))
            .build();
        this.addRenderableWidget(this.tabNavigationBar);

        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(Component.translatable("idc.config.apply"), button -> apply())
            .width(BUTTON_WIDTH)
            .build());
        footer.addChild(Button.builder(CommonComponents.GUI_BACK, button -> onClose())
            .width(BUTTON_WIDTH)
            .build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.tabNavigationBar.selectTab(0, false);
        this.repositionElements();
    }

    private GridLayout buildContentGrid() {
        GridLayout grid = new GridLayout();
        grid.columnSpacing(4).rowSpacing(ROW_SPACING);

        GridLayout.RowHelper rows = grid.createRowHelper(1);
        rows.addChild(toggle("idc.config.enabled", this.enabled, value -> this.enabled = value));
        rows.addChild(contentButton());
        rows.addChild(toggle("idc.config.permanent", this.permanent, value -> this.permanent = value));
        rows.addChild(new DoubleSlider(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("idc.config.duration"),
            0.5D, 15.0D, 0.5D, this.durationSeconds, value -> this.durationSeconds = value));
        rows.addChild(new DoubleSlider(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("idc.config.fade"),
            0.0D, 5.0D, 0.1D, this.fadeSeconds, value -> this.fadeSeconds = value));
        rows.addChild(toggle("idc.config.background", this.background, value -> this.background = value));
        rows.addChild(colorRow("idc.config.text_color", this.textColor, value -> this.textColor = value));
        rows.addChild(toggle("idc.config.special_day_text", this.specialDayText, value -> this.specialDayText = value));
        rows.addChild(colorRow("idc.config.special_day_text_color", this.specialDayTextColor,
            value -> this.specialDayTextColor = value));
        rows.addChild(dayTimeFormatButton());
        if (this.target == StyleTarget.CORNER) {
            rows.addChild(positionButton());
        }

        return grid;
    }

    private CycleButton<Boolean> toggle(String translationKey, boolean initialValue, Consumer<Boolean> setter) {
        return CycleButton.onOffBuilder(initialValue)
            .create(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable(translationKey),
                (button, value) -> setter.accept(value));
    }

    private LinearLayout colorRow(String translationKey, String initialValue, Consumer<String> setter) {
        int labelWidth = WIDGET_WIDTH - ColorField.WIDTH - COLOR_ROW_SPACING;
        LinearLayout row = LinearLayout.horizontal().spacing(COLOR_ROW_SPACING);
        row.addChild(new StringWidget(labelWidth, WIDGET_HEIGHT, Component.translatable(translationKey), this.font));
        ColorField field = new ColorField(this.font, Component.translatable(translationKey), initialValue);
        field.setResponder(setter::accept);
        row.addChild(field);
        return row;
    }

    private CycleButton<IDCConfig.Position> positionButton() {
        return CycleButton.builder(
                position -> Component.translatable("idc.config.position." + position.name().toLowerCase(Locale.ROOT)),
                this.position)
            .withValues(IDCConfig.Position.values())
            .create(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("idc.config.position"),
                (button, value) -> this.position = value);
    }

    private CycleButton<IDCConfig.DisplayContent> contentButton() {
        return CycleButton.builder(
                content -> Component.translatable("idc.config.content." + content.name().toLowerCase(Locale.ROOT)),
                this.content)
            .withValues(IDCConfig.DisplayContent.values())
            .create(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("idc.config.content"),
                (button, value) -> this.content = value);
    }

    private CycleButton<IDCConfig.DayTimeFormat> dayTimeFormatButton() {
        return CycleButton.builder(
                format -> Component.translatable("idc.config.day_time_format." + format.name().toLowerCase(Locale.ROOT)),
                this.dayTimeFormat)
            .withValues(IDCConfig.DayTimeFormat.values())
            .create(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("idc.config.day_time_format"),
                (button, value) -> this.dayTimeFormat = value);
    }

    private void apply() {
        IDCConfig.Style style = style();
        style.enabled = this.enabled;
        style.permanent = this.permanent;
        style.durationSeconds = this.durationSeconds;
        style.fadeSeconds = this.fadeSeconds;
        style.background = this.background;
        style.textColor = this.textColor;
        style.specialDayText = this.specialDayText;
        style.specialDayTextColor = this.specialDayTextColor;
        style.content = this.content;
        style.dayTimeFormat = this.dayTimeFormat;
        IDCConfig.CornerStyle corner = cornerStyle();
        if (corner != null && this.position != null) {
            corner.position = this.position;
        }
        IDCConfig.save();
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar == null) {
            return;
        }
        this.tabNavigationBar.setWidth(this.width);
        this.tabNavigationBar.arrangeElements();
        int headerBottom = this.tabNavigationBar.getRectangle().bottom();
        ScreenRectangle tabArea = new ScreenRectangle(0, headerBottom, this.width,
            this.height - this.layout.getFooterHeight() - headerBottom);
        this.tabManager.setTabArea(tabArea);
        this.layout.setHeaderHeight(headerBottom);
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void renderMenuBackground(GuiGraphics graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, CreateWorldScreen.TAB_HEADER_BACKGROUND,
            0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderMenuBackground(graphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR,
            0, this.height - this.layout.getFooterHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
    }
}
