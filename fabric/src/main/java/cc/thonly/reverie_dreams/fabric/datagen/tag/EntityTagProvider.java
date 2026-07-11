package cc.thonly.reverie_dreams.fabric.datagen.tag;

import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.tag.RDEntityTypeTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class EntityTagProvider extends FabricTagsProvider.EntityTypeTagsProvider {

    public EntityTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider wrapperLookup) {
        TagAppender<EntityType<?>, EntityType<?>> roleBuilder = valueLookupBuilder(RDEntityTypeTags.NPC_ROLE);

        RegistryImpls.NPC_ROLE.values().forEach(role -> roleBuilder.add(role.getEntityType().value()));

        TagAppender<EntityType<?>, EntityType<?>> undead = valueLookupBuilder(EntityTypeTags.UNDEAD);
        undead.add(RDEntityTypes.GHOST.value());

        TagAppender<EntityType<?>, EntityType<?>> yokai = valueLookupBuilder(RDEntityTypeTags.YOKAI);
        yokai.add(RDEntityTypes.MAID_YOUSEI.value());
        yokai.add(RDEntityTypes.SUNFLOWER_YOUSEI.value());
        yokai.add(RDEntityTypes.YOUSEI.value());
        yokai.add(RDEntityTypes.HAIRBALL.value());
        yokai.add(RDEntityTypes.GHOST.value());
        yokai.add(RDEntityTypes.MUSHROOM_MONSTER.value());
        yokai.add(RDEntityTypes.MAID_YOUSEI.value());
        yokai.add(RDEntityTypes.ONI.value());
    }
}
