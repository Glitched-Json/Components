package engine;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public final class Vector {
    private final int size;
    private final Number[] values;
    private int type = 4;

    public Vector(Number... values) {
        this.values = Arrays.copyOf(values, size = values.length);
    }

    public static Vector ofSize(int size) {
        return new Vector(size);
    }
    private Vector(int size) {
        this.values = new Number[this.size = size];
    }

    public Vector setAll(byte[] values) {return setAll(IntStream.range(0, values.length).mapToObj(i -> values[i]).toArray(Byte[]::new));}
    public Vector setAll(short[] values) {return setAll(IntStream.range(0, values.length).mapToObj(i -> values[i]).toArray(Short[]::new));}
    public Vector setAll(int[] values) {return setAll(Arrays.stream(values).boxed().toArray(Integer[]::new));}
    public Vector setAll(long[] values) {return setAll(Arrays.stream(values).boxed().toArray(Long[]::new));}
    public Vector setAll(double[] values) {return setAll(Arrays.stream(values).boxed().toArray(Double[]::new));}
    public Vector setAll(float[] values) {return setAll(IntStream.range(0, values.length).mapToDouble(i -> values[i]).boxed().toArray(Double[]::new));}
    public Vector setAll(Number[] values) {
        IntStream.range(0, size).forEach(i -> {
            if (i < values.length) this.values[i] = values[i];
            else this.values[i] = 0;
        });
        return this;
    }

    public static Vector concatenate(Vector... vectors) { return concatenate(Arrays.asList(vectors)); }
    public static Vector concatenate(List<Vector> vectors) {
        int size = vectors.stream().mapToInt(Vector::size).sum();
        Vector result = ofSize(size);
        int offset = 0;

        for (Vector v: vectors) {
            for (int i=0; i<v.size; i++)
                result.set(offset+i, v.get(i));
            offset += v.size;
        }
        return result;
    }

    /**
     * @param type The primitive type to become default.
     *             <p>0 - Byte</p>
     *             <p>1 - Short</p>
     *             <p>2 - Int</p>
     *             <p>3 - Long</p>
     *             <p>4 - Float</p>
     *             <p>5 - Double</p>
     * @return This Vector instance.
     */
    public Vector setType(int type) {
        this.type = type;
        return this;
    }

    /** @noinspection UnusedReturnValue*/
    public Vector set(int index, Number value) {
        try { values[index] = value; }
        catch (IndexOutOfBoundsException ignored) {}
        return this;
    }

    // --- Getter Methods ----------------------------------------------------------------------------------------------

    public byte[] getByteArray() {
        byte[] result = new byte[size];
        for (int i=0; i<size; i++)
            try { result[i] = values[i].byteValue(); }
        catch (NullPointerException ignored) { result[i] = 0; }
        return result;
    }
    public short[] getShortArray() {
        short[] result = new short[size];
        for (int i=0; i<size; i++)
            try { result[i] = values[i].shortValue(); }
        catch (NullPointerException ignored) { result[i] = 0; }
        return result;
    }
    public int[] getIntArray() {
        int[] result = new int[size];
        for (int i=0; i<size; i++)
            try { result[i] = values[i].intValue(); }
        catch (NullPointerException ignored) { result[i] = 0; }
        return result;
    }
    public long[] getLongArray() {
        long[] result = new long[size];
        for (int i=0; i<size; i++)
            try { result[i] = values[i].longValue(); }
        catch (NullPointerException ignored) { result[i] = 0; }
        return result;
    }
    public float[] getFloatArray() {
        float[] result = new float[size];
        for (int i=0; i<size; i++)
            try { result[i] = values[i].floatValue(); }
        catch (NullPointerException ignored) { result[i] = 0; }
        return result;
    }
    public double[] getDoubleArray() {
        double[] result = new double[size];
        for (int i=0; i<size; i++)
            try { result[i] = values[i].doubleValue(); }
        catch (NullPointerException ignored) { result[i] = 0; }
        return result;
    }

    public int size() {return size;}

    public Number get(int index) {
        try { return values[index]; }
        catch (IndexOutOfBoundsException | NullPointerException ignored) { return 0; }
    }
    public Number getFirst() { return get(0); }
    public Number getLast() { return get(size - 1); }

    public byte getByte(int index) {
        try {return values[index].byteValue();}
        catch (IndexOutOfBoundsException | NullPointerException ignored) {return 0;}
    }
    public byte getFirstByte() { return getByte(0); }
    public byte getLastByte() { return getByte(size-1); }

    public short getShort(int index) {
        try {return values[index].shortValue();}
        catch (IndexOutOfBoundsException | NullPointerException ignored) {return 0;}
    }
    public short getFirstShort() { return getShort(0); }
    public short getLastShort() { return getShort(size-1); }

    public int getInt(int index) {
        try {return values[index].intValue();}
        catch (IndexOutOfBoundsException | NullPointerException ignored) {return 0;}
    }
    public int getFirstInt() { return getInt(0); }
    public int getLastInt() { return getInt(size-1); }

    public long getLong(int index) {
        try {return values[index].longValue();}
        catch (IndexOutOfBoundsException | NullPointerException ignored) {return 0;}
    }
    public long getFirstLong() { return getLong(0); }
    public long getLastLong() { return getLong(size-1); }

    public float getFloat(int index) {
        try {return values[index].floatValue();}
        catch (IndexOutOfBoundsException | NullPointerException ignored) {return 0;}
    }
    public float getFirstFloat() { return getFloat(0); }
    public float getLastFloat() { return getFloat(size-1); }

    public double getDouble(int index) {
        try {return values[index].doubleValue();}
        catch (IndexOutOfBoundsException | NullPointerException ignored) {return 0;}
    }
    public double getFirstDouble() { return getDouble(0); }
    public double getLastDouble() { return getDouble(size-1); }

    public String toString() {
        return Arrays.toString(Arrays.stream(values)
                .map(n -> n == null ? 0 : n)
                .map(n -> switch (type) {
                    case 0 -> Byte.toString(n.byteValue());
                    case 1 -> Short.toString(n.shortValue());
                    case 2 -> Integer.toString(n.intValue());
                    case 3 -> Long.toString(n.longValue());
                    case 5 -> Double.toString(n.doubleValue());
                    default -> Float.toString(n.floatValue());
                }).toArray());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;

        Vector vector = (Vector) object;
        for (int i=0; i<Math.max(size, vector.size); i++)
            if (!Objects.equals(get(i), vector.get(i))) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = size;
        result = 31 * result + Arrays.hashCode(values);
        result = 31 * result + type;
        return result;
    }
}
