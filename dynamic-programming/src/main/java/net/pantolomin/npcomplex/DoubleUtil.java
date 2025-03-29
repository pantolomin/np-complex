package net.pantolomin.npcomplex;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DoubleUtil {
    private static final double TINY_DOUBLE = 1e-10;

    public static boolean ltZero(double value) {
        return value < -TINY_DOUBLE;
    }

    public static boolean gtZero(double value) {
        return value > TINY_DOUBLE;
    }

    public static boolean leZero(double value) {
        return value < TINY_DOUBLE;
    }

    public static boolean geZero(double value) {
        return value > -TINY_DOUBLE;
    }

    public static boolean isZero(double value) {
        return geZero(value) && leZero(value);
    }

    public static boolean lt(double value, double value2) {
        return value < value2 - TINY_DOUBLE;
    }

    public static boolean gt(double value, double value2) {
        return value > value2 + TINY_DOUBLE;
    }

    public static boolean le(double value, double value2) {
        return value < value2 + TINY_DOUBLE;
    }

    public static boolean ge(double value, double value2) {
        return value > value2 - TINY_DOUBLE;
    }

    public static boolean isEqual(double value, double value2) {
        return isZero(value - value2);
    }
}
