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
    private static final List<String> DEFAULT_WHISPER_ALIASES = List.of("w", "msg", "tell", "whisper");
    private static final String ICON_PREFIX = "(?:[^\\w\\s.]{1,8}\\s*)?";
    private static final String DEFAULT_PLAYER_NAME = "[a-zA-Z0-9_.\\-]{2,32}";

    public static final List<String> TIMESTAMP_STYLES = List.of(
            "Flexible (recommended)",
            "None",
            "[HH:MM:SS]",
            "[HH:MM]",
            "<HH:MM:SS>",
            "<HH:MM>",
            "(HH:MM:SS)",
            "HH:MM:SS"
    );

    public static final List<String> HEAD_STYLES = List.of(
            "None",
            "[anything]",
            "[PLAYER head]",
            "[PLAYER]",
            "[PLAYER*]"
    );

    private static final List<String> DEFAULT_WHISPER_FORMATS = List.of(
            "{player} whispers: {message}",
            "{player} whispers to you: {message}",
            "[{player} -> You]: {message}",
            "{player} -> you: {message}",
            "[PM] {player}: {message}",
            "[MSG] {player}: {message}",
            "[WHISPER] {player}: {message}"
    );

    private String whisperCommand = DEFAULT_WHISPER_COMMAND;
    private List<String> whisperAliases = new ArrayList<>(DEFAULT_WHISPER_ALIASES);
    private boolean timestampsEnabled = true;
    private String timestampStyle = "Flexible (recommended)";
    private boolean headsEnabled = true;
    private String headStyle = "[anything]";
    private String playerNamePattern = DEFAULT_PLAYER_NAME;
    private List<String> whisperFormats = new ArrayList<>(DEFAULT_WHISPER_FORMATS);

    public String whisperCommand() { return whisperCommand; }
    public List<String> whisperAliases() { return Collections.unmodifiableList(whisperAliases); }
    public boolean timestampsEnabled() { return timestampsEnabled; }
    public void setTimestampsEnabled(boolean v) { timestampsEnabled = v; }
    public String timestampStyle() { return timestampStyle == null ? "Flexible (recommended)" : timestampStyle; }
    public void setTimestampStyle(String style) { if (style != null && !style.isBlank()) timestampStyle = style.trim(); }
    public boolean headsEnabled() { return headsEnabled; }
    public void setHeadsEnabled(boolean v) { headsEnabled = v; }
    public String headStyle() { return headStyle == null ? "[anything]" : headStyle; }
    public void setHeadStyle(String style) { if (style != null && !style.isBlank()) headStyle = style.trim(); }
    public String playerNamePattern() { return playerNamePattern == null || playerNamePattern.isBlank() ? DEFAULT_PLAYER_NAME : playerNamePattern; }
    public void setPlayerNamePattern(String pattern) { if (pattern != null && !pattern.isBlank()) playerNamePattern = pattern.trim(); }
    public List<String> whisperFormats() { return Collections.unmodifiableList(whisperFormats); }

    public static String defaultWhisperCommand() { return DEFAULT_WHISPER_COMMAND; }
    public static List<String> defaultWhisperAliases() { return DEFAULT_WHISPER_ALIASES; }
    public static List<String> defaultWhisperFormats() { return DEFAULT_WHISPER_FORMATS; }
    public static String iconPrefixFragment() { return ICON_PREFIX; }
    public static String defaultPlayerNamePattern() { return DEFAULT_PLAYER_NAME; }

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

    public void setWhisperFormats(List<String> formats) {
        List<String> cleaned = new ArrayList<>();
        if (formats != null) {
            for (String f : formats) {
                if (f != null && !f.isBlank()) cleaned.add(f.trim());
            }
        }
        whisperFormats = cleaned;
    }


    public static final int MIN_SENT_HISTORY = 1;
    public static final int MAX_SENT_HISTORY = 1000;
    public static final int DEFAULT_SENT_HISTORY = 50;

    private boolean persistSentMessages = true;
    private int sentMessageHistoryLimit = DEFAULT_SENT_HISTORY;

    public boolean persistSentMessages() { return persistSentMessages; }
    public void setPersistSentMessages(boolean v) { persistSentMessages = v; }

    public int sentMessageHistoryLimit() { return (int) clamp(sentMessageHistoryLimit, MIN_SENT_HISTORY, MAX_SENT_HISTORY); }
    public void setSentMessageHistoryLimit(int v) { sentMessageHistoryLimit = (int) clamp(v, MIN_SENT_HISTORY, MAX_SENT_HISTORY); }

    public void resetSentHistoryDefaults() {
        persistSentMessages = true;
        sentMessageHistoryLimit = DEFAULT_SENT_HISTORY;
    }

    public static final int MIN_CHAT_HISTORY = 10;
    public static final int MAX_CHAT_HISTORY = 100000;
    public static final int DEFAULT_CHAT_HISTORY = 1000;

    private boolean persistChat = true;
    private int chatHistoryLimit = DEFAULT_CHAT_HISTORY;
    private boolean persistTabs = true;

    public boolean persistChat() { return persistChat; }
    public void setPersistChat(boolean v) { persistChat = v; }

    public int chatHistoryLimit() { return (int) clamp(chatHistoryLimit, MIN_CHAT_HISTORY, MAX_CHAT_HISTORY); }
    public void setChatHistoryLimit(int v) { chatHistoryLimit = (int) clamp(v, MIN_CHAT_HISTORY, MAX_CHAT_HISTORY); }

    public boolean persistTabs() { return persistTabs; }
    public void setPersistTabs(boolean v) { persistTabs = v; }

    public void resetChatPersistenceDefaults() {
        persistChat = true;
        chatHistoryLimit = DEFAULT_CHAT_HISTORY;
        persistTabs = true;
    }

    public void resetAllDefaults() {
        resetHudDefaults();
        resetAppearanceDefaults();
        resetWhisperDetectionDefaults();
        resetSentHistoryDefaults();
        resetChatPersistenceDefaults();
    }

    public void resetWhisperDetectionDefaults() {
        whisperCommand = DEFAULT_WHISPER_COMMAND;
        whisperAliases = new ArrayList<>(DEFAULT_WHISPER_ALIASES);
        timestampsEnabled = true;
        timestampStyle = "Flexible (recommended)";
        headsEnabled = true;
        headStyle = "[anything]";
        playerNamePattern = DEFAULT_PLAYER_NAME;
        whisperFormats = new ArrayList<>(DEFAULT_WHISPER_FORMATS);
    }

    public String buildTimestampFragment() {
        if (!timestampsEnabled) return "";
        String style = timestampStyle();
        return switch (style) {
            case "None" -> "";
            case "[HH:MM:SS]" -> "(?:\\[\\d{1,2}:\\d{2}:\\d{2}\\]\\s*)?";
            case "[HH:MM]" -> "(?:\\[\\d{1,2}:\\d{2}\\]\\s*)?";
            case "<HH:MM:SS>" -> "(?:<\\d{1,2}:\\d{2}:\\d{2}>\\s*)?";
            case "<HH:MM>" -> "(?:<\\d{1,2}:\\d{2}>\\s*)?";
            case "(HH:MM:SS)" -> "(?:\\(\\d{1,2}:\\d{2}:\\d{2}\\)\\s*)?";
            case "HH:MM:SS" -> "(?:\\d{1,2}:\\d{2}:\\d{2}\\s+)?";
            default -> "(?:[\\[<]\\d{1,2}:\\d{2}(?::\\d{2})?[\\]>]\\s*)?";
        };
    }

    public String buildHeadFragment() {
        if (!headsEnabled) return "";
        String style = headStyle();
        String name = playerNamePattern();
        return switch (style) {
            case "None" -> "";
            case "[PLAYER head]" -> "(?:\\[" + name + "\\s+head\\]\\s*)?";
            case "[PLAYER]" -> "(?:\\[" + name + "\\]\\s*)?";
            case "[PLAYER*]" -> "(?:\\[" + name + "[^\\]]*\\]\\s*)?";
            default -> "(?:\\[[^\\]]{1,48}\\]\\s*)?";
        };
    }

    public List<String> buildIncomingRegexes() {
        String ts = buildTimestampFragment();
        String head = buildHeadFragment();
        String icon = ICON_PREFIX;
        String name = "(" + playerNamePattern() + ")";
        String prefix = "^" + ts + head + icon;
        List<String> result = new ArrayList<>();
        for (String fmt : whisperFormats) {
            if (fmt == null || fmt.isBlank()) continue;
            String pattern = fmt.replace("{player}", name).replace("{message}", "(.*)");
            pattern = escapeLiterals(pattern, name, "(.*)");
            result.add(prefix + pattern + "$");
        }
        return result;
    }

    public String buildSelfEchoRegex() {
        String ts = buildTimestampFragment();
        String head = buildHeadFragment();
        String icon = ICON_PREFIX;
        String name = "(" + playerNamePattern() + ")";
        return "^" + ts + head + icon + "(?:You|you)\\s+whispers?(?:ed)?\\s+to\\s+"
                + icon + name + "\\s*:?[ \\u00a0]*(.*)$";
    }

    private static String escapeLiterals(String template, String nameGroup, String msgGroup) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            if (template.startsWith(nameGroup, i)) {
                sb.append(nameGroup);
                i += nameGroup.length();
            } else if (template.startsWith(msgGroup, i)) {
                sb.append(msgGroup);
                i += msgGroup.length();
            } else {
                char c = template.charAt(i);
                if ("\\.^$|?*+()[]{}".indexOf(c) >= 0) sb.append('\\');
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
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
