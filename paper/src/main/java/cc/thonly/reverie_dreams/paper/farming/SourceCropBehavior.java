package cc.thonly.reverie_dreams.paper.farming;

import java.util.concurrent.ThreadLocalRandom;
import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.BonemealableBlock;
import net.momirealms.craftengine.core.block.behavior.RandomTickBlock;
import net.momirealms.craftengine.core.block.property.Property;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockAndLightGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.World;

/** Only exact source crop rules missing from CE's fixed-speed crop behavior. No placed-block state is shared. */
final class SourceCropBehavior extends BukkitBlockBehavior implements RandomTickBlock, BonemealableBlock {
    static final BlockBehaviorFactory<SourceCropBehavior> FACTORY = (block, section) ->
            new SourceCropBehavior(block, section.getNonNullInt("max_age"));
    private final Property<Integer> age;
    private final int maxAge;
    private SourceCropBehavior(BlockDefinition block, int maxAge) {
        super(block); this.maxAge = maxAge;
        age = BlockBehaviorFactory.getProperty("age", block, "age", Integer.class);
    }
    @Override public boolean canRandomlyTick(ImmutableBlockState state) { return state.get(age) < maxAge; }
    @Override public boolean canSurvive(Object block, Object[] args) {
        Object below = BlockGetterProxy.INSTANCE.getBlockState(args[1], LocationUtils.below(args[2]));
        var custom = BlockStateUtils.getNullableCustomBlockState(below);
        return custom != null ? FarmingModule.PADDY_ID.equals(custom.owner().value().id())
                : Key.of("minecraft:farmland").equals(BlockStateUtils.getBlockOwnerIdFromState(below));
    }
    private static Block at(Object level, Object pos) {
        var p = LocationUtils.fromBlockPos(pos);
        return LevelProxy.INSTANCE.getWorld(level).getBlockAt(p.x(), p.y(), p.z());
    }
    @Override public void randomTick(Object block, Object[] args) {
        if (!FarmingModule.enabled()) return;
        var crop = at(args[1], args[2]);
        if (FarmingModule.checkSupport(crop)) return;
        int brightness = BlockAndLightGetterProxy.INSTANCE.getRawBrightness(args[1], args[2], 0);
        FarmingModule.tryRandomTick(crop, brightness, ThreadLocalRandom.current().nextInt(FarmingModule.growthDenominator(crop)));
    }
    @Override public void neighborChanged(Object block, Object[] args) {
        if (FarmingModule.enabled()) FarmingModule.checkSupport(at(args[1], args[2]));
    }
    @Override public boolean isValidBonemealTarget(Object block, Object[] args) {
        var state = BlockStateUtils.getNullableCustomBlockState(args[2]);
        return FarmingModule.enabled() && state != null && state.get(age) < maxAge;
    }
    @Override public boolean isBonemealSuccess(Object block, Object[] args) { return true; }
    @Override public void performBonemeal(Object block, Object[] args) {
        if (FarmingModule.enabled()) FarmingModule.boneMeal(at(args[0], args[2]), ThreadLocalRandom.current().nextInt(1, 4));
    }
    @Override public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        if (!FarmingModule.enabled() || context.getPlayer() == null) return InteractionResult.PASS;
        // As CE26.8.2 CropBlockBehavior does, SUCCESS lets the real vanilla bone-meal item drive
        // native consumption, dispenser behavior and its fertilize event; no second manual debit.
        if (context.getItem().vanillaId().equals(Key.of("minecraft:bone_meal")) && state.get(age) < maxAge) {
            var p = context.getClickedPos(); var player = (Player) context.getPlayer().platformPlayer();
            var crop = ((World) context.getLevel().platformWorld()).getBlockAt(p.x(), p.y(), p.z());
            if (!BukkitCraftEngine.instance().antiGriefProvider().test(player, Flag.INTERACT, crop.getLocation())) return InteractionResult.FAIL;
            return InteractionResult.SUCCESS;
        }
        return useWithoutItem(context, state);
    }
    @Override public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) {
        if (!FarmingModule.enabled() || context.getPlayer() == null || state.get(age) < maxAge) return InteractionResult.PASS;
        var p = context.getClickedPos();
        var crop = ((World) context.getLevel().platformWorld()).getBlockAt(p.x(), p.y(), p.z());
        return FarmingModule.harvest(crop, (Player) context.getPlayer().platformPlayer())
                ? InteractionResult.SUCCESS_AND_CANCEL : InteractionResult.FAIL;
    }
}
