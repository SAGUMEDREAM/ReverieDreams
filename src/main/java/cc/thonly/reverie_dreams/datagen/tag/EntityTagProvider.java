package cc.thonly.reverie_dreams.datagen.tag;

import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.tag.RDEntityTypeTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public class EntityTagProvider extends FabricTagProvider.EntityTypeTagProvider {

    public EntityTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        TagAppender<EntityType<?>, EntityType<?>> roleBuilder = valueLookupBuilder(RDEntityTypeTags.NPC_ROLE);

        RegistryHandlers.NPC_ROLE.values().forEach(role -> roleBuilder.add(role.getEntityType()));

        TagAppender<EntityType<?>, EntityType<?>> undead = valueLookupBuilder(EntityTypeTags.UNDEAD);
        undead.add(RDEntityTypes.GHOST);

        TagAppender<EntityType<?>, EntityType<?>> yokai = valueLookupBuilder(RDEntityTypeTags.YOKAI);
        yokai.add(RDEntityTypes.MAID_YOUSEI);
        yokai.add(RDEntityTypes.SUNFLOWER_YOUSEI);
        yokai.add(RDEntityTypes.YOUSEI);
        yokai.add(RDEntityTypes.HAIRBALL);
        yokai.add(RDEntityTypes.GHOST);
        yokai.add(RDEntityTypes.MUSHROOM_MONSTER);
    }
}
