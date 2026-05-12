package cc.thonly.reverie_dreams.data.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.entity.misc.BaseDanmakuEntity;
import cc.thonly.reverie_dreams.item.danmaku.DanmakuItem;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.ItemColor;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.OwnerBinding;
import cc.thonly.reverie_dreams.registry.Translatable;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.util.item.ItemStackTemplateHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.item.BalmItemRegistration;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseCooldown;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

@Setter
@Getter
@ToString
public class DanmakuType implements CodecStep<DanmakuType>, OwnerBinding<DanmakuType>, Translatable, BuiltinObject {
    public static final Codec<DanmakuType> COMPONENT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("registry_key").forGetter(DanmakuType::getId),
                    ResourceKey.codec(Registries.DAMAGE_TYPE).fieldOf("damage_type").forGetter(DanmakuType::getDamageType),
                    Codec.FLOAT.fieldOf("damage").forGetter(DanmakuType::getDamage),
                    Codec.FLOAT.fieldOf("scale").forGetter(DanmakuType::getScale),
                    Codec.FLOAT.fieldOf("speed").forGetter(DanmakuType::getSpeed),
                    Codec.BOOL.fieldOf("tile").forGetter(DanmakuType::isTile),
                    Codec.BOOL.fieldOf("infinite").forGetter(DanmakuType::isInfinite)
            ).apply(instance, DanmakuType::getOrCreate)
    );

    private Identifier id;
    private final ResourceKey<DamageType> damageType;
    private final float damage;
    private final float scale;
    private final float speed;
    private final boolean tile;
    private final boolean infinite;
    private DeferredItem itemHolder;
    private BaseDanmakuEntity.HitCallback hitFactory;
    private RegistryImpl<DanmakuType> owner;
    private boolean deleteFromList = false;

    public DanmakuType(Identifier id, ResourceKey<DamageType> damageType, float damage, float scale, float speed, boolean tile, boolean infinite) {
        this.id = id;
        this.damageType = damageType;
        this.damage = damage;
        this.scale = scale;
        this.speed = speed;
        this.tile = tile;
        this.infinite = infinite;
        this.createItemEntry();
    }

    public static DanmakuType getOrCreate(Identifier id, ResourceKey<DamageType> damageType, float damage, float scale, float speed, boolean tile, boolean infinite) {
        DanmakuType type = RegistryImpls.DANMAKU_TYPE.getValue(id);
        if (type == null) {
            return new DanmakuType(id, damageType, damage, scale, speed, tile, infinite);
        }
        return type;
    }

    @Override
    public String translateKey() {
        return this.itemHolder.asItem().getDescriptionId();
    }

    public DanmakuShape toShape() {
        for (Map.Entry<ResourceKey<DanmakuShape>, DanmakuShape> mapEntry : RegistryImpls.DANMAKU_SHAPE.entrySet()) {
            DanmakuShape shape = mapEntry.getValue();
            if (shape.getType() == this) {
                return shape;
            }
        }
        return new DanmakuShape(this);
    }

    public void createItemEntry() {
        BalmItemRegistrar itemRegistrar = ReverieDreams.getItemRegistrar();
        BalmItemRegistration itemRegistration = itemRegistrar.register(this.getItemId().getPath(), (props) -> {
                    DanmakuItem item = new DanmakuItem(props
                            .component(RDDataComponents.DANMAKU_PROPERTIES.value(), this.createDanmakuProperties())
                            .component(DataComponents.USE_COOLDOWN, new UseCooldown(0.5f, Optional.of(Identifier.parse(UUID.randomUUID().toString()))))
                            .durability(120)
                            .repairable(RDItemTags.POWER_BLOCK)
                            .component(DataComponents.DYED_COLOR, new DyedItemColor(14606046))
                            .component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new ReferenceLinkedOpenHashSet<>(List.of(DataComponents.DYED_COLOR))))
                            .repairable(RDItemTags.DANMAKU_REPAIR_ACCEPTABLE_ITEM)
                            .durability(120));
                    item.type(this);
                    return item;
                }
        );
        this.itemHolder = itemRegistration.asDeferredItem();
    }

    @SuppressWarnings({"deprecation", "OptionalGetWithoutIsPresent"})
    public Supplier<List<Tuple<Item, ItemStackTemplate>>> getColorPairs() {
        List<Tuple<Item, ItemStackTemplate>> pairList = new LinkedList<>();
        ItemStackTemplate defaultStack = new ItemStackTemplate(this.itemHolder.asItem());
        for (Map.Entry<Item, Long> itemLongEntry : ItemColor.getView().entrySet()) {
            Item dyeItem = itemLongEntry.getKey();
            ItemStackTemplate template = new ItemStackTemplate(defaultStack.item(), defaultStack.count(), defaultStack.components());
            int color = itemLongEntry.getValue().intValue();
            Component hoverName = ItemStackTemplateHelper.getHoverName(template);
            Style style = hoverName.getStyle().withColor(brighten(color, 1.25f));
            Component colored = hoverName.copy().setStyle(style);
            String gid = "%s_%s_%s".formatted(template.item().unwrapKey().get().identifier(), dyeItem.builtInRegistryHolder().key().identifier(), color);
            UUID uuid = UUID.nameUUIDFromBytes(gid.getBytes(StandardCharsets.UTF_8));
            ItemStackTemplateHelper.modify(template, (old, modifier) -> {
                modifier.set(DataComponents.ITEM_NAME, colored);
                modifier.set(DataComponents.DYED_COLOR, new DyedItemColor(itemLongEntry.getValue().intValue()));
                modifier.set(DataComponents.USE_COOLDOWN, new UseCooldown(0.5f, Optional.of(Identifier.parse(uuid.toString()))));
            });
            pairList.add(new Tuple<>(dyeItem, template));
        }
        return () -> pairList;
    }

    private int brighten(int color, float factor) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = Math.min(255, (int) (r * factor));
        g = Math.min(255, (int) (g * factor));
        b = Math.min(255, (int) (b * factor));

        return (r << 16) | (g << 8) | b;
    }

    public Identifier getItemId() {
        return Identifier.fromNamespaceAndPath(this.id.getNamespace(), "danmaku/" + this.id.getPath());
    }

    public DanmakuType unlist() {
        this.deleteFromList = true;
        return this;
    }

    public DanmakuProperties createDanmakuProperties() {
        return new DanmakuProperties(
                ReverieDreams.id("single"),
                1,
                this.damage,
                this.damageType,
                this.scale,
                this.speed,
                0,
                this.tile,
                this.infinite
        );
    }

    @Override
    public Codec<DanmakuType> getCodec() {
        return COMPONENT_CODEC;
    }
}
