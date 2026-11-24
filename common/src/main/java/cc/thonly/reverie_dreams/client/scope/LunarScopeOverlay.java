package cc.thonly.reverie_dreams.client.scope;

import cc.thonly.reverie_dreams.item.weapon.LunarWeaponType;

/**
 * Integer-pixel overlay geometry shared by the actual HUD and offline review.
 */
public final class LunarScopeOverlay {
    private LunarScopeOverlay() {
    }

    @FunctionalInterface
    public interface Fill {
        void fill(int x1, int y1, int x2, int y2, int argb);
    }

    private static int fade(int rgb, float opacity) {
        return ((int) (255 * Math.clamp(opacity, 0, 1)) << 24) | (rgb & 0xffffff);
    }

    private static void line(Fill g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, Math.max(x1 + 1, x2), Math.max(y1 + 1, y2), color);
    }

    public static void draw(Fill g, LunarWeaponType type, int width, int height, float amount) {
        int w = width, h = height, cx = w / 2, cy = h / 2;
        int white = fade(0xc7f5e4, amount), teal = fade(0x74d6cd, amount);
        if (type == LunarWeaponType.SNIPER) {
            int r = LunarScopeStyle.scopeRadius(w, h), mask = fade(0x05090d, .96f * amount);
            g.fill(0, 0, w, cy - r, mask);
            g.fill(0, cy + r, w, h, mask);
            for (int y = -r; y < r; y += 3) {
                int extent = LunarScopeStyle.circleExtent(r, y), bottom = Math.min(cy + r, cy + y + 3);
                g.fill(0, cy + y, cx - extent, bottom, mask);
                g.fill(cx + extent, cy + y, w, bottom, mask);
            }
            line(g, cx - r + 6, cy, cx - 5, cy + 1, white);
            line(g, cx + 5, cy, cx + r - 6, cy + 1, white);
            line(g, cx, cy - r + 6, cx + 1, cy - 5, white);
            line(g, cx, cy + 5, cx + 1, cy + r - 6, white);
            g.fill(cx - 1, cy - 1, cx + 2, cy + 2, teal);
            int gap = Math.max(8, r / 5);
            for (int i = 1; i <= 3; i++) {
                int distance = i * gap;
                if (distance >= r - 8)
                    break;
                line(g, cx - 3, cy + distance, cx + 4, cy + distance + 1, white);
                line(g, cx - distance, cy - 3, cx - distance + 1, cy + 4, white);
                line(g, cx + distance, cy - 3, cx + distance + 1, cy + 4, white);
            }
        } else if (type == LunarWeaponType.SHOTGUN) {
            int r = 12;
            for (int y : new int[]{-r, r}) {
                line(g, cx - r, cy + y, cx - 5, cy + y + 1, white);
                line(g, cx + 5, cy + y, cx + r, cy + y + 1, white);
            }
            line(g, cx - r, cy - r, cx - r + 1, cy - 5, white);
            line(g, cx - r, cy + 5, cx - r + 1, cy + r, white);
            line(g, cx + r, cy - r, cx + r + 1, cy - 5, white);
            line(g, cx + r, cy + 5, cx + r + 1, cy + r, white);
            g.fill(cx, cy, cx + 1, cy + 1, teal);
        } else {
            int gap = type == LunarWeaponType.SMG ? 5 : 3, length = type == LunarWeaponType.PISTOL ? 4 : 7;
            line(g, cx - gap - length, cy, cx - gap, cy + 1, white);
            line(g, cx + gap, cy, cx + gap + length, cy + 1, white);
            line(g, cx, cy + gap, cx + 1, cy + gap + length, white);
            g.fill(cx, cy, cx + 1, cy + 1, teal);
        }
    }
}
