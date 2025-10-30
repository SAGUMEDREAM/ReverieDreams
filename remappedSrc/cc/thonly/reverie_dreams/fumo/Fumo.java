package cc.thonly.reverie_dreams.fumo;

import cc.thonly.reverie_dreams.block.BaseFumoBlock;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.registry.*;
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
public class Fumo implements CodecStep<Fumo>, OwnerBinding<Fumo>, BuiltinObject, Translatable {
    public static final Codec<Fumo> CODEC = Codec.unit(Fumo::new);
    private ResourceLocation id;
    private ResourceLocation registryKey;
    private IntrinsicalRegister<Fumo> owner;

    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Block block;
    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Item item;
    private Vec3 offset;

    private Fumo() {
    }

    public Fumo(ResourceLocation id, Vec3 offset) {
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

    public Fumo build() {
        Tuple<Block, Item> pair = this.registerBlock();
        this.block = pair.getA();
        this.item = pair.getB();
        return this;
    }

    private Tuple<Block, Item> registerBlock() {
        Tuple<Block, Item> pair = new Tuple<>(null, null);
        Block left = ModBlocks.registerSimpleBlock(this.registryKey, (settings) -> new BaseFumoBlock(this.offset, settings.noCollission()), BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL));
        pair.setA(left);
        pair.setB(left.asItem());
        return pair;
    }

    @Override
    public Codec<Fumo> getCodec() {
        return CODEC;
    }
}
