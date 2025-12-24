package com.notunanancyowen.spears.neoforge;

import com.notunanancyowen.spears.SpearsClient;
import com.notunanancyowen.spears.components.*;
import com.notunanancyowen.spears.criteria.SpearedMobs;
import com.notunanancyowen.spears.packets.PlayerStabC2SPacket;
import com.notunanancyowen.spears.packets.TriggerStabEffectsC2SPacket;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemGroup.StackVisibility;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import com.notunanancyowen.spears.Spears;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Mod(Spears.MOD_ID)
public final class SpearsNeoForge {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(RegistryKeys.DATA_COMPONENT_TYPE, "minecraft");
    private static final DeferredRegister.DataComponents ENCHANTMENT_COMPONENTS = DeferredRegister.createDataComponents(RegistryKeys.ENCHANTMENT_EFFECT_COMPONENT_TYPE, "minecraft");
    public SpearsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Run our common setup.
        Spears.hasBetterCombat = ModList.get().isLoaded("bettercombat");
        Spears.init();
        NeoForge.EVENT_BUS.register(this);
        DATA_COMPONENTS.register(modEventBus);
        ENCHANTMENT_COMPONENTS.register(modEventBus);
        modEventBus.addListener(this::registerStuff);
        modEventBus.addListener(this::modifyComponents);
        modEventBus.addListener(this::buildContents);
        modEventBus.addListener(this::registerPayloads);
        modContainer.registerConfig(ModConfig.Type.COMMON, SpearsConfig.SPEC);
    }
    private static final Supplier<ComponentType<UseEffects>> USE_EFFECTS = DATA_COMPONENTS.registerComponentType("use_effects", builder -> builder.codec(UseEffects.CODEC).packetCodec(UseEffects.PACKET_CODEC));
    private static final Supplier<ComponentType<AttackRange>> ATTACK_RANGE = DATA_COMPONENTS.registerComponentType("attack_range", builder -> builder.codec(AttackRange.CODEC).packetCodec(AttackRange.PACKET_CODEC));
    private static final Supplier<ComponentType<KineticWeapon>> KINETIC_WEAPON = DATA_COMPONENTS.registerComponentType("kinetic_weapon", builder -> builder.codec(KineticWeapon.CODEC).packetCodec(KineticWeapon.PACKET_CODEC));
    private static final Supplier<ComponentType<SwingAnimation>> SWING_ANIMATION = DATA_COMPONENTS.registerComponentType("swing_animation", builder -> builder.codec(SwingAnimation.CODEC).packetCodec(SwingAnimation.PACKET_CODEC));
    private static final Supplier<ComponentType<PiercingWeapon>> PIERCING_WEAPON = DATA_COMPONENTS.registerComponentType("piercing_weapon", builder -> builder.codec(PiercingWeapon.CODEC).packetCodec(PiercingWeapon.PACKET_CODEC));
    private static final Supplier<ComponentType<Float>> MINIMUM_ATTACK_CHARGE = DATA_COMPONENTS.registerComponentType("minimum_attack_charge", builder -> builder.codec(Spears.rangedInclusiveFloat(0.0F, 1.0F)).packetCodec(PacketCodecs.FLOAT));
    private static final Supplier<ComponentType<List<EnchantmentEffectEntry<EnchantmentEntityEffect>>>> POST_PIERCING_ATTACK = ENCHANTMENT_COMPONENTS.registerComponentType("post_piercing_attack", builder -> builder.codec(EnchantmentEffectEntry.createCodec(EnchantmentEntityEffect.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()));
    private void registerStuff(final RegisterEvent event) {
        event.register(RegistryKeys.DATA_COMPONENT_TYPE, registry -> {
            Spears.USE_EFFECTS = USE_EFFECTS.get();
            Spears.ATTACK_RANGE = ATTACK_RANGE.get();
            Spears.KINETIC_WEAPON = KINETIC_WEAPON.get();
            Spears.SWING_ANIMATION = SWING_ANIMATION.get();
            Spears.PIERCING_WEAPON = PIERCING_WEAPON.get();
            Spears.MINIMUM_ATTACK_CHARGE = MINIMUM_ATTACK_CHARGE.get();
        });
        event.register(RegistryKeys.SOUND_EVENT, registry -> {
            registry.register(Identifier.ofVanilla("item.spear.attack"), Spears.SPEAR_ATTACK = SoundEvent.of(Identifier.ofVanilla("item.spear.attack")));
            registry.register(Identifier.ofVanilla("item.spear.use"), Spears.SPEAR_USE = SoundEvent.of(Identifier.ofVanilla("item.spear.use")));
            registry.register(Identifier.ofVanilla("item.spear.hit"), Spears.SPEAR_HIT = SoundEvent.of(Identifier.ofVanilla("item.spear.hit")));
            registry.register(Identifier.ofVanilla("item.spear_wood.attack"), Spears.SPEAR_WOOD_ATTACK = SoundEvent.of(Identifier.ofVanilla("item.spear_wood.attack")));
            registry.register(Identifier.ofVanilla("item.spear_wood.hit"), Spears.SPEAR_WOOD_HIT = SoundEvent.of(Identifier.ofVanilla("item.spear_wood.hit")));
            registry.register(Identifier.ofVanilla("item.spear_wood.use"), Spears.SPEAR_WOOD_USE = SoundEvent.of(Identifier.ofVanilla("item.spear_wood.use")));
            registry.register(Identifier.ofVanilla("item.spear.lunge"), Spears.SPEAR_LUNGE = SoundEvent.of(Identifier.ofVanilla("item.spear.lunge")));
        });
        event.register(RegistryKeys.ITEM, registry -> {
            registry.register(Identifier.ofVanilla("wooden_spear"), Spears.WOODEN_SPEAR = Spears.registerSpearRaw(ToolMaterials.WOOD, 0.65F, 0.7F, 0.75F, 5.0F, 14.0F, 6.0F, 5.1F, 15.0F, 4.6F));
            registry.register(Identifier.ofVanilla("stone_spear"), Spears.STONE_SPEAR = Spears.registerSpearRaw(ToolMaterials.STONE, 0.75F, 0.82F, 0.7F, 4.5F, 10.0F, 5.5F, 5.1F, 13.75F, 4.6F));
            registry.register(Identifier.ofVanilla("golden_spear"), Spears.GOLDEN_SPEAR = Spears.registerSpearRaw(ToolMaterials.GOLD, 0.95F, 0.7F, 0.7F, 3.5F, 10.0F, 5.5F, 5.1F, 13.75F, 4.6F));
            registry.register(Identifier.ofVanilla("iron_spear"), Spears.IRON_SPEAR = Spears.registerSpearRaw(ToolMaterials.IRON, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 4.5F, 5.1F, 11.25F, 4.6F));
            registry.register(Identifier.ofVanilla("diamond_spear"), Spears.DIAMOND_SPEAR = Spears.registerSpearRaw(ToolMaterials.DIAMOND, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F));
            registry.register(Identifier.ofVanilla("netherite_spear"), Spears.NETHERITE_SPEAR = Spears.registerSpearRaw(ToolMaterials.NETHERITE, 1.15F, 1.2F, 0.4F, 2.5F, 7.0F, 3.5F, 5.1F, 8.75F, 4.6F));
            if(ModList.get().isLoaded("copperagebackport") && Spears.COPPER_SPEAR == null) registry.register(Identifier.ofVanilla("copper_spear"), Spears.tryLoadCopperSpear(true, true));
        });
        event.register(RegistryKeys.ENCHANTMENT_EFFECT_COMPONENT_TYPE, registry -> Spears.POST_PIERCING_ATTACK = POST_PIERCING_ATTACK.get());
        event.register(RegistryKeys.CRITERION, registry -> registry.register(Identifier.ofVanilla("spear_mobs"), Spears.SPEARED_MOBS = new SpearedMobs()));
    }
    private void modifyComponents(ModifyDefaultComponentsEvent event) {
        if(Spears.makeConfig().getOrDefault("tridents_use_spear_attack_animation", false)) event.modify(Items.TRIDENT, builder -> builder.add(PIERCING_WEAPON.get(), new PiercingWeapon( 0.2F, true, false, Optional.of(SoundEvents.ITEM_TRIDENT_THROW), Optional.empty())).add(SWING_ANIMATION.get(), new SwingAnimation(20, "stab")).add(MINIMUM_ATTACK_CHARGE.get(), 0.9F).add(ATTACK_RANGE.get(), new AttackRange(0F, 4F)));
    }
    private void buildContents(final BuildCreativeModeTabContentsEvent itemGroup) {
        if(itemGroup.getTabKey() == ItemGroups.COMBAT) {
            itemGroup.insertAfter(Items.NETHERITE_SWORD.getDefaultStack(), Spears.WOODEN_SPEAR.getDefaultStack(), StackVisibility.PARENT_AND_SEARCH_TABS);
            itemGroup.insertAfter(Spears.WOODEN_SPEAR.getDefaultStack(), Spears.STONE_SPEAR.getDefaultStack(), StackVisibility.PARENT_AND_SEARCH_TABS);
            if(Spears.COPPER_SPEAR != null) {
                itemGroup.insertAfter(Spears.STONE_SPEAR.getDefaultStack(), Spears.COPPER_SPEAR.getDefaultStack(), StackVisibility.PARENT_AND_SEARCH_TABS);
                itemGroup.insertAfter(Spears.COPPER_SPEAR.getDefaultStack(), Spears.IRON_SPEAR.getDefaultStack(), StackVisibility.PARENT_AND_SEARCH_TABS);
            }
            else itemGroup.insertAfter(Spears.STONE_SPEAR.getDefaultStack(), Spears.IRON_SPEAR.getDefaultStack(), StackVisibility.PARENT_AND_SEARCH_TABS);
            itemGroup.insertAfter(Spears.IRON_SPEAR.getDefaultStack(), Spears.GOLDEN_SPEAR.getDefaultStack(), StackVisibility.PARENT_AND_SEARCH_TABS);
            itemGroup.insertAfter(Spears.GOLDEN_SPEAR.getDefaultStack(), Spears.DIAMOND_SPEAR.getDefaultStack(), StackVisibility.PARENT_AND_SEARCH_TABS);
            itemGroup.insertAfter(Spears.DIAMOND_SPEAR.getDefaultStack(), Spears.NETHERITE_SPEAR.getDefaultStack(), StackVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                PlayerStabC2SPacket.ID,
                PlayerStabC2SPacket.PACKET_CODEC,
                (packet, context) -> {
                    var p = packet.getEntity(context.player().getWorld());
                    if(p instanceof ServerPlayerEntity sp) {
                        if(sp.getMainHandStack().get(Spears.PIERCING_WEAPON) instanceof PiercingWeapon pw) pw.stab(sp, EquipmentSlot.MAINHAND);
                        sp.resetLastAttackedTicks();
                    }
                });
        registrar.playToServer(
                TriggerStabEffectsC2SPacket.ID,
                TriggerStabEffectsC2SPacket.PACKET_CODEC,
                (packet, context) -> {
                    if(packet.toggle() && context.player() instanceof ServerPlayerEntity s) packet.trigger(s);
                });
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = Spears.MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent public static void onClientSetup(FMLClientSetupEvent event) {
            SpearsClient.sendPacketUniversal = (payload) -> {
                PacketDistributor.sendToServer(payload);
                return true;
            };
        }
    }
}
