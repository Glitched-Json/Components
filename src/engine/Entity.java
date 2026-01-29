package engine;

import com.glitched.annotations.Uniform;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@SuppressWarnings("unused")
public abstract class Entity {
    @Getter protected final Vector3f
            position = new Vector3f(0),
            rotation = new Vector3f(0),
            scale = new Vector3f(1);
    private final String modelName;
    protected Model model;
    @Getter private boolean visible = true;
    private final BoundingBox boundingBox = new BoundingBox(this);

    public Entity(String model) {this(Model.get(model));}
    public Entity(String model, Shader shader) {this(Model.get(model, shader));}
    public Entity(String model, String shader) {this(Model.get(model, shader));}
    public Entity(Model model) {
        this.model = model;
        modelName = model.getName();
        boundingBox.setBounds(model.getBoundingBox());
    }

    public final void checkVisibility() {
        visible = Scene.get().getCamera().isInView(boundingBox.getMin(), boundingBox.getMax());
    }

    public void update(double dt) {}

    public void staticUpdate(double dt) {}

    public void render() {render(model.shader);}
    public void render(Shader shader) {
        shader.bind();
        model.bindVBO();
        Shader.get().applyUniforms(this);
        model.render();
    }

    public void setShader(Shader shader) { model = Model.get(modelName, shader); }
    public void setShader(String shader) { model = Model.get(modelName, shader); }

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
        return model.toString();
    }

    public void onDestroy() {}
    public void onCreate() {}
}
