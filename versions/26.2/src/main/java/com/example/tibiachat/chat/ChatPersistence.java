package com.example.tibiachat.chat;

import com.example.tibiachat.TibiaChatTabsClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

public final class ChatPersistence {
    private static final Gson GSON = new GsonBuilder().create();
    private static boolean dirty;
    private static long lastSaveNanos;

    private ChatPersistence() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("tibia_chat_tabs_history.json");
    }

    public static void scheduleSave() {
        dirty = true;
    }

    public static void tick() {
        if (!dirty) return;
        long now = System.nanoTime();
        if (now - lastSaveNanos < 2_000_000_000L) return;
        save();
    }

    public static synchronized void save() {
        dirty = false;
        lastSaveNanos = System.nanoTime();
        var config = TibiaChatTabsClient.CONFIG;
        if (!config.persistChat() && !config.persistTabs()) return;

        ChatManager chat = TibiaChatTabsClient.CHAT;
        JsonObject root = new JsonObject();
        root.addProperty("selectedKey", chat.selectedKey());
        root.addProperty("version", 2);

        if (config.persistChat()) {
            root.add("main", serializeMessages(chat.main(), config.chatHistoryLimit()));
        } else {
            root.add("main", new JsonArray());
        }

        JsonArray tabs = new JsonArray();
        if (config.persistTabs()) {
            int limit = config.chatHistoryLimit();
            for (Conversation c : chat.conversations().all()) {
                JsonObject tab = new JsonObject();
                tab.addProperty("key", c.key());
                tab.addProperty("playerName", c.playerName());
                if (c.playerUuid() != null) tab.addProperty("playerUuid", c.playerUuid().toString());
                tab.addProperty("unread", c.unread());
                tab.add("messages", serializeMessages(c.messages(), limit));
                tabs.add(tab);
            }
        }
        root.add("conversations", tabs);

        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(root));
        } catch (Exception e) {
            // Persistence is best-effort; avoid crashing the client on IO errors
        }
    }

    public static synchronized void load() {
        Path p = path();
        if (!Files.exists(p)) return;
        var config = TibiaChatTabsClient.CONFIG;
        if (!config.persistChat() && !config.persistTabs()) return;

        try {
            JsonObject root = GSON.fromJson(Files.readString(p), JsonObject.class);
            if (root == null) return;

            List<ChatMessage> main = new ArrayList<>();
            if (config.persistChat() && root.has("main") && root.get("main").isJsonArray()) {
                main = deserializeMessages(root.getAsJsonArray("main"));
            }

            List<Conversation> conversations = new ArrayList<>();
            if (config.persistTabs() && root.has("conversations") && root.get("conversations").isJsonArray()) {
                for (var el : root.getAsJsonArray("conversations")) {
                    if (!el.isJsonObject()) continue;
                    JsonObject tab = el.getAsJsonObject();
                    String key = tab.has("key") ? tab.get("key").getAsString() : null;
                    String name = tab.has("playerName") ? tab.get("playerName").getAsString() : key;
                    if (key == null || key.isBlank()) continue;
                    UUID uuid = null;
                    if (tab.has("playerUuid") && !tab.get("playerUuid").isJsonNull()) {
                        try {
                            uuid = UUID.fromString(tab.get("playerUuid").getAsString());
                        } catch (Exception ignored) {}
                    }
                    Conversation c = new Conversation(key, name == null ? key : name, uuid);
                    if (tab.has("unread")) c.setUnread(tab.get("unread").getAsInt());
                    if (tab.has("messages") && tab.get("messages").isJsonArray()) {
                        c.replaceMessages(deserializeMessages(tab.getAsJsonArray("messages")));
                    }
                    c.trimTo(config.chatHistoryLimit());
                    conversations.add(c);
                }
            }

            String selected = root.has("selectedKey") ? root.get("selectedKey").getAsString() : ConversationManager.MAIN;
            TibiaChatTabsClient.CHAT.restoreState(main, conversations, selected);
        } catch (Exception ignored) {}
    }

    public static void applyToHud() {
        TibiaChatTabsClient.CHAT.refreshHud();
    }

    private static JsonArray serializeMessages(List<ChatMessage> messages, int limit) {
        JsonArray arr = new JsonArray();
        if (messages == null || messages.isEmpty()) return arr;
        int start = Math.max(0, messages.size() - limit);
        for (int i = start; i < messages.size(); i++) {
            ChatMessage m = messages.get(i);
            JsonObject o = new JsonObject();
            JsonElement componentJson = ComponentJson.toJson(m.component());
            if (componentJson != null) {
                o.add("component", componentJson);
            }
            o.addProperty("text", ComponentJson.plainText(m.component()));
            o.addProperty("type", m.type() == null ? MessageType.UNKNOWN.name() : m.type().name());
            if (m.speakerName() != null) o.addProperty("speakerName", m.speakerName());
            if (m.speakerUuid() != null) o.addProperty("speakerUuid", m.speakerUuid().toString());
            if (m.conversationKey() != null) o.addProperty("conversationKey", m.conversationKey());
            if (m.receivedAt() != null) o.addProperty("receivedAt", m.receivedAt().toString());
            if (m.fingerprint() != null) o.addProperty("fingerprint", m.fingerprint());
            arr.add(o);
        }
        return arr;
    }

    private static List<ChatMessage> deserializeMessages(JsonArray arr) {
        List<ChatMessage> list = new ArrayList<>();
        if (arr == null) return list;
        for (var el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            String text = o.has("text") ? o.get("text").getAsString() : "";
            Component component = ComponentJson.fromJson(
                    o.has("component") ? o.get("component") : null,
                    text
            );
            MessageType type = MessageType.UNKNOWN;
            if (o.has("type")) {
                try {
                    type = MessageType.valueOf(o.get("type").getAsString());
                } catch (Exception ignored) {}
            }
            String speakerName = o.has("speakerName") ? o.get("speakerName").getAsString() : null;
            UUID speakerUuid = null;
            if (o.has("speakerUuid") && !o.get("speakerUuid").isJsonNull()) {
                try {
                    speakerUuid = UUID.fromString(o.get("speakerUuid").getAsString());
                } catch (Exception ignored) {}
            }
            String conversationKey = o.has("conversationKey") && !o.get("conversationKey").isJsonNull()
                    ? o.get("conversationKey").getAsString() : null;
            Instant receivedAt = Instant.now();
            if (o.has("receivedAt")) {
                try {
                    receivedAt = Instant.parse(o.get("receivedAt").getAsString());
                } catch (Exception ignored) {}
            }
            String fingerprint = o.has("fingerprint") ? o.get("fingerprint").getAsString() : null;
            list.add(new ChatMessage(
                    component,
                    type,
                    speakerName,
                    speakerUuid,
                    conversationKey,
                    receivedAt,
                    fingerprint
            ));
        }
        return list;
    }
}
