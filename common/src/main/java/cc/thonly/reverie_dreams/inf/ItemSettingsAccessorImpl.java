package cc.thonly.reverie_dreams.inf;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;

public interface ItemSettingsAccessorImpl {
    public static DependantName<Item, String> BLOCK_PREFIXED_TRANSLATION_KEY() {
        throw new UnsupportedOperationException();
    }

    public static DependantName<Item, String> ITEM_PREFIXED_TRANSLATION_KEY() {
        throw new UnsupportedOperationException();
    }

    DataComponentMap.Builder getComponents();

    Item getRecipeRemainder();

    FeatureFlagSet getRequiredFeatures();

    ResourceKey<Item> getRegistryKey();

    DependantName<Item, String> getTranslationKey();

    DependantName<Item, Identifier> getModelId();
}
