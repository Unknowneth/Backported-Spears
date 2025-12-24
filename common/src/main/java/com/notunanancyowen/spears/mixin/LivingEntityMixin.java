package com.notunanancyowen.spears.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.components.KineticWeapon;
import com.notunanancyowen.spears.components.SwingAnimation;
import com.notunanancyowen.spears.components.UseEffects;
import com.notunanancyowen.spears.dataholders.SpearUser;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements SpearUser {
    @Unique private long spears$lastKineticAttackTime = -2147483648L;
    @Unique @Nullable private Object2LongMap<Entity> spears$piercingCooldowns;
    @Shadow public abstract ItemStack getMainHandStack();
    @Shadow public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);
    @Shadow @Nullable public abstract StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect);
    @Shadow public abstract ItemStack getActiveItem();
    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    private void overrideSwingDurationForSpear(CallbackInfoReturnable<Integer> cir) {
        if(getMainHandStack().get(Spears.SWING_ANIMATION) instanceof SwingAnimation s) {
            LivingEntity me = (LivingEntity)(Object)this;
            int swing = s.swingTicks();
            if (StatusEffectUtil.hasHaste(me)) cir.setReturnValue(swing - (1 + StatusEffectUtil.getHasteAmplifier(me)));
            else cir.setReturnValue(hasStatusEffect(StatusEffects.MINING_FATIGUE) ? swing + (1 + Objects.requireNonNull(getStatusEffect(StatusEffects.MINING_FATIGUE)).getAmplifier()) * 2 : swing);
        }
    }
    @Inject(method = "setCurrentHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;)V"))
    private void recordPiercingCooldowns(Hand hand, CallbackInfo ci) {
        if(getActiveItem().get(Spears.KINETIC_WEAPON) instanceof KineticWeapon k) {
            spears$piercingCooldowns = new Object2LongOpenHashMap<>();
            k.playSound((LivingEntity)(Object)this);
        }
    }
    @WrapOperation(method = "setCurrentHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;)V"))
    private void suppressSoundOnStart(LivingEntity instance, RegistryEntry<GameEvent> registryEntry, Operation<Void> original) {
        if(instance.getActiveItem().get(Spears.USE_EFFECTS) instanceof UseEffects u && !u.interactVibrations()) return;
        original.call(instance, registryEntry);
    }
    @WrapOperation(method = "clearActiveItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;)V"))
    private void suppressSoundOnStop(LivingEntity instance, RegistryEntry<GameEvent> registryEntry, Operation<Void> original) {
        if(instance.getActiveItem().get(Spears.USE_EFFECTS) instanceof UseEffects u && !u.interactVibrations()) return;
        original.call(instance, registryEntry);
    }
    @Inject(method = "clearActiveItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setLivingFlag(IZ)V"))
    private void forgetPiercingCooldowns(CallbackInfo ci) {
        spears$piercingCooldowns = null;
    }
    @Inject(method = "handleStatus", at = @At("HEAD"))
    private void handleKineticAttack(byte status, CallbackInfo ci) {
        if(status == 2) spears$lastKineticAttackTime = ((LivingEntity)(Object)this).getEntityWorld().getTime();
    }
    @Inject(method = "tick", at = @At("TAIL"))
    private void tickPiercingCooldowns(CallbackInfo ci) {
        if(!((LivingEntity)(Object)this).getEntityWorld().isClient() && spears$piercingCooldowns != null) for(Entity e : spears$piercingCooldowns.keySet()) {
            long l = spears$piercingCooldowns.getLong(e);
            if(l > 1) spears$piercingCooldowns.replace(e, l - 1);
            else spears$piercingCooldowns.remove(e, l);
        }
    }
    @SuppressWarnings("all")
    @Override public float getTimeSinceLastKineticAttack(float tickProgress) {
        LivingEntity me = (LivingEntity)(Object)this;
        return this.spears$lastKineticAttackTime < 0L ? 0.0F : (float)(me.getEntityWorld().getTime() - spears$lastKineticAttackTime) + tickProgress;
    }
    @SuppressWarnings("all")
    @Override public boolean isInPiercingCooldown(Entity target, int cooldownTicks) {
        if (this.spears$piercingCooldowns == null) return false;
        LivingEntity me = (LivingEntity)(Object)this;
        return this.spears$piercingCooldowns.containsKey(target) ? me.getEntityWorld().getTime() - this.spears$piercingCooldowns.getLong(target) < cooldownTicks : false;
    }
    @SuppressWarnings("all")
    @Override public void startPiercingCooldown(Entity target) {
        if (this.spears$piercingCooldowns != null) {
            LivingEntity me = (LivingEntity)(Object)this;
            this.spears$piercingCooldowns.put(target, me.getEntityWorld().getTime());
        }
    }
    @SuppressWarnings("all")
    @Override public int countSpearedMobs() {
        return this.spears$piercingCooldowns == null ? 0 : this.spears$piercingCooldowns.size();
    }
    @SuppressWarnings("all")
    @Override public boolean pierce(EquipmentSlot slot, Entity target, float damage, boolean dealDamage, boolean knockback, boolean dismount) {
        LivingEntity attacker = (LivingEntity)(Object)this;
        if (!(attacker.getEntityWorld() instanceof ServerWorld serverWorld)) return false;
        else if(attacker instanceof PlayerEntity player) {
            ItemStack itemStack = attacker.getEquippedStack(slot);
            DamageSource damageSource = attacker.getDamageSources().playerAttack(player);
            float halfCD = player.getAttackCooldownProgress(0.5F);
            float f = halfCD * (EnchantmentHelper.getDamage(serverWorld, player.getWeaponStack(),target, damageSource, damage) - damage);
            damage *= 0.2F + halfCD * halfCD * 0.8F;
            if(knockback && target.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE) && target instanceof ProjectileEntity projectileEntity && projectileEntity.deflect(ProjectileDeflection.REDIRECTED, attacker, attacker, true)) return true;
            else {
                float g = dealDamage ? damage + f : 0.0F;
                float h = 0.0F;
                if (target instanceof LivingEntity livingEntity) h = livingEntity.getHealth();
                boolean bl = dealDamage && target.damage(damageSource, g);
                if (knockback && target instanceof LivingEntity knockable) knockable.takeKnockback(0.4F, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
                boolean bl2 = false;
                if (dismount && target.hasVehicle()) {
                    bl2 = true;
                    target.stopRiding();
                }
                if (!bl && !knockback && !bl2) return false;
                else {
                    if(EnchantmentHelper.getDamage(serverWorld, itemStack, target, damageSource, f) > f) player.addEnchantedHitParticles(target);
                    player.onAttacking(target);
                    Entity entity = target;
                    if (target instanceof EnderDragonPart) entity = ((EnderDragonPart)target).owner;
                    boolean runEnchantmentEffects = bl;
                    bl = false;
                    if (entity instanceof LivingEntity livingEntity) bl = itemStack.postHit(livingEntity, player);
                    if (runEnchantmentEffects) EnchantmentHelper.onTargetDamaged(serverWorld, target, damageSource, itemStack);
                    if (!player.getEntityWorld().isClient() && !itemStack.isEmpty() && entity instanceof LivingEntity livingEntity) {
                        if (bl) itemStack.postDamageEntity(livingEntity, player);
                        if (itemStack.isEmpty()) if (itemStack == player.getMainHandStack()) player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                        else player.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
                    }
                    if (target instanceof LivingEntity) {
                        f = h - ((LivingEntity)target).getHealth();
                        player.increaseStat(Stats.DAMAGE_DEALT, Math.round(f * 10.0F));
                        if (player.getEntityWorld() instanceof ServerWorld && f > 2.0F) {
                            int i = (int)(f * 0.5);
                            serverWorld.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.5), target.getZ(), i, 0.1, 0.0, 0.1, 0.2);
                        }
                    }
                    player.addExhaustion(0.1F);
                    return true;
                }
            }
        }
        else {
            ItemStack itemStack = attacker.getEquippedStack(slot);
            DamageSource damageSource = attacker.getDamageSources().mobAttack(attacker);
            float f = EnchantmentHelper.getDamage(serverWorld, itemStack, target, damageSource, damage);
            boolean bl2 = dealDamage && target.damage(damageSource, f);
            boolean bl = knockback | bl2;
            if (knockback && target instanceof LivingEntity knockable) knockable.takeKnockback(0.4F, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
            if (dismount && target.hasVehicle()) {
                bl = true;
                target.stopRiding();
            }
            if (bl2) EnchantmentHelper.onTargetDamaged(serverWorld, target, damageSource);
            if (!bl) return false;
            if(attacker instanceof LivingEntity livingEntity) livingEntity.onAttacking(target);
            return true;
        }
    }
}
