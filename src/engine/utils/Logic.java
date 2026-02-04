package engine.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@SuppressWarnings("unused")
public final class Logic {
    public static float remap(@NotNull Number minA, @NotNull Number maxA, @NotNull Number minB, @NotNull Number maxB, @NotNull Number value) {
        return minB.floatValue() + (value.floatValue() - minA.floatValue()) * (maxB.floatValue() - minB.floatValue()) / (maxA.floatValue() - minA.floatValue());
    }
    public static float remapClamped(@NotNull Number minA, @NotNull Number maxA, @NotNull Number minB, @NotNull Number maxB, @NotNull Number value) {
        return clamp(remap(minA, maxA, minB, maxB, value), Math.min(minB.doubleValue(), maxB.doubleValue()), Math.max(minB.doubleValue(), maxB.doubleValue()));
    }
    @NotNull @Contract("_, _, _, _, _ -> new")
    public static Vector3f remapClamped(@NotNull Number minA, @NotNull Number maxA, @NotNull Number minB, @NotNull Number maxB, @NotNull Vector3f value) {
        return new Vector3f(remapClamped(minA, maxA, minB, maxB, value.x), remapClamped(minA, maxA, minB, maxB, value.y), remapClamped(minA, maxA, minB, maxB, value.z));
    }

    public static float clamp(Number value, Number min, Number max) { return Math.max(min.floatValue(), Math.min(max.floatValue(), value.floatValue())); }
}
