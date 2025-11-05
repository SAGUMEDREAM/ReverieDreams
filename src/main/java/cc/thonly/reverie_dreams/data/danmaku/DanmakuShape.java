package cc.thonly.reverie_dreams.data.danmaku;

import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;

@Setter
@Getter
public class DanmakuShape implements CodecStep<DanmakuShape>, OwnerBinding<DanmakuShape>, Translatable, BuiltinObject {
    public static final Codec<DanmakuShape> CODEC = Codec.unit(DanmakuShape::new);
    private RegistryHandler<DanmakuShape> owner;
    private final DanmakuType type;
    private final ItemStack baseItemStack;

    private DanmakuShape() {
        this(DanmakuTypes.AMULET);
    }

    public DanmakuShape(DanmakuType danmakuType) {
        this.type = danmakuType;
        ItemStack stack = RDItems.DANMAKU_SHAPE_CREATOR.getDefaultInstance();
        stack.set(RDDataComponentTypes.DANMAKU_SHAPE, ItemStackWrapper.of(danmakuType.getItem()));
        this.baseItemStack = stack;
    }

    @Override
    public String translateKey() {
        return this.type.translateKey();
    }

    public ItemStack getItemStack() {
        return this.baseItemStack.copy();
    }

    @Override
    public Codec<DanmakuShape> getCodec() {
        return CODEC;
    }

}
