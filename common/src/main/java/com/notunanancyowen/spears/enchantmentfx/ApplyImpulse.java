package com.notunanancyowen.spears.enchantmentfx;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public record ApplyImpulse(Vec3d direction, Vec3d coordinateScale, EnchantmentLevelBasedValue magnitude) implements EnchantmentEntityEffect {
    public static final MapCodec<ApplyImpulse> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Vec3d.CODEC.fieldOf("direction").forGetter(ApplyImpulse::direction),
                            Vec3d.CODEC.fieldOf("coordinate_scale").forGetter(ApplyImpulse::coordinateScale),
                            EnchantmentLevelBasedValue.CODEC.fieldOf("magnitude").forGetter(ApplyImpulse::magnitude)
                    )
                    .apply(instance, ApplyImpulse::new)
    );

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        Vec3d vec3d2 = transformLocalPos(getYawAndPitch(user.getRotationVector()), this.direction).multiply(this.coordinateScale).multiply(this.magnitude.getValue(level));
        user.addVelocityInternal(vec3d2);
        user.velocityModified = true;
        user.velocityDirty = true;
    }
    private static Vec2f getYawAndPitch(Vec3d vec3d) {
        float f = (float)Math.atan2(-vec3d.x, vec3d.z) * (180.0F / (float)Math.PI);
        float g = (float)Math.asin(-vec3d.y / Math.sqrt(vec3d.x * vec3d.x + vec3d.y * vec3d.y + vec3d.z * vec3d.z)) * (180.0F / (float)Math.PI);
        return new Vec2f(g, f);

    }
    private static Vec3d transformLocalPos(Vec2f rotation, Vec3d vec) {
        float f = MathHelper.cos((rotation.y + 90.0F) * (float) (Math.PI / 180.0));
        float g = MathHelper.sin((rotation.y + 90.0F) * (float) (Math.PI / 180.0));
        float h = MathHelper.cos(-rotation.x * (float) (Math.PI / 180.0));
        float i = MathHelper.sin(-rotation.x * (float) (Math.PI / 180.0));
        float j = MathHelper.cos((-rotation.x + 90.0F) * (float) (Math.PI / 180.0));
        float k = MathHelper.sin((-rotation.x + 90.0F) * (float) (Math.PI / 180.0));
        Vec3d vec3d = new Vec3d(f * h, i, g * h);
        Vec3d vec3d2 = new Vec3d(f * j, k, g * j);
        Vec3d vec3d3 = vec3d.crossProduct(vec3d2).multiply(-1.0);
        double d = vec3d.x * vec.z + vec3d2.x * vec.y + vec3d3.x * vec.x;
        double e = vec3d.y * vec.z + vec3d2.y * vec.y + vec3d3.y * vec.x;
        double l = vec3d.z * vec.z + vec3d2.z * vec.y + vec3d3.z * vec.x;
        return new Vec3d(d, e, l);
    }
    @Override public MapCodec<ApplyImpulse> getCodec() {
        return CODEC;
    }
}
