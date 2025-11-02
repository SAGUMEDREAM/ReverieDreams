package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.datagen.generator.RecipeTypeProvider;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.danmaku.DanmakuItem;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.registry.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.UseCooldown;

import java.util.*;

@Setter
@Getter
public class DanmakuType implements CodecStep<DanmakuType>, OwnerBinding<DanmakuType>, Translatable, BuiltinObject {
    public static final Codec<DanmakuType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("registry_key").forGetter(DanmakuType::getId),
                    Codec.FLOAT.fieldOf("damage").forGetter(DanmakuType::getDamage),
                    Codec.FLOAT.fieldOf("scale").forGetter(DanmakuType::getScale),
                    Codec.FLOAT.fieldOf("speed").forGetter(DanmakuType::getSpeed),
                    Codec.BOOL.fieldOf("tile").forGetter(DanmakuType::isTile),
                    Codec.BOOL.fieldOf("infinite").forGetter(DanmakuType::isInfinite)
            ).apply(instance, DanmakuType::new)
    );

    private ResourceLocation id;
    private final float damage;
    private final float scale;
    private final float speed;
    private final boolean tile;
    private final boolean infinite;
    private Item item;
    private DanmakuEntity.OnHitFactory hitFactory;
    private IntrinsicalRegister<DanmakuType> owner;

    public DanmakuType(ResourceLocation id, float damage, float scale, float speed, boolean tile, boolean infinite) {
        this.id = id;
        this.damage = damage;
        this.scale = scale;
        this.speed = speed;
        this.tile = tile;
        this.infinite = infinite;
        this.buildItem();
    }

    @Override
    public String translateKey() {
        return this.item.getDescriptionId();
    }

    public DanmakuShape toShape() {
        for (Map.Entry<ResourceKey<DanmakuShape>, DanmakuShape> mapEntry : RegistryManager.DANMAKU_SHAPE.entrySet()) {
            DanmakuShape shape = mapEntry.getValue();
            if (shape.getType() == this) {
                return shape;
            }
        }
        return new DanmakuShape(this);
    }

    public void buildShapeRecipe(RecipeTypeProvider.Factory<DanmakuShapeDrawRecipe> factory, List<List<Boolean>> shape) {
        factory.register(this.id, new DanmakuShapeDrawRecipe(shape, ItemStackWrapper.of(this.toShape().getItemStack().copy())));
    }

    public void buildShapeRecipe(RecipeTypeProvider.Factory<DanmakuShapeDrawRecipe> factory, String[] shape) {
        List<List<Boolean>> list = new ArrayList<>();

        for (String line : shape) {
            ArrayList<Boolean> row = new ArrayList<>();
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == 'T') {
                    row.add(true);
                } else if (c == 'F') {
                    row.add(false);
                } else {
                    throw new IllegalArgumentException("Invalid character in shape string: " + c);
                }
            }
            list.add(row);
        }

        this.buildShapeRecipe(factory, list);
    }

    public void buildItem() {
        DanmakuItem item = new DanmakuItem(this.createItemSettings()
                .component(DataComponents.DYED_COLOR, new DyedItemColor(14606046))
                .durability(120)
        );
        item.type(this);
        this.item = item;
        Registry.register(BuiltInRegistries.ITEM, this.getIdentifier(), this.item);
    }

    public List<Tuple<Item, ItemStack>> getColorPairs() {
        List<Tuple<Item, ItemStack>> pairList = new LinkedList<>();
        ItemStack defaultStack = this.item.getDefaultInstance();
        for (Map.Entry<Item, Long> itemLongEntry : ItemColor.getView().entrySet()) {
            Item dyeItem = itemLongEntry.getKey();
            ItemStack stack = defaultStack.copy();
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(itemLongEntry.getValue().intValue()));
            stack.set(DataComponents.USE_COOLDOWN, new UseCooldown(0.5f, Optional.of(ResourceLocation.parse(UUID.randomUUID().toString()))));
            pairList.add(new Tuple<>(dyeItem, stack));
        }
        return pairList;
    }

    public ResourceLocation getIdentifier() {
        return ResourceLocation.fromNamespaceAndPath(this.id.getNamespace(), "danmaku/" + this.id.getPath());
    }

    public Item.Properties createItemSettings() {
        return new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, this.getIdentifier()))
                .component(ModDataComponentTypes.DANMAKU_PROPERTIES, this.createDanmakuProperties())
                .component(DataComponents.USE_COOLDOWN, new UseCooldown(0.5f, Optional.of(ResourceLocation.parse(UUID.randomUUID().toString()))))
                .durability(120)
                .repairable(ModTags.ItemTypeTag.POWER_BLOCK);
    }

    public DanmakuProperties createDanmakuProperties() {
        return new DanmakuProperties(
                ReverieDreams.id("single"),
                1,
                this.damage,
                ReverieDreams.id("generic"),
                this.scale,
                this.speed,
                0,
                this.tile,
                this.infinite
        );
    }

    @Override
    public Codec<DanmakuType> getCodec() {
        return CODEC;
    }
}
