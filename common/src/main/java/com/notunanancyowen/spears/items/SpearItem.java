package com.notunanancyowen.spears.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import com.notunanancyowen.spears.Spears;
import com.notunanancyowen.spears.dataholders.MovementFixer;
import com.notunanancyowen.spears.dataholders.SpearUser;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.util.*;
import java.util.function.Predicate;

public class SpearItem extends ToolItem {
    public final int swingAnimationTicks;
    public final float chargeDamageMultiplier;
    public final float chargeDelaySeconds;
    public final float maxDurationForDismountSeconds;
    public final float minSpeedForDismount;
    public final float maxDurationForChargeKnockbackInSeconds;
    public final float minSpeedForChargeKnockback;
    public final float maxDurationForChargeDamageInSeconds;
    public final float minRelativeSpeedForChargeDamage;
    public final SoundEvent hitSound;
    public final SoundEvent attackSound;
    public final SoundEvent useSound;
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;
    private static Settings handleSpearSettings(ToolMaterial material, Settings settings) {
        if(material == ToolMaterials.NETHERITE) settings.fireproof();
        else if(material.getDurability() == 2785) try {
            {
                Item item = Registries.ITEM.get(Identifier.of("dragonloot", "dragon_scale"));
                if(item != null && material.getRepairIngredient().test(item.getDefaultStack())) settings.fireproof();
            }
            {
                Item item = Registries.ITEM.get(Identifier.of("ender_dragon_loot", "dragon_scale"));
                if(item != null && material.getRepairIngredient().test(item.getDefaultStack())) settings.fireproof();
            }
        }
        catch(Throwable ignore) {
        }
        else if(material.getDurability() == 4096) try {
            Item item = Registries.ITEM.get(Identifier.of("enderitemod", "enderite_ingot"));
            if(item != null && material.getRepairIngredient().test(item.getDefaultStack())) settings.fireproof();
        }
        catch(Throwable ignore) {
        }
        return settings;
    }
    public SpearItem(ToolMaterial material, int swingAnimationTicks, float chargeDamageMultiplier, float chargeDelaySeconds, float maxDurationForDismountSeconds, float minSpeedForDismount, float maxDurationForChargeKnockbackInSeconds, float minSpeedForChargeKnockback, float maxDurationForChargeDamageInSeconds, float minRelativeSpeedForChargeDamage, SoundEvent hitSound, SoundEvent attackSound, SoundEvent useSound, Settings settings) {
        super(material, handleSpearSettings(material, settings));
        this.swingAnimationTicks = swingAnimationTicks;
        this.chargeDamageMultiplier = chargeDamageMultiplier;
        this.chargeDelaySeconds = chargeDelaySeconds;
        this.maxDurationForDismountSeconds = maxDurationForDismountSeconds;
        this.minSpeedForDismount = minSpeedForDismount;
        this.maxDurationForChargeKnockbackInSeconds = maxDurationForChargeKnockbackInSeconds;
        this.minSpeedForChargeKnockback = minSpeedForChargeKnockback;
        this.maxDurationForChargeDamageInSeconds = maxDurationForChargeDamageInSeconds;
        this.minRelativeSpeedForChargeDamage = minRelativeSpeedForChargeDamage;
        this.hitSound = hitSound;
        this.attackSound = attackSound;
        this.useSound = useSound;
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", material.getAttackDamage(), EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Tool modifier", 1.0 / (swingAnimationTicks * 0.05) - 4.0, EntityAttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }
    @Override public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }
    @Override public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(selected) stack.setHolder(entity);
        else stack.setHolder(null);
        super.inventoryTick(stack, world, entity, slot, selected);
    }
    @Override public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }
    @Override public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        return true;
    }
    @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        if (user.getWorld() instanceof ServerWorld server) server.playSound(null, user.getX(), user.getY(), user.getZ(), useSound, user.getSoundCategory(), 1F, 1F);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }
    public static Vec3d getAmplifiedMovement(Entity entity) {
        if (!(entity instanceof PlayerEntity) && entity.hasVehicle()) entity = entity.getRootVehicle();
        return (entity instanceof PlayerEntity player && player instanceof MovementFixer m ? m.getMovement() : entity.getPos().subtract(entity.prevX, entity.prevY, entity.prevZ)).multiply(20.0);
    }
    @Override public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);
        if (world.isClient) return;
        int i = stack.getMaxUseTime() - remainingUseTicks;
        if (i >= delayTicks() && user instanceof SpearUser s) {
            i -= delayTicks();
            Vec3d vec3d = user.getRotationVector();
            double d = vec3d.dotProduct(getAmplifiedMovement(user));
            float f = user instanceof PlayerEntity ? 1.0F : 0.2F;
            float g = user instanceof PlayerEntity ? 1.0F : 0.5F;
            double e = user.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            boolean bl = false;
            for(EntityHitResult entityHitResult : collectPiercingCollisions(user, g * 2.0F, g * 4.5F, 0.25F, target -> canHit(user, target))) {
                Entity entity = entityHitResult.getEntity();
                boolean bl2 = s.isInPiercingCooldown(entity, 10);
                s.startPiercingCooldown(entity);
                if (!bl2) {
                    double j = Math.max(0.0, d - vec3d.dotProduct(getAmplifiedMovement(entity)));
                    boolean bl3 = canDismount(i, d, f);
                    boolean bl4 = canKnockback(i, d, f);
                    boolean bl5 = canDamage(i, j, f);
                    if(bl3 || bl4 || bl5) {
                        float k = (float)e + EnchantmentHelper.getAttackDamage(stack, entity instanceof LivingEntity l ? l.getGroup() : EntityGroup.DEFAULT) + MathHelper.floor(j * chargeDamageMultiplier);
                        bl |= s.pierce(user.getActiveHand() == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, entity, k, bl5, bl4, bl3);
                    }
                }
            }
            if(bl) {
                if(user.getWorld() instanceof ServerWorld server) server.playSound(null, user.getX(), user.getY(), user.getZ(), hitSound, user.getSoundCategory(), 1F, 1F);
                user.getEntityWorld().sendEntityStatus(user, (byte)2);
            }
        }
    }
    public boolean canDismount(int durationTicks, double speed, double minSpeedMultiplier) {
        return durationTicks <= getMaxDurationForDismountInTicks() && speed >= minSpeedForDismount * minSpeedMultiplier;
    }
    public boolean canKnockback(int durationTicks, double speed,  double minSpeedMultiplier) {
        return durationTicks <= getMaxDurationForChargeKnockbackInTicks() && speed >= minSpeedForChargeKnockback * minSpeedMultiplier;
    }
    public boolean canDamage(int durationTicks, double relativeSpeed, double minSpeedMultiplier) {
        return durationTicks <= getMaxDurationForChargeDamageInTicks() && relativeSpeed >= minRelativeSpeedForChargeDamage * minSpeedMultiplier;
    }
    @Override public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }
    public int swingTicks() {
        return swingAnimationTicks;
    }
    public int delayTicks() {
        return (int)(chargeDelaySeconds * 20);
    }
    public int getUseTicks() {
        return delayTicks() + (int)(Math.max(Math.max(maxDurationForChargeDamageInSeconds, maxDurationForChargeKnockbackInSeconds), maxDurationForDismountSeconds) * 20);
    }
    public int getMaxDurationForDismountInTicks() {
        return (int)(maxDurationForDismountSeconds * 20);
    }
    public int getMaxDurationForChargeDamageInTicks() {
        return (int)(maxDurationForChargeDamageInSeconds * 20);
    }
    public int getMaxDurationForChargeKnockbackInTicks() {
        return (int)(maxDurationForChargeKnockbackInSeconds * 20);
    }
    public boolean stab(LivingEntity attacker, EquipmentSlot slot) {
        float f = (float)attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        boolean bl = false;
        if(attacker instanceof SpearUser s) for(EntityHitResult entityHitResult : collectPiercingCollisions(attacker, 2F, 4.5F, 0.25F, entity -> canHit(attacker, entity))) bl |= s.pierce(slot, entityHitResult.getEntity(), f, true, true, false);
        ItemStack stack = attacker.getEquippedStack(slot);
        int lunge = EnchantmentHelper.getLevel(Spears.LUNGE, stack);
        if(!attacker.isFallFlying() && !attacker.isTouchingWater() && (!(attacker instanceof PlayerEntity player) || player.getHungerManager().getFoodLevel() > 5) && lunge > 0) {
            Vec3d vec3d2 = transformLocalPos(getYawAndPitch(attacker.getRotationVector()), new Vec3d(0, 0, 1)).multiply(new Vec3d(1, 0, 1)).multiply(lunge * 0.458);
            attacker.addVelocity(vec3d2);
            attacker.velocityModified = true;
            attacker.velocityDirty = true;
            if(attacker.getWorld() instanceof ServerWorld server) server.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), Spears.SPEAR_LUNGE, attacker.getSoundCategory(), 1F, 1F);
            if(attacker instanceof PlayerEntity player && !player.isCreative()) player.addExhaustion(lunge * 4F);
            stack.damage(1, attacker, (user) -> user.sendEquipmentBreakStatus(slot));
        }
        if(attacker.getWorld() instanceof ServerWorld server) {
            if(bl) server.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), hitSound, attacker.getSoundCategory(), 1F, 1F);
            server.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), attackSound, attacker.getSoundCategory(), 1F, 1F);
        }
        attacker.swingHand(Hand.MAIN_HAND, false);
        return bl;
    }
    public static Vec2f getYawAndPitch(Vec3d vec3d) {
        float f = (float)Math.atan2(-vec3d.x, vec3d.z) * (180.0F / (float)Math.PI);
        float g = (float)Math.asin(-vec3d.y / Math.sqrt(vec3d.x * vec3d.x + vec3d.y * vec3d.y + vec3d.z * vec3d.z)) * (180.0F / (float)Math.PI);
        return new Vec2f(g, f);

    }
    public static Vec3d transformLocalPos(Vec2f rotation, Vec3d vec) {
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
    public static boolean canHit(Entity attacker, Entity target) {
        if (target.canBeHitByProjectile() && !target.isInvulnerable() && target.isAlive()) return (!(target instanceof PlayerEntity playerEntity) || !(attacker instanceof PlayerEntity playerEntity2) || playerEntity2.shouldDamagePlayer(playerEntity)) && !attacker.isConnectedThroughVehicle(target);
        return false;
    }
    private static Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * (float) (Math.PI / 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }
    public static Collection<EntityHitResult> collectPiercingCollisions(
            LivingEntity entity, float minReach, float maxReach, float hitboxMargin, Predicate<Entity> hitPredicate
    ) {
        Vec3d vec3d = getRotationVector(entity.getPitch(), entity.getHeadYaw());
        Vec3d vec3d2 = entity.getEyePos();
        Vec3d vec3d3 = vec3d2.add(vec3d.multiply(minReach));
        double d = entity.getVelocity().dotProduct(vec3d);
        Vec3d vec3d4 = vec3d2.add(vec3d.multiply(maxReach + Math.max(0.0, d)));
        return collectPiercingCollisions(entity, vec3d2, vec3d3, hitPredicate, vec3d4, hitboxMargin)
                .map(hitResult -> List.of(), hitResults -> hitResults);
    }
    private static Either<BlockHitResult, Collection<EntityHitResult>> collectPiercingCollisions(
            Entity entity, Vec3d pos, Vec3d minReach, Predicate<Entity> hitPredicate, Vec3d maxReach, float hitboxMargin
    ) {
        World world = entity.getEntityWorld();
        BlockHitResult blockHitResult = getCollisionsIncludingWorldBorder(world,
                new RaycastContext(pos, maxReach, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity)
        );
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            maxReach = blockHitResult.getPos();
            if (pos.squaredDistanceTo(maxReach) < pos.squaredDistanceTo(minReach)) {
                return Either.left(blockHitResult);
            }
        }

        Box box = Box.of(minReach, hitboxMargin, hitboxMargin, hitboxMargin).stretch(maxReach.subtract(minReach)).expand(1.0);
        Collection<EntityHitResult> collection = collectPiercingCollisions(world, entity, minReach, maxReach, box, hitPredicate, hitboxMargin, RaycastContext.ShapeType.COLLIDER, true);
        return !collection.isEmpty() ? Either.right(collection) : Either.left(blockHitResult);
    }
    public static Collection<EntityHitResult> collectPiercingCollisions(
            World world, Entity entity, Vec3d from, Vec3d to, Box box, Predicate<Entity> hitPredicate, float hitboxMargin, RaycastContext.ShapeType shapeType, boolean bl
    ) {
        List<EntityHitResult> list = new ArrayList<>();

        for (Entity entity2 : world.getOtherEntities(entity, box, hitPredicate)) {
            Box box2 = entity2.getBoundingBox();
            if (bl && box2.contains(from)) {
                list.add(new EntityHitResult(entity2, from));
            } else {
                Optional<Vec3d> optional = box2.raycast(from, to);
                if (optional.isPresent()) {
                    list.add(new EntityHitResult(entity2, (Vec3d)optional.get()));
                } else if (!(hitboxMargin <= 0.0)) {
                    Optional<Vec3d> optional2 = box2.expand(hitboxMargin).raycast(from, to);
                    if (optional2.isPresent()) {
                        Vec3d vec3d = (Vec3d)optional2.get();
                        Vec3d vec3d2 = box2.getCenter();
                        BlockHitResult blockHitResult = getCollisionsIncludingWorldBorder(world,
                                new RaycastContext(vec3d, vec3d2, shapeType, RaycastContext.FluidHandling.NONE, entity)
                        );
                        if (blockHitResult.getType() != HitResult.Type.MISS) {
                            vec3d2 = blockHitResult.getPos();
                        }

                        Optional<Vec3d> optional3 = entity2.getBoundingBox().raycast(vec3d, vec3d2);
                        optional3.ifPresent(d -> list.add(new EntityHitResult(entity2, (Vec3d) d)));
                    }
                }
            }
        }
        return list;
    }
    private static BlockHitResult getCollisionsIncludingWorldBorder(World world, RaycastContext context) {
        BlockHitResult blockHitResult = world.raycast(context);
        WorldBorder worldBorder = world.getWorldBorder();
        if (worldBorder.contains(BlockPos.ofFloored(context.getStart())) && !worldBorder.contains(blockHitResult.getBlockPos())) {
            Vec3d vec3d = blockHitResult.getPos().subtract(context.getStart());
            Direction direction = Direction.getFacing(vec3d.x, vec3d.y, vec3d.z);
            Vec3d vec3d2 = blockHitResult.getPos();
            vec3d2 = new Vec3d(MathHelper.clamp(vec3d2.x, worldBorder.getBoundWest(), worldBorder.getBoundEast() - 1.0E-5F), vec3d2.y, MathHelper.clamp(vec3d2.z, worldBorder.getBoundNorth(), worldBorder.getBoundSouth() - 1.0E-5F));
            return new BlockHitResult(vec3d2, direction, BlockPos.ofFloored(vec3d2), false);
        } else {
            return blockHitResult;
        }
    }
}
