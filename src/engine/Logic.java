package engine;

public final class Logic {
    public static float clamp(Number value, Number min, Number max) { return Math.max(min.floatValue(), Math.min(max.floatValue(), value.floatValue())); }
}
