package engine;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_POINTS;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.*;

public final class Model {
    private static final Pattern pattern = Pattern.compile("\\$\\s*(byte|short|integer|int|long|float|double)?\\s*([a-z].*)", Pattern.CASE_INSENSITIVE);
    private static final Map<String, Map<String, Model>> models = new HashMap<>();

    public static Model get(String model) {return get(model, Shader.get());}
    public static Model get(String model, String shader) {return get(model, Shader.get(shader));}
    public static Model get(String model, Shader shader) {
        Map<String, Model> shaderMap = getShaderMap(shader);
        if (shaderMap.containsKey(model)) return shaderMap.get(model);
        Model m = new Model(model, shader);
        shaderMap.put(model, m);
        return m;
    }
    private static Map<String, Model> getShaderMap(Shader shader) {
        if (models.containsKey(shader.fileName)) return models.get(shader.fileName);
        Map<String, Model> map = new HashMap<>();
        models.put(shader.fileName, map);
        return map;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private final Map<String, List<Vector>> fields = new HashMap<>();
    public final Shader shader;
    private int type = GL_POINTS;
    private int activeType = 4;
    private String activeParameter = null;
    @Getter private final String fileName;
    private final int VBO;
    private int vertexCount = 0;
    private boolean cullFront = false, cullBack = true;
    private int[] indicesBuffer;
    private float[] verticesBuffer;

    private Model(String model, Shader shader) {
        parseModel("models/" + (fileName = model) + ".glitchedObj");
        generateVertices(shader);

        glBindBuffer(GL_ARRAY_BUFFER, VBO = glGenBuffers());
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, glGenBuffers()); // EBO
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        shader.bindVBO(VBO);
        this.shader = shader;
    }

    public void bindVBO() {
        shader.bindVBO(VBO);
    }

    private void generateVertices(Shader shader) {
        List<String> sortedFields = new ArrayList<>(fields.keySet());
        int vertexSize = shader.getVertexSize();

        List<Vector> buffer = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<Vector, Integer> seen = new HashMap<>();

        int counter = 0;
        if (fields.containsKey("Indices")) {
            vertexCount = fields.get("Indices").size();
            for (Vector index: fields.get("Indices")) {
                if (seen.containsKey(index)) {
                    indices.add(seen.get(index));
                    continue;
                }
                indices.add(counter);
                seen.put(index, counter++);

                int i = 0;
                Vector v = Vector.ofSize(vertexSize);
                for (String field: sortedFields) {
                    if (field.equals("Indices")) continue;

                    int ind = 0;
                    try { ind = index.getInt(i++); } catch (IndexOutOfBoundsException ignored) {}

                    Vector v2 = new Vector();
                    try { v2 = fields.get(field).get(ind); } catch (IndexOutOfBoundsException ignored) {}

                    int offset = shader.getLayoutOffset(shader.getFieldLocation(field));
                    for (int j=0; j<v2.size(); j++)
                        v.set(offset+j, v2.get(j));
                }
                buffer.add(v);
            }
        } else {
            vertexCount = sortedFields.stream().mapToInt(s -> fields.get(s).size()).max().orElse(0);
            for (int i = 0; i<vertexCount; i++) {
                Vector v = Vector.ofSize(vertexSize);
                for (String field: sortedFields) {
                    Vector v2;
                    try { v2 = fields.get(field).get(i); }
                    catch (IndexOutOfBoundsException ignored) { v2 = new Vector(); }

                    int offset = shader.getLayoutOffset(shader.getFieldLocation(field));
                    for (int j=0; j<v2.size(); j++)
                        v.set(offset+j, v2.get(j));
                }

                if (seen.containsKey(v)) {
                    indices.add(seen.get(v));
                    continue;
                }
                indices.add(counter);
                seen.put(v, counter++);
                buffer.add(v);
            }
        }

        double[] flattened = buffer.stream().map(Vector::getDoubleArray).flatMapToDouble(Arrays::stream).toArray();
        verticesBuffer = new float[flattened.length];
        IntStream.range(0, flattened.length).forEach(i -> verticesBuffer[i] = (float) flattened[i]);

        this.indicesBuffer = indices.stream().mapToInt(i->i).toArray();
    }

    public void render() {
        if (cullFront || cullBack) {
            glEnable(GL_CULL_FACE);
            glCullFace(cullFront ? GL_FRONT : GL_BACK);
        } else glDisable(GL_CULL_FACE);

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glDrawElements(type, vertexCount, GL_UNSIGNED_INT, 0);
    }

    private void parseModel(String model) {
        try {
            List<Vector> list = null;
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(model))));
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("//")) continue;
                if (line.startsWith("$$")) list = decode$$(line);
                else if (line.startsWith("$")) list = decode$(line);
                else decode(line, list);
            }
        } catch (NullPointerException | IOException ignored) {}
    }

    private List<Vector> decode$$(String line) {
        type = switch (line.trim().toLowerCase().replaceFirst("\\$\\$\\s*(.*)", "$1").replaceAll("\\s+", "_")) {
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

            case "cull_disable", "culling_disable" -> { cullFront = cullBack = false; yield type; }
            case "cull_back", "culling_back" -> { cullBack = true; cullFront = false; yield type; }
            case "cull_front", "culling_front" -> { cullBack = false; cullFront = true; yield type; }

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
            list.add(new Vector(Arrays.stream(v.trim().split("\\s+"))
                    .map(s -> s.isBlank() ? "0" : s)
                    .map(Float::parseFloat)
                    .toArray(Float[]::new)
            ).setType(activeType));
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
