package com.notunanancyowen.spears.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.notunanancyowen.spears.PacketCodecHelper;
import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.dataholders.SpearUser;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public record KineticWeapon(
        float hitboxMargin,
        int contactCooldownTicks,
        int delayTicks,
        Optional<Condition> dismountConditions,
        Optional<Condition> knockbackConditions,
        Optional<Condition> damageConditions,
        float forwardMovement,
        float damageMultiplier,
        Optional<RegistryEntry<SoundEvent>> sound,
        Optional<RegistryEntry<SoundEvent>> hitSound
) {
    public static final Codec<KineticWeapon> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Spears.rangedInclusiveFloat(0.0F, 1.0F).optionalFieldOf("hitbox_margin", 0.3F).forGetter(KineticWeapon::hitboxMargin),
                            Codecs.NONNEGATIVE_INT.optionalFieldOf("contact_cooldown_ticks", 10).forGetter(KineticWeapon::contactCooldownTicks),
                            Codecs.NONNEGATIVE_INT.optionalFieldOf("delay_ticks", 0).forGetter(KineticWeapon::delayTicks),
                            Condition.CODEC.optionalFieldOf("dismount_conditions").forGetter(KineticWeapon::dismountConditions),
                            Condition.CODEC.optionalFieldOf("knockback_conditions").forGetter(KineticWeapon::knockbackConditions),
                            Condition.CODEC.optionalFieldOf("damage_conditions").forGetter(KineticWeapon::damageConditions),
                            Codec.FLOAT.optionalFieldOf("forward_movement", 0.0F).forGetter(KineticWeapon::forwardMovement),
                            Codec.FLOAT.optionalFieldOf("damage_multiplier", 1.0F).forGetter(KineticWeapon::damageMultiplier),
                            SoundEvent.ENTRY_CODEC.optionalFieldOf("sound").forGetter(KineticWeapon::sound),
                            SoundEvent.ENTRY_CODEC.optionalFieldOf("hit_sound").forGetter(KineticWeapon::hitSound)
                    )
                    .apply(instance, KineticWeapon::new)
    );
    public static final PacketCodec<RegistryByteBuf, KineticWeapon> PACKET_CODEC = PacketCodecHelper.tuple(
            PacketCodecs.FLOAT,
            KineticWeapon::hitboxMargin,
            PacketCodecs.VAR_INT,
            KineticWeapon::contactCooldownTicks,
            PacketCodecs.VAR_INT,
            KineticWeapon::delayTicks,
            Condition.PACKET_CODEC.collect(PacketCodecs::optional),
            KineticWeapon::dismountConditions,
            Condition.PACKET_CODEC.collect(PacketCodecs::optional),
            KineticWeapon::knockbackConditions,
            Condition.PACKET_CODEC.collect(PacketCodecs::optional),
            KineticWeapon::damageConditions,
            PacketCodecs.FLOAT,
            KineticWeapon::forwardMovement,
            PacketCodecs.FLOAT,
            KineticWeapon::damageMultiplier,
            SoundEvent.ENTRY_PACKET_CODEC.collect(PacketCodecs::optional),
            KineticWeapon::sound,
            SoundEvent.ENTRY_PACKET_CODEC.collect(PacketCodecs::optional),
            KineticWeapon::hitSound,
            KineticWeapon::new
    );

    public static Vec3d getAmplifiedMovement(Entity entity) {
        if (!(entity instanceof PlayerEntity) && entity.hasVehicle()) {
            entity = entity.getRootVehicle();
        }

        return (entity instanceof PlayerEntity p ? p.getMovement() : entity.getPos().subtract(entity.prevX, entity.prevY, entity.prevZ)).multiply(20.0);
    }

    public void playSound(Entity entity) {
        if(entity instanceof PlayerEntity p) this.sound.ifPresent(sound -> entity.getEntityWorld().playSound(p, entity.getX(), entity.getY(), entity.getZ(), sound, entity.getSoundCategory(), 1.0F, 1.0F));
    }

    public void playHitSound(Entity entity) {
        this.hitSound.ifPresent(sound -> entity.getEntityWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, entity.getSoundCategory(), 1.0F, 1.0F));
    }

    public int getUseTicks() {
        return this.delayTicks + this.damageConditions.map(Condition::maxDurationTicks).orElse(0);
    }

    public void usageTick(ItemStack stack, int remainingUseTicks, LivingEntity user, EquipmentSlot slot) {
        int i = stack.getMaxUseTime(user) - remainingUseTicks;
        if (i >= this.delayTicks && user instanceof SpearUser s) {
            i -= this.delayTicks;
            Vec3d vec3d = user.getRotationVector();
            double d = vec3d.dotProduct(getAmplifiedMovement(user));
            float f = user instanceof PlayerEntity ? 1.0F : 0.2F;
            float g = user instanceof PlayerEntity ? 1.0F : 0.5F;
            double e = user.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            boolean bl = false;
            float minReach = 0F;
            float maxReach = 3F;
            if(stack.get(Spears.ATTACK_RANGE) instanceof AttackRange(float reachMin, float reachMax)) {
                minReach = reachMin;
                maxReach = reachMax;
            }
            for(EntityHitResult entityHitResult : Spears.collectPiercingCollisions(user, g * minReach, g * maxReach, this.hitboxMargin, target -> PiercingWeapon.canHit(user, target))) {

                Entity entity = entityHitResult.getEntity();
                boolean bl2 = s.isInPiercingCooldown(entity, this.contactCooldownTicks);
                s.startPiercingCooldown(entity);
                if (!bl2) {
                    double j = Math.max(0.0, d - vec3d.dotProduct(getAmplifiedMovement(entity)));
                    boolean bl3 = this.dismountConditions.isPresent() && (this.dismountConditions.get()).isSatisfied(i, d, j, f);
                    boolean bl4 = this.knockbackConditions.isPresent() && (this.knockbackConditions.get()).isSatisfied(i, d, j, f);
                    boolean bl5 = this.damageConditions.isPresent() && (this.damageConditions.get()).isSatisfied(i, d, j, f);
                    if(bl3 || bl4 || bl5) {
                        float k = (float)e + MathHelper.floor(j * this.damageMultiplier);
                        bl |= s.pierce(slot, entity, k, bl5, bl4, bl3);
                    }
                }
            }
            if(bl) {
                this.playHitSound(user);
                user.getEntityWorld().sendEntityStatus(user, (byte)2);
                if (user instanceof ServerPlayerEntity serverPlayerEntity) Spears.SPEARED_MOBS.spearMob(serverPlayerEntity, s.countSpearedMobs());
            }
        }
    }

    public record Condition(int maxDurationTicks, float minSpeed, float minRelativeSpeed) {
        public static final Codec<Condition> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                Codecs.NONNEGATIVE_INT.fieldOf("max_duration_ticks").forGetter(Condition::maxDurationTicks),
                                Codec.FLOAT.optionalFieldOf("min_speed", 0.0F).forGetter(Condition::minSpeed),
                                Codec.FLOAT.optionalFieldOf("min_relative_speed", 0.0F).forGetter(Condition::minRelativeSpeed)
                        )
                        .apply(instance, Condition::new)
        );
        public static final PacketCodec<ByteBuf, Condition> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT,
                Condition::maxDurationTicks,
                PacketCodecs.FLOAT,
                Condition::minSpeed,
                PacketCodecs.FLOAT,
                Condition::minRelativeSpeed,
                Condition::new
        );

        public boolean isSatisfied(int durationTicks, double speed, double relativeSpeed, double minSpeedMultiplier) {
            return durationTicks <= this.maxDurationTicks && speed >= this.minSpeed * minSpeedMultiplier && relativeSpeed >= this.minRelativeSpeed * minSpeedMultiplier;
        }

        public static Optional<Condition> ofMinSpeed(int maxDurationTicks, float minSpeed) {
            return Optional.of(new Condition(maxDurationTicks, minSpeed, 0.0F));
        }

        public static Optional<Condition> ofMinRelativeSpeed(int maxDurationTicks, float minRelativeSpeed) {
            return Optional.of(new Condition(maxDurationTicks, 0.0F, minRelativeSpeed));
        }
    }
}
