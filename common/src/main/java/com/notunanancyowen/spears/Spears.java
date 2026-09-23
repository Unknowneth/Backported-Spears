package com.notunanancyowen.spears;

import com.notunanancyowen.spears.items.SpearItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Spears {
    public static final String MOD_ID = "spears";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static SoundEvent SPEAR_ATTACK = SoundEvent.of(new Identifier("item.spear.attack"));
    public static SoundEvent SPEAR_HIT = SoundEvent.of(new Identifier("item.spear.hit"));
    public static SoundEvent SPEAR_USE = SoundEvent.of(new Identifier("item.spear.use"));
    public static SoundEvent SPEAR_WOOD_ATTACK = SoundEvent.of(new Identifier("item.spear_wood.attack"));
    public static SoundEvent SPEAR_WOOD_HIT = SoundEvent.of(new Identifier("item.spear_wood.hit"));
    public static SoundEvent SPEAR_WOOD_USE = SoundEvent.of(new Identifier("item.spear_wood.use"));
    public static SoundEvent SPEAR_LUNGE = SoundEvent.of(new Identifier("item.spear.lunge"));
    public static RegistryEntry<SoundEvent> registerSound(String id) {
        Identifier identifier = new Identifier(id);
        return Registry.registerReference(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }
    public static final TagKey<Item> SPEARS = TagKey.of(RegistryKeys.ITEM, Identifier.of("minecraft", MOD_ID));
    public static Item WOODEN_SPEAR;// = registerSpear("wooden_spear", ToolMaterials.WOOD, 0.65F, 0.7F, 0.75F, 5.0F, 14.0F, 6.0F, 5.1F, 15.0F, 4.6F, SPEAR_WOOD_HIT, SPEAR_WOOD_ATTACK, SPEAR_WOOD_USE);
    public static Item STONE_SPEAR;// = registerSpear("stone_spear", ToolMaterials.STONE, 0.75F, 0.82F, 0.7F, 4.5F, 10.0F, 5.5F, 5.1F, 13.75F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
    public static Item GOLDEN_SPEAR;// = registerSpear("golden_spear", ToolMaterials.GOLD, 0.95F, 0.7F, 0.7F, 3.5F, 10.0F, 5.5F, 5.1F, 13.75F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
    public static Item IRON_SPEAR;// = registerSpear("iron_spear", ToolMaterials.IRON, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 4.5F, 5.1F, 11.25F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
    public static Item DIAMOND_SPEAR;// = registerSpear("diamond_spear", ToolMaterials.DIAMOND, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
    public static Item NETHERITE_SPEAR;// = registerSpear("netherite_spear", ToolMaterials.NETHERITE, 1.15F, 1.2F, 0.4F, 2.5F, 7.0F, 3.5F, 5.1F, 8.75F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
    public static Item COPPER_SPEAR = null;
    public static Item DRAGON_SPEAR = null;
    public static Item ELECTRUM_SPEAR = null;
    public static Item ENDERITE_SPEAR = null;
    public static Item CC_SILVER_SPEAR = null;
    public static Item NECROMIUM_SPEAR = null;
    public static Item tryLoadCopperSpear(boolean shouldLoad, boolean forge) {
		if(shouldLoad && forge) return COPPER_SPEAR = Spears.registerSpearRaw(new ToolMaterial() {
			@Override public int getDurability() {
				return 190;
			}
			@Override public float getMiningSpeedMultiplier() {
				return 5F;
			}
			@Override public float getAttackDamage() {
				return 1F;
			}
            @Override public int getMiningLevel() {
                return 1;
            }
			@Override public int getEnchantability() {
				return 13;
			}
			@Override public Ingredient getRepairIngredient() {
				return Ingredient.ofItems(Items.COPPER_INGOT);
			}
		}, 0.85F, 0.82F, 0.65F, 4.0F, 9.0F, 5.0F, 5.1F, 12.5F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
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
            @Override public int getMiningLevel() {
                return 1;
            }
            @Override public int getEnchantability() {
                return 13;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Items.COPPER_INGOT);
            }
        }, 0.85F, 0.82F, 0.65F, 4.0F, 9.0F, 5.0F, 5.1F, 12.5F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
        return COPPER_SPEAR = null;
    }
    public static Item tryLoadCCSilverSpear(boolean shouldLoad, boolean forge) {
        if(shouldLoad && forge) return CC_SILVER_SPEAR = Spears.registerSpearRaw(new ToolMaterial() {
            @Override public int getDurability() {
                return 157;
            }
            @Override public float getMiningSpeedMultiplier() {
                return 9F;
            }
            @Override public float getAttackDamage() {
                return 1F;
            }
            @Override public int getMiningLevel() {
                return 2;
            }
            @Override public int getEnchantability() {
                return 18;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Registries.ITEM.get(Identifier.of("caverns_and_chasms", "silver_ingot")));
            }
        }, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
        if(shouldLoad) return CC_SILVER_SPEAR = Spears.registerSpear(Identifier.of("caverns_and_chasms", "silver_spear"), new ToolMaterial() {
            @Override public int getDurability() {
                return 157;
            }
            @Override public float getMiningSpeedMultiplier() {
                return 9F;
            }
            @Override public float getAttackDamage() {
                return 1F;
            }
            @Override public int getMiningLevel() {
                return 2;
            }
            @Override public int getEnchantability() {
                return 18;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Registries.ITEM.get(Identifier.of("caverns_and_chasms", "silver_ingot")));
            }
        }, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
        return CC_SILVER_SPEAR = null;
    }
    public static Item tryLoadDragonSpear(boolean shouldLoad, boolean forge, String id) {
        if(shouldLoad && forge) return DRAGON_SPEAR = Spears.registerSpearRaw(new ToolMaterial() {
            @Override public int getDurability() {
                return 2785;
            }
            @Override public float getMiningSpeedMultiplier() {
                return 11F;
            }
            @Override public float getAttackDamage() {
                return 5F;
            }
            @Override public int getMiningLevel() {
                return 4;
            }
            @Override public int getEnchantability() {
                return 20;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Registries.ITEM.get(Identifier.of(id, "dragon_scale")));
            }
        }, 1.2F, 1.25F, 0.3F, 2.0F, 6.5F, 3.0F, 5.1F, 8.75F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
        if(shouldLoad) return DRAGON_SPEAR = Spears.registerSpear(Identifier.of(id, "dragon_spear"), new ToolMaterial() {
            @Override public int getDurability() {
                return 2785;
            }
            @Override public float getMiningSpeedMultiplier() {
                return 11F;
            }
            @Override public float getAttackDamage() {
                return 5F;
            }
            @Override public int getMiningLevel() {
                return 4;
            }
            @Override public int getEnchantability() {
                return 20;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Registries.ITEM.get(Identifier.of(id, "dragon_scale")));
            }
        }, 1.2F, 1.25F, 0.3F, 2.0F, 6.5F, 3.0F, 5.1F, 8.75F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
        return DRAGON_SPEAR = null;
    }
    public static Item tryLoadEnderiteSpear(boolean shouldLoad, boolean forge) {
        if(shouldLoad && forge) return ENDERITE_SPEAR = Spears.registerSpearRaw(new ToolMaterial() {
            @Override public int getDurability() {
                return 4096;
            }
            @Override public float getMiningSpeedMultiplier() {
                return 15F;
            }
            @Override public float getAttackDamage() {
                return 5F;
            }
            @Override public int getMiningLevel() {
                return 4;
            }
            @Override public int getEnchantability() {
                return 17;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Registries.ITEM.get(Identifier.of("enderitemod", "enderite_ingot")));
            }
        }, 1.2F, 1.25F, 0.3F, 2.0F, 6.5F, 3.0F, 5.1F, 8.75F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
        if(shouldLoad) return ENDERITE_SPEAR = Spears.registerSpear(Identifier.of("enderitemod", "enderite_spear"), new ToolMaterial() {
            @Override public int getDurability() {
                return 4096;
            }
            @Override public float getMiningSpeedMultiplier() {
                return 15F;
            }
            @Override public float getAttackDamage() {
                return 5F;
            }
            @Override public int getMiningLevel() {
                return 4;
            }
            @Override public int getEnchantability() {
                return 17;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Registries.ITEM.get(Identifier.of("enderitemod", "enderite_ingot")));
            }
        }, 1.2F, 1.25F, 0.3F, 2.0F, 6.5F, 3.0F, 5.1F, 8.75F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
        return ENDERITE_SPEAR = null;
    }
    public static Item tryLoadElectrumSpear(boolean shouldLoad, boolean forge) {
        if(shouldLoad && forge) return ELECTRUM_SPEAR = Spears.registerSpearRaw(new ToolMaterial() {
            @Override public int getDurability() {
                return 1561;
            }
            @Override public float getMiningSpeedMultiplier() {
                return 8F;
            }
            @Override public float getAttackDamage() {
                return 3F;
            }
            @Override public int getMiningLevel() {
                return 3;
            }
            @Override public int getEnchantability() {
                return 14;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Registries.ITEM.get(Identifier.of("oreganized", "electrum_ingot")));
            }
        }, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
        if(shouldLoad) return ELECTRUM_SPEAR = Spears.registerSpear(Identifier.of("oreganized", "electrum_spear"), new ToolMaterial() {
            @Override public int getDurability() {
                return 1561;
            }
            @Override public float getMiningSpeedMultiplier() {
                return 8F;
            }
            @Override public float getAttackDamage() {
                return 3F;
            }
            @Override public int getMiningLevel() {
                return 3;
            }
            @Override public int getEnchantability() {
                return 14;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Registries.ITEM.get(Identifier.of("oreganized", "electrum_ingot")));
            }
        }, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
        return ELECTRUM_SPEAR = null;
    }
    public static Item tryLoadNecromiumSpear(boolean shouldLoad, boolean forge) {
        if(shouldLoad && forge) return NECROMIUM_SPEAR = Spears.registerSpearRaw(new ToolMaterial() {
            @Override public int getDurability() {
                return 2031;
            }
            @Override public float getMiningSpeedMultiplier() {
                return 9F;
            }
            @Override public float getAttackDamage() {
                return 3F;
            }
            @Override public int getMiningLevel() {
                return 4;
            }
            @Override public int getEnchantability() {
                return 15;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Registries.ITEM.get(Identifier.of("caverns_and_chasms", "necromium_ingot")));
            }
        }, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
        if(shouldLoad) return NECROMIUM_SPEAR = Spears.registerSpear(Identifier.of("caverns_and_chasms", "necromium_spear"), new ToolMaterial() {
            @Override public int getDurability() {
                return 2031;
            }
            @Override public float getMiningSpeedMultiplier() {
                return 9F;
            }
            @Override public float getAttackDamage() {
                return 3F;
            }
            @Override public int getMiningLevel() {
                return 4;
            }
            @Override public int getEnchantability() {
                return 15;
            }
            @Override public Ingredient getRepairIngredient() {
                return Ingredient.ofItems(Registries.ITEM.get(Identifier.of("caverns_and_chasms", "necromium_ingot")));
            }
        }, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F, SPEAR_HIT, SPEAR_ATTACK, SPEAR_USE);
        return NECROMIUM_SPEAR = null;
    }
    public static SpearItem registerSpear(String id, ToolMaterial material, float swingAnimationSeconds, float chargeDamageMultiplier, float chargeDelaySeconds, float maxDurationForDismountSeconds, float minSpeedForDismount, float maxDurationForChargeKnockbackInSeconds, float minSpeedForChargeKnockback, float maxDurationForChargeDamageInSeconds, float minRelativeSpeedForChargeDamage, SoundEvent hitSound, SoundEvent attackSound, SoundEvent useSound) {
        return Registry.register(Registries.ITEM, Identifier.of("minecraft", id), new SpearItem(material, (int)(swingAnimationSeconds * 20), chargeDamageMultiplier, chargeDelaySeconds, maxDurationForDismountSeconds, minSpeedForDismount, maxDurationForChargeKnockbackInSeconds, minSpeedForChargeKnockback, maxDurationForChargeDamageInSeconds, minRelativeSpeedForChargeDamage, hitSound, attackSound, useSound, new Item.Settings()));
    }
    public static SpearItem registerSpear(Identifier id, ToolMaterial material, float swingAnimationSeconds, float chargeDamageMultiplier, float chargeDelaySeconds, float maxDurationForDismountSeconds, float minSpeedForDismount, float maxDurationForChargeKnockbackInSeconds, float minSpeedForChargeKnockback, float maxDurationForChargeDamageInSeconds, float minRelativeSpeedForChargeDamage, SoundEvent hitSound, SoundEvent attackSound, SoundEvent useSound) {
        return Registry.register(Registries.ITEM, id, new SpearItem(material, (int)(swingAnimationSeconds * 20), chargeDamageMultiplier, chargeDelaySeconds, maxDurationForDismountSeconds, minSpeedForDismount, maxDurationForChargeKnockbackInSeconds, minSpeedForChargeKnockback, maxDurationForChargeDamageInSeconds, minRelativeSpeedForChargeDamage, hitSound, attackSound, useSound, new Item.Settings()));
    }
    public static SpearItem registerSpearRaw(ToolMaterial material, float swingAnimationSeconds, float chargeDamageMultiplier, float chargeDelaySeconds, float maxDurationForDismountSeconds, float minSpeedForDismount, float maxDurationForChargeKnockbackInSeconds, float minSpeedForChargeKnockback, float maxDurationForChargeDamageInSeconds, float minRelativeSpeedForChargeDamage, SoundEvent hitSound, SoundEvent attackSound, SoundEvent useSound) {
        return new SpearItem(material, (int)(swingAnimationSeconds * 20), chargeDamageMultiplier, chargeDelaySeconds, maxDurationForDismountSeconds, minSpeedForDismount, maxDurationForChargeKnockbackInSeconds, minSpeedForChargeKnockback, maxDurationForChargeDamageInSeconds, minRelativeSpeedForChargeDamage, hitSound, attackSound, useSound, new Item.Settings());
    }
    public static Enchantment LUNGE;
    public static boolean hasBetterCombat;
    public static void init() {
        LOGGER.info("Backported spears!");
    }
}
