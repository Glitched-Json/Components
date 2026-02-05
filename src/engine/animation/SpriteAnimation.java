package engine.animation;

import engine.utils.Entity;
import engine.utils.Logic;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class SpriteAnimation {
    private final Entity entity;
    private int tilesY = 1;
    private final Map<String, Stage> stages = new HashMap<>();
    private final Map<String, Integer> variantTypes = new HashMap<>();
    private final Map<Trigger, Integer> triggers = new HashMap<>();
    private Stage currentStage = null;
    private int currentVariant = 0, prevTileX = -1;
    @Getter private String currentVariantLabel = null, currentStageLabel = null;
    private double t = 0;
    @Getter private int trigger = -1;

    public SpriteAnimation(Entity entity) {
        this.entity = entity;
    }

    public void update(double dt) {
        trigger = -1;
        if (!isRunning()) return;
        if (t > currentStage.time) {
            setStage(currentStage.next);
            if (currentStage == null) return;
        }

        int textureX = (int) Logic.remapClamped(0, currentStage.time, 0, currentStage.tileX, t);
        if (prevTileX != textureX && triggers.containsKey(new Trigger(currentStageLabel, textureX))) trigger = triggers.get(new Trigger(currentStageLabel, textureX));
        prevTileX = textureX;
        entity.setTextureX(textureX);

        t += dt;
    }

    public SpriteAnimation setStage(String stage) {
        if ((currentStage = stages.get(currentStageLabel = stage)) != null) {
            t = 0;
            prevTileX = -1;
            entity.setTextureY(currentStage.tileY + tilesY * currentVariant);
        }
        return this;
    }
    public SpriteAnimation addStage(String stage, int tileY, int maxTileX, Number time) { return addStage(stage, tileY, maxTileX, time, null); }
    public SpriteAnimation addStage(String stage, int tileY, int maxTileX, Number time, String next) {
        stages.put(stage, new Stage(tileY, maxTileX, time.doubleValue(), next));
        return this;
    }

    public SpriteAnimation setVariant(String variant) {
        currentVariant = variantTypes.get(currentVariantLabel = variant);
        if (currentStage != null) entity.setTextureY(currentStage.tileY + tilesY * currentVariant);
        return this;
    }
    public SpriteAnimation addVariant(String variant, int variantNumber) {
        variantTypes.put(variant, variantNumber);
        return this;
    }

    public SpriteAnimation info(int tilesX, int tilesY, int variants) {
        entity.setTextureColumns(Math.max(tilesX, 1));
        entity.setTextureRows((this.tilesY = Math.max(tilesY, 1)) * variants);
        return this;
    }

    public SpriteAnimation addTrigger(String stage, int frame, int triggerID) {
        triggers.put(new Trigger(stage, frame), triggerID);
        return this;
    }

    public boolean isRunning() { return currentStage != null; }

    private record Stage(
            int tileY,
            int tileX,
            double time,
            String next
    ) {}

    private record Trigger(
            String stage,
            int frame
    ) {}
}
