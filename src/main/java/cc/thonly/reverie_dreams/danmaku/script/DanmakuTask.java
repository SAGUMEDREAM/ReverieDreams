package cc.thonly.reverie_dreams.danmaku.script;

import com.mojang.serialization.Codec;

public class DanmakuTask {
    public static final Codec<DanmakuTask> EMPTY_CODEC = Codec.unit(DanmakuTask::new);
    public static final Codec<DanmakuTask> CODEC = EMPTY_CODEC;
    private DanmakuTask() {

    }
    public void runTask() {

    }
}
