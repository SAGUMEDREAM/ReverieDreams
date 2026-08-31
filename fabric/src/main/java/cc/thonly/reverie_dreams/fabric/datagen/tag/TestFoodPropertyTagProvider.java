package cc.thonly.reverie_dreams.fabric.datagen.tag;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractCustomRegistryTagProvider;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;

import java.util.concurrent.CompletableFuture;

@Deprecated
public class TestFoodPropertyTagProvider extends AbstractCustomRegistryTagProvider<FoodProperty, FoodProperty> {
    public TestFoodPropertyTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(BuiltInRegistryProviders.FOOD_PROPERTY, output, future);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        TagKey<FoodProperty> testPropertySet = TagKey.create(BuiltInRegistryProviders.FOOD_PROPERTY.key(), ReverieDreams.id("test_property_set"));
        TagAppender<FoodProperty, FoodProperty> tagAppender = valueLookupBuilder(testPropertySet);
        tagAppender.add(FoodProperties.AQUATIC_PRODUCTS);
        tagAppender.add(FoodProperties.BIZARRE);
    }
}
