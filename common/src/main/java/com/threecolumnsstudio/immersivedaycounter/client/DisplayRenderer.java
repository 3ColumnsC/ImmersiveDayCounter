package com.threecolumnsstudio.immersivedaycounter.client;

import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import com.threecolumnsstudio.immersivedaycounter.day.DayClock;
import com.threecolumnsstudio.immersivedaycounter.mixin.GuiAccessor;
import java.text.NumberFormat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;

public final class DisplayRenderer {

    private static final long NO_ANNOUNCE = Long.MIN_VALUE;
    private static final int PANEL_COLOR_ALPHA = 0x60;
    private static final int PANEL_PADDING = 3;
    private static final int FONT_TRAILING_SPACING = 1;
    private static final int FONT_LINE_SLACK = 1;
    private static final int SPECIAL_DAY_INTERVAL = 100;
    private static final float CENTER_TIME_SCALE = 1.0F;
    private static final NumberFormat DAY_FORMAT = NumberFormat.getIntegerInstance();

    private static long announceStartNanos = NO_ANNOUNCE;
    private static boolean hotbarSuppressed;
    private static long hotbarRevealNanos = NO_ANNOUNCE;

    private DisplayRenderer() {
    }

    public static void announce(long day) {
        announceStartNanos = System.nanoTime();
        IDCSounds.playDaily();
    }

    public static void reset() {
        announceStartNanos = NO_ANNOUNCE;
        hotbarSuppressed = false;
        hotbarRevealNanos = NO_ANNOUNCE;
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
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

    private static void renderCenter(GuiGraphics graphics, Font font, IDCConfig.Style style, long day, double elapsed) {
        if (!style.enabled || !isActive(style, elapsed)) {
            return;
        }
        int alpha = alpha(style, elapsed);
        if (alpha <= 0) {
            return;
        }

        Component title = Component.translatable("idc.display.title");
        Component dayLine = Component.literal(formatDay(day));
        boolean showTime = style.showDayTime;
        Component timeLine = showTime ? timeLabel(style.dayTimeFormat) : null;

        float titleScale = 4.0F;
        float numberScale = 2.0F;
        float timeScale = CENTER_TIME_SCALE;
        int gap = 2;
        int titleHeight = Math.round(font.lineHeight * titleScale);
        int numberHeight = Math.round(font.lineHeight * numberScale);
        int timeHeight = showTime ? Math.round(font.lineHeight * timeScale) : 0;
        int blockHeight = titleHeight + gap + numberHeight + (showTime ? gap + timeHeight : 0);
        int blockTop = -Math.round(blockHeight / 2.0F);
        int titleWidth = font.width(title);
        int dayWidth = font.width(dayLine);
        int timeWidth = showTime ? font.width(timeLine) : 0;

        int titleX = -titleWidth / 2;
        int titleY = Math.round(blockTop / titleScale);
        int numberX = -dayWidth / 2;
        int numberY = Math.round((blockTop + titleHeight + gap) / numberScale);
        int timeX = -timeWidth / 2;
        int timeY = Math.round((blockTop + titleHeight + gap + numberHeight + gap) / timeScale);

        boolean background = hasBackground(style);
        boolean shadow = !background;

        int left = Math.min(Math.round(titleX * titleScale), Math.round(numberX * numberScale));
        int right = Math.max(Math.round((titleX + titleWidth - FONT_TRAILING_SPACING) * titleScale),
                Math.round((numberX + dayWidth - FONT_TRAILING_SPACING) * numberScale));
        int bottom = Math.round((numberY + font.lineHeight - FONT_LINE_SLACK) * numberScale);
        if (showTime) {
            left = Math.min(left, Math.round(timeX * timeScale));
            right = Math.max(right, Math.round((timeX + timeWidth - FONT_TRAILING_SPACING) * timeScale));
            bottom = Math.round((timeY + font.lineHeight - FONT_LINE_SLACK) * timeScale);
        }
        int top = Math.round(titleY * titleScale);
        if (background) {
            top = Math.min(top, Math.round(textBounds(font, title, titleX, titleY).top() * titleScale));
        }

        graphics.nextStratum();
        graphics.pose().pushMatrix();
        graphics.pose().translate(graphics.guiWidth() / 2.0F, graphics.guiHeight() / 2.0F);

        if (background) {
            graphics.fill(left - PANEL_PADDING, top - PANEL_PADDING, right + PANEL_PADDING, bottom + PANEL_PADDING, panelColor(alpha));
        }

        graphics.pose().pushMatrix();
        graphics.pose().scale(titleScale, titleScale);
        graphics.drawString(font, title, titleX, titleY, ARGB.color(alpha, textColor(style, day)), shadow);
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().scale(numberScale, numberScale);
        graphics.drawString(font, dayLine, numberX, numberY, ARGB.color(alpha, textColor(style, day)), shadow);
        graphics.pose().popMatrix();

        if (showTime) {
            graphics.pose().pushMatrix();
            graphics.pose().scale(timeScale, timeScale);
            graphics.drawString(font, timeLine, timeX, timeY, ARGB.color(alpha, textColor(style, day)), shadow);
            graphics.pose().popMatrix();
        }

        graphics.pose().popMatrix();
    }

    private static void renderHotbar(GuiGraphics graphics, Font font, IDCConfig.Style style, long day, double elapsed) {
        if (!style.enabled) {
            hotbarSuppressed = false;
            hotbarRevealNanos = NO_ANNOUNCE;
            return;
        }
        if (actionBarVisible()) {
            hotbarSuppressed = true;
            hotbarRevealNanos = NO_ANNOUNCE;
            return;
        }
        if (hotbarSuppressed) {
            hotbarSuppressed = false;
            hotbarRevealNanos = System.nanoTime();
        }
        if (!isActive(style, elapsed)) {
            return;
        }
        int alpha = Math.min(alpha(style, elapsed), revealAlpha(style.fadeSeconds));
        if (alpha <= 0) {
            return;
        }
        Component text = dayText(style, day);
        boolean background = hasBackground(style);
        int width = font.width(text);
        int x = graphics.guiWidth() / 2 - width / 2;
        int y = graphics.guiHeight() - 70;
        if (background) {
            graphics.fill(x - PANEL_PADDING, y - PANEL_PADDING,
                    x + width - FONT_TRAILING_SPACING + PANEL_PADDING, y + font.lineHeight - FONT_LINE_SLACK + PANEL_PADDING, panelColor(alpha));
        }
        graphics.drawString(font, text, x, y, ARGB.color(alpha, textColor(style, day)), !background);
    }

    private static void renderCorner(GuiGraphics graphics, Font font, IDCConfig.CornerStyle style, long day, double elapsed) {
        if (!style.enabled || !isActive(style, elapsed)) {
            return;
        }
        int alpha = alpha(style, elapsed);
        if (alpha <= 0) {
            return;
        }
        Component text = dayText(style, day);
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
        graphics.drawString(font, text, x, y, ARGB.color(alpha, textColor(style, day)), !background);
    }

    private static boolean hasBackground(IDCConfig.Style style) {
        return Boolean.TRUE.equals(style.background);
    }

    private static ScreenRectangle textBounds(Font font, Component text, int x, int y) {
        ScreenRectangle bounds = font.prepareText(text.getVisualOrderText(), x, y, -1, false, false, 0).bounds();
        return bounds != null ? bounds : new ScreenRectangle(x, y, font.width(text), font.lineHeight);
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

    private static boolean actionBarVisible() {
        Gui gui = Minecraft.getInstance().gui;
        return ((GuiAccessor) gui).idc$getOverlayMessageTime() > 0;
    }

    private static int revealAlpha(double fadeSeconds) {
        if (hotbarRevealNanos == NO_ANNOUNCE || fadeSeconds <= 0.0D) {
            return 255;
        }
        double revealElapsed = (System.nanoTime() - hotbarRevealNanos) / 1_000_000_000.0D;
        if (revealElapsed >= fadeSeconds) {
            return 255;
        }
        return clampAlpha(255.0D * (revealElapsed / fadeSeconds));
    }

    private static double elapsedSeconds() {
        if (announceStartNanos == NO_ANNOUNCE) {
            return Double.MAX_VALUE;
        }
        return (System.nanoTime() - announceStartNanos) / 1_000_000_000.0D;
    }

    private static String formatDay(long day) {
        return DAY_FORMAT.format(day);
    }

    private static Component dayText(IDCConfig.Style style, long day) {
        Component dayOnly = Component.translatable("idc.hud.day", formatDay(day));
        if (!style.showDayTime) {
            return dayOnly;
        }
        return Component.translatable("idc.hud.day_time", dayOnly, timeLabel(style.dayTimeFormat));
    }

    private static Component timeLabel(IDCConfig.DayTimeFormat format) {
        return Component.translatable("idc.hud.time", formatClock(format));
    }

    private static String formatClock(IDCConfig.DayTimeFormat format) {
        Level level = Minecraft.getInstance().level;
        long ticks = level == null ? 0L : DayClock.currentDayTicks(level);
        long dayTicks = Math.floorMod(ticks, DayClock.TICKS_PER_DAY);
        int hours24 = (int) ((dayTicks / 1000L + 6L) % 24L);
        int minutes = (int) (dayTicks % 1000L * 60L / 1000L);
        if (format == IDCConfig.DayTimeFormat.H24) {
            return String.format("%02d:%02d", hours24, minutes);
        }
        int hours12 = hours24 % 12;
        if (hours12 == 0) {
            hours12 = 12;
        }
        String suffix = Component.translatable(hours24 < 12 ? "idc.time.am" : "idc.time.pm").getString();
        return String.format("%d:%02d %s", hours12, minutes, suffix);
    }
}
