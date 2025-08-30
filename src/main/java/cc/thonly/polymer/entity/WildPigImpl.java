package cc.thonly.polymer.entity;

import cc.thonly.mystias_izakaya.entity.WildPigEntity;
import cc.thonly.reverie_dreams.mixin.accessor.PigEntityAccessor;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.PigVariant;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public record WildPigImpl(WildPigEntity wildPig) implements PolymerEntity {

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.PIG;
    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        if (initial && !this.wildPig.getWorld().isClient) {
            MinecraftServer server = this.wildPig.getServer();
            assert server != null;
            DynamicRegistryManager.Immutable registryManager = server.getRegistryManager();
            Registry<PigVariant> registry = registryManager.getOrThrow(RegistryKeys.PIG_VARIANT);
            RegistryEntry<PigVariant> pigVariant = registry.getEntry(registry.get(WildPigEntity.VARIANT));

            DataTracker.SerializedEntry<RegistryEntry<PigVariant>> entry = DataTracker.SerializedEntry.of(
                    PigEntityAccessor.VARIANT(),
                    pigVariant);

            data.add(entry);

        }
    }
}
