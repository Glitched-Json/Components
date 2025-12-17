package engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_POINTS;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.*;

public final class Model {
    private static final Pattern pattern = Pattern.compile("\\$\\s*(byte|short|integer|int|long|float|double)?\\s*([a-z].*)", Pattern.CASE_INSENSITIVE);
    private final Map<String, List<Vector>> fields = new HashMap<>();
    private int type = GL_POINTS;
    private int activeType = 4;
    private String activeParameter = null;
    private final String fileName;
    private final int VBO;
    private int vertexCount = 0;

    public Model(String model) {
        parseModel("models/" + (fileName = model) + ".glitchedObj");
        VBO = glGenBuffers();
    }

    public Model generate() {return generate(Shader.get());}
    public Model generate(Shader shader) {
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, generateVertices(shader), GL_STATIC_DRAW);
        shader.bindVBO(VBO);
        return this;
    }

    private float[] generateVertices(Shader shader) {
        List<String> sortedFields = new ArrayList<>(fields.keySet());
        vertexCount = sortedFields.stream().mapToInt(s -> fields.get(s).size()).min().orElse(0);
        int vertexSize = shader.getVertexSize();

        List<Vector> buffer = new ArrayList<>();
        for (int i = 0; i<vertexCount; i++) {
            Vector v = Vector.ofSize(vertexSize);
            for (String field: sortedFields) {
                Vector v2 = fields.get(field).get(i);
                int offset = shader.getLayoutOffset(shader.getFieldLocation(field));
                for (int j=0; j<v2.size(); j++)
                    v.set(offset+j, v2.get(j));
            }
            buffer.add(v);
        }

        float[] result = new float[vertexSize * vertexCount];
        for (int i=0; i<buffer.size(); i++)
            for (int j = 0; j< vertexSize; j++)
                result[i* vertexSize +j] = buffer.get(i).getFloat(j);
        return result;
    }

    public void render() {
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glDrawArrays(type, 0, vertexCount);
    }

    private void parseModel(String model) {
        try {
            List<Vector> list = null;
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(model))));
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (line.startsWith("$$")) list = decode$$(line);
                else if (line.startsWith("$")) list = decode$(line);
                else decode(line, list);
            }
        } catch (NullPointerException | IOException ignored) {}
    }

    private List<Vector> decode$$(String line) {
        type = switch (line.trim().toLowerCase().replaceFirst("\\$\\$\\s*(.*)", "$1").replaceFirst("\\s+", "_")) {
            case "points" -> GL_POINTS;
            case "lines" -> GL_LINES;
            case "line_loop" -> GL_LINE_LOOP;
            case "line_strip" -> GL_LINE_STRIP;
            case "triangles" -> GL_TRIANGLES;
            case "triangle_strip" -> GL_TRIANGLE_STRIP;
            case "triangle_fan" -> GL_TRIANGLE_FAN;
            case "quads" -> GL_QUADS;
            case "quad_strip" -> GL_QUAD_STRIP;

            case "index", "indices" -> {
                activeType = 2;
                activeParameter = "Indices";
                yield type;
            }

            default -> type;
        };

        return getField();
    }

    private List<Vector> decode$(String line) {
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) return null;

        try { activeType = switch (matcher.group(1).toLowerCase()) {
            case "byte" -> 0;
            case "short" -> 1;
            case "int", "integer" -> 2;
            case "long" -> 3;
            case "double" -> 5;
            default -> 4;
        };} catch (NullPointerException ignored) { activeType = 4; }

        activeParameter = matcher.group(2).trim().replaceAll("\\s+", "_");
        if (activeParameter.equalsIgnoreCase("Indices") || activeParameter.equalsIgnoreCase("Index")) {
            activeType = 2;
            activeParameter = "Indices";
        }

        return getField();
    }

    private void decode(String line, List<Vector> list) {
        if (list == null) return;

        String[] vectors = line.trim().split("\\|");
        for (String v: vectors)
            list.add(new Vector(Arrays.stream(v.trim().split("\\s+")).map(Float::parseFloat).toArray(Float[]::new)).setType(activeType));
    }

    private List<Vector> getField() {
        if (activeParameter == null) return null;

        List<Vector> list;
        if (fields.containsKey(activeParameter)) list = fields.get(activeParameter);
        else fields.put(activeParameter, list = new ArrayList<>());
        return list;
    }

    public String toString() {
        List<String> lines = new ArrayList<>();

        lines.add("Model: " + fileName);
        lines.add("Drawing Mode = " + switch (type) {
            case GL_POINTS -> "Points";
            case GL_LINES -> "Lines";
            case GL_LINE_LOOP -> "Line Loop";
            case GL_LINE_STRIP -> "Line Strip";
            case GL_TRIANGLES -> "Triangles";
            case GL_TRIANGLE_STRIP -> "Triangle Strip";
            case GL_TRIANGLE_FAN -> "Triangle Fan";
            case GL_QUADS -> "Quads";
            case GL_QUAD_STRIP -> "Quad Strip";
            default -> "Undefined";
        });

        for (Map.Entry<String, List<Vector>> entry: fields.entrySet())
            lines.add(entry.getKey() + ": " + entry.getValue().toString().replaceFirst("\\[(.*)]", "$1"));

        return String.join("\n", lines);
    }
}
