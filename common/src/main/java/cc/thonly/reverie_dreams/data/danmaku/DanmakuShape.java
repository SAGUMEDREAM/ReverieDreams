package cc.thonly.reverie_dreams.data.danmaku;

import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

@Setter
@Getter
@ToString
public class DanmakuShape implements CodecStep<DanmakuShape>, OwnerBinding<DanmakuShape>, Translatable, BuiltinObject {
    public static final Codec<DanmakuShape> CODEC = UnitCodec.unit(DanmakuShape::new);
    public static final Function<DanmakuType, ItemStack> ITEM_STACK_TEMPLATE = (danmakuType) -> {
        ItemStack stack = RDItems.DANMAKU_SHAPE_CREATOR.createStack();
        stack.set(RDDataComponents.DANMAKU_SHAPE.value(), ItemStackWrapper.of(danmakuType.getItemHolder()));
        return stack;
    };
    private RegistryImpl<DanmakuShape> owner;
    private final DanmakuType type;
    private final Function<Unit, ItemStack> getter;

    private DanmakuShape() {
        this(DanmakuTypes.AMULET);
    }

    public DanmakuShape(DanmakuType danmakuType) {
        this.type = danmakuType;
        this.getter = (type) -> ITEM_STACK_TEMPLATE.apply(this.type);
    }

    @Override
    public String translateKey() {
        return this.type.translateKey();
    }

    public ItemStack getItemStackTemplate() {
        return this.getter.apply(Unit.INSTANCE);
    }

    public ItemStack getItemStack() {
        return this.getter.apply(Unit.INSTANCE);
    }

    @Override
    public Codec<DanmakuShape> getCodec() {
        return CODEC;
    }

}
