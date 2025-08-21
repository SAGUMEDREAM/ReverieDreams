package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.BasicDanmakuTypeItem;
import cc.thonly.reverie_dreams.registry.RegistrableObject;
import cc.thonly.reverie_dreams.registry.ItemColor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.*;

@Setter
@Getter
public class DanmakuType implements RegistrableObject<DanmakuType> {
    public static final Codec<DanmakuType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("registry_key").forGetter(DanmakuType::getId),
                    Codec.FLOAT.fieldOf("damage").forGetter(DanmakuType::getDamage),
                    Codec.FLOAT.fieldOf("scale").forGetter(DanmakuType::getScale),
                    Codec.FLOAT.fieldOf("speed").forGetter(DanmakuType::getSpeed),
                    Codec.BOOL.fieldOf("tile").forGetter(DanmakuType::isTile),
                    Codec.BOOL.fieldOf("infinite").forGetter(DanmakuType::isInfinite)
            ).apply(instance, DanmakuType::new)
    );

    private Identifier id;
    private final float damage;
    private final float scale;
    private final float speed;
    private final boolean tile;
    private final boolean infinite;
    private Item item;
    private DanmakuEntity.OnHitFactory hitFactory;

    public DanmakuType(Identifier id, float damage, float scale, float speed, boolean tile, boolean infinite) {
        this.id = id;
        this.damage = damage;
        this.scale = scale;
        this.speed = speed;
        this.tile = tile;
        this.infinite = infinite;
        this.buildItem();
    }

    public void buildItem() {
        var item = new BasicDanmakuTypeItem(this.getRegistryKey(), this.createItemSettings()
                .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(14606046))
                .maxDamage(120)
        );
        item.type(this);
        this.item = item;
        Registry.register(Registries.ITEM, Identifier.of(this.id.getNamespace(), "danmaku/" + this.id.getPath()), this.item);
    }

    public List<Pair<Item, ItemStack>> getColorPairs() {
        List<Pair<Item, ItemStack>> pairList = new LinkedList<>();
        ItemStack defaultStack = this.item.getDefaultStack();
        for (Map.Entry<Item, Long> itemLongEntry : ItemColor.getView().entrySet()) {
            Item dyeItem = itemLongEntry.getKey();
            ItemStack stack = defaultStack.copy();
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(itemLongEntry.getValue().intValue()));
            stack.set(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(0.5f, Optional.of(Identifier.of(UUID.randomUUID().toString()))));
            pairList.add(new Pair<>(dyeItem, stack));
        }
        return pairList;
    }

    public Identifier getRegistryKey() {
        return Identifier.of(this.id.getNamespace(), "danmaku/" + this.id.getPath());
    }

    public Item.Settings createItemSettings() {
        return new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, this.getRegistryKey()))
                .component(ModDataComponentTypes.Danmaku.TEMPLATE, Touhou.id("single").toString())
                .component(ModDataComponentTypes.Danmaku.DAMAGE, this.damage)
                .component(ModDataComponentTypes.Danmaku.SPEED, this.speed)
                .component(ModDataComponentTypes.Danmaku.SCALE, this.scale)
                .component(ModDataComponentTypes.Danmaku.COUNT, 1)
                .component(ModDataComponentTypes.Danmaku.TILE, this.tile)
                .component(ModDataComponentTypes.Danmaku.INFINITE, this.infinite)
                .component(ModDataComponentTypes.Danmaku.DAMAGE_TYPE, Touhou.id("generic").toString())
                .component(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(0.5f, Optional.of(Identifier.of(UUID.randomUUID().toString()))))
                .maxDamage(120)
                .repairable(ModTags.ItemTypeTag.POWER_BLOCK);
    }

    @Override
    public Codec<DanmakuType> getCodec() {
        return CODEC;
    }
}
