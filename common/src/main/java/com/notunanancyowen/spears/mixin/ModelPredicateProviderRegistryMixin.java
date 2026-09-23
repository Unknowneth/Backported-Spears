package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.Spears;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelPredicateProviderRegistry.class)
public abstract class ModelPredicateProviderRegistryMixin {
    @Shadow private static ClampedModelPredicateProvider register(Identifier id, ClampedModelPredicateProvider provider) {
        return null;
    }
    static {
        register(Identifier.of(Spears.MOD_ID, "in_gui"), (stack, world, entity, seed) -> stack.getHolder() != null && stack.getFrame() == null ? 0F : 1F);
    }
}
