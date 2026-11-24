package cc.thonly.reverie_dreams.entity.aquatic;

/** Stable species descriptions; no registry values are resolved while these constants initialize. */
public enum AquaticSpecies {
    TROUT("trout", .45f, .30f, 4, .55, Habitat.OPEN_WATER, true),
    LAMPREY("lamprey", .35f, .25f, 4, .35, Habitat.BOTTOM_SWIMMER, true),
    SALMON("salmon", .55f, .40f, 5, .60, Habitat.OPEN_WATER, true),
    TUNA("tuna", .85f, .55f, 8, .75, Habitat.OPEN_WATER, true),
    SUPREME_TUNA("supreme_tuna", 1.0f, .65f, 10, .70, Habitat.OPEN_WATER, true),
    SHRIMP("shrimp", .35f, .25f, 2, .22, Habitat.BOTTOM_WALKER, false),
    CRAB("crab", .60f, .30f, 4, .20, Habitat.BOTTOM_WALKER, false),
    OCTOPUS("octopus", .70f, .65f, 6, .25, Habitat.BOTTOM_SWIMMER, false),
    SEA_URCHIN("sea_urchin", .35f, .35f, 3, 0, Habitat.STATIONARY_BOTTOM, false);

    public enum Habitat { OPEN_WATER, BOTTOM_SWIMMER, BOTTOM_WALKER, STATIONARY_BOTTOM }

    private final String key;
    private final float width, height;
    private final double health, speed;
    private final Habitat habitat;
    private final boolean flops;

    AquaticSpecies(String key, float width, float height, double health, double speed, Habitat habitat, boolean flops) {
        this.key = key;
        this.width = width;
        this.height = height;
        this.health = health;
        this.speed = speed;
        this.habitat = habitat;
        this.flops = flops;
    }

    public String key() { return this.key; }
    public float width() { return this.width; }
    public float height() { return this.height; }
    public double health() { return this.health; }
    public double speed() { return this.speed; }
    public Habitat habitat() { return this.habitat; }
    public boolean bottomDweller() { return this.habitat != Habitat.OPEN_WATER; }
    public boolean stationary() { return this.habitat == Habitat.STATIONARY_BOTTOM; }
    public boolean flops() { return this.flops; }

    /** Exact shared clip names used by GeckoLib and the Polymer/BIL adapter. */
    public String animationName(boolean inWater, boolean moving) {
        if (this.stationary()) return "animation.idle";
        if (!inWater) return this.flops ? "animation.flop"
                : moving && this.habitat == Habitat.BOTTOM_WALKER ? "animation.walk" : "animation.idle";
        if (!moving) return "animation.idle";
        return this.habitat == Habitat.BOTTOM_WALKER ? "animation.walk" : "animation.swim";
    }
}
