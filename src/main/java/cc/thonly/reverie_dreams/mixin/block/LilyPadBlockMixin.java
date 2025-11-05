package cc.thonly.reverie_dreams.mixin.block;


import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("NullableProblems")
@Mixin(WaterlilyBlock.class)
public class LilyPadBlockMixin implements BonemealableBlock {

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        this.applyGrowth(world, random, pos, state);
    }

    @Unique
    public void applyGrowth(Level world, RandomSource random, BlockPos pos, BlockState state) {
        int chance = random.nextIntBetweenInclusive(0, 100);
        int number = random.nextIntBetweenInclusive(1, 2);
        ItemEntity itemEntity = null;
        if (chance < 25) {
            itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(RDIngredientItems.LOTUS_NUTS, number), 0, 0.2, 0);
        } else if (chance < 45) {
            itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(RDIngredientItems.TWIN_LOTUS, number), 0, 0.2, 0);
        } else if (chance < 65) {
            itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(Items.LILY_PAD, 1), 0, 0.2, 0);
        }

        if (itemEntity == null) {
            return;
        }
        world.addFreshEntity(itemEntity);
    }
}
