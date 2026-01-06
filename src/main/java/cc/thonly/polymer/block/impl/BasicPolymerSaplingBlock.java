package cc.thonly.polymer.block.impl;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

@Getter
public class BasicPolymerSaplingBlock extends SaplingBlock implements PolymerTexturedBlock {
    private final ResourceLocation blockId;
    private final BlockState model;

    public BasicPolymerSaplingBlock(TreeGrower generator, Properties settings) {
        super(generator, settings);
        assert settings.id != null;
        this.blockId = settings.id.location();
        this.model = PolymerBlockResourceUtils.requestBlock(BlockModelType.PLANT_BLOCK, PolymerBlockModel.of(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(), "block/" + this.blockId.getPath())));
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return model;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return model;
    }
}