package cc.thonly.reverie_dreams.paper.specialweapons;

import cc.thonly.reverie_dreams.paper.util.Ids;
import org.bukkit.inventory.ItemStack;

/** Game balance mirror of the source Lunar Capital numbered weapon series, including legacy alias lookups. */
public enum LunarWeaponType {
    RIFLE(1, "rifle", 30, 2, 40, 4.0f, 1, 3.2f, .6f, 1.3f, 30, true),
    PISTOL(2, "pistol", 12, 7, 26, 6.0f, 1, 3.5f, .5f, 2.0f, 24, false),
    SHOTGUN(3, "shotgun", 8, 16, 56, 2.5f, 8, 3.0f, 6.5f, 4.0f, 7, false),
    SMG(4, "smg", 40, 1, 36, 1.75f, 1, 3.0f, 1.8f, .8f, 18, true),
    SNIPER(5, "sniper", 5, 24, 60, 28.0f, 1, 10.0f, 1.3f, 5.0f, 24, false);

    public static final String LEGACY_WEAPON = "weapon_of_the_moon";
    public static final String LEGACY_MAGAZINE = "magazine";

    private final int number, capacity, shotInterval, reloadTicks, pellets, lifetime;
    private final String kind;
    private final float damage, speed, spread, recoil;
    private final boolean automatic;

    LunarWeaponType(int number, String kind, int capacity, int shotInterval, int reloadTicks,
                    float damage, int pellets, float speed, float spread, float recoil, int lifetime, boolean automatic) {
        this.number = number;
        this.kind = kind;
        this.capacity = capacity;
        this.shotInterval = shotInterval;
        this.reloadTicks = reloadTicks;
        this.damage = damage;
        this.pellets = pellets;
        this.speed = speed;
        this.spread = spread;
        this.recoil = recoil;
        this.lifetime = lifetime;
        this.automatic = automatic;
    }

    public int number() { return number; }
    public String itemPath() { return "lunar_type_" + number + "_" + kind; }
    public String magazinePath() { return "lunar_type_" + number + "_magazine"; }
    public String itemId() { return Ids.MOD_ID + ":" + itemPath(); }
    public String magazineId() { return Ids.MOD_ID + ":" + magazinePath(); }
    public String bulletId() { return Ids.MOD_ID + ":" + switch (this) {
        case SHOTGUN -> "lunar_bullet_pellet";
        case SNIPER -> "lunar_bullet_sniper";
        default -> "lunar_bullet_standard";
    }; }
    public int capacity() { return capacity; }
    public int shotInterval() { return shotInterval; }
    public int reloadTicks() { return reloadTicks; }
    public float damage() { return damage; }
    public float damageAtLevel(int experienceLevel) {
        return damage * (1.0f + .25f * Math.clamp(experienceLevel, 0, 30) / 30.0f);
    }
    /** Independent of visual scale: .5 block entity width multiplied by this value. */
    public float bulletHitboxScale() {
        return switch (this) {
            case RIFLE, SHOTGUN -> .7f;
            case PISTOL, SNIPER -> .8f;
            case SMG -> .6f;
        };
    }
    public int pellets() { return pellets; }
    public float speed() { return speed; }
    public float spread(boolean aimed) { return spread * (aimed ? (this == SNIPER ? .02f : .35f) : 1.0f); }
    public float recoil() { return recoil; }
    public int lifetime() { return lifetime; }
    public boolean automatic() { return automatic; }
    public int aimSlowness() { return this == SNIPER ? 4 : 3; }

    public static LunarWeaponType weapon(ItemStack item) {
        String local = localId(item);
        if (local.equals(LEGACY_WEAPON)) return RIFLE;
        for (LunarWeaponType type : values()) if (local.equals(type.itemPath())) return type;
        return null;
    }

    public static LunarWeaponType weapon(String id) {
        String local = localName(id);
        if (local.equals(LEGACY_WEAPON)) return RIFLE;
        for (LunarWeaponType type : values()) if (local.equals(type.itemPath())) return type;
        return null;
    }

    public static LunarWeaponType magazine(ItemStack item) {
        String local = localId(item);
        if (local.equals(LEGACY_MAGAZINE)) return RIFLE;
        for (LunarWeaponType type : values()) if (local.equals(type.magazinePath())) return type;
        return null;
    }

    public static LunarWeaponType magazine(String id) {
        String local = localName(id);
        if (local.equals(LEGACY_MAGAZINE)) return RIFLE;
        for (LunarWeaponType type : values()) if (local.equals(type.magazinePath())) return type;
        return null;
    }

    private static String localId(ItemStack item) {
        String id = SpecialWeaponsModule.id(item);
        return localName(id);
    }

    private static String localName(String id) {
        int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }
}
