package com.example.tibiachat.chat;

import com.example.tibiachat.TibiaChatTabsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class NotificationSounds {
    public static final String[] IDS = {
            "block.note_block.pling",
            "block.note_block.bell",
            "block.note_block.chime",
            "block.note_block.xylophone",
            "block.note_block.cow_bell",
            "block.note_block.bit",
            "block.amethyst_block.chime",
            "entity.experience_orb.pickup",
            "entity.player.levelup",
            "ui.toast.challenge_complete"
    };

    public static final String[] LABELS = {
            "Pling",
            "Bell",
            "Chime",
            "Xylophone",
            "Cow Bell",
            "Bit",
            "Amethyst",
            "XP Orb",
            "Level Up",
            "Challenge"
    };

    private NotificationSounds() {}

    public static int count() {
        return IDS.length;
    }

    public static String label(int index) {
        int i = clampIndex(index);
        return LABELS[i];
    }

    public static int clampIndex(int index) {
        if (index < 0) {
            return 0;
        }
        if (index >= IDS.length) {
            return IDS.length - 1;
        }
        return index;
    }

    public static int nextIndex(int index) {
        return (clampIndex(index) + 1) % IDS.length;
    }

    public static void playConfigured() {
        var config = TibiaChatTabsClient.CONFIG;
        if (!config.notifSoundEnabled()) {
            return;
        }
        float volume = config.notifSoundVolume() / 100.0f;
        if (volume <= 0.0f) {
            return;
        }
        play(config.notifSoundIndex(), volume);
    }

    public static void play(int index, float volume) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSoundManager() == null) {
            return;
        }
        int i = clampIndex(index);
        float vol = Math.max(0.0f, Math.min(1.0f, volume));
        if (vol <= 0.0f) {
            return;
        }
        SoundEvent event = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("minecraft", IDS[i]));
        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0f, vol));
    }
}
