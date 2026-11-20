package com.example.longteleport;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record LongTeleportPayload(double distance) implements CustomPayload {

    public static final CustomPayload.Id<LongTeleportPayload> ID =
            new CustomPayload.Id<>(Identifier.of(LongTeleport.MOD_ID, "long_teleport"));

    public static final PacketCodec<RegistryByteBuf, LongTeleportPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.DOUBLE, LongTeleportPayload::distance, LongTeleportPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
