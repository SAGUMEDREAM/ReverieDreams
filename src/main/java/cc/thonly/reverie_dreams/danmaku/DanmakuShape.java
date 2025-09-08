package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.registry.*;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;

@Setter
@Getter
public class DanmakuShape implements CodecStep<DanmakuShape>, OwnerBinding<DanmakuShape>, Translatable, BuiltinObject {
    public static final Codec<DanmakuShape> CODEC = Codec.unit(DanmakuShape::new);
    private IntrinsicalRegister<DanmakuShape> owner;
    private final DanmakuType type;
    private final ItemStack baseItemStack;

    private DanmakuShape() {
        this(DanmakuTypes.AMULET);
    }

    public DanmakuShape(DanmakuType danmakuType) {
        this.type = danmakuType;
        ItemStack stack = ModItems.DANMAKU_SHAPE_CREATOR.getDefaultStack();
        stack.set(ModDataComponentTypes.Danmaku.SHAPE, ItemStackWrapper.of(danmakuType.getItem()));
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
