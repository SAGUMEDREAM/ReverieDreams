package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.Properties.class)
public interface ItemSettingsAccessor {
    @Accessor("BLOCK_DESCRIPTION_ID")
    public static DependantName<Item, String> BLOCK_PREFIXED_TRANSLATION_KEY() {
        throw new UnsupportedOperationException();
    }

    @Accessor("ITEM_DESCRIPTION_ID")
    public static DependantName<Item, String> ITEM_PREFIXED_TRANSLATION_KEY() {
        throw new UnsupportedOperationException();
    }

    @Accessor("componentInitializer")
    public DataComponentInitializers.Initializer<Item> getComponentInitializers();

    @Accessor("craftingRemainingItem")
    @Nullable
    public ItemStackTemplate getRecipeRemainder();

    @Accessor("requiredFeatures")
    public FeatureFlagSet getRequiredFeatures();

    @Accessor("id")
    @Nullable
    public ResourceKey<Item> getRegistryKey();

    @Accessor("descriptionId")
    public DependantName<Item, String> getTranslationKey();

    @Accessor("model")
    public DependantName<Item, Identifier> getModelId();
}
