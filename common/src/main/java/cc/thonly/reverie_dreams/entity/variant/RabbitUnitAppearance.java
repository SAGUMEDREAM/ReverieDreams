package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.resources.Identifier;

/** Selects the soldier's resources from the synchronized legacy skin ID. */
public enum RabbitUnitAppearance {
    LILAC("rabbit_unit_0"), MINT("rabbit_unit_1"), VIOLET("rabbit_unit_2");

    private final Identifier modelId;
    private final Identifier textureId;

    RabbitUnitAppearance(String name) {
        this.modelId = ReverieDreams.id(name);
        this.textureId = ReverieDreams.id("textures/entity/" + name + ".png");
    }

    public Identifier modelId() { return this.modelId; }
    public Identifier resourceId() { return this.modelId.withPrefix("entity/"); }
    public Identifier textureId() { return this.textureId; }

    public static RabbitUnitAppearance of(Identifier skin) {
        for (var appearance : values()) if (appearance.modelId.equals(skin)) return appearance;
        return LILAC;
    }
}
