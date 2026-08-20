package cc.thonly.reverie_dreams.util;

public class YsmHolder {
    private static boolean INITIALIZED = false;

    public static void setInitialized() {
        YsmHolder.INITIALIZED = true;
    }

    public static boolean isInitialized() {
        return INITIALIZED;
    }
}
