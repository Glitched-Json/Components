package engine;

import com.glitched.annotations.Uniform;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@SuppressWarnings("unused")
public abstract class Entity {
    protected Model model;
    protected final Vector3f
            position = new Vector3f(0),
            rotation = new Vector3f(0),
            scale = new Vector3f(1);

    public Entity(String model) {this(Model.get(model));}
    public Entity(String model, Shader shader) {this(Model.get(model, shader));}
    public Entity(String model, String shader) {this(Model.get(model, shader));}
    public Entity(Model model) {
        this.model = model;
    }

    public void update(double dt) {}

    public void render() {
        model.shader.bind();
        model.bindVBO();
        model.shader.applyUniforms(this);
        model.render();
    }

    @Uniform
    private float[] transform() {
        return new Matrix4f().identity()
                .translate(position.x, position.y, position.z)
                .scale(scale.x, scale.y, scale.z)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z))
                .get(new float[16]);
    }

    @Override
    public String toString() {
        return """
               Model: %s
               """.formatted(model.getFileName());
    }
}
