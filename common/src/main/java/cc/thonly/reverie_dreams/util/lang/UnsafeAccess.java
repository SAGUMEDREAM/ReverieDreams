package cc.thonly.reverie_dreams.util.lang;

import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

@Slf4j
public class UnsafeAccess {
    private static Unsafe __UNSAFE = null;

    public static Unsafe get_unsafe() {
        if (__UNSAFE == null) {
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                __UNSAFE = (Unsafe) f.get(null);
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
        return __UNSAFE;
    }
}
