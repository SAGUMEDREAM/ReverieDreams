package cc.thonly.reverie_dreams.state;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum SixteenDirection implements StringRepresentable {
    SOUTH,
    SOUTH_SOUTHWEST,
    SOUTHWEST,
    WEST_SOUTHWEST,
    WEST,
    WEST_NORTHWEST,
    NORTHWEST,
    NORTH_NORTHWEST,
    NORTH,
    NORTH_NORTHEAST,
    NORTHEAST,
    EAST_NORTHEAST,
    EAST,
    EAST_SOUTHEAST,
    SOUTHEAST,
    SOUTH_SOUTHEAST;

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static SixteenDirection fromYaw(double yaw) {
        double adjustedYaw = yaw + 180;
        int index = Math.floorMod((int)Math.round(adjustedYaw / 22.5), 16);
        return values()[index];
    }

    public float getYaw() {
        return this.ordinal() * 22.5f;
    }
}