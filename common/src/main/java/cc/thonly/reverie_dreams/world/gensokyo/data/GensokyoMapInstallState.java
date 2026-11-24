package cc.thonly.reverie_dreams.world.gensokyo.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 记录当前 Gensokyo 维度里已经安装过的模板锚点，避免每次启动重复放结构。
 */
public class GensokyoMapInstallState extends SavedData {
    private static final Codec<GensokyoMapInstallState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("installed").orElse(List.of())
                    .forGetter(state -> List.copyOf(state.installed)),
            Codec.STRING.listOf().optionalFieldOf("started", List.of())
                    .forGetter(state -> List.copyOf(state.started)),
            Codec.STRING.listOf().optionalFieldOf("done", List.of())
                    .forGetter(state -> List.copyOf(state.done))
    ).apply(instance, GensokyoMapInstallState::new));

    public static final SavedDataType<GensokyoMapInstallState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("reverie_dreams", "gensokyo_map_install"),
            GensokyoMapInstallState::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Set<String> installed = new HashSet<>();
    private final Set<String> started = new HashSet<>();
    private final Set<String> done = new HashSet<>();

    public GensokyoMapInstallState() {
    }

    public GensokyoMapInstallState(List<String> installed) {
        this.installed.addAll(installed);
    }

    public GensokyoMapInstallState(List<String> installed, List<String> started, List<String> done) {
        this.installed.addAll(installed);
        this.started.addAll(started);
        this.done.addAll(done);
    }

    public boolean isInstalled(String id) {
        return this.installed.contains(id);
    }

    public boolean markInstalled(String id) {
        if (this.installed.add(id)) {
            this.setDirty();
            return true;
        }
        return false;
    }

    public boolean isPieceStarted(String key) {
        return this.started.contains(key);
    }

    public boolean markStarted(String key) {
        if (this.started.add(key)) {
            this.setDirty();
            return true;
        }
        return false;
    }

    public boolean isPieceDone(String key) {
        return this.done.contains(key);
    }

    public boolean markDone(String key) {
        boolean changed = this.done.add(key);
        changed |= this.started.remove(key);
        if (changed) {
            this.setDirty();
        }
        return changed;
    }
}
