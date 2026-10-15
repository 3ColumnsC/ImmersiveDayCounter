package com.threecolumnsstudio.immersivedaycounter.client.screen;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

final class ScrollConfigTab implements Tab {

    private static final int CONTENT_TOP_PADDING = 8;
    private static final int CONTENT_BOTTOM_PADDING = 4;

    private final Component title;
    private final ScrollableLayout content;

    ScrollConfigTab(Component title, ScrollableLayout content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public Component getTabTitle() {
        return this.title;
    }

    @Override
    public Component getTabExtraNarration() {
        return Component.empty();
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        this.content.visitWidgets(consumer);
    }

    @Override
    public void doLayout(ScreenRectangle area) {
        this.content.arrangeElements();
        int viewportHeight = area.height() - CONTENT_TOP_PADDING - CONTENT_BOTTOM_PADDING;
        this.content.setMaxHeight(viewportHeight);
        this.content.arrangeElements();
        int centeredX = area.left() + (area.width() - this.content.getWidth()) / 2;
        this.content.setPosition(centeredX, area.top() + CONTENT_TOP_PADDING);
    }
}
