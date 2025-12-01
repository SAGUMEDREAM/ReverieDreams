package cc.thonly.reverie_dreams.block.creator;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Accessors(chain = true)
@Setter
@Getter
public final class CropBlockCreator {
    private static final Map<ResourceLocation, CropBlockCreator.Instance> INSTANCES = new Object2ObjectOpenHashMap<>();
    private final ResourceLocation identifier;
    private Integer maxAge;
    private float seedCompostingLevel = 0.3f;
    private float cropCompostingLevel = 0.6f;
    private Item gain;
    private BasicBlockFactory factory;
    private boolean inWater;
    private boolean selfSeed = false;
    private ModelType modelType = ModelType.CROSS;
    @Setter(value = AccessLevel.PRIVATE)
    private CropBlockCreator.Instance instance;

    private CropBlockCreator(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    public static CropBlockCreator createCreator(ResourceLocation identifier) {
        return new CropBlockCreator(identifier);
    }

    /**
     * 设置此作物的掉落物既是种子。
     */
    public CropBlockCreator self() {
        this.selfSeed = true;
        return this;
    }

    /**
     * 构建并注册作物 block 与 item
     */
    public CropBlockCreator.Instance build() {
        AbstractCropBlock basicCropBlock = this.factory.newInstance(BlockBehaviour.Properties.of().setId(RDBlocks.keyOf(this.identifier)));
        RDBlocks.registerSimpleBlock(basicCropBlock);
        Registry.register(BuiltInRegistries.BLOCK, this.identifier, basicCropBlock);

        Item seedItem;
        ResourceLocation seedId = ResourceLocation.fromNamespaceAndPath(this.identifier.getNamespace(), this.identifier.getPath() + "_seeds");
        seedItem = RDItems.registerSimpleItem(
                seedId,
                (settings) -> new BlockItem(
                        basicCropBlock,
                        settings
                                .setId(ResourceKey.create(Registries.ITEM, seedId))
                                .useItemDescriptionPrefix()
                ),
                new Item.Properties()
        );

        CompostingChanceRegistry.INSTANCE.add(seedItem, getSeedCompostingLevel());

        if (this.selfSeed) {
            this.gain = seedItem;
        } else {
            CompostingChanceRegistry.INSTANCE.add(gain, getCropCompostingLevel());
        }

        basicCropBlock.setSeed(seedItem);

        Instance instance = Instance.createInstance(this.identifier)
                .setCropBlock(basicCropBlock)
                .setSeed(seedItem)
                .setProduct(this.gain)
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

    public static Optional<Instance> getInstance(ResourceLocation identifier) {
        return Optional.ofNullable(INSTANCES.get(identifier));
    }

    public static Optional<Instance> getInstance(Block block) {
        return Optional.ofNullable(INSTANCES.get(BuiltInRegistries.BLOCK.getKey(block)));
    }

    public static Set<Map.Entry<ResourceLocation, Instance>> getViews() {
        return INSTANCES.entrySet();
    }

    public interface BasicBlockFactory {
        AbstractCropBlock newInstance(BlockBehaviour.Properties settings);
    }

    @Accessors(chain = true)
    @Setter
    @Getter
    @ToString
    public static class Instance {
        private final ResourceLocation identifier;
        private final Set<Item> items = new HashSet<>();
        private Item seed;
        private Item product;
        private AbstractCropBlock cropBlock;
        private ModelType modelType;
        private boolean inWater = false;
        private boolean selfSeed = false;

        private Instance(ResourceLocation identifier) {
            this.identifier = identifier;
        }

        public static Instance createInstance(ResourceLocation identifier) {
            return new Instance(identifier);
        }

        public void generateTranslation(FabricLanguageProvider.TranslationBuilder builder, String seed) {
            builder.add(this.cropBlock, seed);
            builder.add(this.seed, seed);
        }

        public void generateLoot(FabricBlockLootTableProvider provider) {

        }
    }

    public enum ModelType {
        CROSS(),
        CROP(),
        ;
    }
}
