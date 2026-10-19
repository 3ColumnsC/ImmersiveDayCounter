package com.threecolumnsstudio.immersivedaycounter.client.screen;

import com.threecolumnsstudio.immersivedaycounter.client.IDCSounds;
import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class IDCConfigScreen extends Screen {

    private static final int WIDGET_WIDTH = 300;
    private static final int WIDGET_HEIGHT = 20;
    private static final int ROW_SPACING = 6;
    private static final int INITIAL_SCROLL_HEIGHT = 120;
    private static final int BUTTON_WIDTH = 100;

    private final Screen parent;
    private final HeaderAndFooterLayout layout;
    private final TabManager tabManager;

    private MenuTabBar tabNavigationBar;
    private ScrollableLayout scrollArea;

    private boolean soundsEnabled;
    private IDCConfig.DaySound daySound;

    public IDCConfigScreen(Screen parent) {
        super(Component.translatable("idc.config.title"));
        this.parent = parent;
        this.layout = new HeaderAndFooterLayout(this);
        this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
        this.soundsEnabled = IDCConfig.get().sounds.enabled;
        this.daySound = IDCConfig.get().sounds.sound;
    }

    @Override
    protected void init() {
        this.scrollArea = new ScrollableLayout(this.minecraft, buildContentGrid(), INITIAL_SCROLL_HEIGHT);

        this.tabNavigationBar = MenuTabBar.builder(this.tabManager, this.width)
            .addTab(new ScrollConfigTab(Component.translatable("idc.config.tab.general"), this.scrollArea))
            .build();
        this.addRenderableWidget(this.tabNavigationBar);

        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(Component.translatable("idc.config.apply"), button -> apply())
            .width(BUTTON_WIDTH)
            .build());
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
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
        rows.addChild(CycleButton.onOffBuilder(this.soundsEnabled)
            .create(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("idc.config.sounds"),
                (button, value) -> this.soundsEnabled = value));
        rows.addChild(CycleButton.builder(
                sound -> Component.translatable("idc.config.sound." + sound.name().toLowerCase(Locale.ROOT)),
                this.daySound)
            .withValues(IDCConfig.DaySound.values())
            .create(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("idc.config.sound"),
                (button, value) -> {
                    this.daySound = value;
                    IDCSounds.play(value);
                }));
        rows.addChild(styleButton("idc.config.display_center", IDCStyleConfigScreen.StyleTarget.CENTER));
        rows.addChild(styleButton("idc.config.display_hotbar", IDCStyleConfigScreen.StyleTarget.HOTBAR));
        rows.addChild(styleButton("idc.config.display_corner", IDCStyleConfigScreen.StyleTarget.CORNER));

        return grid;
    }

    private Button styleButton(String translationKey, IDCStyleConfigScreen.StyleTarget target) {
        return Button.builder(Component.translatable(translationKey),
                button -> this.minecraft.gui.setScreen(new IDCStyleConfigScreen(this, target)))
            .width(WIDGET_WIDTH)
            .build();
    }

    private void apply() {
        IDCConfig.get().sounds.enabled = this.soundsEnabled;
        IDCConfig.get().sounds.sound = this.daySound;
        IDCConfig.save();
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar == null) {
            return;
        }
        this.tabNavigationBar.arrangeElements(this.width);
        int headerBottom = this.tabNavigationBar.getRectangle().bottom();
        ScreenRectangle tabArea = new ScreenRectangle(0, headerBottom, this.width,
            this.height - this.layout.getFooterHeight() - headerBottom);
        this.tabManager.setTabArea(tabArea);
        this.layout.setHeaderHeight(headerBottom);
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    protected void extractMenuBackground(GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, CreateWorldScreen.TAB_HEADER_BACKGROUND,
            0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.extractMenuBackground(graphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR,
            0, this.height - this.layout.getFooterHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
    }
}
