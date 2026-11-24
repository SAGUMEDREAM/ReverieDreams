package cc.thonly.reverie_dreams.world.moon.signal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-dimension, per-natural-structure progress. Breaking a console cannot duplicate its reward.
 */
public final class MoonSignalData extends SavedData {
    public record Target(String kind, BlockPos pos) {
        public static final Codec<Target> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("kind").forGetter(Target::kind), BlockPos.CODEC.fieldOf("pos").forGetter(Target::pos)
        ).apply(i, Target::new));
    }

    public record Tower(int stage, long scannedAt, List<Target> targets) {
        public static final Tower BROKEN = new Tower(0, -1200, List.of());
        public static final Codec<Tower> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.intRange(0, 3).fieldOf("stage").forGetter(Tower::stage),
                Codec.LONG.optionalFieldOf("scanned_at", -1200L).forGetter(Tower::scannedAt),
                Target.CODEC.listOf().optionalFieldOf("targets", List.of()).forGetter(Tower::targets)
        ).apply(i, Tower::new));

        public Tower {
            targets = List.copyOf(targets);
        }

        public boolean repaired() {
            return stage == 3;
        }

        public Tower advance() {
            return new Tower(Math.min(3, stage + 1), scannedAt, targets);
        }
    }

    public static final Codec<MoonSignalData> CODEC = Codec.unboundedMap(Codec.STRING, Tower.CODEC)
            .xmap(MoonSignalData::new, data -> Map.copyOf(data.towers));
    public static final SavedDataType<MoonSignalData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("reverie_dreams", "moon_signal_towers"), MoonSignalData::new, CODEC, DataFixTypes.LEVEL);
    private final Map<String, Tower> towers = new HashMap<>();

    public MoonSignalData() {
    }

    private MoonSignalData(Map<String, Tower> towers) {
        this.towers.putAll(towers);
    }

    public Tower tower(long key) {
        return towers.getOrDefault(Long.toString(key), Tower.BROKEN);
    }

    public void put(long key, Tower tower) {
        towers.put(Long.toString(key), tower);
        setDirty();
    }
}
