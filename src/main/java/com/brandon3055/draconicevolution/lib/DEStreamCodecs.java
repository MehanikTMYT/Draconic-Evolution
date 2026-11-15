package com.brandon3055.draconicevolution.lib;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * port-26.1.2: BCStreamCodec удалён из BrandonsCore-26 (unzip подтверждён).
 * DE-локальные composite-надстройки над vanilla StreamCodec.composite для 4–8 полей
 * (vanilla перегрузки есть только до 6). Семантика 1:1 как в BC 1.21: кодеки пишутся/читаются по порядку.
 */
public class DEStreamCodecs {

    @FunctionalInterface
    public interface Func3<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    @FunctionalInterface
    public interface Func4<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }

    @FunctionalInterface
    public interface Func5<A, B, C, D, E, R> {
        R apply(A a, B b, C c, D d, E e);
    }

    @FunctionalInterface
    public interface Func6<A, B, C, D, E, F, R> {
        R apply(A a, B b, C c, D d, E e, F f);
    }

    @FunctionalInterface
    public interface Func7<A, B, C, D, E, F, G, R> {
        R apply(A a, B b, C c, D d, E e, F f, G g);
    }

    @FunctionalInterface
    public interface Func8<A, B, C, D, E, F, G, H, R> {
        R apply(A a, B b, C c, D d, E e, F f, G g, H h);
    }

    public static <B extends ByteBuf, O, F1, F2, F3> StreamCodec<B, O> composite(
        StreamCodec<? super B, F1> c1, Function<O, F1> g1,
        StreamCodec<? super B, F2> c2, Function<O, F2> g2,
        StreamCodec<? super B, F3> c3, Function<O, F3> g3,
        Func3<F1, F2, F3, O> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public O decode(B buf) {
                return factory.apply(c1.decode(buf), c2.decode(buf), c3.decode(buf));
            }

            @Override
            public void encode(B buf, O value) {
                c1.encode(buf, g1.apply(value));
                c2.encode(buf, g2.apply(value));
                c3.encode(buf, g3.apply(value));
            }
        };
    }

    public static <B extends ByteBuf, O, F1, F2, F3, F4> StreamCodec<B, O> composite(
        StreamCodec<? super B, F1> c1, Function<O, F1> g1,
        StreamCodec<? super B, F2> c2, Function<O, F2> g2,
        StreamCodec<? super B, F3> c3, Function<O, F3> g3,
        StreamCodec<? super B, F4> c4, Function<O, F4> g4,
        Func4<F1, F2, F3, F4, O> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public O decode(B buf) {
                return factory.apply(c1.decode(buf), c2.decode(buf), c3.decode(buf), c4.decode(buf));
            }

            @Override
            public void encode(B buf, O value) {
                c1.encode(buf, g1.apply(value));
                c2.encode(buf, g2.apply(value));
                c3.encode(buf, g3.apply(value));
                c4.encode(buf, g4.apply(value));
            }
        };
    }

    public static <B extends ByteBuf, O, F1, F2, F3, F4, F5> StreamCodec<B, O> composite(
        StreamCodec<? super B, F1> c1, Function<O, F1> g1,
        StreamCodec<? super B, F2> c2, Function<O, F2> g2,
        StreamCodec<? super B, F3> c3, Function<O, F3> g3,
        StreamCodec<? super B, F4> c4, Function<O, F4> g4,
        StreamCodec<? super B, F5> c5, Function<O, F5> g5,
        Func5<F1, F2, F3, F4, F5, O> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public O decode(B buf) {
                return factory.apply(c1.decode(buf), c2.decode(buf), c3.decode(buf), c4.decode(buf), c5.decode(buf));
            }

            @Override
            public void encode(B buf, O value) {
                c1.encode(buf, g1.apply(value));
                c2.encode(buf, g2.apply(value));
                c3.encode(buf, g3.apply(value));
                c4.encode(buf, g4.apply(value));
                c5.encode(buf, g5.apply(value));
            }
        };
    }

    public static <B extends ByteBuf, O, F1, F2, F3, F4, F5, F6> StreamCodec<B, O> composite(
        StreamCodec<? super B, F1> c1, Function<O, F1> g1,
        StreamCodec<? super B, F2> c2, Function<O, F2> g2,
        StreamCodec<? super B, F3> c3, Function<O, F3> g3,
        StreamCodec<? super B, F4> c4, Function<O, F4> g4,
        StreamCodec<? super B, F5> c5, Function<O, F5> g5,
        StreamCodec<? super B, F6> c6, Function<O, F6> g6,
        Func6<F1, F2, F3, F4, F5, F6, O> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public O decode(B buf) {
                return factory.apply(c1.decode(buf), c2.decode(buf), c3.decode(buf), c4.decode(buf), c5.decode(buf), c6.decode(buf));
            }

            @Override
            public void encode(B buf, O value) {
                c1.encode(buf, g1.apply(value));
                c2.encode(buf, g2.apply(value));
                c3.encode(buf, g3.apply(value));
                c4.encode(buf, g4.apply(value));
                c5.encode(buf, g5.apply(value));
                c6.encode(buf, g6.apply(value));
            }
        };
    }

    public static <B extends ByteBuf, O, F1, F2, F3, F4, F5, F6, F7> StreamCodec<B, O> composite(
        StreamCodec<? super B, F1> c1, Function<O, F1> g1,
        StreamCodec<? super B, F2> c2, Function<O, F2> g2,
        StreamCodec<? super B, F3> c3, Function<O, F3> g3,
        StreamCodec<? super B, F4> c4, Function<O, F4> g4,
        StreamCodec<? super B, F5> c5, Function<O, F5> g5,
        StreamCodec<? super B, F6> c6, Function<O, F6> g6,
        StreamCodec<? super B, F7> c7, Function<O, F7> g7,
        Func7<F1, F2, F3, F4, F5, F6, F7, O> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public O decode(B buf) {
                return factory.apply(c1.decode(buf), c2.decode(buf), c3.decode(buf), c4.decode(buf), c5.decode(buf), c6.decode(buf), c7.decode(buf));
            }

            @Override
            public void encode(B buf, O value) {
                c1.encode(buf, g1.apply(value));
                c2.encode(buf, g2.apply(value));
                c3.encode(buf, g3.apply(value));
                c4.encode(buf, g4.apply(value));
                c5.encode(buf, g5.apply(value));
                c6.encode(buf, g6.apply(value));
                c7.encode(buf, g7.apply(value));
            }
        };
    }

    public static <B extends ByteBuf, O, F1, F2, F3, F4, F5, F6, F7, F8> StreamCodec<B, O> composite(
        StreamCodec<? super B, F1> c1, Function<O, F1> g1,
        StreamCodec<? super B, F2> c2, Function<O, F2> g2,
        StreamCodec<? super B, F3> c3, Function<O, F3> g3,
        StreamCodec<? super B, F4> c4, Function<O, F4> g4,
        StreamCodec<? super B, F5> c5, Function<O, F5> g5,
        StreamCodec<? super B, F6> c6, Function<O, F6> g6,
        StreamCodec<? super B, F7> c7, Function<O, F7> g7,
        StreamCodec<? super B, F8> c8, Function<O, F8> g8,
        Func8<F1, F2, F3, F4, F5, F6, F7, F8, O> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public O decode(B buf) {
                return factory.apply(c1.decode(buf), c2.decode(buf), c3.decode(buf), c4.decode(buf), c5.decode(buf), c6.decode(buf), c7.decode(buf), c8.decode(buf));
            }

            @Override
            public void encode(B buf, O value) {
                c1.encode(buf, g1.apply(value));
                c2.encode(buf, g2.apply(value));
                c3.encode(buf, g3.apply(value));
                c4.encode(buf, g4.apply(value));
                c5.encode(buf, g5.apply(value));
                c6.encode(buf, g6.apply(value));
                c7.encode(buf, g7.apply(value));
                c8.encode(buf, g8.apply(value));
            }
        };
    }
}
