package com.example.tibiachat.chat;

import java.util.*;

public final class ConversationManager {
    public static final String MAIN = "main";
    private final Map<String,Conversation> conversations = new LinkedHashMap<>();
    public ConversationManager(){ }

    /**
     * IMPORTANT: the key is always derived from the lowercased player name, never from
     * the UUID. Incoming whispers usually arrive with no resolvable GameProfile/UUID
     * (servers deliver /w output as unsigned system messages), while outgoing whispers
     * may resolve a UUID via the tab-list. If the key depended on whether a UUID was
     * available, the same player could end up with two different conversations - one
     * for messages received from them, another for messages sent to them.
     */
    public Conversation getOrCreate(String name, UUID uuid){
        String key = name.toLowerCase(Locale.ROOT);
        Conversation c=conversations.get(key);
        if(c==null){ c=new Conversation(key,name,uuid); conversations.put(key,c); }
        else c.updateIdentity(name,uuid);
        return c;
    }
    public Conversation getByKey(String key){return conversations.get(key);}
    public Collection<Conversation> all(){return Collections.unmodifiableCollection(conversations.values());}
    public boolean isEmpty(){return conversations.isEmpty();}
}
