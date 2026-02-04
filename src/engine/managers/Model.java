package engine.managers;

import engine.utils.BoundingBox;
import engine.utils.Vector;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_POINTS;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.*;

@SuppressWarnings("unused")
public final class Model {
    private static final String INDICES_FIELD = "Indices", TEXTURES_FIELD = "Texture";
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

    public static void remove(String model) {remove(model, null, true);}
    public static void remove(String model, String shader) {remove(model, shader, false);}
    public static void remove(String model, Shader shader) {remove(model, shader.fileName, false);}
    private static void remove(String model, String shader, boolean bypassShader) {
        if (!models.containsKey(model)) return;
        Map<String, Model> map = models.get(model);
        if (bypassShader) {
            for (Model m: map.values()) m.cleanupBuffers();
            models.remove(model);
        } else if (map.containsKey(shader)) {
            Model m = map.get(shader);
            m.cleanupBuffers();
            map.remove(shader);
            if (map.isEmpty()) models.remove(model);
        }
    }

    public static void cleanup() {
        if (Main.isRunning()) return;

        for (Map<String, Model> map: models.values())
            for (Model m: map.values())
                m.cleanupBuffers();
        models.clear();
    }

    // -----------------------------------------------------------------------------------------------------------------

    private final Map<String, List<engine.utils.Vector>> fields = new HashMap<>();
    public final Shader shader;
    private int type = GL_POINTS;
    private int activeType = 4;
    private String activeParameter = null;
    @Getter private final String fileName, name;
    private final int VBO, EBO;
    private int vertexCount = 0;
    private boolean cullFront = false, cullBack = true, active = true;
    private int[] indicesBuffer;
    private float[] verticesBuffer;
    @Getter private final BoundingBox boundingBox = new BoundingBox();

    private Model(String model, Shader shader) {
        if ((fileName = parseModel(name = model)) == null) throw new RuntimeException("File \"models/%s\" not found or supported.".formatted(model));
        generateVertices(shader);

        glBindBuffer(GL_ARRAY_BUFFER, VBO = glGenBuffers());
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO = glGenBuffers());
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        this.shader = shader;
    }

    public void bindVBO() {
        if (!active) return;

        shader.bindVBO(VBO);
    }

    public void render() {
        if (!active) return;

        if (cullFront || cullBack) {
            glEnable(GL_CULL_FACE);
            glCullFace(cullFront ? GL_FRONT : GL_BACK);
        } else glDisable(GL_CULL_FACE);

        // glBindBuffer(GL_ARRAY_BUFFER, VBO);
        shader.bind();
        bindVBO();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);

        glDrawElements(type, vertexCount, GL_UNSIGNED_INT, 0);
    }

    private void cleanupBuffers() {
        if (!active) return;
        active = false;

        glDeleteBuffers(VBO);
        glDeleteBuffers(EBO);
    }

    private void generateVertices(Shader shader) {
        List<String> sortedFields = new ArrayList<>(fields.keySet());
        int vertexSize = shader.getVertexSize();

        List<engine.utils.Vector> buffer = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<engine.utils.Vector, Integer> seen = new HashMap<>();

        int counter = 0;
        if (fields.containsKey(INDICES_FIELD)) {
            vertexCount = fields.get(INDICES_FIELD).size();
            for (engine.utils.Vector index: fields.get(INDICES_FIELD)) {
                if (seen.containsKey(index)) {
                    indices.add(seen.get(index));
                    continue;
                }
                indices.add(counter);
                seen.put(index, counter++);

                int i = 0;
                engine.utils.Vector v = engine.utils.Vector.ofSize(vertexSize);
                for (String field: sortedFields) {
                    if (field.equals("Indices")) continue;

                    int ind = 0;
                    try { ind = index.getInt(i++); } catch (IndexOutOfBoundsException ignored) {}

                    engine.utils.Vector v2 = new engine.utils.Vector();
                    try { v2 = fields.get(field).get(ind); } catch (IndexOutOfBoundsException ignored) {}

                    int layoutID = shader.getFieldLocation(field);
                    int offset = shader.getLayoutOffset(layoutID);
                    engine.utils.Vector inversion = shader.getInversionVector(layoutID);
                    for (int j=0; j<v2.size(); j++)
                        v.set(offset+j, inversion.getInt(j) == 1 ? 1 - v2.get(j).doubleValue() : v2.get(j));
                }
                buffer.add(v);
            }
        } else {
            vertexCount = sortedFields.stream().mapToInt(s -> fields.get(s).size()).max().orElse(0);
            for (int i = 0; i<vertexCount; i++) {
                engine.utils.Vector v = engine.utils.Vector.ofSize(vertexSize);
                for (String field: sortedFields) {
                    engine.utils.Vector v2;
                    try { v2 = fields.get(field).get(i); }
                    catch (IndexOutOfBoundsException ignored) { v2 = new engine.utils.Vector(); }

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

        boundingBox.setMin(
                buffer.stream().mapToDouble(engine.utils.Vector::getFirstDouble).min().orElse(0),
                buffer.stream().mapToDouble(v -> v.getDouble(1)).min().orElse(0),
                buffer.stream().mapToDouble(v -> v.getDouble(2)).min().orElse(0)
        );
        boundingBox.setMax(
                buffer.stream().mapToDouble(engine.utils.Vector::getFirstDouble).max().orElse(0),
                buffer.stream().mapToDouble(v -> v.getDouble(1)).max().orElse(0),
                buffer.stream().mapToDouble(v -> v.getDouble(2)).max().orElse(0)
        );

        double[] flattened = buffer.stream().map(engine.utils.Vector::toDoubleArray).flatMapToDouble(Arrays::stream).toArray();
        verticesBuffer = new float[flattened.length];
        IntStream.range(0, flattened.length).forEach(i -> verticesBuffer[i] = (float) flattened[i]);

        this.indicesBuffer = indices.stream().mapToInt(i->i).toArray();
    }

    private String parseModel(String model) {
        try {
            List<engine.utils.Vector> list = null;
            String line, file;

            InputStream stream = ClassLoader.getSystemResourceAsStream(file = "models/%s".formatted(model));
            if (stream == null) stream = ClassLoader.getSystemResourceAsStream(file = "models/%s.glitchedObj".formatted(model));
            if (stream == null) return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            if (file.endsWith(".glitchedObj")) {
                // Glitched OBJ format
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("//")) continue;
                    if (line.startsWith("$$")) list = decode$$(line);
                    else if (line.startsWith("$")) list = decode$(line);
                    else decode(line, list);
                }
            } else return null;

            return file;
        } catch (NullPointerException | IOException ignored) {}
        return null;
    }

    private List<engine.utils.Vector> decode$$(String line) {
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
                activeParameter = INDICES_FIELD;
                yield type;
            }
            case "texture", "textures", "uv" -> {
                activeType = 4;
                activeParameter = TEXTURES_FIELD;
                yield type;
            }

            case "cull_disable", "culling_disable" -> { cullFront = cullBack = false; yield type; }
            case "cull_back", "culling_back" -> { cullBack = true; cullFront = false; yield type; }
            case "cull_front", "culling_front" -> { cullBack = false; cullFront = true; yield type; }

            default -> type;
        };

        return getField();
    }

    private List<engine.utils.Vector> decode$(String line) {
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
            activeParameter = INDICES_FIELD;
        }
        if (activeParameter.equalsIgnoreCase("Texture") || activeParameter.equalsIgnoreCase("Textures") || activeParameter.equalsIgnoreCase("UV"))
            activeParameter = TEXTURES_FIELD;

        return getField();
    }

    private void decode(String line, List<engine.utils.Vector> list) {
        if (list == null) return;

        String[] vectors = line.trim().split("\\|");
        for (String v: vectors)
            list.add(new engine.utils.Vector(Arrays.stream(v.trim().split("\\s+"))
                    .map(s -> s.isBlank() ? "0" : s)
                    .map(Float::parseFloat)
                    .toArray(Float[]::new)
            ).setType(activeType));
    }

    private List<engine.utils.Vector> getField() {
        if (activeParameter == null) return null;

        List<engine.utils.Vector> list;
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
        lines.add("Status: " + (active ? "Active" : "Inactive"));

        for (Map.Entry<String, List<Vector>> entry: fields.entrySet())
            lines.add(entry.getKey() + ": " + entry.getValue().toString().replaceFirst("\\[(.*)]", "$1"));

        return String.join("\n", lines);
    }
}
