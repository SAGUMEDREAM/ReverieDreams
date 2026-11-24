package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.resources.Identifier;

/** Saved appearance IDs in append-only order. */
public enum YoukaiRabbitAppearance {
    GREEN("youkai_rabbit", false), MILK("youkai_rabbit_milk", false),
    MOON("youkai_rabbit_moon", false), RED_WHITE("youkai_rabbit_red_white", false),
    AMBER("youkai_rabbit_amber", true), NIGHT_SAKURA("youkai_rabbit_night_sakura", true);

    public static final int COUNT = values().length;
    private final String path;
    private final boolean customGeometry;

    YoukaiRabbitAppearance(String path, boolean customGeometry) {
        this.path = path;
        this.customGeometry = customGeometry;
    }
    public Identifier resourceId() { return ReverieDreams.id(this.path); }
    public Identifier geometryId() { return this.customGeometry ? this.resourceId() : GREEN.resourceId(); }
    public static YoukaiRabbitAppearance byId(int id) {
        return values()[Math.clamp(id, 0, COUNT - 1)];
    }
}
