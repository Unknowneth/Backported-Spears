package com.notunanancyowen.spears.dataholders;

import net.minecraft.util.math.Vec3d;

public interface MovementFixer {
    Vec3d getMovement();
    default void setMovement(Vec3d movement) {};
}
