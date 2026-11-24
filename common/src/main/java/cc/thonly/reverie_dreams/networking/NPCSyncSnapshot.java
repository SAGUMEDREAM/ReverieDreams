package cc.thonly.reverie_dreams.networking;

import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public final class NPCSyncSnapshot {
    private NPCSyncSnapshot() { }

    public static @Nullable CompoundTag encode(UUID npc, HolderLookup.Provider registries, Consumer<ValueOutput> writer) {
        try (var problems = new ProblemReporter.ScopedCollector(LogUtils.getLogger())) {
            TagValueOutput output = TagValueOutput.createWithContext(problems, registries);
            writer.accept(output);
            if (!problems.isEmpty()) {
                LogUtils.getLogger().error("Skipping incomplete sync snapshot for NPC {}: {}", npc, problems.getReport());
                return null;
            }
            return output.buildResult();
        } catch (Exception failure) {
            LogUtils.getLogger().error("Failed to serialize sync snapshot for NPC {}; snapshot not sent", npc, failure);
            return null;
        }
    }
}
