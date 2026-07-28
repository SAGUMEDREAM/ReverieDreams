package cc.thonly.reverie_dreams.block.bundle;

import cc.thonly.keine.api.registry.impl.CompostingChanceRegistry;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.registry.ReverieDreamsRegistries;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.impl.BlockDelegate;
import cc.thonly.reverie_dreams.registry.impl.ItemDelegate;
import cc.thonly.reverie_dreams.util.PlatformContext;
import dev.architectury.registry.registries.RegistrySupplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
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
    private ItemDelegate gain;
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
    public Entry build() {
        String name = this.identifier.getPath();
        RegistrySupplier<Block> block = ReverieDreamsRegistries.BLOCK.register(name, () -> this.factory.newInstance(BlockBehaviour.Properties.of().setId(RDBlocks.keyOf(this.identifier))));
        BlockDelegate blockDelegate = BlockDelegate.of(block);
        RDBlocks.registerSimpleBlock(blockDelegate);

        ItemDelegate seedItem;
        Identifier seedId = Identifier.fromNamespaceAndPath(this.identifier.getNamespace(), this.identifier.getPath() + "_seeds");
        seedItem = RDItems.registerSimpleItem(
                seedId.getPath(),
                (settings) -> new BlockItem(
                        blockDelegate.asBlock(),
                        settings
                                .setId(ResourceKey.create(Registries.ITEM, seedId))
                                .useItemDescriptionPrefix()
                ),
                new Item.Properties()
        );
        CompostingChanceRegistry compostingChanceRegistry = ReverieDreamsRegistries.KEINE_REGISTRIES.compostingChanceRegistry();
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


        ReverieDreams.COMMON_LATE_INIT.add(() -> {
            Block delegateBlock = blockDelegate.asBlock();
            ((AbstractCropBlock) delegateBlock).setSeed(seedItem.asItem());
        });

        Entry entry = Entry.createInstance(this.identifier)
                           .setCropBlock(blockDelegate)
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
        private final Set<ItemDelegate> items = new HashSet<>();
        private ItemDelegate seed;
        private ItemDelegate product;
        private BlockDelegate cropBlock;
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
