package com.notunanancyowen.spears;

import com.notunanancyowen.spears.items.SpearItem;
import net.minecraft.util.math.MathHelper;

import java.lang.reflect.Method;
import java.util.function.Function;

public class SpearsClient {
    public static int lastUpswingTicksForBetterCombat = 0;
    public static Method getUpswingTicks = null;
    public static Function<Integer, Boolean> syncSpears = null;
    public static Function<Integer, Boolean> syncSpearsWithBetterCombat = null;
    public record holdUpAnimation(
            float raiseProgress,
            float raiseProgressStart,
            float raiseProgressMiddle,
            float raiseProgressEnd,
            float swayProgress,
            float lowerProgress,
            float raiseBackProgress,
            float swayIntensity,
            float swayScaleSlow,
            float swayScaleFast
    ) {
        public static holdUpAnimation play(SpearItem spear, float f) {
            int i = spear.delayTicks();
            int j = i + spear.getMaxDurationForDismountInTicks();
            int k = i + spear.getMaxDurationForChargeKnockbackInTicks();
            int l = i + spear.getMaxDurationForChargeDamageInTicks();
            float g = MathHelper.clamp(MathHelper.getLerpProgress(f, 0.0F, i), 0.0F, 1.0F);
            float h = MathHelper.clamp(MathHelper.getLerpProgress(g, 0.0F, 0.5F), 0.0F, 1.0F);
            float m = MathHelper.clamp(MathHelper.getLerpProgress(g, 0.5F, 0.8F), 0.0F, 1.0F);
            float n = MathHelper.clamp(MathHelper.getLerpProgress(g, 0.8F, 1.0F), 0.0F, 1.0F);
            float o = MathHelper.clamp(MathHelper.getLerpProgress(f, j, k), 0.0F, 1.0F);
            float p = MathHelper.clamp(MathHelper.getLerpProgress(f, k, l - 5), 0.0F, 1.0F);
            if (p < 0.0F) p = 0.0F;
            else if (p > 1.0F) p = 1.0F;
            else {
                double d = Math.sin((20.0 * p - 11.125) * (float) Math.PI * 4.0F / 9.0F);
                p = p < 0.5F ? (float)(-(Math.pow(2.0, 20.0 * p - 10.0) * d) / 2.0) : (float)(Math.pow(2.0, -20.0 * p + 10.0) * d / 2.0 + 1.0);
            }
            p = 1F - p * p * p;
            float q = MathHelper.clamp(MathHelper.getLerpProgress(f, l - 5, l), 0.0F, 1.0F);
            float r = 2.0F * (float)Math.sqrt(1F + MathHelper.square(1F - o)) - 2.0F * (float)((-Math.sqrt(1.0F - o * o)) + 1.0F);
            float s = MathHelper.sin(f * 19.0F * (float) (Math.PI / 180.0));
            float t = MathHelper.sin(f * 30.0F * (float) (Math.PI / 180.0));
            float u = MathHelper.clamp((1F - f / Math.max(Math.max(j, k), l)) * 20F, 0F, 1F) * (2.9F - r);
            return new holdUpAnimation(g, h, m, n, o, p, q, r, s * u, t * u);
        }
    }
}
