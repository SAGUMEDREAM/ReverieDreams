package cc.thonly.reverie_dreams.util.block;

import java.util.List;

public class JapaneseDecorAssets {
    public static final List<String> CONNECTION_PREFIXES = List.of(
            "tatami_conn_",
            "tatami_block_conn_",
            "chabudai_conn_"
    );
    public static final List<String> NOREN_IDS = List.of(
            "noren_indigo", "noren_white", "noren_red",
            "noren_black", "noren_brown", "noren_green"
    );
    public static final List<String> FACING_IDS = List.of(
            "tatami_corner", "sudare", "andon", "zabuton", "byobu", "kakejiku"
    );
    public static final List<String> PANE_IDS = List.of("shoji_pane", "ranma");
    public static final List<String> DOOR_IDS = List.of("fusuma_door");
    public static final List<String> SINGLE_MODEL_IDS = List.of(
            "tatami_corner", "sudare", "andon", "chochin", "chochin_hanging",
            "zabuton", "byobu", "kakejiku"
    );
}
