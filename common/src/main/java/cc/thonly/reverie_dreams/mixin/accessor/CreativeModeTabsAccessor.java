package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTabs.class)
public interface CreativeModeTabsAccessor {

    @Accessor("BUILDING_BLOCKS")
    static ResourceKey<CreativeModeTab> getBuildingBlocks() {
        throw new AssertionError();
    }

    @Accessor("COLORED_BLOCKS")
    static ResourceKey<CreativeModeTab> getColoredBlocks() {
        throw new AssertionError();
    }

    @Accessor("NATURAL_BLOCKS")
    static ResourceKey<CreativeModeTab> getNaturalBlocks() {
        throw new AssertionError();
    }

    @Accessor("FUNCTIONAL_BLOCKS")
    static ResourceKey<CreativeModeTab> getFunctionalBlocks() {
        throw new AssertionError();
    }

    @Accessor("REDSTONE_BLOCKS")
    static ResourceKey<CreativeModeTab> getRedstoneBlocks() {
        throw new AssertionError();
    }

    @Accessor("HOTBAR")
    static ResourceKey<CreativeModeTab> getHotbar() {
        throw new AssertionError();
    }

    @Accessor("SEARCH")
    static ResourceKey<CreativeModeTab> getSearch() {
        throw new AssertionError();
    }

    @Accessor("TOOLS_AND_UTILITIES")
    static ResourceKey<CreativeModeTab> getToolsAndUtilities() {
        throw new AssertionError();
    }

    @Accessor("COMBAT")
    static ResourceKey<CreativeModeTab> getCombat() {
        throw new AssertionError();
    }

    @Accessor("FOOD_AND_DRINKS")
    static ResourceKey<CreativeModeTab> getFoodAndDrinks() {
        throw new AssertionError();
    }

    @Accessor("INGREDIENTS")
    static ResourceKey<CreativeModeTab> getIngredients() {
        throw new AssertionError();
    }

    @Accessor("SPAWN_EGGS")
    static ResourceKey<CreativeModeTab> getSpawnEggs() {
        throw new AssertionError();
    }

    @Accessor("OP_BLOCKS")
    static ResourceKey<CreativeModeTab> getOpBlocks() {
        throw new AssertionError();
    }

    @Accessor("INVENTORY")
    static ResourceKey<CreativeModeTab> getInventory() {
        throw new AssertionError();
    }
}