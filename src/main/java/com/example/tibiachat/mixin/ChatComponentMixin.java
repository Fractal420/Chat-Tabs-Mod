package com.example.tibiachat.mixin;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.chat.ChatHudBuffer;
import com.example.tibiachat.chat.SentMessageHistory;
import java.util.List;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin implements ChatHudBuffer.Access {

    @Shadow
    @Final
    private List<?> allMessages;

    @Shadow
    @Final
    private List<?> trimmedMessages;

    @Unique
    private static boolean tibiaChatTabs$observing;

    @Override
    public List<?> tibiaChatTabs$allMessages() {
        return allMessages;
    }

    @Override
    public List<?> tibiaChatTabs$trimmedMessages() {
        return trimmedMessages;
    }

    @ModifyVariable(method = "clearMessages", at = @At("HEAD"), argsOnly = true)
    private boolean tibiaChatTabs$keepSentHistory(boolean clearHistory) {
        if (clearHistory && TibiaChatTabsClient.CONFIG.persistSentMessages()) {
            return false;
        }
        return clearHistory;
    }

    @ModifyConstant(method = "addRecentChat", constant = @Constant(intValue = 100))
    private int tibiaChatTabs$sentHistoryCap(int original) {
        int limit = TibiaChatTabsClient.CONFIG.sentMessageHistoryLimit();
        return Math.max(original, limit);
    }

    @ModifyConstant(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            constant = @Constant(intValue = 100),
            require = 0,
            allow = 8
    )
    private int tibiaChatTabs$chatHistoryCap(int original) {
        int limit = TibiaChatTabsClient.CONFIG.chatHistoryLimit();
        return Math.max(original, limit);
    }

    @Inject(method = "addRecentChat", at = @At("RETURN"))
    private void tibiaChatTabs$persistRecentChat(String message, CallbackInfo ci) {
        if (!TibiaChatTabsClient.CONFIG.persistSentMessages()) return;
        SentMessageHistory.record(message);
        ChatComponent self = (ChatComponent) (Object) this;
        var recent = self.getRecentChat();
        if (recent == null) return;
        int limit = TibiaChatTabsClient.CONFIG.sentMessageHistoryLimit();
        while (recent.size() > limit) {
            recent.removeFirst();
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void tibiaChatTabs$gateSimple(Component message, CallbackInfo ci) {
        tibiaChatTabs$observe(message);
        if (!TibiaChatTabsClient.CHAT.shouldAcceptHudMessage(message)) {
            ci.cancel();
        }
    }

    //#if MC >= 260100
    @Inject(method = "addClientSystemMessage", at = @At("HEAD"), cancellable = true, require = 0)
    private void tibiaChatTabs$gateSystem(Component message, CallbackInfo ci) {
        tibiaChatTabs$observe(message);
        if (!TibiaChatTabsClient.CHAT.shouldAcceptHudMessage(message)) {
            ci.cancel();
        }
    }
    //#endif


    @Unique
    private static void tibiaChatTabs$observe(Component message) {
        if (tibiaChatTabs$observing || message == null || TibiaChatTabsClient.CHAT.isWritingHud()) {
            return;
        }
        tibiaChatTabs$observing = true;
        try {
            TibiaChatTabsClient.CHAT.observe(message, null, java.time.Instant.now());
        } finally {
            tibiaChatTabs$observing = false;
        }
    }
}
