package com.example.tibiachat.mixin;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.chat.Conversation;
import com.example.tibiachat.chat.ConversationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces the old "swap in a whole custom Screen" approach. Vanilla ChatScreen and
 * ChatHud do all the real work (rendering, scrollback, PgUp/PgDn history, text input,
 * compatibility with any other chat-formatting mod); we only draw a thin tab strip on
 * top, route clicks on it, and - since vanilla has no idea what a "whisper tab" is -
 * reroute Enter-to-send while one is active into a /w command.
 */
@Mixin(ChatScreen.class)
public abstract class ChatScreenTabBarMixin {

    // Vanilla ChatScreen's own input widget. Same field the old TibiaChatScreen reused
    // directly (it extended ChatScreen); we just need to @Shadow it here instead.
    @Shadow protected TextFieldWidget chatField;

    private static final int TAB_H = 16;
    private static final int TAB_W_MAIN = 50;
    private static final int TAB_W_MIN = 60;
    private static final int TAB_W_MAX = 120;

    @Inject(method = "render", at = @At("TAIL"))
    private void tibiaChatTabs$drawTabs(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        int top = screenH - 14 - TAB_H - 2;
        int bottom = top + TAB_H;

        ctx.fill(0, top, screenW, bottom, 0xB0101010);

        int x = 2;
        x = tibiaChatTabs$drawTab(ctx, mc, "Main", ConversationManager.MAIN, x, TAB_W_MAIN, top, bottom, mouseX, mouseY, 0);

        for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
            int w = tibiaChatTabs$tabWidth(mc, c.playerName());
            if (x + w > screenW - 4) break;
            x = tibiaChatTabs$drawTab(ctx, mc, c.playerName(), c.key(), x, w, top, bottom, mouseX, mouseY, c.unread());
        }
    }

    private int tibiaChatTabs$tabWidth(MinecraftClient mc, String label) {
        return Math.max(TAB_W_MIN, Math.min(TAB_W_MAX, mc.textRenderer.getWidth(label) + 24));
    }

    private int tibiaChatTabs$drawTab(DrawContext ctx, MinecraftClient mc, String label, String key,
                                       int x, int w, int top, int bottom, int mouseX, int mouseY, int unread) {
        boolean selected = TibiaChatTabsClient.CHAT.selectedKey().equals(key);
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= top && mouseY < bottom;

        ctx.fill(x, top, x + w - 1, bottom, selected ? 0xFF3A3A3A : (hover ? 0xFF303030 : 0xFF202020));

        String shown = label;
        if (unread > 0) shown = shown + " (" + unread + ")";
        if (mc.textRenderer.getWidth(shown) > w - 8) {
            shown = mc.textRenderer.trimToWidth(shown, w - 12) + "…";
        }
        ctx.drawTextWithShadow(mc.textRenderer, Text.literal(shown), x + 5, top + 4,
            unread > 0 ? 0xFFFFD24A : 0xFFFFFFFF);

        return x + w;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void tibiaChatTabs$onClick(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int top = screenH - 14 - TAB_H - 2;
        int bottom = top + TAB_H;

        if (click.y() < top || click.y() >= bottom) return;

        int x = 2;
        if (click.x() >= x && click.x() < x + TAB_W_MAIN) {
            tibiaChatTabs$select(ConversationManager.MAIN);
            cir.setReturnValue(true);
            return;
        }
        x += TAB_W_MAIN;

        for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
            int w = tibiaChatTabs$tabWidth(mc, c.playerName());
            if (x + w > screenW - 4) break;
            if (click.x() >= x && click.x() < x + w) {
                tibiaChatTabs$select(c.key());
                cir.setReturnValue(true);
                return;
            }
            x += w;
        }
    }

    /**
     * Vanilla ChatScreen has no concept of "this input goes to a specific whisper
     * target" - it always sends the field's text as plain chat (or a command, if it
     * starts with "/"). While a whisper tab is selected we intercept Enter ourselves
     * and send "/w <name> <text>" instead, then swallow the keypress so vanilla never
     * gets a chance to also send it as a public message.
     */
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void tibiaChatTabs$onKey(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (input.key() != GLFW.GLFW_KEY_ENTER && input.key() != GLFW.GLFW_KEY_KP_ENTER) return;

        String key = TibiaChatTabsClient.CHAT.selectedKey();
        if (ConversationManager.MAIN.equals(key)) return; // let vanilla handle the normal chat tab as-is

        Conversation c = TibiaChatTabsClient.CHAT.conversations().getByKey(key);
        if (c == null || chatField == null) return;

        String text = chatField.getText().trim();
        if (text.isEmpty()) {
            cir.setReturnValue(true);
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String whisperCmd = TibiaChatTabsClient.CONFIG.whisperCommand(); // e.g. "/w"
        String body = whisperCmd + " " + c.playerName() + " " + text;
        String command = body.startsWith("/") ? body.substring(1) : body;

        mc.player.networkHandler.sendChatCommand(command);

        chatField.setText("");
        cir.setReturnValue(true); // consumed - don't let vanilla also send this as public chat
    }

    /** Selects a tab and repaints the vanilla ChatHud from stored history for that tab. */
    private void tibiaChatTabs$select(String key) {
        TibiaChatTabsClient.CHAT.select(key);
        ChatHud hud = MinecraftClient.getInstance().inGameHud.getChatHud();
        hud.clear(false); // false = keep the up-arrow command history, just clear visible lines
        for (var msg : TibiaChatTabsClient.CHAT.selectedMessages()) {
            hud.addMessage(msg.component());
        }
    }
}