package com.example.tibiachat.chat;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.config.TibiaChatConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;

public final class SentMessageHistory {
    private static final Gson GSON = new GsonBuilder().create();
    private static final List<String> messages = new ArrayList<>();
    private static boolean loaded;

    private SentMessageHistory() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("tibia_chat_tabs_sent_history.json");
    }

    public static synchronized void load() {
        messages.clear();
        loaded = true;
        Path p = path();
        if (!Files.exists(p)) return;
        try {
            List<String> list = GSON.fromJson(Files.readString(p), new TypeToken<List<String>>() {}.getType());
            if (list != null) {
                for (String s : list) {
                    if (s != null && !s.isBlank()) messages.add(s);
                }
            }
            trim();
        } catch (Exception ignored) {}
    }

    public static synchronized void save() {
        if (!TibiaChatTabsClient.CONFIG.persistSentMessages()) return;
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(messages));
        } catch (Exception ignored) {}
    }

    public static synchronized void record(String message) {
        if (message == null || message.isBlank()) return;
        if (!messages.isEmpty() && messages.get(messages.size() - 1).equals(message)) return;
        messages.add(message);
        trim();
        save();
    }

    public static synchronized void syncFromChat(Collection<String> recent) {
        if (recent == null) return;
        messages.clear();
        for (String s : recent) {
            if (s != null && !s.isBlank()) messages.add(s);
        }
        trim();
        save();
    }

    public static synchronized List<String> snapshot() {
        return new ArrayList<>(messages);
    }

    public static synchronized void trim() {
        int limit = TibiaChatTabsClient.CONFIG.sentMessageHistoryLimit();
        while (messages.size() > limit) {
            messages.remove(0);
        }
    }

    public static void applyToChat() {
        if (!TibiaChatTabsClient.CONFIG.persistSentMessages()) return;
        if (!loaded) load();
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null) return;
        ChatComponent chat = mc.gui.getChat();
        if (chat == null) return;
        var recent = chat.getRecentChat();
        if (recent == null) return;
        recent.clear();
        List<String> snap = snapshot();
        recent.addAll(snap);
    }

    public static void onLimitChanged() {
        trim();
        save();
        applyToChat();
    }
}
