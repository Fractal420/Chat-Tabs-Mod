package com.example.tibiachat;

import com.example.tibiachat.chat.ChatManager;
import com.example.tibiachat.config.TibiaChatConfig;
import com.example.tibiachat.gui.TibiaChatScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public final class TibiaChatTabsClient implements ClientModInitializer {
    public static final String MOD_ID = "tibia_chat_tabs";
    public static final TibiaChatConfig CONFIG = TibiaChatConfig.load();
    public static final ChatManager CHAT = new ChatManager();

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, timestamp) ->
            CHAT.onIncomingChat(message, sender, timestamp));
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) CHAT.onSystem(message);
        });
        ClientSendMessageEvents.COMMAND.register(CHAT::onOutgoingCommand);

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> CONFIG.save());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CHAT.tick();
            if (client.currentScreen == null) return;
        });
    }

    public static void open(MinecraftClient client) {
        if (client.player != null) {
            client.setScreen(new TibiaChatScreen(""));
        }
    }
}
