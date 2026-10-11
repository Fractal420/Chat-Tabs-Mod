package com.example.tibiachat.chat;

import com.example.tibiachat.TibiaChatTabsClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;

public final class ChatHudBuffer {
    private static List<Object> mainAllSnapshot;
    private static List<Object> mainTrimmedSnapshot;
    private static final List<Component> pendingMain = new ArrayList<>();
    private static final Map<String, List<Object>> privateAll = new HashMap<>();
    private static final Map<String, List<Object>> privateTrimmed = new HashMap<>();

    private ChatHudBuffer() {}

    public static void snapshotMain() {
        ChatComponent chat = chat();
        if (chat == null) {
            mainAllSnapshot = null;
            mainTrimmedSnapshot = null;
            return;
        }
        Access access = (Access) chat;
        mainAllSnapshot = new ArrayList<>(access.tibiaChatTabs$allMessages());
        mainTrimmedSnapshot = new ArrayList<>(access.tibiaChatTabs$trimmedMessages());
        pendingMain.clear();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void restoreMain() {
        ChatComponent chat = chat();
        if (chat == null) {
            return;
        }
        Access access = (Access) chat;
        TibiaChatTabsClient.CHAT.setWritingHud(true);
        try {
            if (mainAllSnapshot != null) {
                List all = access.tibiaChatTabs$allMessages();
                List trimmed = access.tibiaChatTabs$trimmedMessages();
                all.clear();
                all.addAll(mainAllSnapshot);
                trimmed.clear();
                if (mainTrimmedSnapshot != null) {
                    trimmed.addAll(mainTrimmedSnapshot);
                }
            } else {
                chat.clearMessages(false);
            }
            for (Component pending : pendingMain) {
                chat.addMessage(pending);
            }
            pendingMain.clear();
            mainAllSnapshot = null;
            mainTrimmedSnapshot = null;
        } finally {
            TibiaChatTabsClient.CHAT.setWritingHud(false);
        }
    }

    public static void queuePending(Component message) {
        if (message != null) {
            pendingMain.add(message);
        }
    }

    public static boolean hasPrivateSnapshot(String key) {
        return key != null && privateAll.containsKey(key);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void restorePrivate(String key) {
        ChatComponent chat = chat();
        if (chat == null || key == null) {
            return;
        }
        List<Object> allSnap = privateAll.get(key);
        List<Object> trimmedSnap = privateTrimmed.get(key);
        if (allSnap == null) {
            return;
        }
        Access access = (Access) chat;
        TibiaChatTabsClient.CHAT.setWritingHud(true);
        try {
            List all = access.tibiaChatTabs$allMessages();
            List trimmed = access.tibiaChatTabs$trimmedMessages();
            all.clear();
            all.addAll(allSnap);
            trimmed.clear();
            if (trimmedSnap != null) {
                trimmed.addAll(trimmedSnap);
            }
        } finally {
            TibiaChatTabsClient.CHAT.setWritingHud(false);
        }
    }

    public static void capturePrivate(String key) {
        ChatComponent chat = chat();
        if (chat == null || key == null) {
            return;
        }
        Access access = (Access) chat;
        privateAll.put(key, new ArrayList<>(access.tibiaChatTabs$allMessages()));
        privateTrimmed.put(key, new ArrayList<>(access.tibiaChatTabs$trimmedMessages()));
    }

    public static void invalidatePrivate(String key) {
        if (key == null) {
            return;
        }
        privateAll.remove(key);
        privateTrimmed.remove(key);
    }

    private static ChatComponent chat() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null) {
            return null;
        }
        return mc.gui.getChat();
    }

    public interface Access {
        List<?> tibiaChatTabs$allMessages();

        List<?> tibiaChatTabs$trimmedMessages();
    }
}
