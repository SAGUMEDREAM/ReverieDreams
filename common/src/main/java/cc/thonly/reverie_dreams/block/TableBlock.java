package cc.thonly.reverie_dreams.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TableBlock extends Block {

    /**
     * 桌面
     * X: 0 ~ 16
     * Y: 14 ~ 16
     * Z: 0 ~ 16
     */
    private static final VoxelShape TABLE_TOP = Block.box(
            0, 14, 0,
            16, 16, 16
    );

    /**
     * 四条桌腿
     * 腿宽：2
     * 腿高：14
     */

    // 西北
    private static final VoxelShape LEG_NORTH_WEST = Block.box(
            1, 0, 1,
            3, 14, 3
    );

    // 东北
    private static final VoxelShape LEG_NORTH_EAST = Block.box(
            13, 0, 1,
            15, 14, 3
    );

    // 西南
    private static final VoxelShape LEG_SOUTH_WEST = Block.box(
            1, 0, 13,
            3, 14, 15
    );

    // 东南
    private static final VoxelShape LEG_SOUTH_EAST = Block.box(
            13, 0, 13,
            15, 14, 15
    );

    /**
     * 合并碰撞箱
     */
    private static final VoxelShape COLLISION_SHAPE = Shapes.or(
            TABLE_TOP,
            LEG_NORTH_WEST,
            LEG_NORTH_EAST,
            LEG_SOUTH_WEST,
            LEG_SOUTH_EAST
    );

    public TableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return COLLISION_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return COLLISION_SHAPE;
    }
}