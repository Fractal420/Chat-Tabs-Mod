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
    private int tabBarOffsetY = 0;
    private int tabBarWidth = 300;
    private int notifIconOffsetX = 0;
    private int notifIconOffsetY = 0;

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

    public void resetHudDefaults() {
        tabBarScale = 1.0f;
        notifIconScale = 1.0f;
        tabBarOffsetX = 0;
        tabBarOffsetY = 0;
        tabBarWidth = 300;
        notifIconOffsetX = 0;
        notifIconOffsetY = 0;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static final int DEFAULT_TAB_BAR_COLOR = 0x101010;
    private static final int DEFAULT_TAB_BAR_ALPHA = 176; // 0xB0
    private static final int DEFAULT_TAB_COLOR = 0x202020;
    private static final int DEFAULT_TAB_ALPHA = 255;

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
        tabBarColor = DEFAULT_TAB_BAR_COLOR;
        tabBarAlpha = DEFAULT_TAB_BAR_ALPHA;
        tabColor = DEFAULT_TAB_COLOR;
        tabAlpha = DEFAULT_TAB_ALPHA;
    }

    private static final String DEFAULT_WHISPER_COMMAND = "/w";
    private static final List<String> DEFAULT_WHISPER_ALIASES =
            List.of("w", "msg", "tell", "whisper");
    private static final String ICON_PREFIX = "(?:[^\\w\\s]{1,8}\\s*)?";

    private static final List<String> DEFAULT_INCOMING_WHISPER_REGEXES = List.of(
            "^(?:\\[\\d{2}:\\d{2}\\]\\s*)?" + ICON_PREFIX + "([a-zA-Z0-9_]{2,16})\\s+whispers?(?: to you)?\\s*:?\\s*(.*)$",
            "^(?:\\[\\d{2}:\\d{2}\\]\\s*)?" + ICON_PREFIX + "([a-zA-Z0-9_]{2,16})\\s+->\\s+(?:you|You)\\s*:?\\s*(.*)$",
            "^(?:\\[\\d{2}:\\d{2}\\]\\s*)?" + ICON_PREFIX + "\\[(?:PM|MSG|WHISPER)\\]\\s*" + ICON_PREFIX + "([a-zA-Z0-9_]{2,16})\\s*:\\s*(.*)$"
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
