package com.example.longteleport;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundEvents;

public class LongTeleportClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> TeleportAnimationHandler.tick());

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            TeleportAnimationHandler.renderOverlay(
                    drawContext,
                    client.getWindow().getScaledWidth(),
                    client.getWindow().getScaledHeight(),
                    tickCounter.getTickProgress(true)
            );
        });

        ClientPlayNetworking.registerGlobalReceiver(LongTeleportPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    ClientPlayerEntity player = context.client().player;
                    if (player == null) return;

                    TeleportAnimationHandler.start(payload.distance());
                    playLayeredAudio(player, payload.distance());
                })
        );
    }

    private static void playLayeredAudio(ClientPlayerEntity player, double distance) {
        // Scale slightly with distance, clamped so very long trips aren't painfully loud
        float volume = (float) Math.min(1.5, 0.8 + distance / 20000.0);

        player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, volume, 0.5f);
        player.playSound(SoundEvents.BLOCK_END_PORTAL_SPAWN, volume * 0.6f, 1.6f);
        player.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, volume, 0.7f);
        player.playSound(LongTeleport.LONG_TELEPORT_SOUND, volume, 1.0f);
    }
}
