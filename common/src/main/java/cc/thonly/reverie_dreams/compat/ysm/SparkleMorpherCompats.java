package cc.thonly.reverie_dreams.compat.ysm;

import cc.thonly.reverie_dreams.util.YsmHolder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SparkleMorpherCompats {
    public static void bootstrap() {
        try {
            YsmHolder.setInitialized();
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }
}
