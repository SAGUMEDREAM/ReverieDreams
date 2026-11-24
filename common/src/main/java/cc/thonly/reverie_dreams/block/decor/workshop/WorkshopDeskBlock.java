package cc.thonly.reverie_dreams.block.decor.workshop;

import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseSet2Blocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import cc.thonly.reverie_dreams.workshop.FurnitureSounds;
import cc.thonly.reverie_dreams.workshop.WorkshopItems;
import cc.thonly.reverie_dreams.workshop.WorkshopWriting;

import java.util.Locale;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;

public class WorkshopDeskBlock extends WorkshopStorageBlock {
    public enum Accessory implements StringRepresentable {
        NONE, INKSTONE, BOOKS;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public WorkshopKind kind() {
            return this == INKSTONE ? WorkshopKind.SUZURI : WorkshopKind.BOOK_STACK;
        }
    }

    public static final EnumProperty<Accessory> ACCESSORY = EnumProperty.create("accessory", Accessory.class);
    public static final MapCodec<WorkshopDeskBlock> CODEC = simpleCodec(WorkshopDeskBlock::new);

    public WorkshopDeskBlock(Properties properties) {
        super(WorkshopKind.FUMIZUKUE, properties);
        registerDefaultState(defaultBlockState().setValue(ACCESSORY, Accessory.NONE));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACCESSORY);
    }

    @Override
    protected String model(BlockState state) {
        return kind.id() + "_" + state.getValue(ACCESSORY).getSerializedName() + (state.getValue(OPEN) ? "_open" : "");
    }

    protected Item accessoryItem(Accessory accessory) {
        return RDJapaneseSet2Blocks.get(accessory.kind()).asItem();
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(ACCESSORY) == Accessory.INKSTONE && WorkshopItems.document(stack))
            return WorkshopWriting.open(level, pos, player, hand);
        if (state.getValue(ACCESSORY) == Accessory.NONE)
            for (var accessory : new Accessory[]{Accessory.INKSTONE, Accessory.BOOKS})
                if (stack.is(accessoryItem(accessory)) && ItemStack.isSameItemSameComponents(stack, new ItemStack(accessoryItem(accessory)))) {
                    if (!level.isClientSide()) {
                        if (!level.setBlock(pos, state.setValue(ACCESSORY, accessory), 3))
                            return InteractionResult.FAIL;
                        stack.consume(1, player);
                        FurnitureSounds.play(level, pos, accessory == Accessory.BOOKS ? SoundEvents.BOOK_PUT : SoundEvents.STONE_PLACE, .35f, 1.0f);
                    }
                    return InteractionResult.SUCCESS;
                }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        var accessory = state.getValue(ACCESSORY);
        if (accessory != Accessory.NONE && (player.isShiftKeyDown() || hit.getLocation().y - pos.getY() > 8.01 / 16.0)) {
            if (!level.isClientSide()) {
                if (!level.setBlock(pos, state.setValue(ACCESSORY, Accessory.NONE), 3))
                    return InteractionResult.FAIL;
                ItemStack stack = new ItemStack(accessoryItem(accessory));
                if (!player.getInventory().add(stack))
                    player.drop(stack, false);
                FurnitureSounds.play(level, pos, accessory == Accessory.BOOKS ? SoundEvents.CHISELED_BOOKSHELF_PICKUP : SoundEvents.STONE_PLACE, .35f, 1.1f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }
}
