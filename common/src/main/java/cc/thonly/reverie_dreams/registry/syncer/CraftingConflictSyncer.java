package cc.thonly.reverie_dreams.registry.syncer;

import cc.thonly.reverie_dreams.data.CraftingConflict;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.registry.impl.RegistrySyncer;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class CraftingConflictSyncer implements Supplier<RegistrySyncer<CraftingConflict, CraftingConflict>> {

    @Override
    public RegistrySyncer<CraftingConflict, CraftingConflict> get() {
        return new RegistrySyncer<>(BuiltInRegistryProviders.CRAFTING_CONFLICT, CraftingConflict.CODEC, new RegistrySyncer.ClientReloadListener<CraftingConflict, CraftingConflict>() {
            @Override
            public void preProcessing(RegistryProvider<CraftingConflict> registry) {
                BuiltInRegistryProviders.CRAFTING_CONFLICT.clear();
            }

            @Override
            public void afterProcessing(RegistryProvider<CraftingConflict> registry) {

            }

            @Override
            public CraftingConflict update(Identifier key, @Nullable CraftingConflict old, CraftingConflict data) {
                data.setId(key);
                return data;
            }

            @Override
            public RegistrySyncer<CraftingConflict, CraftingConflict> getSyncer() {
                return BuiltInRegistryProviders.CRAFTING_CONFLICT.getSyncer();
            }
        }) {
            @Override
            public CraftingConflict toT(CraftingConflict craftingConflict) {
                return craftingConflict;
            }

            @Override
            public CraftingConflict toD(CraftingConflict craftingConflict) {
                return craftingConflict;
            }
        };
    }
}
