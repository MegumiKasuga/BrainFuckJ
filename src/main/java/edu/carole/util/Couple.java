package edu.carole.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class Couple<T, R> implements Iterable<Object> {

    private final T a;
    private final R b;

    public Couple(T a, R b) {
        this.a = a;
        this.b = b;
    }

    public static <T, R> Couple<T, R> of(T a, R b) {
        return new Couple<T, R>(a, b);
    }

    public static <T> Couple<T, T> ofSameClass(T a, T b) {
        return new Couple<T, T>(a, b);
    }

    public static <T, R> Couple<R, T> reverse(Couple<T, R> couple) {
        return new Couple<>(couple.b, couple.a);
    }

    public static <T, R> Couple<T, R> copy(Couple<T, R> couple) {
        return new Couple<>(couple.a, couple.b);
    }

    public T getFirst() {
        return a;
    }

    public R getSecond() {
        return b;
    }

    public Object get(boolean first) {
        return first ? a : b;
    }

    @Override
    public @NotNull Iterator<Object> iterator() {
        return new Iterator<Object>() {

            Object cache = a;

            @Override
            public boolean hasNext() {
                return cache != null;
            }

            @Override
            public Object next() {
                Object result = cache;
                if (cache == a) {
                    cache = b;
                } else if (cache == b) {
                    cache = null;
                }
                return result;
            }
        };
    }
}
