package com.example.tibiachat.mixin;

import com.example.tibiachat.gui.TibiaChatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class ChatHudSetScreenMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void tibiaChatTabs$open(Screen screen, CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        // Only intercept the vanilla chat screen being opened directly.
        // Exact class check (not instanceof) is required because
        // TibiaChatScreen itself extends ChatScreen - instanceof would
        // cause this injection to trigger again on our own screen and recurse.
        if (screen == null || screen.getClass() != ChatScreen.class) return;
        if (client.player == null) return;

        ci.cancel();
        client.setScreen(new TibiaChatScreen(""));
    }
}
