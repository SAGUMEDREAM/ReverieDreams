package cc.thonly.reverie_dreams.paper.production;

import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlock;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.block.property.Property;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import org.bukkit.entity.Player;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import java.util.Locale;

/** Definition-only behavior. No inventory, clock, or output is shared between placed blocks. */
final class ProductionBlockBehavior extends BukkitBlockBehavior implements EntityBlock {
    static final BlockBehaviorFactory<ProductionBlockBehavior> FACTORY = (block, section) ->
            new ProductionBlockBehavior(block, ProductionController.Kind.valueOf(
                    section.getNonEmptyString("machine").toUpperCase(Locale.ROOT)));

    private final ProductionController.Kind kind;
    private final Property<Direction> facing;
    private int controllerId;

    private ProductionBlockBehavior(BlockDefinition block, ProductionController.Kind kind) {
        super(block);
        this.kind = kind;
        this.facing = BlockBehaviorFactory.getProperty("facing", block, "facing", Direction.class);
    }

    @Override
    public BlockEntityController createBlockEntityController(BlockEntity entity) {
        return new ProductionController(entity, kind);
    }

    @Override
    public void initControllerId(int id) { controllerId = id; }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        // Ice keeps the original horizontal direction; barrels support all six faces.
        Direction direction = kind == ProductionController.Kind.ICE
                ? context.getHorizontalDirection() : context.getNearestLookingDirection().opposite();
        return state.with(facing, direction);
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        return interact(context);
    }

    @Override
    public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) {
        return interact(context);
    }

    private InteractionResult interact(UseOnContext context) {
        if (context.getPlayer() == null || !ProductionModule.enabled()) return InteractionResult.PASS;
        BlockEntity entity = context.getLevel().storageWorld().getBlockEntityAtIfLoaded(context.getClickedPos());
        if (entity == null) return InteractionResult.FAIL;
        ProductionController controller = entity.controller.getAt(ProductionController.class, controllerId);
        if (controller == null) return InteractionResult.FAIL;
        Player player = (Player) context.getPlayer().platformPlayer();
        ProductionModule.interact(player, context.getHand(), controller);
        return InteractionResult.SUCCESS_AND_CANCEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(Object block, Object[] args) {
        return kind == ProductionController.Kind.ICE;
    }

    @Override
    public int getAnalogOutputSignal(Object block, Object[] args) {
        if (kind != ProductionController.Kind.ICE) return 0;
        var world = BukkitAdaptor.adapt(LevelProxy.INSTANCE.getWorld(args[1])).storageWorld();
        BlockEntity entity = world.getBlockEntityAtIfLoaded(LocationUtils.fromBlockPos(args[2]));
        if (entity == null) return 0;
        ProductionController controller = entity.controller.getAt(ProductionController.class, controllerId);
        return controller == null ? 0 : controller.comparatorSignal();
    }

    @Override
    public void affectNeighborsAfterRemoval(Object block, Object[] args) {
        if (kind == ProductionController.Kind.ICE) {
            // Exact callback/proxy ordering used by CE 26.8.2 SimpleStorageBlockBehavior.
            LevelProxy.INSTANCE.updateNeighbourForOutputSignal(args[1], args[2], BlockStateUtils.getBlockOwner(args[0]));
        }
    }
}
