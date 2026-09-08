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

@Mixin(ChatScreen.class)
public abstract class ChatScreenTabBarMixin {

    @Shadow
    protected TextFieldWidget chatField;

    private static final int TAB_H = 16;
    private static final int TAB_W_MAIN = 50;
    private static final int TAB_W_MIN = 60;
    private static final int TAB_W_MAX = 120;
    private static final int TAB_SCROLL_STEP = 40;

    private int tibiaChatTabs$tabScroll = 0;

    @Inject(method = "render", at = @At("TAIL"))
    private void tibiaChatTabs$drawTabs(
            DrawContext ctx,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        int top = screenH - 14 - TAB_H - 2;
        int bottom = top + TAB_H;

        ctx.fill(0, top, screenW, bottom, 0xB0101010);

        int x = 2;

        x = tibiaChatTabs$drawTab(
                ctx,
                mc,
                "Main",
                ConversationManager.MAIN,
                x,
                TAB_W_MAIN,
                top,
                bottom,
                mouseX,
                mouseY,
                0
        );

        int tabsLeft = x;
        int tabsRight = screenW - 4;

        tibiaChatTabs$clampTabScroll(mc, tabsLeft, tabsRight);

        int tabX = tabsLeft - tibiaChatTabs$tabScroll;

        for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
            int w = tibiaChatTabs$tabWidth(mc, c.playerName());

            if (tabX + w <= tabsLeft) {
                tabX += w;
                continue;
            }

            if (tabX >= tabsRight) {
                break;
            }

            tibiaChatTabs$drawTab(
                    ctx,
                    mc,
                    c.playerName(),
                    c.key(),
                    tabX,
                    w,
                    top,
                    bottom,
                    mouseX,
                    mouseY,
                    c.unread()
            );

            tabX += w;
        }

        int maxScroll = tibiaChatTabs$maxTabScroll(mc, tabsLeft, tabsRight);

        if (tibiaChatTabs$tabScroll > 0) {
            ctx.fill(tabsLeft, top, tabsLeft + 8, bottom, 0xCC101010);
            ctx.drawTextWithShadow(
                    mc.textRenderer,
                    Text.literal("‹"),
                    tabsLeft + 1,
                    top + 3,
                    0xFFFFFFFF
            );
        }

        if (tibiaChatTabs$tabScroll < maxScroll) {
            ctx.fill(tabsRight - 8, top, tabsRight, bottom, 0xCC101010);
            ctx.drawTextWithShadow(
                    mc.textRenderer,
                    Text.literal("›"),
                    tabsRight - 6,
                    top + 3,
                    0xFFFFFFFF
            );
        }
    }

    private int tibiaChatTabs$tabWidth(MinecraftClient mc, String label) {
        return Math.max(
                TAB_W_MIN,
                Math.min(
                        TAB_W_MAX,
                        mc.textRenderer.getWidth(label) + 24
                )
        );
    }

    private int tibiaChatTabs$totalTabsWidth(MinecraftClient mc) {
        int width = 0;

        for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
            width += tibiaChatTabs$tabWidth(mc, c.playerName());
        }

        return width;
    }

    private int tibiaChatTabs$maxTabScroll(
            MinecraftClient mc,
            int tabsLeft,
            int tabsRight
    ) {
        int availableWidth = Math.max(0, tabsRight - tabsLeft);
        int totalWidth = tibiaChatTabs$totalTabsWidth(mc);

        return Math.max(0, totalWidth - availableWidth);
    }

    private void tibiaChatTabs$clampTabScroll(
            MinecraftClient mc,
            int tabsLeft,
            int tabsRight
    ) {
        int maxScroll = tibiaChatTabs$maxTabScroll(
                mc,
                tabsLeft,
                tabsRight
        );

        if (tibiaChatTabs$tabScroll < 0) {
            tibiaChatTabs$tabScroll = 0;
        }

        if (tibiaChatTabs$tabScroll > maxScroll) {
            tibiaChatTabs$tabScroll = maxScroll;
        }
    }

    private int tibiaChatTabs$drawTab(
            DrawContext ctx,
            MinecraftClient mc,
            String label,
            String key,
            int x,
            int w,
            int top,
            int bottom,
            int mouseX,
            int mouseY,
            int unread
    ) {
        boolean selected = TibiaChatTabsClient.CHAT.selectedKey().equals(key);

        boolean hover =
                mouseX >= x &&
                mouseX < x + w &&
                mouseY >= top &&
                mouseY < bottom;

        ctx.fill(
                x,
                top,
                x + w - 1,
                bottom,
                selected
                        ? 0xFF3A3A3A
                        : (hover ? 0xFF303030 : 0xFF202020)
        );

        String shown = label;

        if (unread > 0) {
            shown = shown + " (" + unread + ")";
        }

        if (mc.textRenderer.getWidth(shown) > w - 8) {
            shown =
                    mc.textRenderer.trimToWidth(shown, w - 12)
                            + "…";
        }

        ctx.drawTextWithShadow(
                mc.textRenderer,
                Text.literal(shown),
                x + 5,
                top + 4,
                unread > 0
                        ? 0xFFFFD24A
                        : 0xFFFFFFFF
        );

        return x + w;
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void tibiaChatTabs$onMouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        int top = screenH - 14 - TAB_H - 2;
        int bottom = top + TAB_H;

        if (mouseY < top || mouseY >= bottom) {
            return;
        }

        int tabsLeft = 2 + TAB_W_MAIN;
        int tabsRight = screenW - 4;

        int maxScroll = tibiaChatTabs$maxTabScroll(
                mc,
                tabsLeft,
                tabsRight
        );

        if (maxScroll <= 0) {
            return;
        }

        double amount =
                Math.abs(horizontalAmount) > Math.abs(verticalAmount)
                        ? horizontalAmount
                        : verticalAmount;

        if (amount == 0) {
            return;
        }

        tibiaChatTabs$tabScroll -=
                (int) Math.round(amount * TAB_SCROLL_STEP);

        tibiaChatTabs$clampTabScroll(
                mc,
                tabsLeft,
                tabsRight
        );

        cir.setReturnValue(true);
    }

    @Inject(
            method = "mouseClicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tibiaChatTabs$onClick(
            Click click,
            boolean doubled,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        int top = screenH - 14 - TAB_H - 2;
        int bottom = top + TAB_H;

        if (click.y() < top || click.y() >= bottom) {
            return;
        }

        int x = 2;

        if (
                click.x() >= x &&
                click.x() < x + TAB_W_MAIN
        ) {
            tibiaChatTabs$select(ConversationManager.MAIN);
            cir.setReturnValue(true);
            return;
        }

        int tabsLeft = x + TAB_W_MAIN;
        int tabsRight = screenW - 4;

        tibiaChatTabs$clampTabScroll(
                mc,
                tabsLeft,
                tabsRight
        );

        int tabX = tabsLeft - tibiaChatTabs$tabScroll;

        for (Conversation c :
                TibiaChatTabsClient.CHAT.conversations().all()) {

            int w =
                    tibiaChatTabs$tabWidth(
                            mc,
                            c.playerName()
                    );

            if (tabX + w <= tabsLeft) {
                tabX += w;
                continue;
            }

            if (tabX >= tabsRight) {
                break;
            }

            if (
                    click.x() >= tabX &&
                    click.x() < tabX + w
            ) {
                tibiaChatTabs$select(c.key());
                cir.setReturnValue(true);
                return;
            }

            tabX += w;
        }
    }

    private void tibiaChatTabs$select(String key) {
        TibiaChatTabsClient.CHAT.select(key);

        ChatHud hud =
                MinecraftClient.getInstance()
                        .inGameHud
                        .getChatHud();

        hud.clear(false);

        for (
                var msg :
                TibiaChatTabsClient.CHAT.selectedMessages()
        ) {
            hud.addMessage(msg.component());
        }

        tibiaChatTabs$scrollSelectedIntoView();
    }

    private void tibiaChatTabs$scrollSelectedIntoView() {
        MinecraftClient mc = MinecraftClient.getInstance();

        int screenW = mc.getWindow().getScaledWidth();

        int tabsLeft = 2 + TAB_W_MAIN;
        int tabsRight = screenW - 4;

        String selectedKey =
                TibiaChatTabsClient.CHAT.selectedKey();

        if (ConversationManager.MAIN.equals(selectedKey)) {
            tibiaChatTabs$tabScroll = 0;
            return;
        }

        int tabX = tabsLeft;

        for (
                Conversation c :
                TibiaChatTabsClient.CHAT.conversations().all()
        ) {
            int w =
                    tibiaChatTabs$tabWidth(
                            mc,
                            c.playerName()
                    );

            if (c.key().equals(selectedKey)) {
                int visibleLeft =
                        tabX - tibiaChatTabs$tabScroll;

                int visibleRight =
                        visibleLeft + w;

                if (visibleLeft < tabsLeft) {
                    tibiaChatTabs$tabScroll -=
                            tabsLeft - visibleLeft;
                } else if (visibleRight > tabsRight) {
                    tibiaChatTabs$tabScroll +=
                            visibleRight - tabsRight;
                }

                tibiaChatTabs$clampTabScroll(
                        mc,
                        tabsLeft,
                        tabsRight
                );

                return;
            }

            tabX += w;
        }
    }

    @Inject(
            method = "keyPressed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tibiaChatTabs$onKey(
            KeyInput input,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (
                input.key() != GLFW.GLFW_KEY_ENTER &&
                input.key() != GLFW.GLFW_KEY_KP_ENTER
        ) {
            return;
        }

        String key =
                TibiaChatTabsClient.CHAT.selectedKey();

        if (ConversationManager.MAIN.equals(key)) {
            return;
        }

        Conversation c =
                TibiaChatTabsClient.CHAT.conversations()
                        .getByKey(key);

        if (c == null || chatField == null) {
            return;
        }

        String text =
                chatField.getText().trim();

        if (text.isEmpty()) {
            cir.setReturnValue(true);
            return;
        }

        MinecraftClient mc =
                MinecraftClient.getInstance();

        if (mc.player == null) {
            return;
        }

        String whisperCmd =
                TibiaChatTabsClient.CONFIG.whisperCommand();

        String body =
                whisperCmd +
                " " +
                c.playerName() +
                " " +
                text;

        String command =
                body.startsWith("/")
                        ? body.substring(1)
                        : body;

        mc.player.networkHandler.sendChatCommand(command);

        chatField.setText("");

        cir.setReturnValue(true);
    }
}