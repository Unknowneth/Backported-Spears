package com.notunanancyowen.spears.mixin;

import com.mojang.serialization.MapCodec;
import com.notunanancyowen.spears.enchantmentfx.ApplyExhaustion;
import com.notunanancyowen.spears.enchantmentfx.ApplyImpulse;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentEntityEffect.class)
public interface EnchantmentEntityEffectMixin {
    @Inject(method = "registerAndGetDefault", at = @At("TAIL"))
    private static void registerApplyImpulseAndExhaust(Registry<MapCodec<? extends EnchantmentEntityEffect>> registry, CallbackInfoReturnable<MapCodec<? extends EnchantmentEntityEffect>> cir) {
        Registry.register(registry, "apply_impulse", ApplyImpulse.CODEC);
        Registry.register(registry, "apply_exhaustion", ApplyExhaustion.CODEC);
    }
}
