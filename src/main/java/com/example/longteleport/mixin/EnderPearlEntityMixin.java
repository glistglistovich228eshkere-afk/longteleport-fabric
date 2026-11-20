package com.example.longteleport.mixin;

import com.example.longteleport.LongTeleport;
import com.example.longteleport.LongTeleportPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin {

    @Inject(method = "onCollision", at = @At("HEAD"))
    private void longteleport$onLongTeleport(HitResult hitResult, CallbackInfo ci) {
        EnderPearlEntity self = (EnderPearlEntity) (Object) this;

        // Only pearls thrown by a server-side player
        Entity owner = self.getOwner();
        if (!(owner instanceof ServerPlayerEntity player)) return;
        if (!(self.getEntityWorld() instanceof ServerWorld world)) return;

        // Same-dimension check
        if (player.getEntityWorld() != world) return;

        // Captured before the owner is moved by the pearl's teleport
        Vec3d originPos = player.getEntityPos();
        Vec3d hitPos = hitResult.getPos();
        double distance = originPos.distanceTo(hitPos);

        if (distance <= LongTeleport.LONG_DISTANCE) return;

        spawnSpiral(world, originPos);
        spawnSpiral(world, hitPos);

        ServerPlayNetworking.send(player, new LongTeleportPayload(distance));
    }

    /** Expanding spiral of portal and end-rod particles around a center point. */
    private static void spawnSpiral(ServerWorld world, Vec3d center) {
        final int points = 60;
        final ParticleEffect[] types = {
                ParticleTypes.REVERSE_PORTAL,
                ParticleTypes.PORTAL,
                ParticleTypes.END_ROD
        };

        for (int type = 0; type < types.length; type++) {
            double phaseOffset = type * (Math.PI * 2 / types.length);
            for (int i = 0; i < points; i++) {
                double angle = i * 0.35 + phaseOffset;
                double radius = 0.4 + i * 0.07;
                double x = center.x + Math.cos(angle) * radius;
                double y = center.y + 0.2 + i * 0.03;
                double z = center.z + Math.sin(angle) * radius;

                world.spawnParticles(types[type], x, y, z, 1, 0.05, 0.05, 0.05, 0.02);
            }
        }
    }
}
