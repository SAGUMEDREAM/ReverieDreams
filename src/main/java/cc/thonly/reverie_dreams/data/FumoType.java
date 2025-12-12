package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import com.mojang.serialization.Codec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;

@Setter
@Getter
public class FumoType implements CodecStep<FumoType>, OwnerBinding<FumoType>, BuiltinObject, Translatable {
    public static final Codec<FumoType> CODEC = Codec.unit(FumoType::new);
    private ResourceLocation id;
    private ResourceLocation registryKey;
    private RegistryHandler<FumoType> owner;

    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Block block;
    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Item item;
    private Vec3 offset;

    private FumoType() {
    }

    public FumoType(ResourceLocation id, Vec3 offset) {
        this.id = id;
        this.registryKey = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "fumo/" + id.getPath());
        this.offset = offset;
    }

    public Block block() {
        return this.block;
    }

    public Item item() {
        return this.item;
    }

    public String translateKey() {
        if (this.block == null) {
            return Translatable.super.translateKey();
        }
        return this.block.getDescriptionId();
    }

    public FumoType build() {
        Tuple<Block, Item> pair = this.registerBlock();
        this.block = pair.getA();
        this.item = pair.getB();
        return this;
    }

    private Tuple<Block, Item> registerBlock() {
        Tuple<Block, Item> pair = new Tuple<>(null, null);
        Block left = RDBlocks.registerSimpleBlock(this.registryKey, (settings) -> new BaseFumoBlock(this.offset, settings.noCollision()), BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL));
        pair.setA(left);
        pair.setB(left.asItem());
        return pair;
    }

    @Override
    public Codec<FumoType> getCodec() {
        return CODEC;
    }
}
