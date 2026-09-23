package com.notunanancyowen.spears.forge;

import com.notunanancyowen.spears.SpearsClient;
import com.notunanancyowen.spears.enchantments.Lunge;
import com.notunanancyowen.spears.forge.packets.PlayerStabC2SPacket;
import com.notunanancyowen.spears.forge.packets.SyncLungeC2SPacket;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import com.notunanancyowen.spears.Spears;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

@Mod(Spears.MOD_ID)
public final class SpearsForge {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "minecraft");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");
    private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, "minecraft");
    private static final DeferredRegister<Item> ELECTRUM_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "oreganized");
    private static final DeferredRegister<Item> DRAGON_ITEMS1 = DeferredRegister.create(ForgeRegistries.ITEMS, "dragonloot");
    private static final DeferredRegister<Item> DRAGON_ITEMS2 = DeferredRegister.create(ForgeRegistries.ITEMS, "ender_dragon_loot");
    private static final DeferredRegister<Item> ENDERITE_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "enderitemod");
    private static final DeferredRegister<Item> CAVERNS_AND_CHASMS_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "caverns_and_chasms");
    static {
        SOUNDS.register("item.spear.attack", () -> Spears.SPEAR_ATTACK);
        SOUNDS.register("item.spear.hit", () -> Spears.SPEAR_HIT);
        SOUNDS.register("item.spear.use", () -> Spears.SPEAR_USE);
        SOUNDS.register("item.spear_wood.attack", () -> Spears.SPEAR_WOOD_ATTACK);
        SOUNDS.register("item.spear_wood.hit", () -> Spears.SPEAR_WOOD_HIT);
        SOUNDS.register("item.spear_wood.use", () -> Spears.SPEAR_WOOD_USE);
        SOUNDS.register("item.spear.lunge", () -> Spears.SPEAR_LUNGE);
        ITEMS.register("wooden_spear", () -> Spears.WOODEN_SPEAR = Spears.registerSpearRaw(ToolMaterials.WOOD, 0.65F, 0.7F, 0.75F, 5.0F, 14.0F, 6.0F, 5.1F, 15.0F, 4.6F, Spears.SPEAR_WOOD_HIT, Spears.SPEAR_WOOD_ATTACK, Spears.SPEAR_WOOD_USE));
        ITEMS.register("stone_spear", () -> Spears.STONE_SPEAR = Spears.registerSpearRaw(ToolMaterials.STONE, 0.75F, 0.82F, 0.7F, 4.5F, 10.0F, 5.5F, 5.1F, 13.75F, 4.6F, Spears.SPEAR_HIT, Spears.SPEAR_ATTACK, Spears.SPEAR_USE));
        ITEMS.register("golden_spear", () ->  Spears.GOLDEN_SPEAR = Spears.registerSpearRaw(ToolMaterials.GOLD, 0.95F, 0.7F, 0.7F, 3.5F, 10.0F, 5.5F, 5.1F, 13.75F, 4.6F, Spears.SPEAR_HIT, Spears.SPEAR_ATTACK, Spears.SPEAR_USE));
        ITEMS.register("iron_spear", () -> Spears.IRON_SPEAR = Spears.registerSpearRaw(ToolMaterials.IRON, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 4.5F, 5.1F, 11.25F, 4.6F, Spears.SPEAR_HIT, Spears.SPEAR_ATTACK, Spears.SPEAR_USE));
        ITEMS.register("diamond_spear", () -> Spears.DIAMOND_SPEAR = Spears.registerSpearRaw(ToolMaterials.DIAMOND, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F, Spears.SPEAR_HIT, Spears.SPEAR_ATTACK, Spears.SPEAR_USE));
        ITEMS.register("netherite_spear", () -> Spears.NETHERITE_SPEAR = Spears.registerSpearRaw(ToolMaterials.NETHERITE, 1.15F, 1.2F, 0.4F, 2.5F, 7.0F, 3.5F, 5.1F, 8.75F, 4.6F, Spears.SPEAR_HIT, Spears.SPEAR_ATTACK, Spears.SPEAR_USE));
        ENCHANTMENTS.register("lunge", () -> Spears.LUNGE = new Lunge());
    }
    public SpearsForge(FMLJavaModLoadingContext context) {
        // Run our common setup.
        Spears.hasBetterCombat = ModList.get().isLoaded("bettercombat");
        Spears.init();
        MinecraftForge.EVENT_BUS.register(this);
        PacketHandler.register();
        SOUNDS.register(context.getModEventBus());
        ITEMS.register(context.getModEventBus());
        ENCHANTMENTS.register(context.getModEventBus());
        context.getModEventBus().addListener(this::buildContents);
        if(ModList.get().isLoaded("copperagebackport")) ITEMS.register("copper_spear", () -> Spears.tryLoadCopperSpear(true, true));
        if(ModList.get().isLoaded("oreganized")) {
            ELECTRUM_ITEMS.register(context.getModEventBus());
            ELECTRUM_ITEMS.register("electrum_spear", () -> Spears.ELECTRUM_SPEAR = Spears.tryLoadElectrumSpear(true, true));
        }
        if(ModList.get().isLoaded("dragonloot")) {
            DRAGON_ITEMS1.register(context.getModEventBus());
            DRAGON_ITEMS1.register("dragon_spear", () -> Spears.DRAGON_SPEAR = Spears.tryLoadDragonSpear(true, true, "dragonloot"));
        }
        if(ModList.get().isLoaded("ender_dragon_loot")) {
            DRAGON_ITEMS2.register(context.getModEventBus());
            DRAGON_ITEMS2.register("dragon_spear", () -> Spears.DRAGON_SPEAR = Spears.tryLoadDragonSpear(true, true, "ender_dragon_loot"));
        }
        if(ModList.get().isLoaded("enderitemod")) {
            ENDERITE_ITEMS.register(context.getModEventBus());
            ENDERITE_ITEMS.register("enderite_spear", () -> Spears.ENDERITE_SPEAR = Spears.tryLoadEnderiteSpear(true, true));
        }
        if(ModList.get().isLoaded("caverns_and_chasms")) {
            CAVERNS_AND_CHASMS_ITEMS.register(context.getModEventBus());
            CAVERNS_AND_CHASMS_ITEMS.register("silver_spear", () -> Spears.CC_SILVER_SPEAR = Spears.tryLoadCCSilverSpear(true, true));
            CAVERNS_AND_CHASMS_ITEMS.register("necromium_spear", () -> Spears.NECROMIUM_SPEAR = Spears.tryLoadNecromiumSpear(true, true));
        }
    }
    private void buildContents(BuildCreativeModeTabContentsEvent itemGroup) {
        if(itemGroup.getTabKey() == ItemGroups.COMBAT) {
            itemGroup.getEntries().putAfter(Items.NETHERITE_SWORD.getDefaultStack(), Spears.WOODEN_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            itemGroup.getEntries().putAfter(Spears.WOODEN_SPEAR.getDefaultStack(), Spears.STONE_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            if(Spears.COPPER_SPEAR != null) {
                itemGroup.getEntries().putAfter(Spears.STONE_SPEAR.getDefaultStack(), Spears.COPPER_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
                itemGroup.getEntries().putAfter(Spears.COPPER_SPEAR.getDefaultStack(), Spears.IRON_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            }
            else itemGroup.getEntries().putAfter(Spears.STONE_SPEAR.getDefaultStack(), Spears.IRON_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            itemGroup.getEntries().putAfter(Spears.IRON_SPEAR.getDefaultStack(), Spears.GOLDEN_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            if(Spears.CC_SILVER_SPEAR != null && Spears.ELECTRUM_SPEAR != null) {
                itemGroup.getEntries().putAfter(Spears.GOLDEN_SPEAR.getDefaultStack(), Spears.CC_SILVER_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
                itemGroup.getEntries().putAfter(Spears.CC_SILVER_SPEAR.getDefaultStack(), Spears.ELECTRUM_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
                itemGroup.getEntries().putAfter(Spears.ELECTRUM_SPEAR.getDefaultStack(), Spears.DIAMOND_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            }
            else if(Spears.CC_SILVER_SPEAR != null) {
                itemGroup.getEntries().putAfter(Spears.GOLDEN_SPEAR.getDefaultStack(), Spears.CC_SILVER_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
                itemGroup.getEntries().putAfter(Spears.CC_SILVER_SPEAR.getDefaultStack(), Spears.DIAMOND_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            }
            else if(Spears.ELECTRUM_SPEAR != null) {
                itemGroup.getEntries().putAfter(Spears.GOLDEN_SPEAR.getDefaultStack(), Spears.ELECTRUM_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
                itemGroup.getEntries().putAfter(Spears.ELECTRUM_SPEAR.getDefaultStack(), Spears.DIAMOND_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            }
            else itemGroup.getEntries().putAfter(Spears.GOLDEN_SPEAR.getDefaultStack(), Spears.DIAMOND_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            itemGroup.getEntries().putAfter(Spears.DIAMOND_SPEAR.getDefaultStack(), Spears.NETHERITE_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
            if(Spears.NECROMIUM_SPEAR != null) itemGroup.getEntries().putAfter(Spears.NETHERITE_SPEAR.getDefaultStack(), Spears.NECROMIUM_SPEAR.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void modifyItems(ItemAttributeModifierEvent event) {
        if(Spears.ELECTRUM_SPEAR != null && event.getItemStack().isOf(Spears.ELECTRUM_SPEAR)) event.addModifier(Registries.ATTRIBUTE.getEntry(Registries.ATTRIBUTE.get(Identifier.of("oreganized", "kinetic_damage"))).value(), new EntityAttributeModifier(UUID.fromString("df00eac8-5bd6-482a-b9ce-170388610ae2"), "Kinetic damage", 2.0, EntityAttributeModifier.Operation.ADDITION));
    }
    @SubscribeEvent public void onServerStarting(ServerStartingEvent event) {
    }
    @Mod.EventBusSubscriber(modid = Spears.MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent public static void onClientSetup(TickEvent.ClientTickEvent event) {
            if(SpearsClient.syncSpears == null) SpearsClient.syncSpears = i -> {
                PacketHandler.sendToServer(new PlayerStabC2SPacket(i));
                return true;
            };
            if(SpearsClient.syncSpearsWithBetterCombat == null) SpearsClient.syncSpearsWithBetterCombat = i -> {
                PacketHandler.sendToServer(new SyncLungeC2SPacket(i));
                return Spears.hasBetterCombat;
            };
        }
    }
}
