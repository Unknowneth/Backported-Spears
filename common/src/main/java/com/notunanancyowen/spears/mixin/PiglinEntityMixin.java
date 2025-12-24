package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.Spears;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.ItemConvertible;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PiglinEntity.class)
public abstract class PiglinEntityMixin {
    @ModifyArg(method = "makeInitialWeapon", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;<init>(Lnet/minecraft/item/ItemConvertible;)V", ordinal = 1))
    private ItemConvertible spawnWithGoldSpear(ItemConvertible item) {
        if(((PiglinEntity)(Object)this).getRandom().nextInt(10) == 0) return Spears.GOLDEN_SPEAR;
        return item;
    }
}
