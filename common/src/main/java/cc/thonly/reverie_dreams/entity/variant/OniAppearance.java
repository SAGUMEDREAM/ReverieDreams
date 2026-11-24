package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.resources.Identifier;

/** Resource selection uses the synchronized skin, never a client's random constructor variant. */
public enum OniAppearance {
    GREEN("oni_green"), ORANGE("oni_orange"), RED("oni_red");

    private final Identifier modelId;
    private final Identifier textureId;

    OniAppearance(String name) {
        this.modelId = ReverieDreams.id(name);
        this.textureId = ReverieDreams.id("textures/entity/" + name + ".png");
    }

    public Identifier modelId() { return this.modelId; }
    public Identifier resourceId() { return this.modelId.withPrefix("entity/"); }
    public Identifier textureId() { return this.textureId; }

    public static OniAppearance of(Identifier skin) {
        for (var appearance : values()) if (appearance.modelId.equals(skin)) return appearance;
        return GREEN;
    }
}
