package com.example.longteleport;

import net.minecraft.client.gui.DrawContext;

public final class TeleportAnimationHandler {

    public static final int DURATION_TICKS = 50;      // 2.5 seconds
    private static final float PEAK_FOV = 35.0f;
    private static final double MAX_INTENSITY_DISTANCE = 20000.0;

    private static int age = -1;          // -1 = inactive
    private static double intensity = 1.0;

    private TeleportAnimationHandler() {}

    public static void start(double distance) {
        age = 0;
        // Scale the effect between 0.5x and 1.0x depending on trip length
        intensity = Math.max(0.5, Math.min(1.0, distance / MAX_INTENSITY_DISTANCE));
    }

    public static boolean isActive() {
        return age >= 0;
    }

    /** Called once per client tick. */
    public static void tick() {
        if (age < 0) return;
        age++;
        if (age >= DURATION_TICKS) {
            age = -1;
        }
    }

    /** Normalized progress 0..1 including partial-tick interpolation. */
    private static float progress(float tickDelta) {
        if (age < 0) return 1.0f;
        float t = (age + tickDelta) / (float) DURATION_TICKS;
        return Math.max(0.0f, Math.min(1.0f, t));
    }

    /**
     * FOV offset: starts at +PEAK_FOV on tick 0 and eases back to 0
     * with a cubic ease-out curve. (1 - t)^3 is the remaining distance
     * under cubic ease-out, so this decays fast and settles slowly.
     */
    public static float getFovOffset(float tickDelta) {
        if (age < 0) return 0.0f;
        float t = progress(tickDelta);
        float remaining = (1.0f - t);
        return (float) (PEAK_FOV * intensity * remaining * remaining * remaining);
    }

    /** Purple/magenta radial vignette: flashes on arrival, then fades to 0 alpha. */
    public static void renderOverlay(DrawContext ctx, int width, int height, float tickDelta) {
        if (age < 0) return;

        float t = progress(tickDelta);

        float fade = (1.0f - t) * (1.0f - t) * 0.55f;
        float flash = t < 0.15f ? (1.0f - t / 0.15f) * 0.45f : 0.0f;
        float alpha = Math.max(0.0f, Math.min(1.0f, (fade + flash) * (float) intensity));
        if (alpha <= 0.001f) return;

        final int steps = 24;
        float maxInset = Math.min(width, height) * 0.5f;
        float band = maxInset / steps;
        int bw = Math.max(1, (int) Math.ceil(band));
        int rgb = 0xB000FF; // purple-magenta

        for (int i = 0; i < steps; i++) {
            int ii = (int) (i * band);
            // Stronger toward the edges, weaker toward the center
            float ring = (float) (steps - i) / steps;
            int a = (int) (alpha * ring * ring * 255.0f) & 0xFF;
            if (a == 0) continue;
            int color = (a << 24) | rgb;

            ctx.fill(ii, ii, width - ii, ii + bw, color);                           // top
            ctx.fill(ii, height - ii - bw, width - ii, height - ii, color);          // bottom
            ctx.fill(ii, ii + bw, ii + bw, height - ii - bw, color);                 // left
            ctx.fill(width - ii - bw, ii + bw, width - ii, height - ii - bw, color); // right
        }
    }
}
