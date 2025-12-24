package com.notunanancyowen.spears.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record SwingAnimation(int swingTicks, String swingType) {
    public static final Codec<SwingAnimation> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.optionalFieldOf("duration", 6).forGetter(SwingAnimation::swingTicks),
                            Codec.STRING.optionalFieldOf("type", "whack").forGetter(SwingAnimation::swingType)
                    )
                    .apply(instance, SwingAnimation::new)
    );
    public static final PacketCodec<RegistryByteBuf, SwingAnimation> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            SwingAnimation::swingTicks,
            PacketCodecs.STRING,
            SwingAnimation::swingType,
            SwingAnimation::new
    );
}
