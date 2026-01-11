package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.WildPigEntity;
import cc.thonly.reverie_dreams.mixin.accessor.PigEntityAccessor;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.pig.PigVariant;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public record WildPigImpl(WildPigEntity wildPig) implements PolymerEntity {

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.PIG;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        if (initial && !this.wildPig.level().isClientSide()) {
            MinecraftServer server = this.wildPig.level().getServer();
            assert server != null;
            RegistryAccess.Frozen registryManager = server.registryAccess();
            Registry<PigVariant> registry = registryManager.lookupOrThrow(Registries.PIG_VARIANT);
            Holder<PigVariant> pigVariant = registry.wrapAsHolder(registry.getValue(WildPigEntity.VARIANT));

            SynchedEntityData.DataValue<Holder<PigVariant>> entry = SynchedEntityData.DataValue.create(
                    PigEntityAccessor.VARIANT(),
                    pigVariant);

            data.add(entry);

        }
    }
}
