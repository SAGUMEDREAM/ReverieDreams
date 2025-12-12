package cc.thonly.reverie_dreams.util.advancements;

import cc.thonly.reverie_dreams.advancement.SimpleTrigger;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class SimpleTriggerFactory {
    private static final Map<ResourceLocation, Tool> CACHED = new Object2ObjectOpenHashMap<>(64);

    public static Tool create(ResourceLocation location) {
        return CACHED.computeIfAbsent(location, x -> new Tool() {
            @Override
            public Criterion<SimpleTrigger.Condition> createCriterion() {
                return SimpleTrigger.of(x);
            }

            @Override
            public void trigger(ServerPlayer player) {
                SimpleTrigger.trigger(player, x);
            }

            @Override
            public ResourceLocation location() {
                return x;
            }
        });
    }

    public interface Tool {
        Criterion<SimpleTrigger.Condition> createCriterion();

        void trigger(ServerPlayer player);

        ResourceLocation location();
    }
}
