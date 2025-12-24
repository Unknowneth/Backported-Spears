package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.Spears;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.item.ItemConvertible;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ZombifiedPiglinEntity.class)
public abstract class ZombifiedPiglinEntityMixin {
    @ModifyArg(method = "initEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;<init>(Lnet/minecraft/item/ItemConvertible;)V"))
    private ItemConvertible spawnWithGoldSpear(ItemConvertible item) {
        if(((ZombifiedPiglinEntity)(Object)this).getRandom().nextInt(20) == 0) return Spears.GOLDEN_SPEAR;
        return item;
    }
}
