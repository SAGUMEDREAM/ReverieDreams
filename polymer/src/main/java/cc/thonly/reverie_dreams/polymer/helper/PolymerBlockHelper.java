package cc.thonly.reverie_dreams.polymer.helper;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.*;
import cc.thonly.reverie_dreams.block.kitchen.*;
import cc.thonly.reverie_dreams.block.props.*;
import cc.thonly.reverie_dreams.block.decor.ZabutonBlock;
import cc.thonly.reverie_dreams.block.decor.TatamiBlock;
import cc.thonly.reverie_dreams.block.decor.ChabudaiBlock;
import cc.thonly.reverie_dreams.block.decor.RecordPlayerBlock;
import cc.thonly.reverie_dreams.block.decor.TeaSetBlock;
import cc.thonly.reverie_dreams.block.decor.RakedGravelBlock;
import cc.thonly.reverie_dreams.block.redstone.*;
import cc.thonly.reverie_dreams.fabric.ReverieDreamsFabric;
import cc.thonly.reverie_dreams.polymer.block.*;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.block.base.ModelBlock;
import cc.thonly.reverie_dreams.block.cooking.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.util.PlatformContext;
import eu.pb4.factorytools.api.block.model.SignModel;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
import eu.pb4.factorytools.api.block.model.generic.ShiftyBlockStateModel;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import cc.thonly.reverie_dreams.block.decor.BoardGameBlock;
import cc.thonly.reverie_dreams.block.decor.living.FoldedFutonBlock;
import cc.thonly.reverie_dreams.block.decor.living.FutonBlock;
import cc.thonly.reverie_dreams.block.decor.living.HangingOrnamentBlock;
import cc.thonly.reverie_dreams.block.decor.living.LivingStorageBlock;
import cc.thonly.reverie_dreams.block.decor.living.TimberWhiteWallBlock;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopBridgeBlock;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopDecorBlock;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopStationBlock;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopStorageBlock;
import cc.thonly.reverie_dreams.block.restaurant.RestaurantBlock;
import cc.thonly.reverie_dreams.polymer.block.BoardGameImpl;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;

@Slf4j
public class PolymerBlockHelper {
    public static void registerOverlay(Block block) {
        if (PlatformContext.IS_DATAGEN_MODE) {
            return;
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        PolymerBlock polymerBlock = requestBlockOverlay(block);
        PolymerBlock.registerOverlay(block, polymerBlock);
        if (polymerBlock instanceof BlockWithElementHolder blockWithElementHolder) {
            BlockWithElementHolder.registerOverlay(block, blockWithElementHolder);
        }

        if (block instanceof SignBlock) {
            ReverieDreamsFabric.FABRIC_LATE_INIT.add(() -> SignModel.setModel(block, ReverieDreams.id("block_sign/" + id.getPath())));
        }
    }

    public static PolymerBlock requestBlockOverlay(Block block) {
        if (block instanceof PaddyFieldBlock) {
            return new RealSingleStatePolymerBlock(Blocks.FARMLAND.defaultBlockState()
                    .setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE));
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        BlockState defaultState = block.defaultBlockState();

        ReverieDreamsFabric.FABRIC_LATE_INIT.add(() -> {
            try {
                BlockStateModelManager.addBlock(id, block);
            } catch (Exception err) {
                log.error("Can't add block state model {}", id, err);
            }
        });

        return switch (block) {
            case CuisineBlock ignored -> BaseFactoryBlock.SAPLING;
            case RestaurantBlock restaurant -> new RestaurantImpl(restaurant);
            case RailControllerBlock ignored -> RailPolymerBlock.INSTANCE;
            case SignalRailBlock ignored -> RailPolymerBlock.INSTANCE;
            case SignalDelayerBlock ignored -> BaseFactoryBlock.BARRIER;
            case ChairBlock ignored -> BaseFactoryBlock.BARRIER;
            case RecordPlayerBlock ignored -> BaseFactoryBlock.LOW_TABLE;
            case ChabudaiBlock ignored -> new BoardGameImpl(true);
            case BoardGameBlock ignored -> new BoardGameImpl(false);
            case TeaSetBlock ignored -> BaseFactoryBlock.SAPLING;
            case HangingOrnamentBlock ignored -> BaseFactoryBlock.SAPLING;
            case FutonBlock ignored -> new BaseFactoryBlock(
                    PolymerBlockResourceUtils.requestEmpty(BlockModelType.TRAPDOOR_BOTTOM),
                    false, ShiftyBlockStateModel::longRange);
            case FoldedFutonBlock ignored -> BaseFactoryBlock.LOW_TABLE;
            case WorkshopStorageBlock furniture -> new WorkshopImpl(switch (furniture.kind()) {
                case FUMIZUKUE, BOOK_BOX, INGREDIENT_BASKET -> BaseFactoryBlock.LOW_TABLE;
                default -> BaseFactoryBlock.BARRIER;
            });
            case WorkshopStationBlock furniture -> new WorkshopImpl(furniture.kind().collision()?BaseFactoryBlock.BARRIER:BaseFactoryBlock.SAPLING);
            case WorkshopBridgeBlock bridge -> bridge.kind()
                    == WorkshopKind.TAIKO_BRIDGE_APPROACH
                    ? BaseFactoryBlock.LOW_TABLE : BaseFactoryBlock.BARRIER;
            case WorkshopDecorBlock furniture -> !furniture.kind().collision()
                    ? BaseFactoryBlock.SAPLING : furniture.kind()==WorkshopKind.SHOP_BENCH
                    ? BaseFactoryBlock.LOW_TABLE : BaseFactoryBlock.BARRIER;
            case LivingStorageBlock ignored -> BaseFactoryBlock.BARRIER;
            case TimberWhiteWallBlock ignored -> BaseFactoryBlock.BARRIER;
            case TatamiBlock ignored -> defaultState.isCollisionShapeFullBlock(PolymerCommonUtils.getFakeLevel(), BlockPos.ZERO)
                    ? BaseFactoryBlock.BARRIER : new StateCopyFactoryBlock(Blocks.YELLOW_CARPET, ShiftyBlockStateModel::longRange);
            case RakedGravelBlock ignored -> {
                var mapped = StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
                yield mapped != null ? mapped : BaseFactoryBlock.BARRIER;
            }
            case TableBlock ignored -> BaseFactoryBlock.BARRIER;
            case BrewingBarrelBlock ignored -> BaseFactoryBlock.BARRIER;
            case CupboardBlock ignored -> BaseFactoryBlock.BARRIER;
            case IceMakingMachine ignored -> BaseFactoryBlock.BARRIER;
            case RemoteClientBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case RemoteServerBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case SpeakerBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case PlateBlock ignored -> new PlateImpl();
            case AbstractCropBlock ignored -> new CropHolderImpl(ignored);
            case FruitLeavesBlock ignored -> new FruitLeavesImpl(ignored);
            case ModelBlock ignored -> new ModelFactoryImpl(ignored);
            case BaseFumoBlock ignored -> FumoImpl.INSTANCE;
            case GensokyoAltarBlock ignored -> new GensokyoAltarImpl();
            case CashBoxBlock ignored -> new HorizontalFacingImpl(ignored);
            case AbstractKitchenwareBlock ignored -> new AbstractKitchenwareImpl(ignored);
            case RedstoneLampBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case StairBlock ignored -> StateCopyFactoryBlock.STAIR;
            case SlabBlock ignored -> SlabFactoryBlock.INSTANCE;
            case FenceGateBlock ignored -> StateCopyFactoryBlock.FENCE_GATE;
            case FenceBlock ignored -> StateCopyFactoryBlock.FENCE;
            case WallBlock ignored -> StateCopyFactoryBlock.WALL;
            case LeavesBlock ignored -> RealSingleStatePolymerBlock.of(block, BlockModelType.LEAVES);
            case WallSignBlock ignored -> StateCopyFactoryBlock.WALL_SIGN;
            case CeilingHangingSignBlock ignored -> StateCopyFactoryBlock.HANGING_SIGN;
            case WallHangingSignBlock ignored -> StateCopyFactoryBlock.HANGING_WALL_SIGN;
            case SignBlock ignored -> StateCopyFactoryBlock.SIGN;
            case DoorBlock ignored -> DoorPolymerBlock.INSTANCE;
            case TrapDoorBlock ignored -> TrapdoorPolymerBlock.INSTANCE;
            case ButtonBlock ignored -> StateCopyFactoryBlock.BUTTON;
            case PressurePlateBlock ignored -> StateCopyFactoryBlock.PRESSURE_PLATE;
            case VegetationBlock ignored -> BaseFactoryBlock.SAPLING;
            case FlowerPotBlock ignored -> new PottedPlantPolymerBlock(id);
            case IronBarsBlock ignored -> StateCopyFactoryBlock.PANE;
            case LanternBlock ignored -> StateCopyFactoryBlock.LANTERN;
            case ZabutonBlock cushion -> new StateCopyFactoryBlock(
                    BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(cushion.color().getName() + "_carpet")),
                    ShiftyBlockStateModel::longRange);
            case HorizontalDirectionalBlock ignored -> BaseFactoryBlock.BARRIER;
            case CarpetBlock ignored -> StateCopyFactoryBlock.CARPET;
            case ChainBlock ignored -> StateCopyFactoryBlock.CHAIN;
            case RotatedPillarBlock ignored -> BaseFactoryBlock.BARRIER;
            case WaterloggedTransparentBlock ignored -> BaseFactoryBlock.BARRIER;
            default -> {
                if (defaultState.isCollisionShapeFullBlock(PolymerCommonUtils.getFakeLevel(), BlockPos.ZERO)) {
                    yield StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
                } else {
                    yield BaseFactoryBlock.BARRIER;
                }
            }
        };
    }
}
