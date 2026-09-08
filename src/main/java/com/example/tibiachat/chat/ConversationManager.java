package com.example.tibiachat.chat;

import java.util.*;

public final class ConversationManager {
    public static final String MAIN = "main";
    private final Map<String, Conversation> conversations = new LinkedHashMap<>();

    public ConversationManager() {
    }

    public Conversation getOrCreate(String name, UUID uuid) {
        String key = name.toLowerCase(Locale.ROOT);
        Conversation c = conversations.get(key);
        if (c == null) {
            c = new Conversation(key, name, uuid);
            conversations.put(key, c);
        } else {
            c.updateIdentity(name, uuid);
        }
        return c;
    }

    public Conversation getByKey(String key) {
        return conversations.get(key);
    }

    public Collection<Conversation> all() {
        return Collections.unmodifiableCollection(conversations.values());
    }

    public boolean isEmpty() {
        return conversations.isEmpty();
    }

    public void remove(String key) {
        conversations.remove(key);
    }
}