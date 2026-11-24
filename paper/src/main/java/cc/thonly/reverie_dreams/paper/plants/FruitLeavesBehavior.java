package cc.thonly.reverie_dreams.paper.plants;

import java.util.concurrent.ThreadLocalRandom;
import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.BonemealableBlock;
import net.momirealms.craftengine.core.block.behavior.LiquidBlockContainer;
import net.momirealms.craftengine.core.block.behavior.RandomTickBlock;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockAndLightGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.EquipmentSlot;

/** Complements native leaves_block's scheduled distance updates and decay, rather than replacing them. */
final class FruitLeavesBehavior extends BukkitBlockBehavior implements RandomTickBlock, BonemealableBlock, LiquidBlockContainer {
    static final BlockBehaviorFactory<FruitLeavesBehavior> FACTORY = (block, section) -> new FruitLeavesBehavior(block);
    private FruitLeavesBehavior(BlockDefinition block) { super(block); }
    private static Block at(Object level, Object pos) {
        var p = LocationUtils.fromBlockPos(pos);
        return LevelProxy.INSTANCE.getWorld(level).getBlockAt(p.x(), p.y(), p.z());
    }
    @Override public boolean canRandomlyTick(ImmutableBlockState state) { return true; }
    @Override public void randomTick(Object thisBlock, Object[] args) {
        if (!PlantsModule.enabled()) return;
        Block block = at(args[1], args[2]);
        // leaves_block runs first; never resurrect a leaf already removed/cancelled by its decay callback.
        if (PlantsModule.fruitAge(block) < 0) return;
        PlantsModule.tryRandomTick(block, BlockAndLightGetterProxy.INSTANCE.getRawBrightness(args[1], args[2], 0),
                ThreadLocalRandom.current().nextInt(PlantsModule.growthDenominator(block)));
    }
    @Override public boolean isValidBonemealTarget(Object block, Object[] args) { return PlantsModule.enabled(); }
    @Override public boolean isBonemealSuccess(Object block, Object[] args) {
        var state = BlockStateUtils.getNullableCustomBlockState(args[3]);
        return PlantsModule.enabled() && state != null && state.<Integer>get(state.getProperty("fruit_age")) < 3;
    }
    @Override public void performBonemeal(Object block, Object[] args) { if (PlantsModule.enabled()) PlantsModule.boneMealFruit(at(args[0], args[2])); }
    @Override public boolean canPlaceLiquid(Object block, Object[] args) { return false; }
    @Override public boolean placeLiquid(Object block, Object[] args) { return false; }
    @Override public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        if (!PlantsModule.enabled() || context.getPlayer() == null) return InteractionResult.PASS;
        if (context.getItem().vanillaId().equals(Key.of("minecraft:bone_meal"))) {
            if (context.getPlayer().isAdventureMode()) return InteractionResult.PASS;
            var p = context.getClickedPos(); var player = (Player) context.getPlayer().platformPlayer();
            var location = new Location((World) context.getLevel().platformWorld(), p.x(), p.y(), p.z());
            if (!BukkitCraftEngine.instance().antiGriefProvider().test(player, Flag.INTERACT, location)) return InteractionResult.FAIL;
            context.getPlayer().swingHand(context.getHand());
            // Native BoneMealItem owns success, consumption and fertilizer capture/events.
            return InteractionResult.SUCCESS;
        }
        return useWithoutItem(context, state);
    }
    @Override public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) {
        if (!PlantsModule.enabled() || context.getPlayer() == null) return InteractionResult.PASS;
        var player = (Player) context.getPlayer().platformPlayer();
        if (PlantsModule.holdsBoneMeal(player)) return InteractionResult.PASS;
        var p = context.getClickedPos();
        var block = ((World) context.getLevel().platformWorld()).getBlockAt(p.x(), p.y(), p.z());
        if (PlantsModule.fruitAge(block) < 3) return InteractionResult.PASS;
        var hand = context.getHand() == InteractionHand.MAIN_HAND
                ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
        return PlantsModule.harvestFruit(block, player, ThreadLocalRandom.current().nextInt(1, 4), hand)
                ? InteractionResult.SUCCESS_AND_CANCEL : InteractionResult.FAIL;
    }
}
