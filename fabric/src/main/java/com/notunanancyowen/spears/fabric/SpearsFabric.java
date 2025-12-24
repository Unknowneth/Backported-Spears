package com.notunanancyowen.spears.fabric;

import com.notunanancyowen.spears.components.*;
import com.notunanancyowen.spears.criteria.SpearedMobs;
import com.notunanancyowen.spears.packets.PlayerStabC2SPacket;
import com.notunanancyowen.spears.packets.TriggerStabEffectsC2SPacket;
import net.fabricmc.api.ModInitializer;

import com.notunanancyowen.spears.Spears;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;

import java.io.*;
import java.util.HashMap;
import java.util.Optional;

public final class SpearsFabric implements ModInitializer {
    private static HashMap<String, Boolean> makeConfig() {
        HashMap<String, Boolean> toggles = Spears.makeConfig();
        try {
            File config = FabricLoader.getInstance().getConfigDir().resolve(Spears.MOD_ID + "-common.toml").toFile();
            if(config.createNewFile()) try(FileWriter configWriter = new FileWriter(config)) {
                configWriter.write("#Whether spears have a charge attack\nspear_charge_attacks = true\n#Whether spears have a stabbing animation\nspear_stabbing_animation = true\n#Whether tridents attack like spears\ntridents_use_spear_attack_animation = false");
            }
            try(FileReader configReader = new FileReader(config)) {
                BufferedReader reader = new BufferedReader(configReader);
                StringBuilder bob = new StringBuilder();
                String configText;
                while((configText = reader.readLine()) != null) {
                    bob.append(configText);
                    bob.append(System.lineSeparator());
                }
                bob.deleteCharAt(bob.length() - 1);
                reader.close();
                bob.toString().lines().forEach(configContent -> {
                    if(!configContent.startsWith("#")) {
                        String[] configValues = configContent.replace(" ", "").split("=", 2);
                        toggles.putIfAbsent(configValues[0], configValues[1].contains("true"));
                    }
                });
            }
        }
        catch (IOException | SecurityException e) {
            Spears.LOGGER.info("Error making config: %n" + e.getLocalizedMessage());
        }
        return toggles;
    }
    @Override public void onInitialize() {
        Spears.config = makeConfig();
        Spears.hasBetterCombat = FabricLoader.getInstance().isModLoaded("bettercombat");
        PayloadTypeRegistry.playC2S().register(PlayerStabC2SPacket.ID, PlayerStabC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(TriggerStabEffectsC2SPacket.ID, TriggerStabEffectsC2SPacket.PACKET_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PlayerStabC2SPacket.ID, (packet, context) -> {
            var p = packet.getEntity(context.player().getWorld());
            if(p instanceof ServerPlayerEntity sp) {
                if(sp.getMainHandStack().get(Spears.PIERCING_WEAPON) instanceof PiercingWeapon pw) pw.stab(sp, EquipmentSlot.MAINHAND);
                sp.resetLastAttackedTicks();
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(TriggerStabEffectsC2SPacket.ID, (packet, context) -> {
            if(packet.toggle()) packet.trigger(context.player());
        });
        Spears.init();
        Spears.USE_EFFECTS = Spears.registerComponent("use_effects", builder -> builder.codec(UseEffects.CODEC).packetCodec(UseEffects.PACKET_CODEC));
        Spears.ATTACK_RANGE = Spears.registerComponent("attack_range", builder -> builder.codec(AttackRange.CODEC).packetCodec(AttackRange.PACKET_CODEC));
        Spears.KINETIC_WEAPON = Spears.registerComponent("kinetic_weapon", builder -> builder.codec(KineticWeapon.CODEC).packetCodec(KineticWeapon.PACKET_CODEC));
        Spears.SWING_ANIMATION = Spears.registerComponent("swing_animation", builder -> builder.codec(SwingAnimation.CODEC).packetCodec(SwingAnimation.PACKET_CODEC));
        Spears.PIERCING_WEAPON = Spears.registerComponent("piercing_weapon", builder -> builder.codec(PiercingWeapon.CODEC).packetCodec(PiercingWeapon.PACKET_CODEC));
        Spears.MINIMUM_ATTACK_CHARGE = Spears.registerComponent("minimum_attack_charge", builder -> builder.codec(Spears.rangedInclusiveFloat(0.0F, 1.0F)).packetCodec(PacketCodecs.FLOAT));
        Spears.SPEAR_ATTACK = Spears.registerSound("item.spear.attack").value();
        Spears.SPEAR_HIT = Spears.registerSound("item.spear.hit").value();
        Spears.SPEAR_USE = Spears.registerSound("item.spear.use").value();
        Spears.SPEAR_WOOD_ATTACK = Spears.registerSound("item.spear_wood.attack").value();
        Spears.SPEAR_WOOD_HIT = Spears.registerSound("item.spear_wood.hit").value();
        Spears.SPEAR_WOOD_USE = Spears.registerSound("item.spear_wood.use").value();
        Spears.SPEAR_LUNGE = Spears.registerSound("item.spear.lunge").value();
        Spears.WOODEN_SPEAR = Spears.registerSpear("wooden_spear", ToolMaterials.WOOD, 0.65F, 0.7F, 0.75F, 5.0F, 14.0F, 6.0F, 5.1F, 15.0F, 4.6F);
        Spears.STONE_SPEAR = Spears.registerSpear("stone_spear", ToolMaterials.STONE, 0.75F, 0.82F, 0.7F, 4.5F, 10.0F, 5.5F, 5.1F, 13.75F, 4.6F);
        Spears.GOLDEN_SPEAR = Spears.registerSpear("golden_spear", ToolMaterials.GOLD, 0.95F, 0.7F, 0.7F, 3.5F, 10.0F, 5.5F, 5.1F, 13.75F, 4.6F);
        Spears.IRON_SPEAR = Spears.registerSpear("iron_spear", ToolMaterials.IRON, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 4.5F, 5.1F, 11.25F, 4.6F);
        Spears.DIAMOND_SPEAR = Spears.registerSpear("diamond_spear", ToolMaterials.DIAMOND, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F);
        Spears.NETHERITE_SPEAR = Spears.registerSpear("netherite_spear", ToolMaterials.NETHERITE, 1.15F, 1.2F, 0.4F, 2.5F, 7.0F, 3.5F, 5.1F, 8.75F, 4.6F);
        Spears.COPPER_SPEAR = Spears.tryLoadCopperSpear(FabricLoader.getInstance().isModLoaded("copperagebackport"), false);
        Spears.POST_PIERCING_ATTACK = Spears.registerEffect("post_piercing_attack", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentEntityEffect.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()));
        Spears.SPEARED_MOBS = Spears.registerCriterion("spear_mobs", new SpearedMobs());
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(itemGroup -> {
            itemGroup.addAfter(Items.NETHERITE_SWORD, Spears.WOODEN_SPEAR);
            itemGroup.addAfter(Spears.WOODEN_SPEAR, Spears.STONE_SPEAR);
            if(Spears.COPPER_SPEAR != null) {
                itemGroup.addAfter(Spears.STONE_SPEAR, Spears.COPPER_SPEAR);
                itemGroup.addAfter(Spears.COPPER_SPEAR, Spears.IRON_SPEAR);
            }
            else itemGroup.addAfter(Spears.STONE_SPEAR, Spears.IRON_SPEAR);
            itemGroup.addAfter(Spears.IRON_SPEAR, Spears.GOLDEN_SPEAR);
            itemGroup.addAfter(Spears.GOLDEN_SPEAR, Spears.DIAMOND_SPEAR);
            itemGroup.addAfter(Spears.DIAMOND_SPEAR, Spears.NETHERITE_SPEAR);
        });
        if(Spears.makeConfig().getOrDefault("tridents_use_spear_attack_animation", false)) ServerEntityEvents.ENTITY_LOAD.register(((entity, serverWorld) -> {
            if(entity instanceof InventoryOwner i) for(ItemStack items : i.getInventory().getHeldStacks()) if(items.isOf(Items.TRIDENT)) {
                if(!items.contains(Spears.PIERCING_WEAPON)) items.set(Spears.PIERCING_WEAPON, new PiercingWeapon( 0.2F, true, false, Optional.of(SoundEvents.ITEM_TRIDENT_THROW), Optional.empty()));
                if(!items.contains(Spears.SWING_ANIMATION)) items.set(Spears.SWING_ANIMATION, new SwingAnimation(20, "stab"));
                if(!items.contains(Spears.MINIMUM_ATTACK_CHARGE)) items.set(Spears.MINIMUM_ATTACK_CHARGE, 0.9F);
                if(!items.contains(Spears.ATTACK_RANGE)) items.set(Spears.ATTACK_RANGE, new AttackRange(0F, 4F));
            }
            if(entity instanceof LivingEntity l) {
                if(!l.getMainHandStack().contains(Spears.PIERCING_WEAPON)) l.getMainHandStack().set(Spears.PIERCING_WEAPON, new PiercingWeapon( 0.2F, true, false, Optional.of(SoundEvents.ITEM_TRIDENT_THROW), Optional.empty()));
                if(!l.getMainHandStack().contains(Spears.SWING_ANIMATION)) l.getMainHandStack().set(Spears.SWING_ANIMATION, new SwingAnimation(20, "stab"));
                if(!l.getMainHandStack().contains(Spears.MINIMUM_ATTACK_CHARGE)) l.getMainHandStack().set(Spears.MINIMUM_ATTACK_CHARGE, 0.9F);
                if(!l.getMainHandStack().contains(Spears.ATTACK_RANGE)) l.getMainHandStack().set(Spears.ATTACK_RANGE, new AttackRange(0F, 4F));
                if(!l.getOffHandStack().contains(Spears.PIERCING_WEAPON)) l.getOffHandStack().set(Spears.PIERCING_WEAPON, new PiercingWeapon( 0.2F, true, false, Optional.of(SoundEvents.ITEM_TRIDENT_THROW), Optional.empty()));
                if(!l.getOffHandStack().contains(Spears.SWING_ANIMATION)) l.getOffHandStack().set(Spears.SWING_ANIMATION, new SwingAnimation(20, "stab"));
                if(!l.getOffHandStack().contains(Spears.MINIMUM_ATTACK_CHARGE)) l.getOffHandStack().set(Spears.MINIMUM_ATTACK_CHARGE, 0.9F);
                if(!l.getOffHandStack().contains(Spears.SWING_ANIMATION)) l.getOffHandStack().set(Spears.ATTACK_RANGE, new AttackRange(0F, 4F));
            }
            if(entity instanceof ItemEntity item) {
                if(!item.getStack().contains(Spears.PIERCING_WEAPON)) item.getStack().set(Spears.PIERCING_WEAPON, new PiercingWeapon( 0.2F, true, false, Optional.of(SoundEvents.ITEM_TRIDENT_THROW), Optional.empty()));
                if(!item.getStack().contains(Spears.SWING_ANIMATION)) item.getStack().set(Spears.SWING_ANIMATION, new SwingAnimation(20, "stab"));
                if(!item.getStack().contains(Spears.MINIMUM_ATTACK_CHARGE)) item.getStack().set(Spears.MINIMUM_ATTACK_CHARGE, 0.9F);
                if(!item.getStack().contains(Spears.ATTACK_RANGE)) item.getStack().set(Spears.ATTACK_RANGE, new AttackRange(0F, 4F));
            }
            if(entity instanceof ItemFrameEntity frame) {
                ItemStack item = frame.getHeldItemStack();
                if(!item.contains(Spears.PIERCING_WEAPON)) item.set(Spears.PIERCING_WEAPON, new PiercingWeapon( 0.2F, true, false, Optional.of(SoundEvents.ITEM_TRIDENT_THROW), Optional.empty()));
                if(!item.contains(Spears.SWING_ANIMATION)) item.set(Spears.SWING_ANIMATION, new SwingAnimation(20, "stab"));
                if(!item.contains(Spears.MINIMUM_ATTACK_CHARGE)) item.set(Spears.MINIMUM_ATTACK_CHARGE, 0.9F);
                if(!item.contains(Spears.ATTACK_RANGE)) item.set(Spears.ATTACK_RANGE, new AttackRange(0F, 4F));
                frame.setHeldItemStack(item);
            }
        }));
    }
}
