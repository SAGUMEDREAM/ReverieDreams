package cc.thonly.polymer.block.impl;

import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import xyz.nucleoid.packettweaker.PacketContext;

@Getter
public class BasicPolymerTrapdoorBlock extends TrapDoorBlock implements PolymerTexturedBlock {
    private final ResourceLocation blockId;

    public BasicPolymerTrapdoorBlock(Properties settings) {
        super(BlockSetType.OAK, settings);
        assert settings.id != null;
        this.blockId = settings.id.location();
    }

    private PolymerBlockModel getModel(String type, int x, int y) {
        return PolymerBlockModel.of(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(), "block/%s_%s".formatted(this.blockId.getPath(), type)), x, y);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_TRAPDOOR.withPropertiesOf(state);
    }
    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_TRAPDOOR.withPropertiesOf(state);
    }
}