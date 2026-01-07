package cc.thonly.polymer.block.impl;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

@Setter
@Getter
@ToString
public class BasicPolymerLeavesBlock extends LeavesBlock implements PolymerBlock, PolymerTexturedBlock {
    BlockState polymerBlockState;

    public BasicPolymerLeavesBlock(Properties properties) {
        super(properties);
        ResourceKey<Block> resId = properties.id;
        assert resId != null;
        var id = resId.location();
        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(BlockModelType.BIOME_TRANSPARENT_BLOCK, PolymerBlockModel.of(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath())));
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return this.polymerBlockState;
    }

}
