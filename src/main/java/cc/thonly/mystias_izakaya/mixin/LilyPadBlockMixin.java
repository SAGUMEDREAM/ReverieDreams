package cc.thonly.mystias_izakaya.mixin;


import cc.thonly.mystias_izakaya.item.MIItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LilyPadBlock.class)
public class LilyPadBlockMixin implements Fertilizable {

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        this.applyGrowth(world, random, pos, state);
    }

    @Unique
    public void applyGrowth(World world, Random random, BlockPos pos, BlockState state) {
        int chance = random.nextBetween(0, 100);
        int number = random.nextBetween(1, 2);
        ItemEntity itemEntity = null;
        if (chance < 25) {
            itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(MIItems.LOTUS_NUTS, number), 0, 0.2, 0);
        } else if (chance < 45) {
            itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(MIItems.TWIN_LOTUS, number), 0, 0.2, 0);
        } else if (chance < 65) {
            itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(Items.LILY_PAD, 1), 0, 0.2, 0);
        }

        if (itemEntity == null) {
            return;
        }
        world.spawnEntity(itemEntity);
    }
}
