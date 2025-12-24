package com.notunanancyowen.spears.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.notunanancyowen.spears.Spears;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record AttackRange(float minReach, float maxReach) {

    public static final Codec<AttackRange> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Spears.rangedInclusiveFloat(0.0F, 128.0F).optionalFieldOf("min_reach", 0.0F).forGetter(AttackRange::minReach),
                            Spears.rangedInclusiveFloat(0.0F, 128.0F).optionalFieldOf("max_reach", 3.0F).forGetter(AttackRange::maxReach)
                    )
                    .apply(instance, AttackRange::new)
    );
    public static final PacketCodec<RegistryByteBuf, AttackRange> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT,
            AttackRange::minReach,
            PacketCodecs.FLOAT,
            AttackRange::maxReach,
            AttackRange::new
    );
}
