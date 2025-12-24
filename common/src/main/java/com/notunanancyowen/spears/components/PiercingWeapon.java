package com.notunanancyowen.spears.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.dataholders.SpearUser;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.Optional;

public record PiercingWeapon(
        float hitboxMargin,
        boolean dealsKnockback,
        boolean dismounts,
        Optional<RegistryEntry<SoundEvent>> sound,
        Optional<RegistryEntry<SoundEvent>> hitSound
) {
    public static final Codec<PiercingWeapon> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(

                            Spears.rangedInclusiveFloat(0.0F, 1.0F).optionalFieldOf("hitbox_margin", 0.3F).forGetter(PiercingWeapon::hitboxMargin),
                            Codec.BOOL.optionalFieldOf("deals_knockback", true).forGetter(PiercingWeapon::dealsKnockback),
                            Codec.BOOL.optionalFieldOf("dismounts", false).forGetter(PiercingWeapon::dismounts),
                            SoundEvent.ENTRY_CODEC.optionalFieldOf("sound").forGetter(PiercingWeapon::sound),
                            SoundEvent.ENTRY_CODEC.optionalFieldOf("hit_sound").forGetter(PiercingWeapon::hitSound)
                    )
                    .apply(instance, PiercingWeapon::new)
    );
    public static final PacketCodec<RegistryByteBuf, PiercingWeapon> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT,
            PiercingWeapon::hitboxMargin,
            PacketCodecs.BOOL,
            PiercingWeapon::dealsKnockback,
            PacketCodecs.BOOL,
            PiercingWeapon::dismounts,
            SoundEvent.ENTRY_PACKET_CODEC.collect(PacketCodecs::optional),
            PiercingWeapon::sound,
            SoundEvent.ENTRY_PACKET_CODEC.collect(PacketCodecs::optional),
            PiercingWeapon::hitSound,
            PiercingWeapon::new
    );
    public void playSound(Entity entity) {
        if(entity instanceof PlayerEntity p) this.sound.ifPresent(sound -> entity.getEntityWorld().playSound(p, entity.getX(), entity.getY(), entity.getZ(), sound, entity.getSoundCategory(), 1.0F, 1.0F));
    }
    public void playHitSound(Entity entity) {
        this.hitSound.ifPresent(sound -> entity.getEntityWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, entity.getSoundCategory(), 1.0F, 1.0F));
    }
    public static boolean canHit(Entity attacker, Entity target) {
        if (target.canBeHitByProjectile() && !target.isInvulnerable() && target.isAlive()) return (!(target instanceof PlayerEntity playerEntity) || !(attacker instanceof PlayerEntity playerEntity2) || playerEntity2.shouldDamagePlayer(playerEntity)) && !attacker.isConnectedThroughVehicle(target);
        return false;
    }

    public boolean stab(LivingEntity attacker, EquipmentSlot slot) {
        float f = (float)attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        boolean bl = false;
        float minReach = 0F;
        float maxReach = 3F;
        if(attacker.getEquippedStack(slot).get(Spears.ATTACK_RANGE) instanceof AttackRange(float reachMin, float reachMax)) {
            minReach = reachMin;
            maxReach = reachMax;
        }
        if(attacker instanceof SpearUser s) for (EntityHitResult entityHitResult : Spears.collectPiercingCollisions(attacker, minReach, maxReach, this.hitboxMargin, entity -> canHit(attacker, entity))) bl |= s.pierce(slot, entityHitResult.getEntity(), f, true, this.dealsKnockback, this.dismounts);
        ItemStack stack = attacker.getEquippedStack(slot);
        if (!stack.isEmpty() && attacker.getWorld() instanceof ServerWorld server) {
            ItemEnchantmentsComponent itemEnchantmentsComponent = stack.get(DataComponentTypes.ENCHANTMENTS);
            if (itemEnchantmentsComponent != null && !itemEnchantmentsComponent.isEmpty()) {
                EnchantmentEffectContext enchantmentEffectContext = new EnchantmentEffectContext(stack, slot, attacker);
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
                    RegistryEntry<Enchantment> registryEntry = entry.getKey();
                    int lvl = EnchantmentHelper.getLevel(registryEntry, stack);
                    if (registryEntry.value().slotMatches(slot)) {
                        for (var enchantmentEffectEntry : registryEntry.value().getEffect(Spears.POST_PIERCING_ATTACK)) {
                            LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(server).add(LootContextParameters.THIS_ENTITY, attacker).add(LootContextParameters.ENCHANTMENT_LEVEL, lvl).add(LootContextParameters.ORIGIN, attacker.getPos()).build(LootContextTypes.ENCHANTED_ENTITY);
                            if (enchantmentEffectEntry.test(new LootContext.Builder(lootContextParameterSet).build(Optional.empty()))) enchantmentEffectEntry.effect().apply(server, lvl, enchantmentEffectContext, attacker, attacker.getPos());
                        }
                    }
                }
            }
        }
        if(bl) this.playHitSound(attacker);
        this.playSound(attacker);
        attacker.swingHand(Hand.MAIN_HAND, false);
        return bl;
    }
}
