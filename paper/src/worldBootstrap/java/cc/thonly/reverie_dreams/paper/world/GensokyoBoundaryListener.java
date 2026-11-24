package cc.thonly.reverie_dreams.paper.world;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldInitEvent;

/** Public-event counterpart of the source player-write/blast/piston guards. */
final class GensokyoBoundaryListener implements Listener {
    private final GensokyoWorldPlugin plugin;
    GensokyoBoundaryListener(GensokyoWorldPlugin plugin) { this.plugin = plugin; }
    private boolean outside(Block block) { return plugin.outside(block.getWorld(), block.getX(), block.getZ()); }
    @EventHandler public void worldInit(WorldInitEvent event) { plugin.initializeSpawn(event.getWorld()); }
    @EventHandler(ignoreCancelled = true) public void breakBlock(BlockBreakEvent event) { if (outside(event.getBlock())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void place(BlockPlaceEvent event) { if (outside(event.getBlockPlaced())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void multiPlace(BlockMultiPlaceEvent event) { if (event.getReplacedBlockStates().stream().anyMatch(s -> outside(s.getBlock()))) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void bucketEmpty(PlayerBucketEmptyEvent event) { if (outside(event.getBlock())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void bucketFill(PlayerBucketFillEvent event) { if (outside(event.getBlock())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void fertilize(BlockFertilizeEvent event) { if (event.getPlayer() != null) event.getBlocks().removeIf(s -> outside(s.getBlock())); }
    @EventHandler(ignoreCancelled = true) public void interact(PlayerInteractEvent event) { if (event.getClickedBlock() != null && outside(event.getClickedBlock())) event.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void entityBlast(EntityExplodeEvent event) { event.blockList().removeIf(this::outside); }
    @EventHandler(ignoreCancelled = true) public void blockBlast(BlockExplodeEvent event) { event.blockList().removeIf(this::outside); }
    @EventHandler(ignoreCancelled = true) public void pistonExtend(BlockPistonExtendEvent event) {
        if (outside(event.getBlock().getRelative(event.getDirection())) || event.getBlocks().stream().anyMatch(b -> outside(b) || outside(b.getRelative(event.getDirection())))) event.setCancelled(true);
    }
    @EventHandler(ignoreCancelled = true) public void pistonRetract(BlockPistonRetractEvent event) {
        if (outside(event.getBlock().getRelative(event.getDirection())) || event.getBlocks().stream().anyMatch(b -> outside(b) || outside(b.getRelative(event.getDirection().getOppositeFace())))) event.setCancelled(true);
    }
}
