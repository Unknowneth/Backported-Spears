package com.notunanancyowen.spears;

import com.mojang.datafixers.util.Function10;
import net.minecraft.network.codec.PacketCodec;

import java.util.function.Function;

public class PacketCodecHelper {
    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1,
            Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2,
            Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3,
            Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4,
            Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5,
            Function<C, T5> from5,
            PacketCodec<? super B, T6> codec6,
            Function<C, T6> from6,
            PacketCodec<? super B, T7> codec7,
            Function<C, T7> from7,
            PacketCodec<? super B, T8> codec8,
            Function<C, T8> from8,
            PacketCodec<? super B, T9> codec9,
            Function<C, T9> from9,
            PacketCodec<? super B, T10> codec10,
            Function<C, T10> from10,
            Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> to
    ) {
        return new PacketCodec<B, C>() {
            @Override
            public C decode(B object) {
                T1 object2 = codec1.decode(object);
                T2 object3 = codec2.decode(object);
                T3 object4 = codec3.decode(object);
                T4 object5 = codec4.decode(object);
                T5 object6 = codec5.decode(object);
                T6 object7 = codec6.decode(object);
                T7 object8 = codec7.decode(object);
                T8 object9 = codec8.decode(object);
                T9 object10 = codec9.decode(object);
                T10 object11 = codec10.decode(object);
                return to.apply(object2, object3, object4, object5, object6, object7, object8, object9, object10, object11);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, (T1)from1.apply(object2));
                codec2.encode(object, (T2)from2.apply(object2));
                codec3.encode(object, (T3)from3.apply(object2));
                codec4.encode(object, (T4)from4.apply(object2));
                codec5.encode(object, (T5)from5.apply(object2));
                codec6.encode(object, (T6)from6.apply(object2));
                codec7.encode(object, (T7)from7.apply(object2));
                codec8.encode(object, (T8)from8.apply(object2));
                codec9.encode(object, (T9)from9.apply(object2));
                codec10.encode(object, (T10)from10.apply(object2));
            }
        };
    }
}
