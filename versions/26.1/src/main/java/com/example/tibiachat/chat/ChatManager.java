package com.example.tibiachat.chat;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.config.TibiaChatConfig;
import com.example.tibiachat.chat.ChatHudBuffer;
import com.mojang.authlib.GameProfile;
import java.time.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;

public final class ChatManager {
    private final TibiaChatConfig config = TibiaChatTabsClient.CONFIG;
    private final MessageClassifier classifier = new MessageClassifier(config);
    private final ConversationManager conversations = new ConversationManager();
    private final List<ChatMessage> main = new ArrayList<>();
    private String selectedKey = ConversationManager.MAIN;
    private boolean writingHud;

    public List<ChatMessage> main() {
        return Collections.unmodifiableList(main);
    }

    public ConversationManager conversations() {
        return conversations;
    }

    public String selectedKey() {
        return selectedKey;
    }

    public boolean viewingPrivate() {
        return !ConversationManager.MAIN.equals(selectedKey);
    }

    public boolean isWritingHud() {
        return writingHud;
    }

    public void setWritingHud(boolean value) {
        writingHud = value;
    }

    public void select(String key) {
        String next = key == null ? ConversationManager.MAIN : key;
        if (next.equals(selectedKey)) {
            return;
        }

        boolean wasPrivate = viewingPrivate();
        boolean willPrivate = !ConversationManager.MAIN.equals(next);

        if (!wasPrivate && willPrivate) {
            ChatHudBuffer.snapshotMain();
            selectedKey = next;
            Conversation c = conversations.getByKey(selectedKey);
            if (c != null) {
                c.clearUnread();
                c.setScroll(0);
            }
            loadPrivateHud();
            return;
        }

        if (wasPrivate && !willPrivate) {
            selectedKey = ConversationManager.MAIN;
            ChatHudBuffer.restoreMain();
            return;
        }

        if (wasPrivate && willPrivate) {
            selectedKey = next;
            Conversation c = conversations.getByKey(selectedKey);
            if (c != null) {
                c.clearUnread();
                c.setScroll(0);
            }
            loadPrivateHud();
        }
    }

    public List<ChatMessage> selectedMessages() {
        if (ConversationManager.MAIN.equals(selectedKey)) {
            return main;
        }
        Conversation c = conversations.getByKey(selectedKey);
        return c == null ? List.of() : c.messages();
    }

    public Conversation selectedConversation() {
        return conversations.getByKey(selectedKey);
    }

    public int totalUnread() {
        int total = 0;
        for (Conversation c : conversations.all()) {
            total += c.unread();
        }
        return total;
    }

    public void observe(Component message, GameProfile sender, Instant timestamp) {
        Classification cl = classifier.incoming(message, sender);

        if (cl.type() == MessageType.WHISPER_OUTGOING || cl.type() == MessageType.WHISPER_INCOMING) {
            String fp = fingerprint(cl.playerName(), cl.body());
            Conversation c = conversations.getOrCreate(cl.playerName(), cl.playerUuid());

            if (!c.messages().isEmpty()) {
                Instant cutoff = Instant.now().minusMillis(2000);
                for (int i = c.messages().size() - 1; i >= 0; i--) {
                    ChatMessage last = c.messages().get(i);
                    if (last.receivedAt().isBefore(cutoff)) {
                        break;
                    }
                    if (fp.equals(last.fingerprint())) {
                        return;
                    }
                    if (last.type() == cl.type()
                            && last.speakerName() != null
                            && cl.playerName() != null
                            && last.speakerName().equalsIgnoreCase(cl.playerName())
                            && normalizeBody(last).equals(normalizeBody(cl.body()))) {
                        return;
                    }
                }
            }

            ChatMessage cm = make(
                message,
                cl.type(),
                cl.playerName(),
                cl.playerUuid(),
                c.key(),
                timestamp,
                fp
            );
            c.add(cm);
            c.trimTo(config.chatHistoryLimit());
            if (cl.type() == MessageType.WHISPER_INCOMING && !c.key().equals(selectedKey)) {
                c.markUnread();
            }
            ChatPersistence.scheduleSave();

            if (c.key().equals(selectedKey) && viewingPrivate() && !writingHud) {
                writingHud = true;
                try {
                    ChatComponent chat = chat();
                    if (chat != null) {
                        chat.addClientSystemMessage(message);
                        ChatHudBuffer.capturePrivate(selectedKey);
                    }
                } finally {
                    writingHud = false;
                }
            } else {
                ChatHudBuffer.invalidatePrivate(c.key());
            }
            return;
        }

        String raw = ComponentJson.detectionText(message);
        ChatMessage cm = make(
            message,
            cl.type(),
            cl.playerName(),
            cl.playerUuid(),
            null,
            timestamp,
            fingerprint(cl.playerName(), raw)
        );
        main.add(cm);
        trimMain();
        ChatPersistence.scheduleSave();
    }

    public boolean shouldAcceptHudMessage(Component message) {
        if (!viewingPrivate() || writingHud) {
            return true;
        }
        Classification cl = classifier.incoming(message, null);
        if (cl.type() == MessageType.WHISPER_OUTGOING || cl.type() == MessageType.WHISPER_INCOMING) {
            Conversation c = conversations.getByKey(selectedKey);
            if (c != null && cl.playerName() != null && cl.playerName().equalsIgnoreCase(c.playerName())) {
                return false;
            }
        }
        ChatHudBuffer.queuePending(message);
        return false;
    }

    public void onOutgoingCommand(String command) {
        classifier.outgoingCommand(command).ifPresent(out -> {
            conversations.getOrCreate(out.playerName(), findUuid(out.playerName()));
            ChatPersistence.scheduleSave();
        });
    }

    public void resetToMain() {
        if (ConversationManager.MAIN.equals(selectedKey)) {
            return;
        }
        selectedKey = ConversationManager.MAIN;
        ChatHudBuffer.restoreMain();
        ChatPersistence.scheduleSave();
    }

    public void refreshHud() {
        if (viewingPrivate()) {
            loadPrivateHud();
        } else {
            applyMainToHud();
        }
    }

    public void applyMainToHud() {
        ChatComponent chat = chat();
        if (chat == null) {
            return;
        }
        writingHud = true;
        try {
            chat.clearMessages(false);
            int limit = config.chatHistoryLimit();
            int start = Math.max(0, main.size() - limit);
            for (int i = start; i < main.size(); i++) {
                chat.addClientSystemMessage(main.get(i).component());
            }
        } finally {
            writingHud = false;
        }
    }

    private void loadPrivateHud() {
        if (ChatHudBuffer.hasPrivateSnapshot(selectedKey)) {
            ChatHudBuffer.restorePrivate(selectedKey);
            return;
        }
        ChatComponent chat = chat();
        if (chat == null) {
            return;
        }
        writingHud = true;
        try {
            chat.clearMessages(false);
            List<ChatMessage> msgs = selectedMessages();
            int limit = config.chatHistoryLimit();
            int start = Math.max(0, msgs.size() - limit);
            for (int i = start; i < msgs.size(); i++) {
                chat.addClientSystemMessage(msgs.get(i).component());
            }
            ChatHudBuffer.capturePrivate(selectedKey);
        } finally {
            writingHud = false;
        }
    }

    private ChatComponent chat() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null) {
            return null;
        }
        return mc.gui.getChat();
    }

    public void trimMain() {
        int limit = config.chatHistoryLimit();
        int excess = main.size() - limit;
        if (excess > 0) {
            main.subList(0, excess).clear();
        }
    }

    public void replaceMain(List<ChatMessage> messages) {
        main.clear();
        if (messages != null) {
            main.addAll(messages);
        }
        trimMain();
    }

    public void restoreState(List<ChatMessage> mainMessages, List<Conversation> restored, String selected) {
        main.clear();
        conversations.clear();
        if (mainMessages != null) {
            main.addAll(mainMessages);
        }
        trimMain();
        if (restored != null) {
            for (Conversation c : restored) {
                c.trimTo(config.chatHistoryLimit());
                conversations.put(c);
            }
        }
        selectedKey = ConversationManager.MAIN;
    }

    private UUID findUuid(String name) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getName().getString().equalsIgnoreCase(name)) {
            return mc.player.getUUID();
        }
        if (mc.getConnection() != null) {
            var p = mc.getConnection().getPlayerInfo(name);
            if (p != null) {
                return p.getProfile().id();
            }
        }
        return null;
    }

    private ChatMessage make(
        Component text,
        MessageType type,
        String name,
        UUID uuid,
        String key,
        Instant at,
        String fp
    ) {
        return new ChatMessage(text, type, name, uuid, key, at, fp);
    }

    private String normalizeBody(ChatMessage msg) {
        String raw = ComponentJson.detectionText(msg.component());
        int idx = raw.lastIndexOf(':');
        if (idx >= 0 && idx + 1 < raw.length()) {
            return raw.substring(idx + 1).trim();
        }
        return raw.trim();
    }

    private String normalizeBody(String body) {
        return body == null ? "" : body.trim();
    }

    private String fingerprint(String name, String body) {
        return (name == null ? "" : name.toLowerCase(Locale.ROOT)) + "|" + (body == null ? "" : body.trim());
    }

    public void tick() {
        ChatPersistence.tick();
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && (mc.screen == null || !(mc.screen instanceof net.minecraft.client.gui.screens.ChatScreen))) {
            if (!ConversationManager.MAIN.equals(selectedKey)) {
                selectedKey = ConversationManager.MAIN;
                ChatHudBuffer.restoreMain();
            }
        }
    }
}
