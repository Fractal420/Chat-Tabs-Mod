package com.example.tibiachat.chat;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.network.chat.Component;

public record ChatMessage(
    Component component,
    MessageType type,
    String speakerName,
    UUID speakerUuid,
    String conversationKey,
    Instant receivedAt,
    String fingerprint
) {}
