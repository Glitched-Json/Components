package engine.utils;

import com.glitched.annotations.Uniform;
import engine.animation.Animation;
import engine.managers.*;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

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
    private int textureRows = 1, textureColumns = 1, textureX = 0, textureY = 0;
    protected final Animation animation = new Animation(this);

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

    public void move(Vector offset) { position.add(offset.toVector3f()); }
    public void move(Vector3f offset) { position.add(offset); }
    public void move(Number x, Number y, Number z) { position.add(x.floatValue(), y.floatValue(), z.floatValue()); }
    public void rotate(Vector angle) { rotation.add(angle.toVector3f()); }
    public void rotate(Vector3f angle) { rotation.add(angle); }
    public void rotate(Number x, Number y, Number z) { rotation.add(x.floatValue(), y.floatValue(), z.floatValue()); }
    public void setScale(Vector size) { scale.set(size.toVector3f()); }
    public void setScale(Vector3f size) { scale.set(size); }
    public void setScale(Number x, Number y, Number z) { scale.set(x.floatValue(), y.floatValue(), z.floatValue()); }
    public void mulScale(Vector size) { scale.mul(size.toVector3f()); }
    public void mulScale(Vector3f size) { scale.mul(size); }
    public void mulScale(Number x, Number y, Number z) { scale.mul(x.floatValue(), y.floatValue(), z.floatValue()); }

    public final void checkVisibility() {
        visible = Scene.get().getCamera().isInView(boundingBox.getMin(), boundingBox.getMax());
    }

    public void updateAnimation(double dt) { animation.update(dt); }
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

    protected void setTextureRows(int textureRows) { this.textureRows = Math.max(textureRows, 1); }
    protected void setTextureColumns(int textureColumns) { this.textureColumns = Math.max(textureColumns, 1); }
    protected void setTextureX(int textureX) { this.textureX = (int) Logic.clamp(textureX, 0, textureColumns-1);}
    protected void setTextureY(int textureY) { this.textureY = (int) Logic.clamp(textureY, 0, textureRows-1);}

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
        Vector4f bounds = TextureAtlas.getBounds(texture);
        float absGridW = (bounds.z - bounds.x) / textureColumns + bounds.x;
        float absGridH = (bounds.w - bounds.y) / textureRows + bounds.y;
        float gridW = absGridW - bounds.x;
        float gridH = absGridH - bounds.y;
        return new Vector(bounds.x, bounds.y, absGridW, absGridH).add(gridW * textureX, gridH * textureY, gridW * textureX, gridH * textureY).toFloatArray();
    }
}
