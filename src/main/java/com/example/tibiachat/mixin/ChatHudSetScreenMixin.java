package com.example.tibiachat.mixin;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.gui.TibiaChatScreen;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatHudSetScreenMixin {
    @Inject(method="setScreen", at=@At("HEAD"), cancellable=true)
    private void tibiaChatTabs$open(CallbackInfo ci){
        MinecraftClient client=MinecraftClient.getInstance();
        if(client.player==null || client.currentScreen instanceof TibiaChatScreen) return;
        ci.cancel();
        client.setScreen(new TibiaChatScreen(""));
    }
}
