package com.example.tibiachat.gui;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.chat.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.*;
import net.minecraft.client.gui.Click;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public final class TibiaChatScreen extends ChatScreen {
private TextFieldWidget input;
private int panelLeft, panelTop, panelRight, panelBottom, contentTop, inputY;

private static final int TAB_H = 22;
private static final int INPUT_H = 20;
private static final int LINE_H = 10;
private static final int PAD = 6;

public TibiaChatScreen(String initialText) {
    super(initialText, false);
}

@Override
protected void init() {
    clearChildren();

    panelLeft = Math.max(8, width / 2 - 260);
    panelRight = Math.min(width - 8, width / 2 + 260);
    panelTop = Math.max(8, height / 2 - 150);
    panelBottom = Math.min(height - 8, height / 2 + 150);

    contentTop = panelTop + TAB_H + 4;
    inputY = panelBottom - INPUT_H - 6;

    input = new TextFieldWidget(
            textRenderer,
            panelLeft + 6,
            inputY,
            panelRight - panelLeft - 12,
            INPUT_H,
            Text.literal("")
    );

    input.setMaxLength(256);

    input.setPlaceholder(
            Text.literal(
                    TibiaChatTabsClient.CHAT.selectedKey().equals(ConversationManager.MAIN)
                            ? "Type message..."
                            : "Message..."
            )
    );

    addDrawableChild(input);
    setInitialFocus(input);
}


@Override
public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
    renderBackground(ctx, mouseX, mouseY, delta);

    ctx.fill(
            panelLeft,
            panelTop,
            panelRight,
            panelBottom,
            0xD0101010
    );

    ctx.fill(
            panelLeft,
            panelTop,
            panelRight,
            panelTop + TAB_H,
            0xE0202020
    );

    renderTabs(ctx, mouseX, mouseY);

    ctx.enableScissor(
            panelLeft + 2,
            contentTop,
            panelRight - 2,
            inputY - 4
    );

    renderMessages(ctx);

    ctx.disableScissor();

    ctx.fill(
            panelLeft,
            inputY - 4,
            panelRight,
            inputY - 3,
            0xFF404040
    );

    input.render(ctx, mouseX, mouseY, delta);

    renderTooltip(ctx, mouseX, mouseY);
}

private void renderTabs(
        DrawContext ctx,
        int mx,
        int my
) {
    int x = panelLeft + 2;

    drawTab(
            ctx,
            "Main",
            ConversationManager.MAIN,
            x,
            0,
            mx,
            my
    );

    x += 78;

    for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
        int w = Math.max(
                74,
                Math.min(
                        150,
                        textRenderer.getWidth(c.playerName()) + 28
                )
        );

        drawTab(
                ctx,
                c.playerName(),
                c.key(),
                x,
                w,
                mx,
                my
        );

        x += w;

        if (x > panelRight - 30) {
            break;
        }
    }
}

private void drawTab(
        DrawContext ctx,
        String label,
        String key,
        int x,
        int ignored,
        int mx,
        int my
) {
    int w = Math.max(
            74,
            Math.min(
                    150,
                    textRenderer.getWidth(label) + 28
            )
    );

    boolean selected =
            TibiaChatTabsClient.CHAT.selectedKey().equals(key);

    boolean hover =
            mx >= x &&
            mx < x + w &&
            my >= panelTop &&
            my < panelTop + TAB_H;

    ctx.fill(
            x,
            panelTop,
            x + w,
            panelTop + TAB_H,
            selected
                    ? 0xFF3A3A3A
                    : (hover ? 0xFF303030 : 0xFF252525)
    );

    String shown = label;

    // trimToWidth() returns String in the current Minecraft mappings.
    if (textRenderer.getWidth(shown) > w - 25) {
        shown = textRenderer.trimToWidth(shown, w - 31) + "…";
    }

    if (!key.equals(ConversationManager.MAIN)) {
        Conversation c =
                TibiaChatTabsClient.CHAT.conversations().getByKey(key);

        if (c != null && c.unread() > 0) {
            shown += " •" + c.unread();
        }
    }

    ctx.drawTextWithShadow(
            textRenderer,
            Text.literal(shown),
            x + 7,
            panelTop + 7,
            0xFFFFFFFF
    );
}

private void renderMessages(DrawContext ctx) {
    List<ChatMessage> msgs =
            TibiaChatTabsClient.CHAT.selectedMessages();

    int maxLines = Math.max(
            1,
            (inputY - contentTop - 4) / LINE_H
    );

    List<OrderedText> lines = new ArrayList<>();

    for (ChatMessage m : msgs) {
        lines.addAll(
                textRenderer.wrapLines(
                        m.component(),
                        panelRight - panelLeft - 2 * PAD
                )
        );
    }

    int maxScroll = Math.max(
            0,
            lines.size() - maxLines
    );

    int scroll = 0;

    Conversation c =
            TibiaChatTabsClient.CHAT.selectedConversation();

    if (c != null) {
        scroll = Math.min(c.scroll(), maxScroll);
    }

    int start = Math.max(
            0,
            lines.size() - maxLines - scroll
    );

    int y = inputY - LINE_H;

    for (
            int i = start;
            i < lines.size() && y >= contentTop;
            i++, y -= LINE_H
    ) {
        ctx.drawTextWithShadow(
                textRenderer,
                lines.get(i),
                panelLeft + PAD,
                y,
                0xFFFFFFFF
        );
    }
}

private void renderTooltip(
        DrawContext ctx,
        int mx,
        int my
) {
    if (
            my < panelTop ||
            my > panelTop + TAB_H
    ) {
        return;
    }

    int x = panelLeft + 2;

    for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
        int w = Math.max(
                74,
                Math.min(
                        150,
                        textRenderer.getWidth(c.playerName()) + 28
                )
        );

        if (
                mx >= x &&
                mx < x + w &&
                textRenderer.getWidth(c.playerName()) > w - 25
        ) {
            // drawTooltip expects List<Text>, not List<OrderedText>.
            ctx.drawTooltip(
                    textRenderer,
                    List.of(Text.literal(c.playerName())),
                    mx,
                    my
            );

            return;
        }

        x += w;
    }
}

@Override
public boolean mouseClicked(Click click, boolean doubled) {
double mx = click.x();
double my = click.y();

if (
        click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT &&
        my >= panelTop &&
        my < panelTop + TAB_H
) {
    int x = panelLeft + 2;

    if (mx >= x && mx < x + 78) {
        TibiaChatTabsClient.CHAT.select(
                ConversationManager.MAIN
        );
        syncInput();
        return true;
    }

    x += 78;

    for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
        int w = Math.max(
                74,
                Math.min(
                        150,
                        textRenderer.getWidth(c.playerName()) + 28
                )
        );

        if (mx >= x && mx < x + w) {
            TibiaChatTabsClient.CHAT.select(c.key());
            syncInput();
            return true;
        }

        x += w;
    }
}

return super.mouseClicked(click, doubled);

}

@Override
public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount
) {
    Conversation c =
            TibiaChatTabsClient.CHAT.selectedConversation();

    if (c != null) {
        c.scrollBy(
                (int) Math.signum(-verticalAmount)
        );

        return true;
    }

    return true;
}

@Override
public boolean keyPressed(
        net.minecraft.client.input.KeyInput inputEvent
) {
    if (
            inputEvent.key() == GLFW.GLFW_KEY_ENTER ||
            inputEvent.key() == GLFW.GLFW_KEY_KP_ENTER
    ) {
        String text = input.getText().trim();

        if (!text.isEmpty()) {
            send(text);
        }

        input.setText("");
        return true;
    }

    return super.keyPressed(inputEvent);
}

private void send(String text) {
    MinecraftClient mc = MinecraftClient.getInstance();

    if (mc.player == null) {
        return;
    }

    if (
            ConversationManager.MAIN.equals(
                    TibiaChatTabsClient.CHAT.selectedKey()
            )
    ) {
        if (text.startsWith("/")) {
            mc.player.networkHandler.sendChatCommand(
        text.substring(1)
            );
        } else {
            mc.player.networkHandler.sendChatMessage(
                    text
            );
        }

        return;
    }

    Conversation c =
            TibiaChatTabsClient.CHAT.selectedConversation();

    if (c == null) {
        return;
    }

    String cmd =
            TibiaChatTabsClient.CONFIG.whisperCommand();

    String body =
            cmd + " " + c.playerName() + " " + text;

    String command =
            body.startsWith("/")
                    ? body.substring(1)
                    : body;

    mc.player.networkHandler.sendChatCommand(command);

    // ClientSendMessageEvents.Command will add the
    // local outgoing entry immediately.
}

private void syncInput() {
    input.setPlaceholder(
            Text.literal(
                    TibiaChatTabsClient.CHAT.selectedKey()
                            .equals(ConversationManager.MAIN)
                            ? "Type message..."
                            : "Message..."
            )
    );

    input.setText("");
    input.setFocused(true);
}

@Override
public void close() {
    super.close();
}

}
