package com.example.tibiachat.mixin;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.chat.Conversation;
import com.example.tibiachat.chat.ConversationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces the old "swap in a whole custom Screen" approach. Vanilla ChatScreen and
 * ChatHud do all the real work (rendering, scrollback, PgUp/PgDn history, text input,
 * compatibility with any other chat-formatting mod); we only draw a thin tab strip on
 * top and route clicks on it. The actual filtering of which messages are visible per
 * tab happens in TibiaChatTabsClient via ALLOW_CHAT/ALLOW_GAME, not here.
 */
@Mixin(ChatScreen.class)
public abstract class ChatScreenTabBarMixin {

    private static final int TAB_H = 16;
    private static final int TAB_W_MAIN = 50;
    private static final int TAB_W_MIN = 60;
    private static final int TAB_W_MAX = 120;

    @Inject(method = "render", at = @At("TAIL"))
    private void tibiaChatTabs$drawTabs(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        // Sits directly above where vanilla draws its input box (bottom ~14px of screen).
        int top = screenH - 14 - TAB_H - 2;
        int bottom = top + TAB_H;

        ctx.fill(0, top, screenW, bottom, 0xB0101010);

        int x = 2;
        x = tibiaChatTabs$drawTab(ctx, mc, "Main", ConversationManager.MAIN, x, TAB_W_MAIN, top, bottom, mouseX, mouseY, 0);

        for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
            int w = tibiaChatTabs$tabWidth(mc, c.playerName());
            if (x + w > screenW - 4) break; // ran out of room; extra tabs simply won't show
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
