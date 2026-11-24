package cc.thonly.reverie_dreams.api.loot;

import cc.thonly.reverie_dreams.api.proxy.PlatformProxy;
import dev.architectury.event.events.common.LootEvent;
import net.minecraft.world.level.storage.loot.LootPool;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings({"NonExtendableApiUsage", "OptionalUsedAsFieldOrParameterType"})
public interface LootTableModificationContextExtension extends LootEvent.LootTableModificationContext {
    Optional<TableModifier> TABLE_MODIFIER = PlatformProxy.<TableModifier>builder()
            .withFabric("cc.thonly.reverie_dreams.fabric.impl.FabricTableModifier")
            .withNeoForge("cc.thonly.reverie_dreams.neoforge.impl.NeoTableModifier")
            .buildOrNull();

    static LootTableModificationContextExtension get(LootEvent.LootTableModificationContext context) {
        ContextAccessor accessor = () -> context;
        return new LootTableModificationContextExtension() {
            @Override
            public void addPool(LootPool.Builder pool) {
                context.addPool(pool);

            }

            @Override
            public void modifyPool(Consumer<LootPool.Builder> modifier) {
                TABLE_MODIFIER.ifPresent(proxy -> proxy.modifyPool(this, modifier));
            }

            @Override
            public ContextAccessor getAccessor() {
                return accessor;
            }
        };
    }

    ContextAccessor getAccessor();

    void modifyPool(Consumer<LootPool.Builder> modifier);

    interface TableModifier {
        void modifyPool(LootTableModificationContextExtension extension, Consumer<LootPool.Builder> modifier);
    }

    interface ContextAccessor {
        LootEvent.LootTableModificationContext get();
    }

}
