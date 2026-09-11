package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.block.BeehiveBlockProxy;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BeehiveBlock.class)
public abstract class BeehiveBlockMixin extends Block implements BeehiveBlockProxy {

    public BeehiveBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void reverie_dreams$onInteractUse(
            BaseNPCLikeEntity roleEntity,
            ServerLevel serverLevel,
            BlockState blockState,
            @Nullable BlockPos currentTarget
    ) {
        if (currentTarget == null) {
            return;
        }

        int honeyLevel = blockState.getValue(BeehiveBlock.HONEY_LEVEL);

        if (honeyLevel < BeehiveBlock.MAX_HONEY_LEVELS) {
            return;
        }

        BeehiveBlockEntity beehiveBlockEntity = serverLevel.getBlockEntity(currentTarget) instanceof BeehiveBlockEntity beehive ? beehive : null;

        if (beehiveBlockEntity == null) {
            return;
        }

        ItemStack mainHand = roleEntity.getMainHandItem();
        ItemStack offHand = roleEntity.getOffhandItem();

        ItemStack tool = ItemStack.EMPTY;
        InteractionHand hand = InteractionHand.MAIN_HAND;

        if (mainHand.is(Items.SHEARS)) {
            tool = mainHand;
            hand = InteractionHand.MAIN_HAND;
        } else if (offHand.is(Items.SHEARS)) {
            tool = offHand;
            hand = InteractionHand.OFF_HAND;
        } else if (mainHand.is(Items.GLASS_BOTTLE)) {
            tool = mainHand;
            hand = InteractionHand.MAIN_HAND;
        } else if (offHand.is(Items.GLASS_BOTTLE)) {
            tool = offHand;
            hand = InteractionHand.OFF_HAND;
        }

        if (tool.isEmpty()) {
            return;
        }

        boolean hiveEmptied = false;

        // 剪刀采集蜜脾
        if (tool.is(Items.SHEARS)) {
            BeehiveBlock.dropHoneycomb(
                    serverLevel,
                    tool,
                    blockState,
                    beehiveBlockEntity,
                    roleEntity,
                    currentTarget
            );

            tool.hurtAndBreak(
                    1,
                    roleEntity,
                    hand.asEquipmentSlot()
            );

            hiveEmptied = true;
        }

        // 玻璃瓶采集蜂蜜
        else if (tool.is(Items.GLASS_BOTTLE)) {
            tool.shrink(1);

            ItemStack honeyBottle = new ItemStack(Items.HONEY_BOTTLE);

            // 原手中的玻璃瓶已经全部消耗
            if (tool.isEmpty()) {
                roleEntity.setItemInHand(hand, ItemStack.EMPTY);

                // 优先尝试主动堆叠到背包已有蜂蜜瓶
                ItemStack remainder = insertAndStack(
                        roleEntity,
                        honeyBottle
                );

                // 背包无法容纳时，放回手中
                if (!remainder.isEmpty()) {
                    roleEntity.setItemInHand(hand, remainder);
                }
            } else {
                // 手里还有玻璃瓶，蜂蜜瓶尝试主动放入背包
                ItemStack remainder = insertAndStack(
                        roleEntity,
                        honeyBottle
                );

                if (!remainder.isEmpty()) {
                    roleEntity.spawnAtLocation(
                            serverLevel,
                            remainder
                    );
                }
            }

            hiveEmptied = true;
        }

        if (!hiveEmptied) {
            return;
        }

        boolean smokey = CampfireBlock.isSmokeyPos(
                serverLevel,
                currentTarget
        );

        if (!smokey) {
            beehiveBlockEntity.emptyAllLivingFromHive(
                    null,
                    blockState,
                    BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY
            );
        }

        BeehiveBlock beehive = (BeehiveBlock) (Object) this;

        beehive.resetHoneyLevel(
                serverLevel,
                blockState,
                currentTarget
        );

        serverLevel.gameEvent(
                roleEntity,
                GameEvent.SHEAR,
                currentTarget
        );
        beehiveBlockEntity.setChanged();
        SoundEventPlayUtils.playSound(roleEntity, SoundEvents.HONEY_DRINK.value(), SoundSource.BLOCKS);

    }

    @Unique
    private static ItemStack insertAndStack(
            BaseNPCLikeEntity entity,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemUtils.updateItemStackTag(stack);

        var inventory = entity.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack existing = inventory.getItem(slot);

            if (inventory.isArmorSlot(slot)) {
                continue;
            }

            if (existing.isEmpty()) {
                continue;
            }

            if (!ItemStack.isSameItemSameComponents(existing, stack)) {
                continue;
            }

            int maxStackSize = Math.min(
                    existing.getMaxStackSize(),
                    inventory.getMaxStackSize()
            );

            int canAdd = maxStackSize - existing.getCount();

            if (canAdd <= 0) {
                continue;
            }

            int move = Math.min(
                    canAdd,
                    stack.getCount()
            );

            existing.grow(move);
            stack.shrink(move);

            inventory.setItem(slot, existing);

            if (stack.isEmpty()) {
                inventory.setChanged();
                return ItemStack.EMPTY;
            }
        }

        if (!stack.isEmpty()) {
            stack = inventory.addItem(stack);
        }

        return stack;
    }
}