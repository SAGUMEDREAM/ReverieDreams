package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import java.util.concurrent.CompletableFuture;

public class ModEntityTagProvider extends FabricTagProvider.EntityTypeTagProvider {

    public ModEntityTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        TagAppender<EntityType<?>, EntityType<?>> roleBuilder = valueLookupBuilder(ModTags.EntityTag.NPC_ROLE);

        RegistryManager.NPC_ROLE.values().forEach(role -> roleBuilder.add(role.getEntityType()));

        TagAppender<EntityType<?>, EntityType<?>> undeadBuilder = valueLookupBuilder(EntityTypeTags.UNDEAD);
        undeadBuilder.add(ModEntities.GHOST_ENTITY_TYPE);
    }
}
