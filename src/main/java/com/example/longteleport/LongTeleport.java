package com.example.longteleport;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class LongTeleport implements ModInitializer {
    public static final String MOD_ID = "longteleport";
    public static final double LONG_DISTANCE = 5000.0;

    public static final Identifier LONG_TELEPORT_SOUND_ID = Identifier.of(MOD_ID, "long_teleport");
    public static SoundEvent LONG_TELEPORT_SOUND;

    @Override
    public void onInitialize() {
        // Custom sound event; the .ogg is looked up via assets/longteleport/sounds.json
        LONG_TELEPORT_SOUND = Registry.register(
                Registries.SOUND_EVENT,
                LONG_TELEPORT_SOUND_ID,
                SoundEvent.of(LONG_TELEPORT_SOUND_ID)
        );

        // Registered on the common side so the server can send it
        PayloadTypeRegistry.playS2C().register(LongTeleportPayload.ID, LongTeleportPayload.CODEC);
    }
}
