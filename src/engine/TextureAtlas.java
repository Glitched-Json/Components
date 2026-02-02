package engine;

import lombok.Getter;
import org.joml.Vector4f;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

@SuppressWarnings("unused")
public class TextureAtlas {
    private static final List<String> loadedTextures = new ArrayList<>();
    private static final Map<String, Vector4f> imageMapping = new HashMap<>();
    private static boolean initialized = false;
    private static int id;
    private static BufferedImage atlasImage;
    @Getter private static float textureWidth = 0, textureHeight = 0;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        try {
            List<BufferedImage> images = getTextures();
            List<Vector4i> points = texturePacking(images);
            generateAtlas(points, images);
            ByteBuffer buffer = generateBuffer(points.getFirst());
            generateTexture(points.getFirst(), buffer);
            bind();

            if (DataManager.getFlag("show_initialization_messages"))
                System.out.println("Successfully Initialized Texture Atlas.");
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize Texture Atlas.\n" + e.getMessage());
        }
    }

    public static Vector4f getBounds(String texture) {
        if (texture == null || !imageMapping.containsKey(texture)) return new Vector4f(0, 0, 1, 1);
        return imageMapping.get(texture);
    }

    private static List<BufferedImage> getTextures() throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        for (File file: DataManager.getAllDirectoryResourceFiles("textures")) {
            BufferedImage image = ImageIO.read(file);
            images.add(image);
            loadedTextures.add(file.getName());

            if (DataManager.getFlag("show_texture_atlas_image_loading"))
                System.out.printf("Texture \"%s\" added to Texture Atlas.%n", file.getName());
        }
        return images;
    }

    private static List<Vector4i> texturePacking(List<BufferedImage> images) {
        List<Vector4i> positions = new ArrayList<>();
        int buffer = Math.max(0, (int) DataManager.getSetting("texture_atlas_buffer_size"));

        int w = images.stream().mapToInt(BufferedImage::getWidth).sum() + (images.size()) * buffer * 2;
        int h = images.stream().mapToInt(BufferedImage::getHeight).max().orElse(1) + buffer * 2;
        positions.add(new Vector4i(w, h, 0, 0));

        int x = buffer;
        for (BufferedImage image: images) {
            positions.add(new Vector4i(x, buffer, x + image.getWidth(), buffer + image.getHeight()));
            x += image.getWidth() + buffer * 2;
        }

        return positions;
    }

    private static void generateAtlas(List<Vector4i> points, List<BufferedImage> images) {
        int w = points.getFirst().x, h = points.getFirst().y;
        atlasImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = atlasImage.getGraphics();
        int index = 0;
        for (BufferedImage image: images) {
            Vector4i p = points.get(index + 1);
            g.drawImage(image, p.x, p.y, null);

            g.drawImage(image, p.x, p.y - 1, p.z, p.y, 0, 0, p.z - p.x, 1, null);
            g.drawImage(image, p.x, p.w, p.z, p.w + 1, 0, p.w - p.y - 1, p.z - p.x, p.w - p.y, null);
            g.drawImage(atlasImage, p.x - 1, p.y - 1, p.x, p.z + 1, p.x, p.y - 1, p.x + 1, p.z + 1, null);
            g.drawImage(atlasImage, p.z, p.y - 1, p.z + 1, p.w + 1, p.z - 1, p.y - 1, p.z, p.w + 1, null);

            imageMapping.put(
                    loadedTextures.get(index++).replaceFirst("[.][^.]+$", ""),
                    new Vector4f((float) p.x / w, (float) p.y / h, (float) p.z / w, (float) p.w / h)
            );
            if (textureWidth == 0) {
                textureWidth = (float) (p.z - p.x) / w;
                textureHeight = (float) p.y / h;
            }
        }
        g.dispose();
    }

    private static ByteBuffer generateBuffer(Vector4i dimensions) {
        int w = dimensions.x;
        int h = dimensions.y;
        int[] pixels = new int[w * h];
        atlasImage.getRGB(0, 0, w, h, pixels, 0, w);
        ByteBuffer buffer = ByteBuffer.allocateDirect(w * h * 4);
        for (int i=0; i<h; i++) for (int j=0; j<w; j++) {
            int pixel = pixels[i * w + j];
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >>  8) & 0xFF));
            buffer.put((byte) ((pixel      ) & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }
        buffer.flip();
        return buffer;
    }

    private static void generateTexture(Vector4i dimensions, ByteBuffer buffer) {
        id = GL11.glGenTextures();
        GL11.glBindTexture(GL_TEXTURE_2D, id);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, id);
        GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL11.GL_RGBA8, dimensions.x, dimensions.y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public static void bind() {bind(GL_TEXTURE_2D);}
    public static void bind(int target) {GL11.glBindTexture(target, id);}
    public static void cleanup() {if (initialized && !Main.isRunning()) GL11.glDeleteTextures(id);}

    public static void exportAtlas() {
        try {
            ImageIO.write(atlasImage, "PNG", new File("resources/", "atlas.png"));
        } catch (IOException e) {
            System.err.println("Failed to export ATLAS at location: resources/atlas.png");
        }
    }
}
