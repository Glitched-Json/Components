package engine.utils;

import lombok.AllArgsConstructor;

import java.util.Arrays;

import static org.lwjgl.opengl.GL20.*;

@AllArgsConstructor
public enum UniformFunctions {
    UNIFORM_1I,
    UNIFORM_2I,
    UNIFORM_3I,
    UNIFORM_4I,
    UNIFORM_1F,
    UNIFORM_2F,
    UNIFORM_3F,
    UNIFORM_4F,
    UNIFORM_2M,
    UNIFORM_3M,
    UNIFORM_4M;

    public static UniformFunctions fromState(int state) {
        return switch (state) {
            case  0 -> UNIFORM_1I;
            case  1 -> UNIFORM_2I;
            case  2 -> UNIFORM_3I;
            case  3 -> UNIFORM_4I;
            case  4 -> UNIFORM_1F;
            case  5 -> UNIFORM_2F;
            case  6 -> UNIFORM_3F;
            case  7 -> UNIFORM_4F;
            case  8 -> UNIFORM_2M;
            case  9 -> UNIFORM_3M;
            case 10 -> UNIFORM_4M;
            default -> null;
        };
    }

    public static int toState(UniformFunctions function) {
        return function == null ? -1 : function.ordinal();
    }

    public String toString() {
        return switch (ordinal()) {
            case  0 -> "UNIFORM_1I";
            case  1 -> "UNIFORM_2I";
            case  2 -> "UNIFORM_3I";
            case  3 -> "UNIFORM_4I";
            case  4 -> "UNIFORM_1F";
            case  5 -> "UNIFORM_2F";
            case  6 -> "UNIFORM_3F";
            case  7 -> "UNIFORM_4F";
            case  8 -> "UNIFORM_2M";
            case  9 -> "UNIFORM_3M";
            case 10 -> "UNIFORM_4M";
            default -> "null";
        };
    }

    public void setUniform(int location, float[] values) {
        if (values == null || values.length == 0) return;
        try {
            switch (ordinal()) {
                case  0 -> glUniform1i(location, (int) values[0]);
                case  1 -> glUniform2i(location, (int) values[0], (int) values[1]);
                case  2 -> glUniform3i(location, (int) values[0], (int) values[1], (int) values[2]);
                case  3 -> glUniform4i(location, (int) values[0], (int) values[1], (int) values[2], (int) values[3]);
                case  4 -> glUniform1fv(location, Arrays.copyOf(values, 1));
                case  5 -> glUniform2fv(location, Arrays.copyOf(values, 2));
                case  6 -> glUniform3fv(location, Arrays.copyOf(values, 3));
                case  7 -> glUniform4fv(location, Arrays.copyOf(values, 4));
                case  8 -> glUniformMatrix2fv(location, false, Arrays.copyOf(values, 4));
                case  9 -> glUniformMatrix3fv(location, false, Arrays.copyOf(values, 9));
                case 10 -> glUniformMatrix4fv(location, false, Arrays.copyOf(values, 16));
            }
        } catch (IndexOutOfBoundsException ignored) {}
    }
}
