package cc.thonly.reverie_dreams.fumo;

import cc.thonly.reverie_dreams.block.BaseFumoBlock;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.entity.npc.NPCState;
import cc.thonly.reverie_dreams.registry.*;
import com.mojang.serialization.Codec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

@Setter
@Getter
public class Fumo implements CodecStep<Fumo>, OwnerBinding<Fumo>, BuiltinObject, Translatable {
    public static final Codec<Fumo> CODEC = Codec.unit(Fumo::new);
    private Identifier id;
    private Identifier registryKey;
    private IntrinsicalRegister<Fumo> owner;

    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Block block;
    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Item item;
    private Vec3d offset;

    private Fumo() {
    }

    public Fumo(Identifier id, Vec3d offset) {
        this.id = id;
        this.registryKey = Identifier.of(id.getNamespace(), "fumo/" + id.getPath());
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
        return this.block.getTranslationKey();
    }

    public Fumo build() {
        Pair<Block, Item> pair = this.registerBlock();
        this.block = pair.getLeft();
        this.item = pair.getRight();
        return this;
    }

    private Pair<Block, Item> registerBlock() {
        Pair<Block, Item> pair = new Pair<>(null, null);
        Block left = ModBlocks.registerSimpleBlock(this.registryKey, (settings) -> new BaseFumoBlock(this.offset, settings.noCollision()), AbstractBlock.Settings.copy(Blocks.WHITE_WOOL));
        pair.setLeft(left);
        pair.setRight(left.asItem());
        return pair;
    }

    @Override
    public Codec<Fumo> getCodec() {
        return CODEC;
    }
}
