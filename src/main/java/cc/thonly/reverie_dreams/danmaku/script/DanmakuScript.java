package cc.thonly.reverie_dreams.danmaku.script;

import cc.thonly.reverie_dreams.entity.DanmakuScriptEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
@ApiStatus.Experimental
public class DanmakuScript {
    public static final List<RuntimeInstance> RUNTIME_INSTANCES = new ArrayList<>();
    public static final Codec<DanmakuScript> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("max_tick").forGetter(DanmakuScript::getMaxTick),
                    Codec.list(DanmakuScriptNode.CODEC).fieldOf("nodes").forGetter(DanmakuScript::getNodes)
            ).apply(instance, DanmakuScript::new)
    );
    private final int maxTick;
    private final List<DanmakuScriptNode> nodes = new ArrayList<>();

    private DanmakuScript() {
        this(20 * 5, new ArrayList<>());
    }

    public DanmakuScript(int maxTick, List<DanmakuScriptNode> nodes) {
        this.maxTick = maxTick;
        this.nodes.addAll(nodes);
    }

    public static void tick(MinecraftServer server) {
        for (RuntimeInstance runtimeInstance : RUNTIME_INSTANCES) {
            runtimeInstance.tick();
        }
    }

    public RuntimeInstance createRuntimeInstance(ServerWorld world, double startX, double startY, double startZ) {
        RuntimeInstance runtimeInstance = new RuntimeInstance(world, startX, startY, startZ, this);
        RUNTIME_INSTANCES.add(runtimeInstance);
        return runtimeInstance;
    }

    @Getter
    @Setter
    public static class RuntimeInstance {
        private final ServerWorld world;
        private final double startX;
        private final double startY;
        private final double startZ;
        private final DanmakuScript script;
        private double x;
        private double y;
        private double z;
        private int tick = 0;
        private DanmakuScriptEntity entity;

        protected RuntimeInstance(ServerWorld world, double startX, double startY, double startZ, DanmakuScript script) {
            this.world = world;
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.x = startX;
            this.y = startY;
            this.z = startZ;
            this.script = script;
            this.init();
        }

        public void init() {

        }

        public void tick() {
            if (this.entity == null) {
                return;
            }
            List<DanmakuScriptNode> nodes = this.script.getNodes();
            for (DanmakuScriptNode node : nodes) {
                node.tick(this);
            }
            if (this.tick >= this.script.maxTick) {
                this.entity.discard();
                RUNTIME_INSTANCES.remove(this);
            } else {
                this.tick++;
            }
        }
    }
}
