package com.example.tibiachat.config;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;

public final class TibiaChatConfig {
    private String whisperCommand="/w";
    private List<String> whisperAliases=new ArrayList<>(List.of("w","msg","tell","whisper"));
    private List<String> incomingWhisperRegexes =
        new ArrayList<>(List.of(
                "^(.+?)\\s+whispers:\\s*:?(.*)$"
        ));
    public String whisperCommand(){return whisperCommand;}
    public List<String> incomingWhisperRegexes(){return Collections.unmodifiableList(incomingWhisperRegexes);}
    public boolean isWhisperCommand(String base){
        String configured=whisperCommand.startsWith("/")?whisperCommand.substring(1):whisperCommand;
        return base.equalsIgnoreCase(configured) || whisperAliases.stream().anyMatch(a->a.equalsIgnoreCase(base));
    }
    public void setWhisperCommand(String value){if(value!=null&&!value.isBlank())whisperCommand=value.startsWith("/")?value:"/"+value;}
    public static TibiaChatConfig load(){
        Path p=path();
        if(!Files.exists(p))return new TibiaChatConfig();
        try{return new Gson().fromJson(Files.readString(p),TibiaChatConfig.class);}
        catch(Exception e){return new TibiaChatConfig();}
    }
    public void save(){
        try{
            Files.createDirectories(path().getParent());
            Files.writeString(path(),new GsonBuilder().setPrettyPrinting().create().toJson(this));
        }catch(Exception ignored){}
    }
    private static Path path(){return FabricLoader.getInstance().getConfigDir().resolve("tibia_chat_tabs.json");}
}
