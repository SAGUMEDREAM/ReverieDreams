package cc.thonly.reverie_dreams.block.bundle;

import cc.thonly.keine.api.KeineRegistries;
import cc.thonly.keine.api.registry.CompostingChanceRegistry;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.PlatformContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.blay09.mods.balm.world.level.block.BalmBlockRegistration;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
public final class CropBlockBundle {
    private static final Map<Identifier, Entry> INSTANCES = new Object2ObjectOpenHashMap<>();
    private final Identifier identifier;
    private Integer maxAge;
    private float seedCompostingLevel = 0.3f;
    private float cropCompostingLevel = 0.6f;
    private DeferredItem gain;
    private BasicBlockFactory factory;
    private boolean inWater;
    private boolean selfSeed = false;
    private ModelType modelType = ModelType.CROSS;
    @Setter(value = AccessLevel.PRIVATE)
    private Entry entry;

    private CropBlockBundle(Identifier identifier) {
        this.identifier = identifier;
    }

    public static CropBlockBundle create(Identifier identifier) {
        return new CropBlockBundle(identifier);
    }

    /**
     * 设置此作物的掉落物既是种子。
     */
    public CropBlockBundle self() {
        this.selfSeed = true;
        return this;
    }

    /**
     * 构建并注册作物 block 与 item
     */
    public Entry build(BalmBlockRegistrar registrar) {
        String name = this.identifier.getPath();
        BalmBlockRegistration blockRegistration = registrar.register(name, props -> this.factory.newInstance(props), BlockBehaviour.Properties.of().setId(RDBlocks.keyOf(this.identifier)));
        RDBlocks.registerSimpleBlock(blockRegistration.asDeferredBlock());

        DeferredItem seedItem;
        Identifier seedId = Identifier.fromNamespaceAndPath(this.identifier.getNamespace(), this.identifier.getPath() + "_seeds");
        seedItem = RDItems.registerSimpleItem(
                ReverieDreams.getItemRegistrar(),
                seedId.getPath(),
                (settings) -> new BlockItem(
                        blockRegistration.asDeferredBlock().asBlock(),
                        settings
                                .setId(ResourceKey.create(Registries.ITEM, seedId))
                                .useItemDescriptionPrefix()
                ),
                new Item.Properties()
        );
        KeineRegistries keineRegistries = ReverieDreams.getKeineRegistries();
        CompostingChanceRegistry compostingChanceRegistry = keineRegistries.compostingChanceRegistry();
        compostingChanceRegistry.register(context -> {
            context.addItem(seedItem, getSeedCompostingLevel());
        });
        if (this.selfSeed) {
            this.gain = seedItem;
        } else {
            compostingChanceRegistry.register(context -> {
                context.addItem(this.gain, getCropCompostingLevel());
            });
        }

        if (PlatformContext.isFabric()) {
            Block block = blockRegistration.asBlockLike().asBlock();
            ((AbstractCropBlock) block).setSeed(seedItem.asItem());
        } else {
            ReverieDreams.COMMON_LATE_INIT.add(() -> {
                Block block = blockRegistration.asBlockLike().asBlock();
                ((AbstractCropBlock) block).setSeed(seedItem.asItem());
            });
        }

        Entry entry = Entry.createInstance(this.identifier)
                .setCropBlock(blockRegistration.asDeferredBlock())
                .setSeed(seedItem)
                .setProduct(this.gain)
                .setModelType(this.modelType)
                .setInWater(this.inWater)
                .setSelfSeed(this.selfSeed);

        entry.getItems().add(seedItem);
        if (this.gain != null) {
            entry.getItems().add(this.gain);
        }

        this.entry = entry;
        INSTANCES.put(this.identifier, entry);
        return entry;
    }

    public static Optional<Entry> getEntry(Identifier identifier) {
        return Optional.ofNullable(INSTANCES.get(identifier));
    }

    public static Optional<Entry> getEntry(Block block) {
        return Optional.ofNullable(INSTANCES.get(BuiltInRegistries.BLOCK.getKey(block)));
    }

    public static Set<Map.Entry<Identifier, Entry>> getViews() {
        return INSTANCES.entrySet();
    }

    public interface BasicBlockFactory {
        AbstractCropBlock newInstance(BlockBehaviour.Properties settings);
    }

    @Accessors(chain = true)
    @Setter
    @Getter
    @ToString
    public static class Entry {
        private final Identifier identifier;
        private final Set<DeferredItem> items = new HashSet<>();
        private DeferredItem seed;
        private DeferredItem product;
        private DeferredBlock cropBlock;
        private ModelType modelType;
        private boolean inWater = false;
        private boolean selfSeed = false;

        private Entry(Identifier identifier) {
            this.identifier = identifier;
        }

        protected static Entry createInstance(Identifier identifier) {
            return new Entry(identifier);
        }

    }

    public enum ModelType {
        CROSS(),
        CROP(),
        ;
    }
}
