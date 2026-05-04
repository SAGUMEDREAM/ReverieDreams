package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

@Setter
@Getter
@ToString
public class FumoType implements CodecStep<FumoType>, OwnerBinding<FumoType>, BuiltinObject, Translatable {
    public static final Codec<FumoType> CODEC = UnitCodec.unit(FumoType::new);
    private Identifier id;
    private Identifier registryKey;
    private RegistryImpl<FumoType> owner;

    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private DeferredBlock block;

    private FumoType() {
    }

    public FumoType(Identifier id) {
        this.id = id;
        this.registryKey = Identifier.fromNamespaceAndPath(id.getNamespace(), "fumo/" + id.getPath());
    }

    public DeferredBlock blockAsDeferred() {
        return this.block;
    }

    public Block block() {
        return this.block.asBlock();
    }

    public Item item() {
        return this.block.asItem();
    }

    public String translateKey() {
        if (this.block == null) {
            return Translatable.super.translateKey();
        }
        return this.block.asBlock().getDescriptionId();
    }

    public FumoType build() {
        this.block = this.registerBlock();
        return this;
    }

    private DeferredBlock registerBlock() {
        return RDBlocks.registerSimpleBlock(ReverieDreams.getBlockRegistrar(), this.registryKey.getPath(), (settings) -> new BaseFumoBlock(settings.noCollision()), BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL));
    }

    @Override
    public Codec<FumoType> getCodec() {
        return CODEC;
    }
}
