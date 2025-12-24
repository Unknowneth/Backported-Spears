package com.notunanancyowen.spears.packets;

import com.notunanancyowen.spears.Spears;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record TriggerStabEffectsC2SPacket(boolean toggle) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, TriggerStabEffectsC2SPacket> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL,
            TriggerStabEffectsC2SPacket::toggle,
            TriggerStabEffectsC2SPacket::new
    );
    public void trigger(ServerPlayerEntity me) {
        Hand hand = me.preferredHand == null ? Hand.MAIN_HAND : me.preferredHand;
        ItemStack stack = me.getStackInHand(hand);
        if (!stack.isEmpty() && me.getWorld() instanceof ServerWorld server) {
            ItemEnchantmentsComponent itemEnchantmentsComponent = stack.get(DataComponentTypes.ENCHANTMENTS);
            if (itemEnchantmentsComponent != null && !itemEnchantmentsComponent.isEmpty()) {
                EnchantmentEffectContext enchantmentEffectContext = new EnchantmentEffectContext(stack, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, me);
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
                    RegistryEntry<Enchantment> registryEntry = entry.getKey();
                    int lvl = EnchantmentHelper.getLevel(registryEntry, stack);
                    if (registryEntry.value().slotMatches(hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND)) {
                        for (var enchantmentEffectEntry : registryEntry.value().getEffect(Spears.POST_PIERCING_ATTACK)) {
                            LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(server).add(LootContextParameters.THIS_ENTITY, me).add(LootContextParameters.ENCHANTMENT_LEVEL, lvl).add(LootContextParameters.ORIGIN, me.getPos()).build(LootContextTypes.ENCHANTED_ENTITY);
                            if (enchantmentEffectEntry.test(new LootContext.Builder(lootContextParameterSet).build(Optional.empty()))) enchantmentEffectEntry.effect().apply(server, lvl, enchantmentEffectContext, me, me.getPos());
                        }
                    }
                }
            }
        }
    }
    @Override public Id<? extends CustomPayload> getId() {
        return ID;
    }
    public static final Id<TriggerStabEffectsC2SPacket> ID = new Id<>(Identifier.of(Spears.MOD_ID, "send_stab_effects_to_server"));
}
