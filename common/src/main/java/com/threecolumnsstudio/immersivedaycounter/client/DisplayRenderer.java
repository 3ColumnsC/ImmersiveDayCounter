package com.threecolumnsstudio.immersivedaycounter.client;

import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import java.text.NumberFormat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public final class DisplayRenderer {

    private static final long NO_ANNOUNCE = Long.MIN_VALUE;
    private static final int PANEL_COLOR_ALPHA = 0x60;
    private static final int PANEL_PADDING = 3;
    private static final int FONT_TRAILING_SPACING = 1;
    private static final int FONT_LINE_SLACK = 1;
    private static final int SPECIAL_DAY_INTERVAL = 100;

    private static long announceStartNanos = NO_ANNOUNCE;

    private DisplayRenderer() {
    }

    public static void announce(long day) {
        announceStartNanos = System.nanoTime();
        IDCSounds.playDaily();
    }

    public static void reset() {
        announceStartNanos = NO_ANNOUNCE;
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!ClientDayState.isKnown()) {
            return;
        }
        IDCConfig config = IDCConfig.get();
        Font font = Minecraft.getInstance().font;
        long day = ClientDayState.day();
        double elapsed = elapsedSeconds();

        renderCorner(graphics, font, config.display.corner, day, elapsed);
        renderHotbar(graphics, font, config.display.hotbar, day, elapsed);
        renderCenter(graphics, font, config.display.center, day, elapsed);
    }

    private static void renderCenter(GuiGraphicsExtractor graphics, Font font, IDCConfig.Style style, long day, double elapsed) {
        if (!style.enabled || !isActive(style, elapsed)) {
            return;
        }
        int alpha = alpha(style, elapsed);
        if (alpha <= 0) {
            return;
        }

        Component title = Component.translatable("idc.display.title");
        Component dayLine = Component.literal(formatDay(day));

        float titleScale = 4.0F;
        float numberScale = 2.0F;
        int gap = 2;
        int titleHeight = Math.round(font.lineHeight * titleScale);
        int numberHeight = Math.round(font.lineHeight * numberScale);
        int blockTop = -Math.round((titleHeight + gap + numberHeight) / 2.0F);
        int titleWidth = font.width(title);
        int dayWidth = font.width(dayLine);

        int titleX = -titleWidth / 2;
        int titleY = Math.round(blockTop / titleScale);
        int numberX = -dayWidth / 2;
        int numberY = Math.round((blockTop + titleHeight + gap) / numberScale);

        boolean background = hasBackground(style);
        boolean shadow = !background;

        int left = Math.min(Math.round(titleX * titleScale), Math.round(numberX * numberScale));
        int right = Math.max(Math.round((titleX + titleWidth - FONT_TRAILING_SPACING) * titleScale),
                Math.round((numberX + dayWidth - FONT_TRAILING_SPACING) * numberScale));
        int top = Math.round(titleY * titleScale);
        int bottom = Math.round((numberY + font.lineHeight - FONT_LINE_SLACK) * numberScale);

        graphics.nextStratum();
        graphics.pose().pushMatrix();
        graphics.pose().translate(graphics.guiWidth() / 2.0F, graphics.guiHeight() / 2.0F);

        if (background) {
            graphics.fill(left - PANEL_PADDING, top - PANEL_PADDING, right + PANEL_PADDING, bottom + PANEL_PADDING, panelColor(alpha));
        }

        graphics.pose().pushMatrix();
        graphics.pose().scale(titleScale, titleScale);
        graphics.text(font, title, titleX, titleY, ARGB.color(alpha, textColor(style, day)), shadow);
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().scale(numberScale, numberScale);
        graphics.text(font, dayLine, numberX, numberY, ARGB.color(alpha, textColor(style, day)), shadow);
        graphics.pose().popMatrix();

        graphics.pose().popMatrix();
    }

    private static void renderHotbar(GuiGraphicsExtractor graphics, Font font, IDCConfig.Style style, long day, double elapsed) {
        if (!style.enabled || !isActive(style, elapsed)) {
            return;
        }
        int alpha = alpha(style, elapsed);
        if (alpha <= 0) {
            return;
        }
        Component text = Component.translatable("idc.hud.day", formatDay(day));
        boolean background = hasBackground(style);
        int width = font.width(text);
        int x = graphics.guiWidth() / 2 - width / 2;
        int y = graphics.guiHeight() - 70;
        if (background) {
            graphics.fill(x - PANEL_PADDING, y - PANEL_PADDING,
                    x + width - FONT_TRAILING_SPACING + PANEL_PADDING, y + font.lineHeight - FONT_LINE_SLACK + PANEL_PADDING, panelColor(alpha));
        }
        graphics.text(font, text, x, y, ARGB.color(alpha, textColor(style, day)), !background);
    }

    private static void renderCorner(GuiGraphicsExtractor graphics, Font font, IDCConfig.CornerStyle style, long day, double elapsed) {
        if (!style.enabled || !isActive(style, elapsed)) {
            return;
        }
        int alpha = alpha(style, elapsed);
        if (alpha <= 0) {
            return;
        }
        Component text = Component.translatable("idc.hud.day", formatDay(day));
        boolean background = hasBackground(style);
        int margin = 5;
        boolean left = style.position == IDCConfig.Position.TOP_LEFT || style.position == IDCConfig.Position.BOTTOM_LEFT;
        boolean top = style.position == IDCConfig.Position.TOP_LEFT || style.position == IDCConfig.Position.TOP_RIGHT;
        int width = font.width(text);
        int x = left ? margin : graphics.guiWidth() - width - margin;
        int y = top ? margin : graphics.guiHeight() - font.lineHeight - margin;
        if (background) {
            graphics.fill(x - PANEL_PADDING, y - PANEL_PADDING,
                    x + width - FONT_TRAILING_SPACING + PANEL_PADDING, y + font.lineHeight - FONT_LINE_SLACK + PANEL_PADDING, panelColor(alpha));
        }
        graphics.text(font, text, x, y, ARGB.color(alpha, textColor(style, day)), !background);
    }

    private static boolean hasBackground(IDCConfig.Style style) {
        return Boolean.TRUE.equals(style.background);
    }

    private static int textColor(IDCConfig.Style style, long day) {
        if (style.specialDayText && day % SPECIAL_DAY_INTERVAL == 0L) {
            return style.specialDayTextColorRgb;
        }
        return style.textColorRgb;
    }

    private static int panelColor(int alpha) {
        return ARGB.color(PANEL_COLOR_ALPHA * alpha / 255, 0, 0, 0);
    }

    private static boolean isActive(IDCConfig.Style style, double elapsed) {
        return style.permanent || elapsed < style.durationSeconds;
    }

    private static int alpha(IDCConfig.Style style, double elapsed) {
        if (style.permanent) {
            return 255;
        }
        double duration = style.durationSeconds;
        if (elapsed >= duration) {
            return 0;
        }
        double fade = style.fadeSeconds;
        if (fade > 0.0D) {
            if (elapsed < fade) {
                return clampAlpha(255.0D * (elapsed / fade));
            }
            if (duration - elapsed < fade) {
                return clampAlpha(255.0D * ((duration - elapsed) / fade));
            }
        }
        return 255;
    }

    private static int clampAlpha(double value) {
        return (int) Math.round(Math.max(0.0D, Math.min(255.0D, value)));
    }

    private static double elapsedSeconds() {
        if (announceStartNanos == NO_ANNOUNCE) {
            return Double.MAX_VALUE;
        }
        return (System.nanoTime() - announceStartNanos) / 1_000_000_000.0D;
    }

    private static String formatDay(long day) {
        return NumberFormat.getIntegerInstance().format(day);
    }
}
