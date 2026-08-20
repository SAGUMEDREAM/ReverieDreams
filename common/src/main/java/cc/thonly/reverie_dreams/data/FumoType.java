package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.SerializableProvider;
import cc.thonly.reverie_dreams.registry.RegistryEntryOwnerBindable;
import cc.thonly.reverie_dreams.registry.RegistryEntryTranslatable;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

@Setter
@Getter
@ToString
public class FumoType implements SerializableProvider<FumoType>, RegistryEntryOwnerBindable<FumoType>, BuiltinObject, RegistryEntryTranslatable {
    public static final Codec<FumoType> CODEC = UnitCodec.unit(FumoType::new);
    private Identifier id;
    private Identifier registryKey;
    private RegistryProvider<FumoType> owner;

    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private BlockDelegate block;

    private FumoType() {
    }

    public FumoType(Identifier id) {
        this.id = id;
        this.registryKey = Identifier.fromNamespaceAndPath(id.getNamespace(), "fumo/" + id.getPath());
    }

    public BlockDelegate blockAsDeferred() {
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
            return RegistryEntryTranslatable.super.translateKey();
        }
        return this.block.asBlock().getDescriptionId();
    }

    public FumoType build() {
        this.block = this.registerBlock();
        return this;
    }

    private BlockDelegate registerBlock() {
        return RDBlocks.registerSimpleBlock(this.registryKey.getPath(), (settings) -> new BaseFumoBlock(settings.noCollision()), BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL));
    }

    @Override
    public Codec<FumoType> getCodec() {
        return CODEC;
    }
}
