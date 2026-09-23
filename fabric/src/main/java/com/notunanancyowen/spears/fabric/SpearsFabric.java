package com.notunanancyowen.spears.fabric;

import com.notunanancyowen.spears.enchantments.Lunge;
import com.notunanancyowen.spears.fabric.packets.PlayerStabC2SPacket;
import com.notunanancyowen.spears.fabric.packets.SyncLungeC2SPacket;
import com.notunanancyowen.spears.items.SpearItem;
import net.fabricmc.api.ModInitializer;

import com.notunanancyowen.spears.Spears;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class SpearsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Spears.hasBetterCombat = FabricLoader.getInstance().isModLoaded("bettercombat");
        Spears.SPEAR_ATTACK = Spears.registerSound("item.spear.attack").value();
        Spears.SPEAR_HIT = Spears.registerSound("item.spear.hit").value();
        Spears.SPEAR_USE = Spears.registerSound("item.spear.use").value();
        Spears.SPEAR_WOOD_ATTACK = Spears.registerSound("item.spear_wood.attack").value();
        Spears.SPEAR_WOOD_HIT = Spears.registerSound("item.spear_wood.hit").value();
        Spears.SPEAR_WOOD_USE = Spears.registerSound("item.spear_wood.use").value();
        Spears.SPEAR_LUNGE = Spears.registerSound("item.spear.lunge").value();
        Spears.WOODEN_SPEAR = Spears.registerSpear("wooden_spear", ToolMaterials.WOOD, 0.65F, 0.7F, 0.75F, 5.0F, 14.0F, 6.0F, 5.1F, 15.0F, 4.6F, Spears.SPEAR_WOOD_HIT, Spears.SPEAR_WOOD_ATTACK, Spears.SPEAR_WOOD_USE);
        Spears.STONE_SPEAR = Spears.registerSpear("stone_spear", ToolMaterials.STONE, 0.75F, 0.82F, 0.7F, 4.5F, 10.0F, 5.5F, 5.1F, 13.75F, 4.6F, Spears.SPEAR_HIT, Spears.SPEAR_ATTACK, Spears.SPEAR_USE);
        Spears.GOLDEN_SPEAR = Spears.registerSpear("golden_spear", ToolMaterials.GOLD, 0.95F, 0.7F, 0.7F, 3.5F, 10.0F, 5.5F, 5.1F, 13.75F, 4.6F, Spears.SPEAR_HIT, Spears.SPEAR_ATTACK, Spears.SPEAR_USE);
        Spears.IRON_SPEAR = Spears.registerSpear("iron_spear", ToolMaterials.IRON, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 4.5F, 5.1F, 11.25F, 4.6F, Spears.SPEAR_HIT, Spears.SPEAR_ATTACK, Spears.SPEAR_USE);
        Spears.DIAMOND_SPEAR = Spears.registerSpear("diamond_spear", ToolMaterials.DIAMOND, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F, Spears.SPEAR_HIT, Spears.SPEAR_ATTACK, Spears.SPEAR_USE);
        Spears.NETHERITE_SPEAR = Spears.registerSpear("netherite_spear", ToolMaterials.NETHERITE, 1.15F, 1.2F, 0.4F, 2.5F, 7.0F, 3.5F, 5.1F, 8.75F, 4.6F, Spears.SPEAR_HIT, Spears.SPEAR_ATTACK, Spears.SPEAR_USE);
        Spears.COPPER_SPEAR = Spears.tryLoadCopperSpear(FabricLoader.getInstance().isModLoaded("copperagebackport"), false);
        Spears.ELECTRUM_SPEAR = Spears.tryLoadElectrumSpear(FabricLoader.getInstance().isModLoaded("oreganized"), false);
        Spears.DRAGON_SPEAR = Spears.tryLoadDragonSpear(FabricLoader.getInstance().isModLoaded("dragonloot"), false, "dragonloot");
        Spears.ENDERITE_SPEAR = Spears.tryLoadEnderiteSpear(FabricLoader.getInstance().isModLoaded("enderitemod"), false);
        Spears.CC_SILVER_SPEAR = Spears.tryLoadCCSilverSpear(FabricLoader.getInstance().isModLoaded("caverns_and_chasms"), false);
        Spears.NECROMIUM_SPEAR = Spears.tryLoadNecromiumSpear(FabricLoader.getInstance().isModLoaded("caverns_and_chasms"), false);
        Spears.LUNGE = Registry.register(Registries.ENCHANTMENT, new Identifier("lunge"), new Lunge());
        ServerPlayNetworking.registerGlobalReceiver(PlayerStabC2SPacket.TYPE, (packet, receiver, context) -> {
            if(packet.getEntity(receiver.getWorld()) instanceof ServerPlayerEntity sp) {
                if(sp.getMainHandStack().getItem() instanceof SpearItem s) s.stab(sp, EquipmentSlot.MAINHAND);
                sp.resetLastAttackedTicks();
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(SyncLungeC2SPacket.TYPE, (packet, receiver, context) -> {
            if(packet.getEntity(receiver.getWorld()) instanceof ServerPlayerEntity sp) {
                ItemStack stack = sp.getMainHandStack();
                int lunge = EnchantmentHelper.getLevel(Spears.LUNGE, stack);
                if(!sp.isFallFlying() && !sp.isTouchingWater() && sp.getHungerManager().getFoodLevel() > 5 && lunge > 0) {
                    Vec3d vec3d2 = SpearItem.transformLocalPos(SpearItem.getYawAndPitch(sp.getRotationVector()), new Vec3d(0, 0, 1)).multiply(new Vec3d(1, 0, 1)).multiply(lunge * 0.458);
                    sp.addVelocity(vec3d2);
                    sp.velocityModified = true;
                    sp.velocityDirty = true;
                    if(sp.getWorld() instanceof ServerWorld server) server.playSound(null, sp.getX(), sp.getY(), sp.getZ(), Spears.SPEAR_LUNGE, sp.getSoundCategory(), 1F, 1F);
                    if(!sp.isCreative()) sp.addExhaustion(lunge * 4F);
                    stack.damage(1, sp, (user) -> user.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                }
            }
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register((itemGroup) -> {
            itemGroup.addAfter(Items.NETHERITE_SWORD, Spears.WOODEN_SPEAR);
            itemGroup.addAfter(Spears.WOODEN_SPEAR, Spears.STONE_SPEAR);
            if(Spears.COPPER_SPEAR != null) {
                itemGroup.addAfter(Spears.STONE_SPEAR, Spears.COPPER_SPEAR);
                itemGroup.addAfter(Spears.COPPER_SPEAR, Spears.IRON_SPEAR);
            }
            else itemGroup.addAfter(Spears.STONE_SPEAR, Spears.IRON_SPEAR);
            itemGroup.addAfter(Spears.IRON_SPEAR, Spears.GOLDEN_SPEAR);
            if(Spears.CC_SILVER_SPEAR != null && Spears.ELECTRUM_SPEAR != null) {
                itemGroup.addAfter(Spears.GOLDEN_SPEAR, Spears.CC_SILVER_SPEAR);
                itemGroup.addAfter(Spears.CC_SILVER_SPEAR, Spears.ELECTRUM_SPEAR);
                itemGroup.addAfter(Spears.ELECTRUM_SPEAR, Spears.DIAMOND_SPEAR);
            }
            else if(Spears.CC_SILVER_SPEAR != null) {
                itemGroup.addAfter(Spears.GOLDEN_SPEAR, Spears.CC_SILVER_SPEAR);
                itemGroup.addAfter(Spears.CC_SILVER_SPEAR, Spears.DIAMOND_SPEAR);
            }
            else if(Spears.ELECTRUM_SPEAR != null) {
                itemGroup.addAfter(Spears.GOLDEN_SPEAR, Spears.ELECTRUM_SPEAR);
                itemGroup.addAfter(Spears.ELECTRUM_SPEAR, Spears.DIAMOND_SPEAR);
            }
            else itemGroup.addAfter(Spears.GOLDEN_SPEAR, Spears.DIAMOND_SPEAR);
            itemGroup.addAfter(Spears.DIAMOND_SPEAR, Spears.NETHERITE_SPEAR);
            if(Spears.NECROMIUM_SPEAR != null) itemGroup.addAfter(Spears.NETHERITE_SPEAR, Spears.NECROMIUM_SPEAR);
        });
        Spears.init();
    }
}
