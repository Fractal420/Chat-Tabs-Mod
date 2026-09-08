package com.example.tibiachat.chat;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.config.TibiaChatConfig;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class ChatManager {
    private final TibiaChatConfig config=TibiaChatTabsClient.CONFIG;
    private final MessageClassifier classifier=new MessageClassifier(config);
    private final ConversationManager conversations=new ConversationManager();
    private final List<ChatMessage> main=new ArrayList<>();
    private final Deque<PendingEcho> pendingEchoes=new ArrayDeque<>();
    private String selectedKey=ConversationManager.MAIN;
    private static final DateTimeFormatter TIME=DateTimeFormatter.ofPattern("HH:mm");

    public List<ChatMessage> main(){return Collections.unmodifiableList(main);}
    public ConversationManager conversations(){return conversations;}
    public String selectedKey(){return selectedKey;}

    public void select(String key){
        selectedKey=key;
        Conversation c=conversations.getByKey(key);
        if(c!=null)c.clearUnread();
    }
    public List<ChatMessage> selectedMessages(){
        if(ConversationManager.MAIN.equals(selectedKey)) return main;
        Conversation c=conversations.getByKey(selectedKey);
        return c==null?List.of():c.messages();
    }
    public Conversation selectedConversation(){
        return conversations.getByKey(selectedKey);
    }

    public int totalUnread(){
        int total=0;
        for(Conversation c:conversations.all()) total+=c.unread();
        return total;
    }

    public String classifyAndStore(Text message, GameProfile sender, Instant timestamp){
        String raw = message.getString();

        // Some servers echo /w as a message "from" the local player. If the text matches
        // a recently observed outgoing whisper, consume that echo before classification.
        if(sender != null && MinecraftClient.getInstance().player != null
            && sender.id().equals(MinecraftClient.getInstance().player.getUuid())
            && removeMatchingPendingBody(raw)) return null;

        Classification cl=classifier.incoming(message,sender);

        if(cl.type()==MessageType.WHISPER_OUTGOING){
            // This is the server's own confirmation/echo of a whisper WE just sent
            // (e.g. "You whisper to _gregOS: test"), not a message from anyone new.
            // onOutgoingCommand() already rendered our line into the correct tab the
            // instant we sent it, so this is purely for de-duplication. It must never
            // spawn a conversation - "You" is not a player.
            String fp=fingerprint(cl.playerName(),cl.body());
            if(removeMatchingPendingBody(raw) || removeMatchingPending(fp)) return null;
            // Fallback (pending queue already expired/emptied for some reason): still
            // file it into the *existing* conversation for that recipient rather than
            // dropping it or inventing a new tab.
            Conversation existing=conversations.getByKey(cl.playerName().toLowerCase(Locale.ROOT));
            if(existing==null) return null; // no known conversation to attach it to - discard safely
            ChatMessage cm=make(message,MessageType.WHISPER_OUTGOING,cl.playerName(),cl.playerUuid(),existing.key(),timestamp,fp);
            existing.add(cm);
            main.add(cm);
            return existing.key();
        }

        if(cl.type()==MessageType.WHISPER_INCOMING){
            String fp=fingerprint(cl.playerName(),cl.body());
            if(removeMatchingPending(fp)) return null; // server echo of our own outgoing message
            Conversation c=conversations.getOrCreate(cl.playerName(),cl.playerUuid());
            ChatMessage cm=make(message,MessageType.WHISPER_INCOMING,cl.playerName(),cl.playerUuid(),c.key(),timestamp,fp);
            c.add(cm);
            main.add(cm); // retain normal chat stream too
            if(!c.key().equals(selectedKey)) c.markUnread(); // only "new" if you're not already looking at it
            return c.key();
        }

        ChatMessage cm=make(message,cl.type(),cl.playerName(),cl.playerUuid(),null,timestamp,
            fingerprint(cl.playerName(),raw));
        main.add(cm);
        return ConversationManager.MAIN;
    }

    public void onOutgoingCommand(String command){
        classifier.outgoingCommand(command).ifPresent(out -> {
            MinecraftClient mc=MinecraftClient.getInstance();
            if(mc.player==null)return;
            Conversation c=conversations.getOrCreate(out.playerName(),findUuid(out.playerName()));
            String fp=fingerprint(out.playerName(),out.body());
            Text rendered=Text.literal("[").append(Text.literal(TIME.format(LocalTime.now()))).append("] ")
                .append(Text.literal("You: ")).append(Text.literal(out.body()));
            ChatMessage cm=make(rendered,MessageType.WHISPER_OUTGOING,mc.player.getName().getString(),mc.player.getUuid(),c.key(),Instant.now(),fp);
            c.add(cm);
            main.add(cm);
            pendingEchoes.addLast(new PendingEcho(fp,System.nanoTime()));
            // If you're actively looking at this tab, reflect the outgoing line immediately.
            if(c.key().equals(selectedKey)){
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(cm.component());
            }
        });
    }

    private UUID findUuid(String name){
        MinecraftClient mc=MinecraftClient.getInstance();
        if(mc.player!=null && mc.player.getName().getString().equalsIgnoreCase(name)) return mc.player.getUuid();
        if(mc.getNetworkHandler()!=null){
            var p=mc.getNetworkHandler().getPlayerListEntry(name);
            if(p!=null)return p.getProfile().id();
        }
        return null;
    }
    private ChatMessage make(Text text,MessageType type,String name,UUID uuid,String key,Instant at,String fp){
        Text withTime=text;
        if(type==MessageType.WHISPER_INCOMING || type==MessageType.PUBLIC || type==MessageType.SYSTEM){
            withTime=Text.literal("[").append(Text.literal(TIME.format(at.atZone(ZoneId.systemDefault()).toLocalTime())))
                .append(Text.literal("] ")).append(text);
        }
        return new ChatMessage(withTime,type,name,uuid,key,at,fp);
    }
    private String fingerprint(String name,String body){
        return (name==null?"":name.toLowerCase(Locale.ROOT))+"|"+body.trim();
    }
    private boolean removeMatchingPendingBody(String raw){
        long now=System.nanoTime();
        while(!pendingEchoes.isEmpty() && now-pendingEchoes.peekFirst().nanoTime()>5_000_000_000L) pendingEchoes.removeFirst();
        var it=pendingEchoes.iterator();
        while(it.hasNext()){
            PendingEcho p=it.next();
            String body=p.fingerprint().substring(p.fingerprint().indexOf('|')+1);
            if(!body.isBlank() && raw.contains(body)){it.remove();return true;}
        }
        return false;
    }
    private boolean removeMatchingPending(String fp){
        long now=System.nanoTime();
        while(!pendingEchoes.isEmpty() && now-pendingEchoes.peekFirst().nanoTime()>5_000_000_000L) pendingEchoes.removeFirst();
        var it=pendingEchoes.iterator();
        while(it.hasNext()){
            PendingEcho p=it.next();
            if(p.fingerprint().equals(fp)){it.remove();return true;}
        }
        return false;
    }
    public void tick(){ /* bounded maintenance; no per-render parsing */ }
    private record PendingEcho(String fingerprint,long nanoTime){}
}
