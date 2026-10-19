package com.threecolumnsstudio.immersivedaycounter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.threecolumnsstudio.immersivedaycounter.ImmersiveDayCounter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class IDCConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "immersive-day-counter.json";
    private static final int DEFAULT_TEXT_COLOR = 0xFFFFFF;
    private static final int DEFAULT_SPECIAL_DAY_TEXT_COLOR = 0xFF7A5E;

    private static IDCConfig instance;
    private static Path filePath;
    private static long lastModified = -1L;

    public Sounds sounds = new Sounds();
    public Display display = new Display();

    private IDCConfig() {
    }

    public static IDCConfig get() {
        if (instance == null) {
            instance = new IDCConfig();
        }
        return instance;
    }

    public static void load(Path configDir) {
        filePath = configDir.resolve(FILE_NAME);
        IDCConfig config = read(filePath);
        if (config == null) {
            config = new IDCConfig();
        }
        config.sanitize();
        instance = config;
        config.write(filePath);
        lastModified = modifiedTime(filePath);
    }

    public static void reloadIfChanged() {
        if (filePath == null) {
            return;
        }
        long modified = modifiedTime(filePath);
        if (modified == lastModified) {
            return;
        }
        lastModified = modified;
        IDCConfig config = read(filePath);
        if (config == null) {
            return;
        }
        config.sanitize();
        instance = config;
    }

    public static void save() {
        if (instance == null || filePath == null) {
            return;
        }
        instance.sanitize();
        instance.write(filePath);
        lastModified = modifiedTime(filePath);
    }

    private static long modifiedTime(Path file) {
        try {
            return Files.exists(file) ? Files.getLastModifiedTime(file).toMillis() : -1L;
        } catch (IOException e) {
            return -1L;
        }
    }

    private static IDCConfig read(Path file) {
        if (!Files.exists(file)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            return GSON.fromJson(reader, IDCConfig.class);
        } catch (IOException | RuntimeException e) {
            ImmersiveDayCounter.LOGGER.error("Failed to read config {}", file, e);
            return null;
        }
    }

    private void write(Path file) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            ImmersiveDayCounter.LOGGER.error("Failed to write config {}", file, e);
        }
    }

    private void sanitize() {
        if (this.sounds == null) {
            this.sounds = new Sounds();
        }
        if (this.display == null) {
            this.display = new Display();
        }
        this.sounds.sanitize();
        this.display.sanitize();
    }

    private static int parseColor(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        String hex = value.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() != 6) {
            return fallback;
        }
        try {
            return Integer.parseInt(hex, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String formatColor(int rgb) {
        return String.format("#%06X", rgb);
    }

    public enum Position {
        @SerializedName("top_left")
        TOP_LEFT,
        @SerializedName("top_right")
        TOP_RIGHT,
        @SerializedName("bottom_right")
        BOTTOM_RIGHT,
        @SerializedName("bottom_left")
        BOTTOM_LEFT
    }

    public enum DaySound {
        @SerializedName("click")
        CLICK,
        @SerializedName("experience")
        EXPERIENCE,
        @SerializedName("level_up")
        LEVEL_UP,
        @SerializedName("bell")
        BELL,
        @SerializedName("pickup")
        PICKUP,
        @SerializedName("pling")
        PLING,
        @SerializedName("creeper")
        CREEPER
    }

    public enum DayTimeFormat {
        @SerializedName("24h")
        H24,
        @SerializedName("12h")
        H12
    }

    public static final class Sounds {
        public boolean enabled = true;
        public DaySound sound = DaySound.PLING;

        private void sanitize() {
            if (this.sound == null) {
                this.sound = DaySound.PLING;
            }
        }
    }

    public static final class Display {
        public Style center = Style.create(true, false, false, false);
        public Style hotbar = Style.create(true, true, false, true);
        public CornerStyle corner = CornerStyle.create(false, true, true, false);

        private void sanitize() {
            if (this.center == null) {
                this.center = Style.create(true, false, false, false);
            }
            if (this.hotbar == null) {
                this.hotbar = Style.create(true, true, false, true);
            }
            if (this.corner == null) {
                this.corner = CornerStyle.create(false, true, true, false);
            }
            this.center.sanitize();
            this.hotbar.sanitize();
            this.corner.sanitize();
        }
    }

    public static class Style {
        public boolean enabled;
        public boolean permanent;
        public double durationSeconds;
        public double fadeSeconds;
        public Boolean background;
        public String textColor = "#FFFFFF";
        public boolean specialDayText = true;
        public String specialDayTextColor = "#FF7A5E";
        public boolean showDayTime;
        public DayTimeFormat dayTimeFormat = DayTimeFormat.H24;
        public transient int textColorRgb = DEFAULT_TEXT_COLOR;
        public transient int specialDayTextColorRgb = DEFAULT_SPECIAL_DAY_TEXT_COLOR;

        private static Style create(boolean enabled, boolean permanent, boolean background, boolean showDayTime) {
            Style style = new Style();
            style.enabled = enabled;
            style.permanent = permanent;
            style.durationSeconds = 3.0D;
            style.fadeSeconds = 0.6D;
            style.background = background;
            style.showDayTime = showDayTime;
            return style;
        }

        protected void sanitize() {
            if (this.durationSeconds < 0.5D) {
                this.durationSeconds = 0.5D;
            }
            if (this.fadeSeconds < 0.0D) {
                this.fadeSeconds = 0.0D;
            }
            if (this.fadeSeconds > this.durationSeconds) {
                this.fadeSeconds = this.durationSeconds;
            }
            if (this.background == null) {
                this.background = Boolean.FALSE;
            }
            this.textColorRgb = parseColor(this.textColor, DEFAULT_TEXT_COLOR);
            this.textColor = formatColor(this.textColorRgb);
            this.specialDayTextColorRgb = parseColor(this.specialDayTextColor, DEFAULT_SPECIAL_DAY_TEXT_COLOR);
            this.specialDayTextColor = formatColor(this.specialDayTextColorRgb);
            if (this.dayTimeFormat == null) {
                this.dayTimeFormat = DayTimeFormat.H24;
            }
        }
    }

    public static final class CornerStyle extends Style {
        public Position position = Position.TOP_LEFT;

        private static CornerStyle create(boolean enabled, boolean permanent, boolean background, boolean showDayTime) {
            CornerStyle style = new CornerStyle();
            style.enabled = enabled;
            style.permanent = permanent;
            style.durationSeconds = 3.0D;
            style.fadeSeconds = 0.6D;
            style.background = background;
            style.showDayTime = showDayTime;
            return style;
        }

        @Override
        protected void sanitize() {
            if (this.background == null) {
                this.background = Boolean.TRUE;
            }
            super.sanitize();
            if (this.position == null) {
                this.position = Position.TOP_LEFT;
            }
        }
    }
}
