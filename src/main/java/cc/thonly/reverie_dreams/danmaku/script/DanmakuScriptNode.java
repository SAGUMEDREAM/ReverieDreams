package cc.thonly.reverie_dreams.danmaku.script;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class DanmakuScriptNode {
    public static final Codec<DanmakuTask> TASK_CODEC = DanmakuTask.CODEC;
    public static final Codec<DanmakuScriptNodeCondition> CONDITION_CODEC = DanmakuScriptNodeCondition.CODEC;
    public static final Codec<DanmakuScriptNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(TASK_CODEC, CONDITION_CODEC.listOf())
                    .fieldOf("values")
                    .forGetter(DanmakuScriptNode::getTask2conditions)
    ).apply(instance, DanmakuScriptNode::new));
    private final Map<DanmakuTask, List<DanmakuScriptNodeCondition>> task2conditions;

    private DanmakuScriptNode() {
        this(new Object2ObjectOpenHashMap<>());
    }

    public DanmakuScriptNode(Map<DanmakuTask, List<DanmakuScriptNodeCondition>> task2conditions) {
        this.task2conditions = new Object2ObjectOpenHashMap<>(task2conditions);
    }

    public void tick(DanmakuScript.RuntimeInstance runtimeInstance) {
        for (Map.Entry<DanmakuTask, List<DanmakuScriptNodeCondition>> entry : this.task2conditions.entrySet()) {
            DanmakuTask task = entry.getKey();
            List<DanmakuScriptNodeCondition> conditions = entry.getValue();
            boolean allPassed = true;
            for (DanmakuScriptNodeCondition condition : conditions) {
                boolean test = condition.test(runtimeInstance.getTick());
                if (!test && allPassed) {
                    allPassed = false;
                }
            }
            if (allPassed) {
                task.runTask();
            }
        }
    }
}
