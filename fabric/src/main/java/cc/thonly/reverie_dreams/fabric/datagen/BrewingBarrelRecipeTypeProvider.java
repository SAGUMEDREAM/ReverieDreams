package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.data.BeverageProperty;
import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractRecipeTypeProvider;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.BrewingBarrelRecipe;
import cc.thonly.reverie_dreams.registry.content.BeverageProperties;
import cc.thonly.reverie_dreams.registry.content.block.RDCropBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.util.IdCompletableFuture;
import cc.thonly.reverie_dreams.util.IdCompletableFutureKeys;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"deprecation", "SameParameterValue"})
public class BrewingBarrelRecipeTypeProvider extends AbstractRecipeTypeProvider {
    private final Factory<BrewingBarrelRecipe> factory = this.getOrCreateFactory(RecipeManager.BREWING_BARREL, BrewingBarrelRecipe.class);

    public BrewingBarrelRecipeTypeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return IdCompletableFuture.waitFor(
                IdCompletableFutureKeys.BEVERAGE_PROVIDER
        ).thenCompose(ignored -> super.run(writer));
    }

    @Override
    public void configured(HolderLookup.Provider provider) {
        // DLC0
        this.barrelRecipe(List.of(Items.OAK_LEAVES, Items.OAK_LEAVES), RDBeverageItems.GREEN_TEA.asItem());
        this.barrelRecipe(List.of(Items.WHEAT_SEEDS, RDIngredientItems.LEMON), RDBeverageItems.FRUITY_HIGH_BALL.asItem());
        this.barrelRecipe(List.of(Items.WHEAT_SEEDS, RDIngredientItems.BLACK_SALT, Items.SWEET_BERRIES.asItem()), RDBeverageItems.FRUITY_SOUR.asItem());
        this.barrelRecipe(List.of(Items.WHEAT_SEEDS, RDIngredientItems.BLACK_SALT, RDIngredientItems.BLACK_SALT), RDBeverageItems.QI.asItem());
        this.barrelRecipe(List.of(Items.WHEAT_SEEDS, Items.WHEAT_SEEDS, Items.WHEAT_SEEDS), RDBeverageItems.BEER.asItem());
        this.barrelRecipe(List.of(RDIngredientItems.STICKY_RICE, RDIngredientItems.STICKY_RICE, RDIngredientItems.STICKY_RICE), RDBeverageItems.SUN_MOON_STAR.asItem());
        this.barrelRecipe(List.of(RDIngredientItems.PLUM, RDIngredientItems.PLUM), RDBeverageItems.PLUM_WINE.asItem());
        this.barrelRecipe(List.of(Items.RABBIT_FOOT, Items.RED_MUSHROOM, Items.QUARTZ), RDBeverageItems.TENGU_DANCE.asItem());
        this.barrelRecipe(List.of(Items.WHEAT_SEEDS, RDIngredientItems.LEMON, RDIngredientItems.TOMATO), RDBeverageItems.SCARLET_DEVIL.asItem());
        this.barrelRecipe(List.of(Items.WHEAT, Items.WHEAT, Items.WHEAT), RDBeverageItems.GODS_WHEAT.asItem());
        this.barrelRecipe(List.of(Items.GOLD_INGOT, RDIngredientItems.STICKY_RICE, RDIngredientItems.STICKY_RICE), RDBeverageItems.OTTER_FESTIVAL.asItem());
        this.barrelRecipe(List.of(Items.WHEAT, RDIngredientItems.STICKY_RICE, RDCropBlocks.SOY_BEANS.getSeed()), RDBeverageItems.DAWN.asItem());
        this.barrelRecipe(List.of(RDIngredientItems.STICKY_RICE, Items.BAMBOO, RDIngredientItems.DEW), RDBeverageItems.SPARROW_SAKE.asItem());
        this.barrelRecipe(List.of(Items.CHERRY_LEAVES, Items.ACACIA_LEAVES, Items.DARK_OAK_LEAVES, Items.POPPY), RDBeverageItems.SCARLET_DEVIL_MANSION_BLACK_TEA.asItem());
        this.barrelRecipe(List.of(Items.ICE, Items.SNOWBALL, Items.COCOA_BEANS), RDBeverageItems.AFFGADO.asItem());
        this.barrelRecipe(List.of(RDIngredientItems.GRAPE, RDIngredientItems.GRAPE, Items.ROSE_BUSH), RDBeverageItems.RED_MIST.asItem());
        this.barrelRecipe(List.of(RDIngredientItems.GRAPE, RDIngredientItems.LEMON, Items.SNOWBALL, RDIngredientItems.GINKGO), RDBeverageItems.NEGRONI.asItem());
        this.barrelRecipe(List.of(Items.WHEAT, Items.WHEAT, RDIngredientItems.GINKGO), RDBeverageItems.GODFATHER.asItem());
        this.barrelRecipe(List.of(Items.OAK_LEAVES, Items.SNOWBALL, RDIngredientItems.CREAM), RDBeverageItems.BLESSING_WIND.asItem());
        this.barrelRecipe(List.of(RDIngredientItems.FLOWERS, Items.WHEAT, Items.WHEAT_SEEDS, Items.WHEAT_SEEDS), RDBeverageItems.WINTER_BREW.asItem());
        this.barrelRecipe(List.of(RDIngredientItems.MOONFLOWER, Items.BAMBOO), RDBeverageItems.FOURTEENTH_NIGHT.asItem());
        this.barrelRecipe(List.of(Items.BLAZE_POWDER, Items.MAGMA_CREAM, RDIngredientItems.CHILI), RDBeverageItems.FIRE_RAT_FUR.asItem());
        this.barrelRecipe(List.of(Items.DARK_OAK_LEAVES, Items.DARK_OAK_LEAVES), RDBeverageItems.GYOKURO_TEA.asItem());
        this.barrelRecipe(List.of(RDIngredientItems.MOONFLOWER, RDIngredientItems.LEMON, Items.SNOWBALL), RDBeverageItems.MOON_ROCKET.asItem());
        this.barrelRecipe(List.of(Items.SUGAR, Items.MILK_BUCKET), RDBeverageItems.MILK.asItem());
        this.barrelRecipe(List.of(Items.SWEET_BERRIES, Items.SWEET_BERRIES), RDBeverageItems.RED_GRAPEFRUIT_JUICE.asItem());
        this.barrelRecipe(List.of(Items.SUGAR, Items.SUGAR), RDBeverageItems.SODA.asItem());
        this.barrelRecipe(List.of(Items.SUGAR, RDIngredientItems.LEMON, Items.SNOWBALL), RDBeverageItems.ICEBERG_MAPLE_FROZEN_LEMON.asItem());
        this.barrelRecipe(List.of(Items.ICE, Items.HONEY_BOTTLE, Items.SNOWBALL), RDBeverageItems.BIG_POPSICLE.asItem());

        // DLC1
        this.barrelRecipe(List.of(Items.APPLE, Items.WHEAT_SEEDS), RDBeverageItems.DAIGINJO.asItem());
        this.barrelRecipe(List.of(Items.COCOA_BEANS, Items.BLAZE_POWDER), RDBeverageItems.COFFEE.asItem());
        this.barrelRecipe(List.of(RDIngredientItems.FLOWERS, RDIngredientItems.DEW), RDBeverageItems.FAIRY_RAIN.asItem());
        this.barrelRecipe(List.of(Items.MILK_BUCKET, Items.SUGAR, Items.ICE, Items.SNOWBALL), RDBeverageItems.PALEO_CREAMY_SMOOTHIE.asItem());
        this.barrelRecipe(List.of(Items.DARK_OAK_LEAVES, Items.DARK_OAK_LEAVES, Items.BLAZE_POWDER), RDBeverageItems.ORDINARY_FITNESS_TEA.asItem());

        // DLC2
        this.barrelRecipe(List.of(Items.BREAD, Items.BLAZE_POWDER, Items.MAGMA_CREAM), RDBeverageItems.DEMON_SLAYER.asItem());
        this.barrelRecipe(List.of(Items.SUGAR, Items.BIRCH_LEAVES, Items.BIRCH_LEAVES), RDBeverageItems.QI_HEALTH.asItem());
        this.barrelRecipe(List.of(Items.SNOW, RDIngredientItems.FLOWERS, RDIngredientItems.PEACH, RDIngredientItems.FICUS_MICROCARPA), RDBeverageItems.KOMEIJI_ICE_CREAM.asItem());

        // DLC3
        this.barrelRecipe(List.of(RDIngredientItems.LEMON, RDIngredientItems.BUTTER, Items.OAK_LEAVES, Items.SUGAR), RDBeverageItems.MANGO_POMELO_SAGO.asItem());
        this.barrelRecipe(List.of(Items.NETHER_WART, Items.WHEAT, Items.WHEAT), RDBeverageItems.QILIN.asItem());

        // DLC4
        this.barrelRecipe(List.of(Items.BLAZE_POWDER, Items.WHEAT, Items.BONE), RDBeverageItems.HEAVEN_AND_EARTH_ARE_USELESS.asItem());
        this.barrelRecipe(List.of(Items.CHERRY_LEAVES, RDIngredientItems.PEACH), RDBeverageItems.DRUNK_ACTOR.asItem());

        // DLC5
        this.barrelRecipe(List.of(Items.SUGAR, Items.SEA_PICKLE, RDIngredientItems.DEW), RDBeverageItems.DAUGHTER_OF_THE_SEA.asItem());
        this.barrelRecipe(List.of(Items.COCOA_BEANS, Items.BLAZE_POWDER, RDIngredientItems.CREAM), RDBeverageItems.DEMONIC_COFFEE.asItem());
        this.barrelRecipe(List.of(Items.KELP, Items.BONE_MEAL, RDIngredientItems.BLACK_SALT, Items.SNOWBALL), RDBeverageItems.MOJITO_BURST_BALL.asItem());
        this.barrelRecipe(List.of(RDIngredientItems.LEMON, Items.GUNPOWDER, Items.WHEAT), RDBeverageItems.SPACE_BEER.asItem());
        this.barrelRecipe(List.of(Items.COCOA_BEANS, Items.ICE, Items.BLAZE_POWDER), RDBeverageItems.SATELLITE_ICED_COFFEE.asItem());
    }

    protected void barrelRecipe(List<IngredientStack> materials, IngredientStack output, int costTime) {
        Holder.Reference<Item> itemReference = output.getItem().builtInRegistryHolder();
        ResourceKey<Item> key = itemReference.key();
        this.factory.register(key.identifier(), new BrewingBarrelRecipe(materials, output, costTime));
    }

    protected void barrelRecipe(Collection<ItemLike> materials, Item output) {
        Holder.Reference<Item> itemReference = output.asItem().builtInRegistryHolder();
        ResourceKey<Item> key = itemReference.key();
        this.factory.register(key.identifier(), new BrewingBarrelRecipe(materials.stream().map(ItemLike::asItem).map(IngredientStack::new).toList(), IngredientStack.of(output), this.getCostTime(output)));
    }

    private int getCostTime(ItemLike output) {
        List<BeverageProperty> properties = BeverageProperties.get(IngredientStack.of(output));
        if (properties.contains(BeverageProperties.ALCOHOL_FREE)) {
            return 3 * 20;
        }
        int costTime = 20;
        if (properties.contains(BeverageProperties.LOW_ALCOHOL)) {
            costTime += 5 * 20;
        } else if (properties.contains(BeverageProperties.MID_ALCOHOL)) {
            costTime += 10 * 20;
        } else if (properties.contains(BeverageProperties.HIGH_ALCOHOL)) {
            costTime += 20 * 20;
        }
        int size = properties.size();
        costTime += size * 9 * 20;
        return costTime;
    }

    protected void barrelRecipe(Collection<ItemLike> materials, Item output, int costTime) {
        Holder.Reference<Item> itemReference = output.asItem().builtInRegistryHolder();
        ResourceKey<Item> key = itemReference.key();
        this.factory.register(key.identifier(), new BrewingBarrelRecipe(materials.stream().map(ItemLike::asItem).map(IngredientStack::new).toList(), IngredientStack.of(output), costTime));
    }

    protected void barrelRecipe(List<Holder<Item>> materials, Holder<Item> output, int costTime) {
        Holder.Reference<Item> itemReference = output.value().asItem().builtInRegistryHolder();
        ResourceKey<Item> key = itemReference.key();
        this.factory.register(key.identifier(), new BrewingBarrelRecipe(materials.stream().map(IngredientStack::new).toList(), IngredientStack.of(output.value()), costTime));
    }

    @Override
    public String getName() {
        return "Barrel Recipe Provider";
    }
}
