package engine;

import lombok.Getter;
import org.joml.Vector3i;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL43.*;

public final class Shader {
    private static final Map<String, Shader> shaders = new HashMap<>();
    private static Shader activeShader = null;

    public static Shader get(String file) {
        if (shaders.containsKey(file)) return shaders.get(file);
        Shader shader = new Shader(file);
        shaders.put(file, shader);
        return shader;
    }

    public static Shader get() {
        return activeShader;
    }

    public static void cleanup() {
        for (Shader shader: shaders.values())
            glDeleteProgram(shader.shaderProgram);
        shaders.clear();
    }

    // -----------------------------------------------------------------------------------------------------------------

    private final String fileName;
    private final int shaderProgram;
    private final Map<String, Integer> uniformLocations = new HashMap<>();
    private final Map<String, Integer> fields = new HashMap<>();
    private final List<int[]> layouts = new ArrayList<>();
    private final Pattern layoutPattern = Pattern.compile("layout \\(location = (\\d+)\\) in (\\S+) (\\w+)(?:\\[(\\d+)])?;(?:.*//\\s*(\\$\\s*[a-zA-Z].*))?");
    private final Pattern variablePattern = Pattern.compile("//\\$var\\s+(?:(int|float)?\\s+)?([a-zA-Z]\\w*)\\s+(?:=\\s+(-?\\d+.?\\d*|Settings.[a-zA-Z]\\w*))?");
    private final int VAO, stride;
    @Getter private int vertexSize;

    private Shader(String file) {
        String id = UUID.randomUUID().toString();
        String code = DataManager.readResource("shaders/" + (fileName = file) + ".glsl");
        code = applyVariables(code);

        String[] shaderSourceCodes = code
                .replaceAll("//\\$(Vertex|Geometry|Fragment) Shader\\n", id + "$1" + id)
                .split(id);

        shaderProgram = createProgram();
        glBindVertexArray(VAO = glGenVertexArrays());

        int vertexShader = 0, geometryShader = 0, fragmentShader = 0;
        for (int i=1; i<shaderSourceCodes.length-1; i+=2) switch (shaderSourceCodes[i]) {
            case "Vertex":   vertexShader   = createShader(shaderSourceCodes[i+1], GL_VERTEX_SHADER); break;
            case "Geometry": geometryShader = createShader(shaderSourceCodes[i+1], GL_GEOMETRY_SHADER); break;
            case "Fragment": fragmentShader = createShader(shaderSourceCodes[i+1], GL_FRAGMENT_SHADER); break;
        }

        link(vertexShader, geometryShader, fragmentShader);
        stride = setVertexAttributes();

        findUniforms(code);
    }

    public int getFieldLocation(String field) {
        if (fields.containsKey(field)) return fields.get(field);
        return -1;
    }

    public int getLayoutOffset(int location) {
        int offset = 0;
        for (int[] layout: layouts) {
            if (layout[0] == location) return offset;
            offset += layout[1];
        }
        return offset;
    }

    public int getUniform(String uniform) {
        if (uniformLocations.containsKey(uniform)) return uniformLocations.get(uniform);
        int location = glGetUniformLocation(shaderProgram, uniform);
        uniformLocations.put(uniform, location);
        return location;
    }

    /** @noinspection unused*/
    public int getUniform(String uniform, int index) {
        return getUniform(uniform + "[" + index + "]");
    }

    private void findUniforms(String file) {
        Pattern pattern = Pattern.compile("uniform \\w+ (\\w+)");
        Matcher matcher = pattern.matcher(file);

        while (matcher.find())
            uniformLocations.put(matcher.group(1), glGetUniformLocation(shaderProgram, matcher.group(1)));
    }

    private int createProgram() {
        int id = glCreateProgram();
        if (id == 0)
            throw new RuntimeException("Failed to create Shader Program.");

        return id;
    }

    private int createShader(String code, int type) {
        String shaderType = switch (type) {
            case GL_VERTEX_SHADER -> "Vertex";
            case GL_GEOMETRY_SHADER -> "Geometry";
            case GL_FRAGMENT_SHADER -> "Fragment";
            default -> "";
        };

        if (shaderType.equals("Vertex")) generateVAO(code);

        int id;
        if ((id = glCreateShader(type)) == 0)
            throw new RuntimeException("Failed to create " + shaderType + " Shader.");

        glShaderSource(id, code);
        glCompileShader(id);
        if (glGetShaderi(id, GL_COMPILE_STATUS) == 0)
            throw new RuntimeException("Failed to compile " + shaderType + " Shader.\n" + glGetShaderInfoLog(id));

        glAttachShader(shaderProgram, id);
        return id;
    }

    private String applyVariables(String code) {
        List<Variable> variables = new ArrayList<>();

        // locate variables
        Matcher matcher = variablePattern.matcher(code);
        while (matcher.find()) {
            boolean isFloat;
            try {isFloat = matcher.group(1).equals("float");}
            catch (NullPointerException ignored) {isFloat = true;}

            String var = matcher.group(2);

            float value;
            if (matcher.group(3) == null) value = 0;
            else if (matcher.group(3).startsWith("Settings"))
                value = DataManager.getSetting(matcher.group(3).replaceFirst("Settings\\.", ""));
            else try {value = Float.parseFloat(matcher.group(3));}
                catch (NullPointerException ignored) {value = 0;}

            variables.add(new Variable(var, isFloat, value));
        }

        for (Variable var: variables) System.out.println(var + " | " + var.getWord());

        // apply variables
        for (Variable var: variables)
            code = code.replaceAll("\\b" + var.var + "\\b", var.getWord());
        return code;
    }

    private record Variable(String var, boolean isFloat, float defaultValue) {
        public String getWord() {
            if (isFloat) return Float.toString(defaultValue);
            return Integer.toString((int) defaultValue);
        }
    }

    private void generateVAO(String code) {
        layouts.clear();
        Matcher matcher = layoutPattern.matcher(code);
        vertexSize = 0;
        while (matcher.find()) {
            int location = Integer.parseInt(matcher.group(1));

            String type = matcher.group(2);

            String name = matcher.group(3);

            int array;
            try {array = Integer.parseInt(matcher.group(4));}
            catch (NumberFormatException ignored) {array = 0;}

            String[] arguments;
            try {
                arguments = Arrays
                        .stream(matcher.group(5).split("\\$"))
                        .map(s -> s.trim().replaceAll("\\s+", "_"))
                        .filter(s -> !s.isBlank())
                        .map(s -> s.equalsIgnoreCase("normalized") ? "normalized" : s)
                        .toArray(String[]::new);
            } catch (NullPointerException ignored) {arguments = new String[0];}

            fields.put(name, location);
            for (String argument: arguments) {
                if (argument.equals("normalized")) continue;
                fields.put(argument, location);
            }

            Vector3i info = decodeType(type);
            layouts.add(new int[]{location, info.x, info.y, info.z, array, Arrays.asList(arguments).contains("normalized") ? 1 : 0});
            vertexSize += info.x;
        }
        layouts.sort(Comparator.comparingInt(a -> a[0]));
    }

    /** @noinspection SpellCheckingInspection*/
    private Vector3i decodeType(String type) {
        int size = switch (type) {
            case "bool", "int", "float" -> 1;
            case "bvec2", "ivec2", "vec2" -> 2;
            case "bvec3", "ivec3", "vec3" -> 3;
            case "bvec4", "ivec4", "vec4", "mat2" -> 4;
            case "mat3" -> 9;
            case "mat4" -> 16;
            default -> 0;
        };
        int bytes = switch (type) {
            case "bool", "bvec2", "bvec3", "bvec4" -> 1;
            case "int", "ivec2", "ivec3", "ivec4" -> Integer.BYTES;
            case "float", "vec2", "vec3", "vec4", "mat2", "mat3", "mat4" -> Float.BYTES;
            default -> 0;
        };
        int glType = switch (type) {
            case "bool", "bvec2", "bvec3", "bvec4" -> GL_BYTE;
            case "int", "ivec2", "ivec3", "ivec4" -> GL_INT;
            case "float", "vec2", "vec3", "vec4", "mat2", "mat3", "mat4" -> GL_FLOAT;
            default -> 0;
        };
        return new Vector3i(size, bytes, glType);
    }

    private int setVertexAttributes() {
        int offset = 0;
        for (int[] layout: layouts) {
            switch (layout[3]) {
                case GL_INT:
                    glVertexAttribIFormat(layout[0], layout[1], GL_INT, offset);
                    break;
                case GL_FLOAT:
                    glVertexAttribFormat(layout[0], layout[1], GL_FLOAT,  layout[5] == 1, offset);
                    break;
            }
            glVertexAttribBinding(layout[0], 0);
            offset += layout[1] * layout[2];
        }
        return offset;
    }

    public void enableAttributes() {
        for (int[] layout: layouts) glEnableVertexAttribArray(layout[0]);
    }

    public void disableAttributes() {
        for (int[] layout: layouts) glDisableVertexAttribArray(layout[0]);
    }

    public void bindVBO(int VBO) {
        glBindVertexBuffer(0, VBO, 0, stride);
    }

    private void link(int vertexShader, int geometryShader, int fragmentShader) {
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == 0)
            throw new RuntimeException("Failed to link Shader Program.\n" + glGetProgramInfoLog(shaderProgram));

        if (vertexShader   != 0) glDetachShader(shaderProgram, vertexShader);
        if (geometryShader != 0) glDetachShader(shaderProgram, geometryShader);
        if (fragmentShader != 0) glDetachShader(shaderProgram, fragmentShader);

        glValidateProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_VALIDATE_STATUS) == 0)
            throw new RuntimeException("Failed to validate Shader Program.\n" + glGetProgramInfoLog(shaderProgram));
    }

    public void bind() {
        glUseProgram(shaderProgram);
        glBindVertexArray(VAO);
        enableAttributes();
        activeShader = this;
    }

    public void unbind() {
        glUseProgram(0);
        disableAttributes();
        glBindVertexArray(0);
        activeShader = null;
    }

    /** @noinspection unused*/
    public void destroy() {
        unbind();
        if (shaderProgram != 0)
            glDeleteProgram(shaderProgram);
        shaders.remove(fileName);
    }
}
