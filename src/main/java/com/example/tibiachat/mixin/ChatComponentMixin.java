package com.example.tibiachat.mixin;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.chat.SentMessageHistory;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @ModifyVariable(method = "clearMessages", at = @At("HEAD"), argsOnly = true)
    private boolean tibiaChatTabs$keepSentHistory(boolean clearHistory) {
        if (clearHistory && TibiaChatTabsClient.CONFIG.persistSentMessages()) {
            return false;
        }
        return clearHistory;
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
}
