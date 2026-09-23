package com.notunanancyowen.spears.mixin;

import com.notunanancyowen.spears.dataholders.MovementFixer;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements MovementFixer {
    @Unique private Vec3d movement;
    @SuppressWarnings("all")
    @Override public Vec3d getMovement() {
        ServerPlayerEntity me = (ServerPlayerEntity & MovementFixer)(Object)this;
        Entity entity = me.getVehicle();
        return entity instanceof MovementFixer m && entity.getControllingPassenger() != me ? m.getMovement() : movement;
    }
    @SuppressWarnings("all")
    @Override public void setMovement(Vec3d movement) {
        this.movement = movement;
    }
}
