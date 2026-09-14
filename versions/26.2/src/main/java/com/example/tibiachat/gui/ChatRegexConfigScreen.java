package com.example.tibiachat.gui;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.config.TibiaChatConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Lets people change the whisper command / aliases and the private-message
 * detection regexes from in-game, instead of hand-editing the config JSON.
 */
public class ChatRegexConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 22;
    private static final int TOP_Y = 40;
    private static final int LABEL_WIDTH = 130;
    private static final int REMOVE_SIZE = 20;
    private static final int BOTTOM_RESERVED = 90;

    private final Screen parent;
    private final TibiaChatConfig config = TibiaChatTabsClient.CONFIG;

    private final List<String> workingRegexes = new ArrayList<>();
    private int scrollIndex = 0;

    private EditBox whisperCommandBox;
    private EditBox aliasesBox;
    private int listTop;
    private int listBottom;

    public ChatRegexConfigScreen(Screen parent) {
        super(Component.literal("Private Message Detection"));
        this.parent = parent;
        this.workingRegexes.addAll(config.incomingWhisperRegexes());
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int fieldWidth = 220;
        int y = TOP_Y;

        int cmdLabelX = centerX - (LABEL_WIDTH + fieldWidth) / 2;
        int cmdFieldX = cmdLabelX + LABEL_WIDTH;

        whisperCommandBox = new EditBox(this.font, cmdFieldX, y, fieldWidth, 20, Component.literal("Whisper command"));
        whisperCommandBox.setValue(config.whisperCommand());
        whisperCommandBox.setResponder(text -> {
            if (!text.isBlank()) {
                config.setWhisperCommand(text.trim());
                config.save();
            }
        });
        this.addRenderableWidget(whisperCommandBox);
        y += ROW_HEIGHT + 6;

        aliasesBox = new EditBox(this.font, cmdFieldX, y, fieldWidth, 20, Component.literal("Aliases"));
        aliasesBox.setValue(String.join(", ", config.whisperAliases()));
        aliasesBox.setResponder(text -> {
            List<String> aliases = List.of(text.split(","));
            config.setWhisperAliases(aliases);
            config.save();
        });
        this.addRenderableWidget(aliasesBox);
        y += ROW_HEIGHT + 14;

        listTop = y;
        listBottom = this.height - BOTTOM_RESERVED;

        int visibleRows = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
        int maxScroll = Math.max(0, workingRegexes.size() - visibleRows);
        scrollIndex = Math.max(0, Math.min(scrollIndex, maxScroll));

        int rowFieldWidth = 280;
        int rowX = centerX - (rowFieldWidth + REMOVE_SIZE + 6) / 2;
        int removeX = rowX + rowFieldWidth + 6;

        int rowY = listTop;
        int lastIndexShown = Math.min(workingRegexes.size(), scrollIndex + visibleRows);

        for (int i = scrollIndex; i < lastIndexShown; i++) {
            final int index = i;
            EditBox row = new EditBox(this.font, rowX, rowY, rowFieldWidth, 20,
                    Component.literal("Regex " + (index + 1)));
            row.setMaxLength(300);
            row.setValue(workingRegexes.get(index));
            row.setResponder(text -> {
                if (index < workingRegexes.size()) {
                    workingRegexes.set(index, text);
                    applyRegexes();
                }
            });
            this.addRenderableWidget(row);

            this.addRenderableWidget(Button.builder(Component.literal("x"), btn -> {
                        if (index < workingRegexes.size()) {
                            workingRegexes.remove(index);
                            applyRegexes();
                            this.rebuildWidgets();
                        }
                    })
                    .bounds(removeX, rowY, REMOVE_SIZE, 20)
                    .build());

            rowY += ROW_HEIGHT;
        }

        int bottomY = this.height - BOTTOM_RESERVED + 12;

        this.addRenderableWidget(Button.builder(Component.literal("+ Add Rule"), btn -> {
                    workingRegexes.add("");
                    applyRegexes();
                    scrollIndex = Math.max(0, workingRegexes.size() - visibleRows);
                    this.rebuildWidgets();
                })
                .bounds(centerX - 160, bottomY, 150, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("Reset to Defaults"), btn -> {
                    config.resetWhisperDetectionDefaults();
                    workingRegexes.clear();
                    workingRegexes.addAll(config.incomingWhisperRegexes());
                    whisperCommandBox.setValue(config.whisperCommand());
                    aliasesBox.setValue(String.join(", ", config.whisperAliases()));
                    scrollIndex = 0;
                    this.rebuildWidgets();
                })
                .bounds(centerX + 10, bottomY, 150, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> this.onClose())
                .bounds(centerX - 100, this.height - 28, 200, 20)
                .build());
    }

    private void applyRegexes() {
        config.setIncomingWhisperRegexes(workingRegexes.stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList()));
        config.save();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseY >= listTop && mouseY < listBottom) {
            int visibleRows = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
            int maxScroll = Math.max(0, workingRegexes.size() - visibleRows);
            int newScroll = Math.max(0, Math.min(maxScroll, scrollIndex - (int) Math.signum(verticalAmount)));
            if (newScroll != scrollIndex) {
                scrollIndex = newScroll;
                this.rebuildWidgets();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.text(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);

        int centerX = this.width / 2;
        int fieldWidth = 220;
        int labelX = centerX - (LABEL_WIDTH + fieldWidth) / 2;

        context.text(this.font, "Whisper command:", labelX, TOP_Y + 6, 0xFFFFFFFF, true);
        context.text(this.font, "Aliases (comma-sep):", labelX, TOP_Y + ROW_HEIGHT + 6 + 6, 0xFFFFFFFF, true);
        context.text(this.font, "Whisper detection regexes (scroll to see more):",
                centerX, listTop - 12, 0xFFFFFFFF);

        if (workingRegexes.isEmpty()) {
            context.text(this.font, "No rules yet - click + Add Rule", centerX, listTop + 4, 0xFFAAAAAA);
        }
    }

    @Override
    public void onClose() {
        config.save();
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(parent);
        }
    }
}
