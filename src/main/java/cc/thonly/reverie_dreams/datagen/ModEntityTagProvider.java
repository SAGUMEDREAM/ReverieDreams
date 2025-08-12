package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EntityTypeTags;

import java.util.concurrent.CompletableFuture;

public class ModEntityTagProvider extends FabricTagProvider.EntityTypeTagProvider {

    public ModEntityTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        ProvidedTagBuilder<EntityType<?>, EntityType<?>> roleBuilder = valueLookupBuilder(ModTags.EntityTag.NPC_ROLE);

        RegistryManager.NPC_ROLE.values().forEach(role -> roleBuilder.add(role.getEntityType()));

        ProvidedTagBuilder<EntityType<?>, EntityType<?>> undeadBuilder = valueLookupBuilder(EntityTypeTags.UNDEAD);
        undeadBuilder.add(ModEntities.GHOST_ENTITY_TYPE);
    }
}
