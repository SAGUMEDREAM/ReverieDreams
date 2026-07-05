package cc.thonly.reverie_dreams.util;

public class YsmModType {
    private static YsmModType.Type TYPE = Type.NONE;

    public static boolean installed() {
        return TYPE != Type.NONE;
    }

    public static void setType(Type TYPE) {
        YsmModType.TYPE = TYPE;
    }

    public static Type getType() {
        return TYPE;
    }

    public enum Type {
        NONE(),
        VANILLA(),
        FOX_MODEL_LOADER(),
        SPARKLE_MORPHER(),
    }
}
