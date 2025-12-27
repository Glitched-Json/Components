package engine;

import lombok.AllArgsConstructor;

import java.util.Arrays;

import static org.lwjgl.opengl.GL20.*;

@AllArgsConstructor
public enum UniformFunctions {
    UNIFORM_1I(1),
    UNIFORM_2I(2),
    UNIFORM_3I(3),
    UNIFORM_4I(4),
    UNIFORM_1F(5),
    UNIFORM_2F(6),
    UNIFORM_3F(7),
    UNIFORM_4F(8),
    UNIFORM_2M(9),
    UNIFORM_3M(10),
    UNIFORM_4M(11);

    private final int state;

    public static UniformFunctions fromState(int state) {
        return switch (state) {
            case 1 -> UNIFORM_1I;
            case 2 -> UNIFORM_2I;
            case 3 -> UNIFORM_3I;
            case 4 -> UNIFORM_4I;
            case 5 -> UNIFORM_1F;
            case 6 -> UNIFORM_2F;
            case 7 -> UNIFORM_3F;
            case 8 -> UNIFORM_4F;
            case 9 -> UNIFORM_2M;
            case 10 -> UNIFORM_3M;
            case 11 -> UNIFORM_4M;
            default -> null;
        };
    }

    public static int toState(UniformFunctions function) {
        return function == null ? 0 : function.state;
    }

    public String toString() {
        return switch (state) {
            case 1 -> "UNIFORM_1I";
            case 2 -> "UNIFORM_2I";
            case 3 -> "UNIFORM_3I";
            case 4 -> "UNIFORM_4I";
            case 5 -> "UNIFORM_1F";
            case 6 -> "UNIFORM_2F";
            case 7 -> "UNIFORM_3F";
            case 8 -> "UNIFORM_4F";
            case 9 -> "UNIFORM_2M";
            case 10 -> "UNIFORM_3M";
            case 11 -> "UNIFORM_4M";
            default -> "null";
        };
    }

    public void setUniform(int location, float[] values) {
        if (values == null || values.length == 0) return;
        try {
            switch (state) {
                case 1 -> glUniform1i(location, (int) values[0]);
                case 2 -> glUniform2i(location, (int) values[0], (int) values[1]);
                case 3 -> glUniform3i(location, (int) values[0], (int) values[1], (int) values[2]);
                case 4 -> glUniform4i(location, (int) values[0], (int) values[1], (int) values[2], (int) values[3]);
                case 5 -> glUniform1fv(location, Arrays.copyOf(values, 1));
                case 6 -> glUniform2fv(location, Arrays.copyOf(values, 2));
                case 7 -> glUniform3fv(location, Arrays.copyOf(values, 3));
                case 8 -> glUniform4fv(location, Arrays.copyOf(values, 4));
                case 9 -> glUniformMatrix2fv(location, false, Arrays.copyOf(values, 4));
                case 10 -> glUniformMatrix3fv(location, false, Arrays.copyOf(values, 9));
                case 11 -> glUniformMatrix4fv(location, false, Arrays.copyOf(values, 16));
            }
        } catch (IndexOutOfBoundsException ignored) {}
    }
}
