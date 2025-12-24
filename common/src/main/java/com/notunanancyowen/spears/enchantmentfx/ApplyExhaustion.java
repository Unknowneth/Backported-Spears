package com.notunanancyowen.spears.enchantmentfx;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record ApplyExhaustion(EnchantmentLevelBasedValue amount) implements EnchantmentEntityEffect {
    public static final MapCodec<ApplyExhaustion> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(EnchantmentLevelBasedValue.CODEC.fieldOf("amount").forGetter(ApplyExhaustion::amount))
                    .apply(instance, ApplyExhaustion::new)
    );

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        if (user instanceof PlayerEntity playerEntity) {
            playerEntity.addExhaustion(this.amount.getValue(level));
        }
    }

    @Override
    public MapCodec<ApplyExhaustion> getCodec() {
        return CODEC;
    }
}
