package com.notunanancyowen.spears.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.notunanancyowen.spears.Spears;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record UseEffects(float strafeSpeed, boolean allowSprinting, boolean interactVibrations) {
    public static final Codec<UseEffects> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Spears.rangedInclusiveFloat(0.0F, 1.0F).optionalFieldOf("speed_multiplier", 0.2F).forGetter(UseEffects::strafeSpeed),
                            Codec.BOOL.optionalFieldOf("can_sprint", false).forGetter(UseEffects::allowSprinting),
                            Codec.BOOL.optionalFieldOf("interact_vibrations", true).forGetter(UseEffects::interactVibrations)
                    )
                    .apply(instance, UseEffects::new)
    );
    public static final PacketCodec<RegistryByteBuf, UseEffects> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT,
            UseEffects::strafeSpeed,
            PacketCodecs.BOOL,
            UseEffects::allowSprinting,
            PacketCodecs.BOOL,
            UseEffects::interactVibrations,
            UseEffects::new
    );
}
