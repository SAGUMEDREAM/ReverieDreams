package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.base.BasicCropBlock;
import cc.thonly.reverie_dreams.block.crop.TransparentFlatTripWire;
import cc.thonly.reverie_dreams.block.crop.TransparentPlant;
import cc.thonly.reverie_dreams.block.crop.TransparentPlantWatterlogged;
import cc.thonly.reverie_dreams.item.base.BasicPolymerBlockItem;
import cc.thonly.reverie_dreams.util.CropAgeModelProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.*;

@Accessors(chain = true)
@Setter
@Getter
public final class PolymerCropCreator {
    private static final Map<Identifier, PolymerCropCreator.Instance> INSTANCES = new Object2ObjectOpenHashMap<>();
    private final Identifier identifier;
    private Integer maxAge;
    private float seedCompostingLevel = 0.3f;
    private float cropCompostingLevel = 0.6f;
    private Item gain;
    private CropAgeModelProvider provider;
    private BasicBlockFactory factory;
    private boolean inWater;
    private boolean selfSeed = false;
    private ModelType modelType = ModelType.CROSS;
    @Setter(value = AccessLevel.PRIVATE)
    private PolymerCropCreator.Instance instance;

    private PolymerCropCreator(Identifier identifier) {
        this.identifier = identifier;
    }

    public static PolymerCropCreator createCreator(Identifier identifier) {
        return new PolymerCropCreator(identifier);
    }

    /**
     * 设置此作物的掉落物既是种子。
     */
    public PolymerCropCreator self() {
        this.selfSeed = true;
        return this;
    }

    /**
     * 构建并注册作物 block 与 item
     */
    public PolymerCropCreator.Instance build() {
        BasicCropBlock basicCropBlock = this.factory.newInstance(this.identifier);
        basicCropBlock.setPolymerBlockState(this.inWater ? TransparentPlantWatterlogged.TRANSPARENT_WATTERLOGGED : TransparentFlatTripWire.TRANSPARENT_FLAT_TRIPIWIRE);

        BasicCropBlock cropBlock = Registry.register(Registries.BLOCK, this.identifier, basicCropBlock);

        Item seedItem;
        Identifier seedId = Identifier.of(this.identifier.getNamespace(), this.identifier.getPath() + "_seeds");
        seedItem = Registry.register(
                Registries.ITEM,
                seedId,
                new BasicPolymerBlockItem(
                        seedId,
                        cropBlock,
                        new Item.Settings()
                                .registryKey(RegistryKey.of(RegistryKeys.ITEM, seedId))
                                .useItemPrefixedTranslationKey(),
                        Items.WHEAT_SEEDS
                )
        );

        CompostingChanceRegistry.INSTANCE.add(seedItem, getSeedCompostingLevel());

        if (this.selfSeed) {
            this.gain = seedItem;
        } else {
            CompostingChanceRegistry.INSTANCE.add(gain, getCropCompostingLevel());
        }

        cropBlock.setSeed(seedItem);
        cropBlock.setModelProvider(this.provider);
        cropBlock.setPolymerBlockState(this.inWater ? TransparentPlantWatterlogged.TRANSPARENT_WATTERLOGGED : TransparentFlatTripWire.TRANSPARENT_FLAT_TRIPIWIRE);

        Instance instance = Instance.createInstance(this.identifier)
                .setCropBlock(cropBlock)
                .setSeed(seedItem)
                .setProduct(this.gain)
                .setProvider(this.provider)
                .setModelType(this.modelType)
                .setInWater(this.inWater)
                .setSelfSeed(this.selfSeed);

        instance.getItems().add(seedItem);
        if (this.gain != null) {
            instance.getItems().add(this.gain);
        }

        this.instance = instance;
        INSTANCES.put(this.identifier, instance);
        return instance;
    }

    public static Optional<Instance> getInstance(Identifier identifier) {
        return Optional.ofNullable(INSTANCES.get(identifier));
    }

    public static Optional<Instance> getInstance(Block block) {
        return Optional.ofNullable(INSTANCES.get(Registries.BLOCK.getId(block)));
    }

    public static Set<Map.Entry<Identifier, Instance>> getViews() {
        return INSTANCES.entrySet();
    }

    public interface BasicBlockFactory {
        BasicCropBlock newInstance(Identifier identifier);
    }

    @Accessors(chain = true)
    @Setter
    @Getter
    @ToString
    public static class Instance {
        private final Identifier identifier;
        private final Set<Item> items = new HashSet<>();
        private Item seed;
        private Item product;
        private BasicCropBlock cropBlock;
        private CropAgeModelProvider provider;
        private ModelType modelType;
        private boolean inWater = false;
        private boolean selfSeed = false;

        private Instance(Identifier identifier) {
            this.identifier = identifier;
        }

        public static Instance createInstance(Identifier identifier) {
            return new Instance(identifier);
        }

        public void generateTranslation(FabricLanguageProvider.TranslationBuilder builder, String seed) {
            builder.add(this.cropBlock, seed);
            builder.add(this.seed, seed);
        }

        public void generateLoot(FabricBlockLootTableProvider provider) {
            if (this.cropBlock != null && this.product != null) {
                BlockStatePropertyLootCondition.Builder condition = BlockStatePropertyLootCondition
                        .builder(this.cropBlock)
                        .properties(
                                StatePredicate.Builder
                                        .create()
                                        .exactMatch(this.cropBlock.getAgeProperty(), this.cropBlock.getMaxAge())
                        );
//                LootTable.Builder lootTableBuilder = provider.cropDrops(this.cropBlock, this.product, this.seed, condition);
                LootTable.Builder lootTableBuilder = LootTable.builder();
                LeafEntry.Builder<?> productEntry = ItemEntry.builder(this.product)
                        .apply(SetCountLootFunction.builder(
                                UniformLootNumberProvider.create(1.0f, 3.0f)
                        ));
                LeafEntry.Builder<?> seedEntry = ItemEntry.builder(this.seed)
                        .apply(SetCountLootFunction.builder(
                                UniformLootNumberProvider.create(1.0f, 2.0f)
                        ));
                LeafEntry.Builder<?> baseSeedEntry = ItemEntry.builder(this.seed)
                        .apply(SetCountLootFunction.builder(
                                ConstantLootNumberProvider.create(1)
                        ));
                lootTableBuilder.pool(
                        LootPool.builder()
                                .conditionally(condition.build())
                                .rolls(ConstantLootNumberProvider.create(1))
                                .with(baseSeedEntry)
                );
                lootTableBuilder.pool(
                        LootPool.builder()
                                .conditionally(condition.build())
                                .rolls(ConstantLootNumberProvider.create(1))
                                .with(productEntry)
                );
                lootTableBuilder.pool(
                        LootPool.builder()
                                .conditionally(condition.build())
                                .rolls(ConstantLootNumberProvider.create(1))
                                .with(seedEntry)
                );
                provider.addDrop(this.cropBlock, lootTableBuilder);
            }
        }
    }

    public enum ModelType {
        CROSS(),
        CROP(),
        ;
    }
}
