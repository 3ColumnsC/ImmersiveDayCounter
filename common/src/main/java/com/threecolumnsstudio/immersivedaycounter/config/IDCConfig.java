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
    public Lumen lumen = new Lumen();
    public Objective objective = new Objective();

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

    public static void resetToDefaults() {
        IDCConfig defaults = new IDCConfig();
        defaults.sanitize();
        if (instance != null && instance.lumen != null) {
            defaults.lumen = instance.lumen;
        }
        if (instance != null && instance.objective != null) {
            defaults.objective = instance.objective;
        }
        instance = defaults;
        save();
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
        if (this.lumen == null) {
            this.lumen = new Lumen();
        }
        if (this.objective == null) {
            this.objective = new Objective();
        }
        this.sounds.sanitize();
        this.display.sanitize();
        this.lumen.sanitize();
        this.objective.sanitize();
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
        CREEPER,
        @SerializedName("random")
        RANDOM
    }

    public enum DayTimeFormat {
        @SerializedName("24h")
        H24,
        @SerializedName("12h")
        H12
    }

    public enum DisplayContent {
        @SerializedName("day")
        DAY,
        @SerializedName("time")
        TIME,
        @SerializedName("day_and_time")
        DAY_AND_TIME,
        @SerializedName("only_objective")
        ONLY_OBJECTIVE
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

    public static final class Lumen {
        public static final double DEFAULT_BRIGHTNESS = 15.0D;

        public double brightness = DEFAULT_BRIGHTNESS;
        public boolean clearFog = true;

        private void sanitize() {
            if (Double.isNaN(this.brightness)) {
                this.brightness = DEFAULT_BRIGHTNESS;
            }
            if (this.brightness < 0.0D) {
                this.brightness = 0.0D;
            }
            if (this.brightness > DEFAULT_BRIGHTNESS) {
                this.brightness = DEFAULT_BRIGHTNESS;
            }
        }
    }

    public static final class Objective {
        public static final long DEFAULT_DAY = 100L;
        public static final long MIN_DAY = 1L;
        public static final long MAX_DAY = 1_000_000L;

        public boolean defaultDay100 = true;
        public long day = DEFAULT_DAY;

        public long effectiveDay() {
            return this.defaultDay100 ? DEFAULT_DAY : this.day;
        }

        public void setCustom(long day) {
            this.day = clampDay(day);
            this.defaultDay100 = false;
        }

        private void sanitize() {
            this.day = clampDay(this.day);
        }

        private static long clampDay(long day) {
            if (day < MIN_DAY) {
                return MIN_DAY;
            }
            if (day > MAX_DAY) {
                return MAX_DAY;
            }
            return day;
        }
    }

    public static final class Display {
        public Style center = Style.create(true, false, false, DisplayContent.DAY, Boolean.FALSE);
        public Style hotbar = Style.create(true, true, false, DisplayContent.DAY_AND_TIME, Boolean.TRUE);
        public CornerStyle corner = CornerStyle.create(false, true, true, DisplayContent.DAY, Boolean.FALSE);

        private void sanitize() {
            if (this.center == null) {
                this.center = Style.create(true, false, false, DisplayContent.DAY, Boolean.FALSE);
            }
            if (this.hotbar == null) {
                this.hotbar = Style.create(true, true, false, DisplayContent.DAY_AND_TIME, Boolean.TRUE);
            }
            if (this.corner == null) {
                this.corner = CornerStyle.create(false, true, true, DisplayContent.DAY, Boolean.FALSE);
            }
            if (this.center.showObjective == null) {
                this.center.showObjective = Boolean.FALSE;
            }
            if (this.hotbar.showObjective == null) {
                this.hotbar.showObjective = Boolean.TRUE;
            }
            if (this.corner.showObjective == null) {
                this.corner.showObjective = Boolean.FALSE;
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
        public Boolean showObjective;
        public String textColor = "#FFFFFF";
        public boolean specialDayText = true;
        public String specialDayTextColor = "#FF7A5E";
        public DisplayContent content;
        @SerializedName("showDayTime")
        private Boolean legacyShowDayTime;
        public DayTimeFormat dayTimeFormat = DayTimeFormat.H24;
        public transient int textColorRgb = DEFAULT_TEXT_COLOR;
        public transient int specialDayTextColorRgb = DEFAULT_SPECIAL_DAY_TEXT_COLOR;

        private static Style create(boolean enabled, boolean permanent, boolean background, DisplayContent content, Boolean showObjective) {
            Style style = new Style();
            style.enabled = enabled;
            style.permanent = permanent;
            style.durationSeconds = 3.0D;
            style.fadeSeconds = 0.6D;
            style.background = background;
            style.content = content;
            style.showObjective = showObjective;
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
            if (this.showObjective == null) {
                this.showObjective = Boolean.FALSE;
            }
            this.textColorRgb = parseColor(this.textColor, DEFAULT_TEXT_COLOR);
            this.textColor = formatColor(this.textColorRgb);
            this.specialDayTextColorRgb = parseColor(this.specialDayTextColor, DEFAULT_SPECIAL_DAY_TEXT_COLOR);
            this.specialDayTextColor = formatColor(this.specialDayTextColorRgb);
            if (this.content == null) {
                this.content = Boolean.TRUE.equals(this.legacyShowDayTime)
                    ? DisplayContent.DAY_AND_TIME
                    : DisplayContent.DAY;
            }
            this.legacyShowDayTime = null;
            if (this.dayTimeFormat == null) {
                this.dayTimeFormat = DayTimeFormat.H24;
            }
        }
    }

    public static final class CornerStyle extends Style {
        public Position position = Position.TOP_LEFT;

        private static CornerStyle create(boolean enabled, boolean permanent, boolean background, DisplayContent content, Boolean showObjective) {
            CornerStyle style = new CornerStyle();
            style.enabled = enabled;
            style.permanent = permanent;
            style.durationSeconds = 3.0D;
            style.fadeSeconds = 0.6D;
            style.background = background;
            style.content = content;
            style.showObjective = showObjective;
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
