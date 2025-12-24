package com.notunanancyowen.spears;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.notunanancyowen.spears.components.*;
import com.notunanancyowen.spears.criteria.SpearedMobs;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public final class Spears {
    public static final String MOD_ID = "spears";
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Collection<EntityHitResult> collectPiercingCollisions(
            LivingEntity entity, float minReach, float maxReach, float hitboxMargin, Predicate<Entity> hitPredicate
    ) {
        Vec3d vec3d = entity.getRotationVector(entity.getPitch(), entity.getHeadYaw());
        Vec3d vec3d2 = entity.getEyePos();
        Vec3d vec3d3 = vec3d2.add(vec3d.multiply(minReach));
        double d = entity.getMovement().dotProduct(vec3d);
        Vec3d vec3d4 = vec3d2.add(vec3d.multiply(maxReach + Math.max(0.0, d)));
        return collectPiercingCollisions(entity, vec3d2, vec3d3, hitPredicate, vec3d4, hitboxMargin, RaycastContext.ShapeType.COLLIDER)
                .map(hitResult -> List.of(), hitResults -> hitResults);
    }
    private static Either<BlockHitResult, Collection<EntityHitResult>> collectPiercingCollisions(
            Entity entity, Vec3d pos, Vec3d minReach, Predicate<Entity> hitPredicate, Vec3d maxReach, float hitboxMargin, RaycastContext.ShapeType shapeType
    ) {
        World world = entity.getEntityWorld();
        BlockHitResult blockHitResult = getCollisionsIncludingWorldBorder(world,
                new RaycastContext(pos, maxReach, shapeType, RaycastContext.FluidHandling.NONE, entity)
        );
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            maxReach = blockHitResult.getPos();
            if (pos.squaredDistanceTo(maxReach) < pos.squaredDistanceTo(minReach)) {
                return Either.left(blockHitResult);
            }
        }

        Box box = Box.of(minReach, hitboxMargin, hitboxMargin, hitboxMargin).stretch(maxReach.subtract(minReach)).expand(1.0);
        Collection<EntityHitResult> collection = collectPiercingCollisions(world, entity, minReach, maxReach, box, hitPredicate, hitboxMargin, shapeType, true);
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
        if (worldBorder.contains(context.getStart()) && !worldBorder.contains(blockHitResult.getPos())) {
            Vec3d vec3d = blockHitResult.getPos().subtract(context.getStart());
            Direction direction = Direction.getFacing(vec3d.x, vec3d.y, vec3d.z);
            Vec3d vec3d2 = blockHitResult.getPos();
            vec3d2 = new Vec3d(MathHelper.clamp(vec3d2.x, worldBorder.getBoundWest(), worldBorder.getBoundEast() - 1.0E-5F), vec3d2.y, MathHelper.clamp(vec3d2.z, worldBorder.getBoundNorth(), worldBorder.getBoundSouth() - 1.0E-5F));
            return new BlockHitResult(vec3d2, direction, BlockPos.ofFloored(vec3d2), false);
        } else {
            return blockHitResult;
        }
    }
    public static Codec<Float> rangedInclusiveFloat(float minInclusive, float maxInclusive, Function<Float, String> messageFactory) {
        return Codec.FLOAT
                .validate(
                        value -> value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0
                                ? DataResult.success(value)
                                : DataResult.error(() -> messageFactory.apply(value))
                );
    }
    public static Codec<Float> rangedInclusiveFloat(float minInclusive, float maxInclusive) {
        return rangedInclusiveFloat(minInclusive, maxInclusive, value -> "Value must be within range [" + minInclusive + ";" + maxInclusive + "]: " + value);
    }
    public static ComponentType<UseEffects> USE_EFFECTS;
    public static ComponentType<AttackRange> ATTACK_RANGE;
    public static ComponentType<KineticWeapon> KINETIC_WEAPON;
    public static ComponentType<SwingAnimation> SWING_ANIMATION;
    public static ComponentType<PiercingWeapon> PIERCING_WEAPON;
    public static ComponentType<Float> MINIMUM_ATTACK_CHARGE;
    public static <T> ComponentType<T> registerComponent(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, id, (builderOperator.apply(ComponentType.builder())).build());
    }
    public static SoundEvent SPEAR_ATTACK;
    public static SoundEvent SPEAR_HIT;
    public static SoundEvent SPEAR_USE;
    public static SoundEvent SPEAR_WOOD_ATTACK;
    public static SoundEvent SPEAR_WOOD_HIT;
    public static SoundEvent SPEAR_WOOD_USE;
    public static SoundEvent SPEAR_LUNGE;
    public static RegistryEntry<SoundEvent> registerSound(String id) {
        Identifier identifier = Identifier.ofVanilla(id);
        return Registry.registerReference(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }
    public static final TagKey<Item> SPEARS = TagKey.of(RegistryKeys.ITEM, Identifier.ofVanilla(MOD_ID));
    public static Item WOODEN_SPEAR;// = registerSpear("wooden_spear", ToolMaterials.WOOD, 0.65F, 0.7F, 0.75F, 5.0F, 14.0F, 6.0F, 5.1F, 15.0F, 4.6F);
    public static Item STONE_SPEAR;// = registerSpear("stone_spear", ToolMaterials.STONE, 0.75F, 0.82F, 0.7F, 4.5F, 10.0F, 5.5F, 5.1F, 13.75F, 4.6F);
    public static Item GOLDEN_SPEAR;// = registerSpear("golden_spear", ToolMaterials.GOLD, 0.95F, 0.7F, 0.7F, 3.5F, 10.0F, 5.5F, 5.1F, 13.75F, 4.6F);
    public static Item IRON_SPEAR;// = registerSpear("iron_spear", ToolMaterials.IRON, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 4.5F, 5.1F, 11.25F, 4.6F);
    public static Item DIAMOND_SPEAR;// = registerSpear("diamond_spear", ToolMaterials.DIAMOND, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F);
    public static Item NETHERITE_SPEAR;// = registerSpear("netherite_spear", ToolMaterials.NETHERITE, 1.15F, 1.2F, 0.4F, 2.5F, 7.0F, 3.5F, 5.1F, 8.75F, 4.6F);
    public static Item COPPER_SPEAR = null;
    public static Item tryLoadCopperSpear(boolean shouldLoad, boolean neoForge) {
        if(shouldLoad && neoForge) return COPPER_SPEAR = Spears.registerSpearRaw(new ToolMaterial() {
            @Override public int getDurability() {
                return 190;
            }
            @Override public float getMiningSpeedMultiplier() {
                return 5F;
            }
            @Override public float getAttackDamage() {
                return 1F;
            }
            @Override public TagKey<Block> getInverseTag() {
                return BlockTags.INCORRECT_FOR_STONE_TOOL;
            }
            @Override public int getEnchantability() {
                return 13;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Items.COPPER_INGOT);
            }
        }, 0.85F, 0.82F, 0.65F, 4.0F, 9.0F, 5.0F, 5.1F, 12.5F, 4.6F);
        if(shouldLoad) return COPPER_SPEAR = Spears.registerSpear("copper_spear", new ToolMaterial() {
            @Override public int getDurability() {
                return 190;
            }
            @Override public float getMiningSpeedMultiplier() {
                return 5F;
            }
            @Override public float getAttackDamage() {
                return 1F;
            }
            @Override public TagKey<Block> getInverseTag() {
                return BlockTags.INCORRECT_FOR_STONE_TOOL;
            }
            @Override public int getEnchantability() {
                return 13;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Items.COPPER_INGOT);
            }
        }, 0.85F, 0.82F, 0.65F, 4.0F, 9.0F, 5.0F, 5.1F, 12.5F, 4.6F);
        return COPPER_SPEAR = null;
    }
    public static Item registerSpear(String id, ToolMaterial material, float swingAnimationSeconds, float chargeDamageMultiplier, float chargeDelaySeconds, float maxDurationForDismountSeconds, float minSpeedForDismount, float maxDurationForChargeKnockbackInSeconds, float minSpeedForChargeKnockback, float maxDurationForChargeDamageInSeconds, float minRelativeSpeedForChargeDamage) {
        return Registry.register(Registries.ITEM, id, registerSpearRaw(material, swingAnimationSeconds, chargeDamageMultiplier, chargeDelaySeconds, maxDurationForDismountSeconds, minSpeedForDismount, maxDurationForChargeKnockbackInSeconds, minSpeedForChargeKnockback, maxDurationForChargeDamageInSeconds, minRelativeSpeedForChargeDamage));
    }
    public static Item registerSpearRaw(ToolMaterial material, float swingAnimationSeconds, float chargeDamageMultiplier, float chargeDelaySeconds, float maxDurationForDismountSeconds, float minSpeedForDismount, float maxDurationForChargeKnockbackInSeconds, float minSpeedForChargeKnockback, float maxDurationForChargeDamageInSeconds, float minRelativeSpeedForChargeDamage) {
        boolean wood = material == ToolMaterials.WOOD;
        var components = new Item.Settings().component(USE_EFFECTS, new UseEffects(1F, true, false)).attributeModifiers(AttributeModifiersComponent.builder().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID, material.getAttackDamage(), EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).add(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(Item.BASE_ATTACK_SPEED_MODIFIER_ID, (double)(1.0F / swingAnimationSeconds) - (double)4.0F, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).build()).component(MINIMUM_ATTACK_CHARGE, 1F).component(ATTACK_RANGE, new AttackRange(2.0F, 4.5F));
        var config = makeConfig();
        boolean chargeAttacksEnabled = config.getOrDefault("spear_charge_attacks", true);
        if(config.getOrDefault("spear_stabbing_animation", true)) components.component(SWING_ANIMATION, new SwingAnimation((int)(swingAnimationSeconds * 20), "stab")).component(PIERCING_WEAPON, new PiercingWeapon(0.25F, true, false, Optional.of(Registries.SOUND_EVENT.getEntry(wood ? SPEAR_WOOD_ATTACK : SPEAR_ATTACK)), Optional.of(Registries.SOUND_EVENT.getEntry(wood ? SPEAR_WOOD_HIT : SPEAR_HIT))));
        if(chargeAttacksEnabled) components.component(KINETIC_WEAPON, new KineticWeapon(0.125F, 10, (int)(chargeDelaySeconds * 20.0F), KineticWeapon.Condition.ofMinSpeed((int)(maxDurationForDismountSeconds * 20.0F), minSpeedForDismount), KineticWeapon.Condition.ofMinSpeed((int)(maxDurationForChargeKnockbackInSeconds * 20.0F), minSpeedForChargeKnockback), KineticWeapon.Condition.ofMinRelativeSpeed((int)(maxDurationForChargeDamageInSeconds * 20.0F), minRelativeSpeedForChargeDamage), 0.38F, chargeDamageMultiplier, Optional.of(Registries.SOUND_EVENT.getEntry(wood ? SPEAR_WOOD_USE : SPEAR_USE)), Optional.of(Registries.SOUND_EVENT.getEntry(wood ? SPEAR_WOOD_HIT : SPEAR_HIT))));
        if(material == ToolMaterials.NETHERITE) components.fireproof();
        if(chargeAttacksEnabled) return new ToolItem(material, components) {
            @Override public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
                if(selected) stack.setHolder(entity);
                else stack.setHolder(null);
                super.inventoryTick(stack, world, entity, slot, selected);
            }
            @Override public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
                return !miner.isCreative();
            }
            @Override public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
                return true;
            }
            @Override public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
                stack.damage(1, attacker, EquipmentSlot.MAINHAND);
            }
            @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
                user.setCurrentHand(hand);
                return TypedActionResult.consume(user.getStackInHand(hand));
            }
            @Override public int getMaxUseTime(ItemStack stack, LivingEntity user) {
                return 72000;
            }
        };
        return new ToolItem(material, components) {
            @Override public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
                if(selected) stack.setHolder(entity);
                else stack.setHolder(null);
                super.inventoryTick(stack, world, entity, slot, selected);
            }
            @Override public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
                return !miner.isCreative();
            }
            @Override public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
                return true;
            }
            @Override public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
                stack.damage(1, attacker, EquipmentSlot.MAINHAND);
            }
        };
    }
    public static ComponentType<List<EnchantmentEffectEntry<EnchantmentEntityEffect>>> POST_PIERCING_ATTACK;
    public static <T> ComponentType<T> registerEffect(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, id, (builderOperator.apply(ComponentType.builder())).build());
    }
    public static SpearedMobs SPEARED_MOBS;// = registerCriterion("spear_mobs", new SpearedMobs());
    public static <T extends Criterion<?>> T registerCriterion(String id, T criterion) {
        return Registry.register(Registries.CRITERION, id, criterion);
    }
    public static HashMap<String, Boolean> makeConfig() {
        if(config == null) config = new HashMap<>();
        return config;
    }
    public static HashMap<String, Boolean> config;
    public static boolean hasBetterCombat = false;
    public static void init() {
        // Write common init code here.
        LOGGER.info("Backported spears!");
    }
}
