package cc.thonly.reverie_dreams.client.scope;

import cc.thonly.reverie_dreams.item.weapon.LunarWeaponType;

/**
 * Pure scope layout and transition rules, independent of a live client.
 */
public final class LunarScopeStyle {
    private LunarScopeStyle() {
    }

    public static float fovScale(LunarWeaponType type) {
        return switch (type) {
            case RIFLE -> .72f;
            case PISTOL -> .82f;
            case SHOTGUN -> .86f;
            case SMG -> .78f;
            case SNIPER -> .25f;
        };
    }

    public static float approach(float amount, boolean aimed, double elapsedSeconds) {
        double seconds = Math.clamp(elapsedSeconds, 0, .1);
        return (float) Math.clamp(amount + ((aimed ? 1 : 0) - amount) * (1 - Math.exp(-seconds * 16)), 0, 1);
    }

    public static float scale(LunarWeaponType type, float amount) {
        return 1 + (fovScale(type) - 1) * Math.clamp(amount, 0, 1);
    }

    public static float fov(float vanillaFov, float dynamicModifier, LunarWeaponType type, float amount) {
        float progress = Math.clamp(amount, 0, 1);
        float dynamic = Float.isFinite(dynamicModifier) && dynamicModifier > .01f ? dynamicModifier : 1;
        // Aiming already applies server SLOWNESS. Remove its dynamic FOV contribution gradually.
        return vanillaFov * (1 - progress + progress / dynamic) * scale(type, progress);
    }

    public static int scopeRadius(int width, int height) {
        return Math.max(1, (int) (Math.min(width, height) * .4));
    }

    public static int circleExtent(int radius, int y) {
        return (int) Math.sqrt(Math.max(0, (double) radius * radius - (double) y * y));
    }
}
