package com.example.tibiachat.chat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public final class ComponentJson {
    private static final Gson GSON = new Gson();
    private static final Pattern HEAD_PLACEHOLDER = Pattern.compile(
            "\\[\\s*(?:unknown\\s+)?(?:player\\s+)?head\\s*\\]|\\[\\s*head\\s*\\]",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern MULTI_SPACE = Pattern.compile("[ \\t\\xA0]{2,}");

    private ComponentJson() {}

    public static HolderLookup.Provider registries() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            if (mc.level != null) return mc.level.registryAccess();
            if (mc.player != null) return mc.player.registryAccess();
            if (mc.getConnection() != null) {
                try {
                    return mc.getConnection().registryAccess();
                } catch (Exception ignored) {}
            }
        }
        return RegistryAccess.EMPTY;
    }

    private static DynamicOps<JsonElement> ops() {
        try {
            return registries().createSerializationContext(JsonOps.INSTANCE);
        } catch (Exception ignored) {
            return JsonOps.INSTANCE;
        }
    }

    public static JsonElement toJson(Component component) {
        if (component == null) return null;
        try {
            return ComponentSerialization.CODEC
                    .encodeStart(ops(), component)
                    .result()
                    .orElse(null);
        } catch (Exception ignored) {}
        try {
            JsonObject fallback = new JsonObject();
            fallback.addProperty("text", component.getString());
            return fallback;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String toJsonString(Component component) {
        JsonElement el = toJson(component);
        return el == null ? null : GSON.toJson(el);
    }

    public static Component fromJson(JsonElement element) {
        return fromJson(element, null);
    }

    public static Component fromJson(JsonElement element, String fallbackText) {
        if (element != null && !element.isJsonNull()) {
            try {
                Component parsed = ComponentSerialization.CODEC
                        .parse(ops(), element)
                        .result()
                        .orElse(null);
                if (parsed != null) return parsed;
            } catch (Exception ignored) {}
            try {
                Component parsed = ComponentSerialization.CODEC
                        .parse(JsonOps.INSTANCE, element)
                        .result()
                        .orElse(null);
                if (parsed != null) return parsed;
            } catch (Exception ignored) {}
        }
        return Component.literal(fallbackText == null ? "" : fallbackText);
    }

    public static Component fromJsonString(String json) {
        if (json == null || json.isBlank()) return Component.empty();
        try {
            return fromJson(JsonParser.parseString(json));
        } catch (Exception ignored) {
            return Component.literal(json);
        }
    }

    public static String plainText(Component component) {
        if (component == null) return "";
        try {
            return component.getString();
        } catch (Exception ignored) {
            return "";
        }
    }

    public static String detectionText(Component component) {
        return normalizeForDetection(plainText(component));
    }

    public static String normalizeForDetection(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String text = HEAD_PLACEHOLDER.matcher(raw).replaceAll("");
        text = text.replace('\u00A0', ' ');
        text = text.replace("\u200B", "");
        text = text.replace("\u200C", "");
        text = text.replace("\u200D", "");
        text = text.replace("\uFEFF", "");
        text = MULTI_SPACE.matcher(text).replaceAll(" ");
        return text.trim();
    }
}
