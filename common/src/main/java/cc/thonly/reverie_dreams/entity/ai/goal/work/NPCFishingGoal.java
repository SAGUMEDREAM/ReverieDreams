package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.NPCFishingHook;
import cc.thonly.reverie_dreams.entity.ai.work.ScheduledWorkGoal;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NPCFishingGoal extends ScheduledWorkGoal {
    private BlockPos water;

    public NPCFishingGoal(NPCSimpleEntity npc) {
        super(npc, NPCWorkModes.FISHING);
    }

    public static boolean isWater(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos).isSource()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }

    private static boolean rod(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof FishingRodItem;
    }

    @Override
    protected boolean findTask(ServerLevel level) {
        water = search.find(site, level.getGameTime(), level::hasChunkAt,
                pos -> work().available(pos, level.getGameTime()) && isWater(level, pos));
        if (water == null) {
            work().status("no_nearby_water");
            return false;
        }
        return true;
    }

    @Override
    protected boolean validTask(ServerLevel level) {
        return water != null && site.contains(water) && isWater(level, water);
    }

    @Override
    protected List<?> resources() {
        return water == null ? List.of() : List.of(water);
    }

    @Override
    protected void clearTask() {
        water = null;
        if (npc.fishing != null)
            npc.fishing.discard();
        npc.fishing = null;
    }

    @Override
    protected void runTask(ServerLevel level) {
        var hand = work().tools().ensure(npc.getInventory(), NPCFishingGoal::rod);
        if (hand == null) {
            work().demand(NPCFishingGoal::rod, 1, "need_fishing_rod");
            finish("need_fishing_rod", 20);
            return;
        }
        var stack = npc.getItemInHand(hand);
        if (npc.fishing != null && !npc.fishing.isRemoved()) {
            var hook = npc.fishing;
            npc.getLookControl().setLookAt(hook.getX(), hook.getY(), hook.getZ());
            if (!hook.isBiting()) {
                work().status("waiting_for_fish");
                return;
            }
            int damage = hook.retrieve(stack);
            if (damage > 0) {
                stack.hurtAndBreak(damage, npc, hand.asEquipmentSlot());
                work().action();
            }
            npc.fishing = null;
            finish("ready", 20);
            return;
        }
        npc.fishing = null;
        if (!reach(water, 2.8))
            return;
        npc.getLookControl().setLookAt(water.getCenter());
        int luck = EnchantmentHelper.getFishingLuckBonus(level, stack, npc);
        int lureTicks = (int) (EnchantmentHelper.getFishingTimeReduction(level, stack, npc) * 20);
        var hook = new NPCFishingHook(npc, level, luck, lureTicks);
        Vec3 direction = water.getCenter().add(0, .75, 0).subtract(hook.position());
        if (direction.lengthSqr() < .001) {
            hook.discard();
            finish("no_nearby_water", 20);
            return;
        }
        hook.setDeltaMovement(direction.normalize().scale(.8));
        if (!level.addFreshEntity(hook)) {
            hook.discard();
            finish("work_action_failed", 40);
            return;
        }
        npc.fishing = hook;
        npc.swing(hand);
        level.playSound(null, npc.blockPosition(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1, 1);
        work().status("waiting_for_fish");
    }
}
