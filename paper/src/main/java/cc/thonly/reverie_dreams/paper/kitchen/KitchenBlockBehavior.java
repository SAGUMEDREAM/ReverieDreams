package cc.thonly.reverie_dreams.paper.kitchen;

import cc.thonly.reverie_dreams.paper.util.Ids;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.block.behavior.AbstractCanSurviveBlockBehavior;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlock;
import net.momirealms.craftengine.core.block.behavior.WorldlyContainerHolder;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.block.property.Property;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.inventory.CraftInventoryProxy;
import net.momirealms.craftengine.proxy.minecraft.core.BlockPosProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.SupportTypeProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.BlockBehaviourProxy;
import org.bukkit.entity.Player;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.inventory.ItemStack;

/** Only immutable kitchen definition parameters live in this shared behavior. */
final class KitchenBlockBehavior extends AbstractCanSurviveBlockBehavior implements EntityBlock, WorldlyContainerHolder {
    static final BlockBehaviorFactory<KitchenBlockBehavior> FACTORY = (block, section) ->
            new KitchenBlockBehavior(block, section.getNonEmptyString("recipe-type"),
                    section.getDouble("speed", 1), section.getDouble("failure-probability", 0));
    private final String type;
    private final double speed;
    private final double failure;
    private final Property<Direction> facing;
    private int controllerId;

    private KitchenBlockBehavior(BlockDefinition block, String type, double speed, double failure) {
        super(block, 0);
        if (!Double.isFinite(speed) || speed <= 0 || failure < 0 || failure > 1) throw new IllegalArgumentException("Invalid kitchen speed/failure");
        this.type = type;
        this.speed = speed;
        this.failure = failure;
        facing = BlockBehaviorFactory.getProperty("facing", block, "facing", Direction.class);
    }

    @Override public BlockEntityController createBlockEntityController(BlockEntity entity) {
        return new KitchenController(entity, blockDefinition.id().asString(), type, speed, failure);
    }
    @Override public void initControllerId(int id) { controllerId = id; }
    @Override public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        return state.with(facing, context.getHorizontalDirection());
    }
    @Override public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) { return use(context); }
    @Override public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) { return use(context); }

    private InteractionResult use(UseOnContext context) {
        if (!KitchenModule.enabled() || context.getPlayer() == null) return InteractionResult.PASS;
        Player player = (Player) context.getPlayer().platformPlayer();
        for (var stack : new ItemStack[]{player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand()}) {
            var id = CraftEngineItems.getCustomItemId(stack);
            if (id != null && id.asString().equals(Ids.MOD_ID + ":fast_book_item")) return InteractionResult.PASS;
        }
        BlockEntity entity = context.getLevel().storageWorld().getBlockEntityAtIfLoaded(context.getClickedPos());
        if (entity == null) return InteractionResult.FAIL;
        KitchenController controller = entity.controller.getAt(KitchenController.class, controllerId);
        if (controller == null) return InteractionResult.FAIL;
        KitchenModule.open(player, controller);
        return InteractionResult.SUCCESS_AND_CANCEL;
    }

    @Override
    protected boolean canSurvive(Object block, Object state, Object level, Object position) {
        var pos = LocationUtils.fromBlockPos(position);
        Object belowPos = BlockPosProxy.INSTANCE.newInstance(pos.x(), pos.y() - 1, pos.z());
        Object below = BlockGetterProxy.INSTANCE.getBlockState(level, belowPos);
        // Current source explicitly permits these four groups even without a full upper face.
        if (BlockStateUtils.getBlockOwnerIdFromState(below).equals(Key.of("minecraft:hopper"))
                || BlockStateUtils.isTag(below, Key.of("minecraft:fences"))
                || BlockStateUtils.isTag(below, Key.of("minecraft:walls"))
                || BlockStateUtils.isTag(below, Key.of("minecraft:leaves"))) return true;
        return BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.isFaceSturdy(below, level, belowPos,
                DirectionUtils.toNMSDirection(Direction.UP), SupportTypeProxy.FULL);
    }

    @Override public Object getContainer(Object block, Object[] args) {
        var world = BukkitAdaptor.adapt(LevelProxy.INSTANCE.getWorld(args[1])).storageWorld();
        BlockEntity entity = world.getBlockEntityAtIfLoaded(LocationUtils.fromBlockPos(args[2]));
        if (entity == null) return null;
        KitchenController controller = entity.controller.getAt(KitchenController.class, controllerId);
        return controller == null || !controller.available() ? null
                : CraftInventoryProxy.INSTANCE.getInventory(controller.storageInventory());
    }
}
