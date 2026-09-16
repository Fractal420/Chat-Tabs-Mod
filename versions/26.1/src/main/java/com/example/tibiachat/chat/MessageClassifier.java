package com.example.tibiachat.chat;

import com.example.tibiachat.config.TibiaChatConfig;
import com.mojang.authlib.GameProfile;
import java.util.*;
import java.util.regex.*;
import net.minecraft.network.chat.Component;

public final class MessageClassifier {
    private final TibiaChatConfig config;

    private Pattern selfEchoPattern;
    private List<Pattern> incomingPatterns = List.of();
    private int regexCacheVersion = -1;

    public MessageClassifier(TibiaChatConfig config) {
        this.config = config;
    }

    private void ensurePatterns() {
        int version = config.regexCacheVersion();
        if (version == regexCacheVersion) {
            return;
        }
        regexCacheVersion = version;
        selfEchoPattern = null;
        incomingPatterns = List.of();

        try {
            selfEchoPattern = Pattern.compile(
                    config.buildSelfEchoRegex(),
                    Pattern.CASE_INSENSITIVE
            );
        } catch (PatternSyntaxException ignored) {}

        List<Pattern> compiled = new ArrayList<>();
        for (String regex : config.buildIncomingRegexes()) {
            try {
                compiled.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            } catch (PatternSyntaxException ignored) {}
        }
        incomingPatterns = List.copyOf(compiled);
    }

    private static boolean isPublicChatLine(String raw) {
        if (raw.indexOf('\u00BB') >= 0) return true;
        if (raw.indexOf('\u203A') >= 0) return true;
        if (raw.contains(" » ") || raw.contains(" › ") || raw.contains(" > ")) return true;
        if (raw.contains("»") || raw.contains("›")) return true;
        int arrow = raw.indexOf(" -> ");
        if (arrow > 0 && !raw.trim().startsWith("[")) return true;
        return false;
    }

    public Classification incoming(Component message, GameProfile sender) {
        String raw = ComponentJson.detectionText(message);
        ensurePatterns();

        if (selfEchoPattern != null) {
            Matcher echo = selfEchoPattern.matcher(raw);
            if (echo.matches()) {
                String name = echo.group(1).trim();
                return new Classification(
                        MessageType.WHISPER_OUTGOING,
                        name,
                        null,
                        echo.group(2),
                        name.toLowerCase(Locale.ROOT)
                );
            }
        }

        if (!isPublicChatLine(raw)) {
            for (Pattern p : incomingPatterns) {
                Matcher m = p.matcher(raw);
                if (m.matches()) {
                    String name = m.group(1).trim();
                    if (name.equalsIgnoreCase("you") || name.equalsIgnoreCase("me")) {
                        continue;
                    }
                    UUID id = sender != null
                            && sender.name() != null
                            && sender.name().equalsIgnoreCase(name)
                            ? sender.id()
                            : null;
                    return whisper(name, id, m.groupCount() >= 2 ? m.group(2) : raw);
                }
            }
        }

        if (sender != null) {
            return new Classification(MessageType.PUBLIC, sender.name(), sender.id(), raw, null);
        }
        return new Classification(MessageType.SYSTEM, null, null, raw, null);
    }

    private Classification whisper(String name, UUID id, String body) {
        String key = id != null ? id.toString() : name.toLowerCase(Locale.ROOT);
        return new Classification(MessageType.WHISPER_INCOMING, name, id, body, key);
    }

    public Optional<OutgoingWhisper> outgoingCommand(String command) {
        String c = command.trim();
        if (c.startsWith("/")) {
            c = c.substring(1);
        }
        String[] parts = c.split("\\s+", 3);
        if (parts.length < 2) {
            return Optional.empty();
        }
        String base = parts[0].toLowerCase(Locale.ROOT);
        if (!config.isWhisperCommand(base)) {
            return Optional.empty();
        }
        String name = parts[1];
        String body = parts.length == 3 ? parts[2] : "";
        if (body.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new OutgoingWhisper(name, body));
    }

    public record OutgoingWhisper(String playerName, String body) {}
}
