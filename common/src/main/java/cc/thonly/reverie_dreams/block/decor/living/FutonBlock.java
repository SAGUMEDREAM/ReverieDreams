package cc.thonly.reverie_dreams.block.decor.living;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import cc.thonly.reverie_dreams.workshop.FurnitureSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Baked JSON two-part bed; no special vanilla bed renderer or ticking entity.
 */
public class FutonBlock extends BedBlock {
    public static final MapCodec<BedBlock> CODEC = simpleCodec(FutonBlock::new);

    public FutonBlock(Properties properties) {
        super(DyeColor.BLUE, properties);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level,
                                            BlockPos pos, Player player, BlockHitResult hit) {
        boolean sleeping = player.isSleeping();
        var result = super.useWithoutItem(state, level, pos, player, hit);
        if (!level.isClientSide() && !sleeping && player.isSleeping())
            FurnitureSounds.play(level, pos, SoundEvents.WOOL_STEP, .3f, .9f);
        return result;
    }

    @Override
    public MapCodec<BedBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return JapaneseLivingShapes.shape(state.getValue(PART) == BedPart.HEAD ? "futon_head" : "futon_foot", state.getValue(FACING));
    }
}
