package engine;

import com.glitched.annotations.Uniform;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

@SuppressWarnings("unused")
public abstract class Entity {
    protected final Vector3f
            position = new Vector3f(0),
            rotation = new Vector3f(0),
            scale = new Vector3f(1);
    private final String modelName;
    protected Model model;
    @Getter private boolean visible = true;
    protected final BoundingBox boundingBox = new BoundingBox(this);
    @Getter private int id = 0;
    @Getter protected String texture;

    public Entity(String model) {this(Model.get(model), "");}
    public Entity(String model, Shader shader) {this(Model.get(model, shader), "");}
    public Entity(String model, String shader) {this(Model.get(model, shader), "");}
    public Entity(String model, String shader, String texture) {this(Model.get(model, shader), texture);}
    public Entity(Model model) {this(model, "");}
    public Entity(Model model, String texture) {
        this.model = model;
        modelName = model.getName();
        boundingBox.setBounds(model.getBoundingBox());
        this.texture = texture;
    }

    public final void checkVisibility() {
        visible = Scene.get().getCamera().isInView(boundingBox.getMin(), boundingBox.getMax());
    }

    public void update(double dt) {}

    public void staticUpdate(double dt) {}

    public final void spatialRender() {
        Shader shader = model.shader;
        setShader("spatial_shader");
        render();
        setShader(shader);
    }

    public void render() {render(model.shader);}
    public void render(Shader shader) {
        shader.bind();
        model.bindVBO();
        Shader.get().applyUniforms(this);
        model.render();
    }

    public void setShader(Shader shader) { model = Model.get(modelName, shader); }
    public void setShader(String shader) { model = Model.get(modelName, shader); }

    @Override
    public String toString() {
        return model.toString();
    }

    public void onDestroy() {}
    public void onCreate() {}

    public Vector3f getPosition() { return new Vector3f(position); }
    public Vector3f getScale() { return new Vector3f(scale); }
    public Vector3f getRotation() { return new Vector3f(rotation); }

    public void setID(int id) {if (this.id == 0) this.id = id; }

    public boolean isHighlighted() { return SpatialManager.getId() == getId(); }
    public boolean isClicked() { return isClicked(GLFW_MOUSE_BUTTON_LEFT); }
    public boolean isClicked(int button) { return isHighlighted() && InputManager.isButtonPressed(button); }

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

    @Uniform
    private float[] colorID() {
        return new Vector((id >> 16) & 0xFF, (id >> 8) & 0xFF, id & 0xFF).div(255).toFloatArray();
    }

    @Uniform("AtlasRemapping")
    private float[] atlasRemapping() {
        return new Vector(TextureAtlas.getBounds(texture)).toFloatArray();
    }
}
