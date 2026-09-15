package com.example.tibiachat.config;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;

public final class TibiaChatConfig {

    public static final float MIN_BAR_SCALE = 0.5f;
    public static final float MAX_BAR_SCALE = 2.0f;
    public static final float MIN_ICON_SCALE = 0.5f;
    public static final float MAX_ICON_SCALE = 3.0f;
    public static final int MIN_BAR_OFFSET = -2000;
    public static final int MAX_BAR_OFFSET = 2000;
    public static final int MIN_ICON_OFFSET = -2000;
    public static final int MAX_ICON_OFFSET = 2000;
    public static final int MIN_BAR_WIDTH = 50;
    public static final int MAX_BAR_WIDTH = 2000;
    public static final int MIN_ALPHA = 0;
    public static final int MAX_ALPHA = 255;

    private float tabBarScale = 1.0f;
    private float notifIconScale = 1.0f;
    private int tabBarOffsetX = 0;
    private int tabBarOffsetY = -128;
    private int tabBarWidth = 286;
    private int notifIconOffsetX = -4;
    private int notifIconOffsetY = 20;
    private int settingsBtnOffsetX = -2;
    private int settingsBtnOffsetY = 0;

    public float tabBarScale() { return tabBarScale; }
    public void setTabBarScale(float v) { tabBarScale = clamp(v, MIN_BAR_SCALE, MAX_BAR_SCALE); }

    public float notifIconScale() { return notifIconScale; }
    public void setNotifIconScale(float v) { notifIconScale = clamp(v, MIN_ICON_SCALE, MAX_ICON_SCALE); }

    public int tabBarOffsetX() { return tabBarOffsetX; }
    public void setTabBarOffsetX(int v) { tabBarOffsetX = (int) clamp(v, MIN_BAR_OFFSET, MAX_BAR_OFFSET); }

    public int tabBarOffsetY() { return tabBarOffsetY; }
    public void setTabBarOffsetY(int v) { tabBarOffsetY = (int) clamp(v, MIN_BAR_OFFSET, MAX_BAR_OFFSET); }

    public int tabBarWidth() { return tabBarWidth; }
    public void setTabBarWidth(int v) { tabBarWidth = (int) clamp(v, MIN_BAR_WIDTH, MAX_BAR_WIDTH); }

    public int notifIconOffsetX() { return notifIconOffsetX; }
    public void setNotifIconOffsetX(int v) { notifIconOffsetX = (int) clamp(v, MIN_ICON_OFFSET, MAX_ICON_OFFSET); }

    public int notifIconOffsetY() { return notifIconOffsetY; }
    public void setNotifIconOffsetY(int v) { notifIconOffsetY = (int) clamp(v, MIN_ICON_OFFSET, MAX_ICON_OFFSET); }

    public int settingsBtnOffsetX() { return settingsBtnOffsetX; }
    public void setSettingsBtnOffsetX(int v) { settingsBtnOffsetX = (int) clamp(v, MIN_ICON_OFFSET, MAX_ICON_OFFSET); }

    public int settingsBtnOffsetY() { return settingsBtnOffsetY; }
    public void setSettingsBtnOffsetY(int v) { settingsBtnOffsetY = (int) clamp(v, MIN_ICON_OFFSET, MAX_ICON_OFFSET); }

    public void resetHudDefaults() {
        tabBarScale = 1.0f;
        notifIconScale = 1.0f;
        tabBarOffsetX = 0;
        tabBarOffsetY = -128;
        tabBarWidth = 286;
        notifIconOffsetX = -4;
        notifIconOffsetY = 20;
        settingsBtnOffsetX = -2;
        settingsBtnOffsetY = 0;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static final int DEFAULT_TAB_BAR_COLOR = 0x0B1C24;
    private static final int DEFAULT_TAB_BAR_ALPHA = 121;
    private static final int DEFAULT_TAB_COLOR = 0x134E5E;
    private static final int DEFAULT_TAB_ALPHA = 162;

    private int tabBarColor = DEFAULT_TAB_BAR_COLOR;
    private int tabBarAlpha = DEFAULT_TAB_BAR_ALPHA;
    private int tabColor = DEFAULT_TAB_COLOR;
    private int tabAlpha = DEFAULT_TAB_ALPHA;

    public int tabBarColor() { return tabBarColor & 0xFFFFFF; }
    public void setTabBarColor(int rgb) { tabBarColor = rgb & 0xFFFFFF; }

    public int tabBarAlpha() { return tabBarAlpha; }
    public void setTabBarAlpha(int a) { tabBarAlpha = (int) clamp(a, MIN_ALPHA, MAX_ALPHA); }

    public int tabColor() { return tabColor & 0xFFFFFF; }
    public void setTabColor(int rgb) { tabColor = rgb & 0xFFFFFF; }

    public int tabAlpha() { return tabAlpha; }
    public void setTabAlpha(int a) { tabAlpha = (int) clamp(a, MIN_ALPHA, MAX_ALPHA); }

    public void resetAppearanceDefaults() {
        colorTheme = "Ocean";
        tabBarColor = DEFAULT_TAB_BAR_COLOR;
        tabBarAlpha = DEFAULT_TAB_BAR_ALPHA;
        tabColor = DEFAULT_TAB_COLOR;
        tabAlpha = DEFAULT_TAB_ALPHA;
    }

    public static final class ColorTheme {
        public final String name;
        public final int barColor;
        public final int barAlpha;
        public final int tabColor;
        public final int tabAlpha;

        public ColorTheme(String name, int barColor, int barAlpha, int tabColor, int tabAlpha) {
            this.name = name;
            this.barColor = barColor & 0xFFFFFF;
            this.barAlpha = barAlpha;
            this.tabColor = tabColor & 0xFFFFFF;
            this.tabAlpha = tabAlpha;
        }
    }

    public static final List<ColorTheme> COLOR_THEMES = List.of(
            new ColorTheme("Ocean", 0x0B1C24, 121, 0x134E5E, 162),
            new ColorTheme("Midnight", 0x0A0A12, 200, 0x1A1A2E, 230),
            new ColorTheme("Slate", 0x1C1F26, 190, 0x2E3440, 240),
            new ColorTheme("Steel", 0x1A1E22, 180, 0x3A424A, 230),
            new ColorTheme("Forest", 0x0A1A0E, 190, 0x1A3A22, 230),
            new ColorTheme("Moss", 0x121A10, 180, 0x2A3A1A, 220),
            new ColorTheme("Ember", 0x1A0A08, 200, 0x3A1810, 240),
            new ColorTheme("Crimson", 0x1A080C, 200, 0x3A1020, 240),
            new ColorTheme("Rose", 0x1A0E14, 190, 0x3A1A28, 230),
            new ColorTheme("Purple", 0x120A1A, 200, 0x2A1A3A, 240),
            new ColorTheme("Violet", 0x140E1C, 190, 0x2E1A4A, 230),
            new ColorTheme("Indigo", 0x0C0E1A, 200, 0x1A2040, 240),
            new ColorTheme("Sky", 0x0A1420, 180, 0x1A3050, 220),
            new ColorTheme("Ice", 0x0E181C, 170, 0x1A3038, 210),
            new ColorTheme("Teal", 0x0A1A1A, 190, 0x1A3A3A, 230),
            new ColorTheme("Gold", 0x1A160A, 200, 0x3A3018, 240),
            new ColorTheme("Sand", 0x1A1810, 180, 0x3A3420, 220),
            new ColorTheme("Dark", 0x101010, 176, 0x202020, 255),
            new ColorTheme("Charcoal", 0x141414, 200, 0x282828, 240),
            new ColorTheme("Transparent", 0x000000, 60, 0x101010, 120),
            new ColorTheme("High Contrast", 0x000000, 230, 0x404040, 255),
            new ColorTheme("Neon", 0x0A0014, 200, 0x1A0030, 240),
            new ColorTheme("Blood", 0x140000, 210, 0x2A0000, 250),
            new ColorTheme("Abyss", 0x000008, 220, 0x000018, 250)
    );

    private String colorTheme = "Ocean";

    public String colorTheme() { return colorTheme == null ? "Ocean" : colorTheme; }

    public void setColorTheme(String name) {
        for (ColorTheme t : COLOR_THEMES) {
            if (t.name.equalsIgnoreCase(name)) {
                colorTheme = t.name;
                tabBarColor = t.barColor;
                tabBarAlpha = t.barAlpha;
                tabColor = t.tabColor;
                tabAlpha = t.tabAlpha;
                return;
            }
        }
    }

    public void cycleColorTheme() {
        int idx = 0;
        for (int i = 0; i < COLOR_THEMES.size(); i++) {
            if (COLOR_THEMES.get(i).name.equalsIgnoreCase(colorTheme)) {
                idx = i;
                break;
            }
        }
        int next = (idx + 1) % COLOR_THEMES.size();
        setColorTheme(COLOR_THEMES.get(next).name);
    }


    private static final String DEFAULT_WHISPER_COMMAND = "/w";
    private static final List<String> DEFAULT_WHISPER_ALIASES =
            List.of("w", "msg", "tell", "whisper");
    private static final String ICON_PREFIX = "(?:[^\\w\\s]{1,8}\\s*)?";

    private static final String TS_FRAG = "(?:[\\[<]\\d{1,2}:\\d{2}(?::\\d{2})?[\\]>]\\s*)?";
    private static final String HEAD_FRAG = "(?:\\[[^\\]]{1,48}\\]\\s*)?";

    private static final List<String> DEFAULT_INCOMING_WHISPER_REGEXES = List.of(
            "^" + TS_FRAG + HEAD_FRAG + ICON_PREFIX + "([a-zA-Z0-9_]{2,16})\\s+whispers?(?: to you)?\\s*:\\s*(.*)$",
            "^" + TS_FRAG + HEAD_FRAG + ICON_PREFIX + "([a-zA-Z0-9_]{2,16})\\s+->\\s+(?:you|You)\\s*:\\s*(.*)$",
            "^" + TS_FRAG + HEAD_FRAG + ICON_PREFIX + "\\[(?:PM|MSG|WHISPER)\\]\\s*" + ICON_PREFIX + "([a-zA-Z0-9_]{2,16})\\s*:\\s*(.*)$"
    );

    private String whisperCommand = DEFAULT_WHISPER_COMMAND;
    private List<String> whisperAliases = new ArrayList<>(DEFAULT_WHISPER_ALIASES);
    private List<String> incomingWhisperRegexes = new ArrayList<>(DEFAULT_INCOMING_WHISPER_REGEXES);

    public String whisperCommand() { return whisperCommand; }
    public List<String> incomingWhisperRegexes() { return Collections.unmodifiableList(incomingWhisperRegexes); }
    public List<String> whisperAliases() { return Collections.unmodifiableList(whisperAliases); }

    public static String defaultWhisperCommand() { return DEFAULT_WHISPER_COMMAND; }
    public static List<String> defaultWhisperAliases() { return DEFAULT_WHISPER_ALIASES; }
    public static List<String> defaultIncomingWhisperRegexes() { return DEFAULT_INCOMING_WHISPER_REGEXES; }
    public static String iconPrefixFragment() { return ICON_PREFIX; }

    public boolean isWhisperCommand(String base) {
        String configured = whisperCommand.startsWith("/") ? whisperCommand.substring(1) : whisperCommand;
        return base.equalsIgnoreCase(configured) || whisperAliases.stream().anyMatch(a -> a.equalsIgnoreCase(base));
    }

    public void setWhisperCommand(String value) {
        if (value != null && !value.isBlank()) whisperCommand = value.startsWith("/") ? value : "/" + value;
    }

    public void setWhisperAliases(List<String> aliases) {
        List<String> cleaned = new ArrayList<>();
        if (aliases != null) {
            for (String a : aliases) {
                if (a == null) continue;
                String t = a.trim();
                if (t.startsWith("/")) t = t.substring(1);
                if (!t.isBlank()) cleaned.add(t);
            }
        }
        whisperAliases = cleaned;
    }

    public void setIncomingWhisperRegexes(List<String> regexes) {
        List<String> cleaned = new ArrayList<>();
        if (regexes != null) {
            for (String r : regexes) {
                if (r != null && !r.isBlank()) cleaned.add(r);
            }
        }
        incomingWhisperRegexes = cleaned;
    }

    public void resetAllDefaults() {
        resetHudDefaults();
        resetAppearanceDefaults();
        resetWhisperDetectionDefaults();
    }

    public void resetWhisperDetectionDefaults() {
        whisperCommand = DEFAULT_WHISPER_COMMAND;
        whisperAliases = new ArrayList<>(DEFAULT_WHISPER_ALIASES);
        incomingWhisperRegexes = new ArrayList<>(DEFAULT_INCOMING_WHISPER_REGEXES);
    }

    public static TibiaChatConfig load() {
        Path p = path();
        if (!Files.exists(p)) return new TibiaChatConfig();
        try {
            return new Gson().fromJson(Files.readString(p), TibiaChatConfig.class);
        } catch (Exception e) {
            return new TibiaChatConfig();
        }
    }

    public void save() {
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), new GsonBuilder().setPrettyPrinting().create().toJson(this));
        } catch (Exception ignored) {}
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("tibia_chat_tabs.json");
    }
}
