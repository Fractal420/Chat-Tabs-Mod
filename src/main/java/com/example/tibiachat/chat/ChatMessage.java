package com.example.tibiachat.chat;

import net.minecraft.text.Text;
import java.time.Instant;
import java.util.UUID;

public record ChatMessage(
    Text component,
    MessageType type,
    String speakerName,
    UUID speakerUuid,
    String conversationKey,
    Instant receivedAt,
    String fingerprint
) {}
