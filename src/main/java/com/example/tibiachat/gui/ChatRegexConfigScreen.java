package com.example.tibiachat.gui;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.config.TibiaChatConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatRegexConfigScreen extends Screen {

    private static final int ROW = 20;
    private static final int GAP = 2;
    private static final int TOP = 28;
    private static final int BOTTOM_PAD = 52;
    private static final int REMOVE_W = 18;
    private static final int LEFT_LABEL_W = 110;
    private static final int LEFT_FIELD_W = 150;
    private static final int LEFT_COL_W = LEFT_LABEL_W + 8 + LEFT_FIELD_W;

    private final Screen parent;
    private final TibiaChatConfig config = TibiaChatTabsClient.CONFIG;

    private final List<String> workingFormats = new ArrayList<>();
    private int scrollIndex = 0;

    private EditBox whisperCommandBox;
    private EditBox aliasesBox;
    private EditBox playerNameBox;
    private int listTop;
    private int listBottom;
    private int leftX;
    private int rightX;
    private int rightW;

    public ChatRegexConfigScreen(Screen parent) {
        super(Component.literal("Message Detection"));
        this.parent = parent;
        this.workingFormats.addAll(config.whisperFormats());
    }

    private void cycleTimestampStyle() {
        List<String> styles = TibiaChatConfig.TIMESTAMP_STYLES;
        int idx = styles.indexOf(config.timestampStyle());
        if (idx < 0) idx = 0;
        config.setTimestampStyle(styles.get((idx + 1) % styles.size()));
        config.save();
        this.rebuildWidgets();
    }

    private void cycleHeadStyle() {
        List<String> styles = TibiaChatConfig.HEAD_STYLES;
        int idx = styles.indexOf(config.headStyle());
        if (idx < 0) idx = 0;
        config.setHeadStyle(styles.get((idx + 1) % styles.size()));
        config.save();
        this.rebuildWidgets();
    }

    @Override
    protected void init() {
        int margin = 12;
        leftX = margin;
        int gapBetween = 16;
        rightX = leftX + LEFT_COL_W + gapBetween;
        rightW = Math.max(180, this.width - rightX - margin - REMOVE_W - 6);

        int y = TOP;
        int fieldX = leftX + LEFT_LABEL_W + 8;

        whisperCommandBox = new EditBox(this.font, fieldX, y, LEFT_FIELD_W, 18, Component.literal("cmd"));
        whisperCommandBox.setValue(config.whisperCommand());
        whisperCommandBox.setResponder(text -> {
            if (!text.isBlank()) {
                config.setWhisperCommand(text.trim());
                config.save();
            }
        });
        this.addRenderableWidget(whisperCommandBox);
        y += ROW + GAP;

        aliasesBox = new EditBox(this.font, fieldX, y, LEFT_FIELD_W, 18, Component.literal("aliases"));
        aliasesBox.setValue(String.join(", ", config.whisperAliases()));
        aliasesBox.setResponder(text -> {
            config.setWhisperAliases(List.of(text.split(",")));
            config.save();
        });
        this.addRenderableWidget(aliasesBox);
        y += ROW + GAP + 4;

        this.addRenderableWidget(Button.builder(
                Component.literal("Timestamps: " + (config.timestampsEnabled() ? "ON" : "OFF")),
                btn -> {
                    config.setTimestampsEnabled(!config.timestampsEnabled());
                    config.save();
                    this.rebuildWidgets();
                }).bounds(leftX, y, LEFT_COL_W, 18).build());
        y += ROW + GAP;

        this.addRenderableWidget(Button.builder(
                Component.literal("Format: " + config.timestampStyle()),
                btn -> cycleTimestampStyle()
        ).bounds(leftX, y, LEFT_COL_W, 18).build());
        y += ROW + GAP;

        this.addRenderableWidget(Button.builder(
                Component.literal("Heads: " + (config.headsEnabled() ? "ON" : "OFF")),
                btn -> {
                    config.setHeadsEnabled(!config.headsEnabled());
                    config.save();
                    this.rebuildWidgets();
                }).bounds(leftX, y, LEFT_COL_W, 18).build());
        y += ROW + GAP;

        this.addRenderableWidget(Button.builder(
                Component.literal("Head style: " + config.headStyle()),
                btn -> cycleHeadStyle()
        ).bounds(leftX, y, LEFT_COL_W, 18).build());
        y += ROW + GAP + 4;

        playerNameBox = new EditBox(this.font, fieldX, y, LEFT_FIELD_W, 18, Component.literal("name"));
        playerNameBox.setValue(config.playerNamePattern());
        playerNameBox.setMaxLength(80);
        playerNameBox.setResponder(text -> {
            if (!text.isBlank()) {
                config.setPlayerNamePattern(text.trim());
                config.save();
            }
        });
        this.addRenderableWidget(playerNameBox);

        listTop = TOP;
        listBottom = this.height - BOTTOM_PAD;

        int visibleRows = Math.max(1, (listBottom - listTop) / (ROW + GAP));
        int maxScroll = Math.max(0, workingFormats.size() - visibleRows);
        scrollIndex = Math.max(0, Math.min(scrollIndex, maxScroll));

        int removeX = rightX + rightW + 4;
        int rowY = listTop;
        int last = Math.min(workingFormats.size(), scrollIndex + visibleRows);

        for (int i = scrollIndex; i < last; i++) {
            final int index = i;
            EditBox row = new EditBox(this.font, rightX, rowY, rightW, 18,
                    Component.literal("f" + (index + 1)));
            row.setMaxLength(200);
            row.setValue(workingFormats.get(index));
            row.setResponder(text -> {
                if (index < workingFormats.size()) {
                    workingFormats.set(index, text);
                    applyFormats();
                }
            });
            this.addRenderableWidget(row);

            this.addRenderableWidget(Button.builder(Component.literal("x"), btn -> {
                        if (index < workingFormats.size()) {
                            workingFormats.remove(index);
                            applyFormats();
                            this.rebuildWidgets();
                        }
                    })
                    .bounds(removeX, rowY, REMOVE_W, 18)
                    .build());

            rowY += ROW + GAP;
        }

        int bottomY = this.height - BOTTOM_PAD + 10;
        int btnW = 120;
        int totalBtn = btnW * 3 + 16;
        int btnStart = Math.max(margin, (this.width - totalBtn) / 2);

        this.addRenderableWidget(Button.builder(Component.literal("+ Add Format"), btn -> {
                    workingFormats.add("{player} whispers: {message}");
                    applyFormats();
                    scrollIndex = Math.max(0, workingFormats.size() - visibleRows);
                    this.rebuildWidgets();
                })
                .bounds(btnStart, bottomY, btnW, 18)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("Reset Defaults"), btn -> {
                    config.resetWhisperDetectionDefaults();
                    workingFormats.clear();
                    workingFormats.addAll(config.whisperFormats());
                    whisperCommandBox.setValue(config.whisperCommand());
                    aliasesBox.setValue(String.join(", ", config.whisperAliases()));
                    playerNameBox.setValue(config.playerNamePattern());
                    scrollIndex = 0;
                    this.rebuildWidgets();
                })
                .bounds(btnStart + btnW + 8, bottomY, btnW, 18)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> this.onClose())
                .bounds(btnStart + (btnW + 8) * 2, bottomY, btnW, 18)
                .build());
    }

    private void applyFormats() {
        config.setWhisperFormats(workingFormats.stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList()));
        config.save();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= rightX && mouseY >= listTop && mouseY < listBottom) {
            int visibleRows = Math.max(1, (listBottom - listTop) / (ROW + GAP));
            int maxScroll = Math.max(0, workingFormats.size() - visibleRows);
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
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);

        int y = TOP;
        context.drawString(this.font, "Whisper cmd:", leftX, y + 5, 0xFFFFFFFF, true);
        y += ROW + GAP;
        context.drawString(this.font, "Aliases:", leftX, y + 5, 0xFFFFFFFF, true);
        y += ROW + GAP + 4;
        y += (ROW + GAP) * 4;
        context.drawString(this.font, "Player name:", leftX, y + 5, 0xFFFFFFFF, true);

        context.drawString(this.font, "Formats  ({player}  {message})", rightX, listTop - 12, 0xFFAAAAAA, true);

        if (workingFormats.isEmpty()) {
            context.drawString(this.font, "No formats yet", rightX, listTop + 4, 0xFFAAAAAA, true);
        }
    }

    @Override
    public void onClose() {
        config.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }
}
