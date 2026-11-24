package cc.thonly.reverie_dreams.paper.plants;

import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.BonemealableBlock;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/** Original udumbara duplicates one block item on bone meal. Tremella deliberately has no such behavior. */
final class FertilizableFlowerBehavior extends BukkitBlockBehavior implements BonemealableBlock {
    static final BlockBehaviorFactory<FertilizableFlowerBehavior> FACTORY = (block, section) -> new FertilizableFlowerBehavior(block);
    private FertilizableFlowerBehavior(BlockDefinition block) { super(block); }
    @Override public boolean isValidBonemealTarget(Object block, Object[] args) { return PlantsModule.enabled(); }
    @Override public boolean isBonemealSuccess(Object block, Object[] args) { return true; }
    @Override public void performBonemeal(Object block, Object[] args) {
        if (!PlantsModule.enabled()) return;
        var p = LocationUtils.fromBlockPos(args[2]);
        PlantsModule.duplicateUdumbara(LevelProxy.INSTANCE.getWorld(args[0]).getBlockAt(p.x(), p.y(), p.z()));
    }
    @Override public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        if (!PlantsModule.enabled() || context.getPlayer() == null || !context.getItem().vanillaId().equals(Key.of("minecraft:bone_meal")))
            return InteractionResult.PASS;
        if (context.getPlayer().isAdventureMode()) return InteractionResult.PASS;
        var p = context.getClickedPos();
        var location = new Location((World) context.getLevel().platformWorld(), p.x(), p.y(), p.z());
        if (!BukkitCraftEngine.instance().antiGriefProvider().test((Player) context.getPlayer().platformPlayer(), Flag.INTERACT, location))
            return InteractionResult.FAIL;
        context.getPlayer().swingHand(context.getHand());
        return InteractionResult.SUCCESS;
    }
}
