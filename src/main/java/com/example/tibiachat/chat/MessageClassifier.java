package com.example.tibiachat.chat;

import com.example.tibiachat.config.TibiaChatConfig;
import com.mojang.authlib.GameProfile;
import net.minecraft.text.Text;
import java.util.*;
import java.util.regex.*;

public final class MessageClassifier {
    private final TibiaChatConfig config;
    private static final List<Pattern> INCOMING = List.of(
        Pattern.compile("^\\[([^\\]]+)\\s*->\\s*(?:You|you)\\]\\s*:?[ \\u00a0]*(.*)$", Pattern.CASE_INSENSITIVE|Pattern.DOTALL),
        Pattern.compile("^([^:]{1,40})\\s+whispers?(?: to you)?\\s*:?\\s*(.*)$", Pattern.CASE_INSENSITIVE|Pattern.DOTALL),
        Pattern.compile("^\\[(?:PM|MSG|WHISPER)\\]\\s*([^:]{1,40})\\s*:\\s*(.*)$", Pattern.CASE_INSENSITIVE|Pattern.DOTALL)
    );
    public MessageClassifier(TibiaChatConfig config){this.config=config;}

    public Classification incoming(Text message, GameProfile sender) {
        String raw=message.getString();
        for(Pattern p:INCOMING){
            Matcher m=p.matcher(raw);
            if(m.matches()){
                String name=m.group(1).trim();
                if(sender!=null && sender.name()!=null && sender.name().equalsIgnoreCase(name))
                    return whisper(name,sender.id(),m.group(2));
                if(sender==null || sender.name()==null)
                    return whisper(name,null,m.group(2));
            }
        }
        // Server-specific regexes are user-configurable: name group 1, body group 2.
        for(String regex:config.incomingWhisperRegexes()){
            try {
                Matcher m=Pattern.compile(regex,Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(raw);
                if(m.matches()){
                    String name=m.group(1).trim();
                    UUID id=sender!=null && sender.name()!=null && sender.name().equalsIgnoreCase(name)?sender.id():null;
                    return whisper(name,id,m.groupCount()>=2?m.group(2):raw);
                }
            } catch(PatternSyntaxException ignored){}
        }
        if(sender!=null) return new Classification(MessageType.PUBLIC,sender.name(),sender.id(),raw,null);
        // No attached player identity (this is how most servers deliver /w, /msg, /tell,
        // system broadcasts, join/leave messages, etc. via ClientReceiveMessageEvents.GAME)
        // and none of the whisper patterns above matched, so treat it as a plain system line.
        return new Classification(MessageType.SYSTEM,null,null,raw,null);
    }

    private Classification whisper(String name, UUID id, String body){
        String key=id!=null?id.toString():name.toLowerCase(Locale.ROOT);
        return new Classification(MessageType.WHISPER_INCOMING,name,id,body,key);
    }

    public Optional<OutgoingWhisper> outgoingCommand(String command){
        String c=command.trim();
        if(c.startsWith("/")) c=c.substring(1);
        String[] parts=c.split("\\s+",3);
        if(parts.length<2) return Optional.empty();
        String base=parts[0].toLowerCase(Locale.ROOT);
        if(!config.isWhisperCommand(base)) return Optional.empty();
        String name=parts[1];
        String body=parts.length==3?parts[2]:"";
        if(body.isBlank()) return Optional.empty();
        return Optional.of(new OutgoingWhisper(name,body));
    }
    public record OutgoingWhisper(String playerName,String body){}
}
