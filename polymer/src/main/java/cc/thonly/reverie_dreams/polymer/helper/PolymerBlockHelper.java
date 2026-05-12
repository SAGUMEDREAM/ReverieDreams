package cc.thonly.reverie_dreams.polymer.helper;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.props.*;
import cc.thonly.reverie_dreams.fabric.ReverieDreamsFabric;
import cc.thonly.reverie_dreams.polymer.block.*;
import cc.thonly.reverie_dreams.block.CashBoxBlock;
import cc.thonly.reverie_dreams.block.FoodDisplayBlock;
import cc.thonly.reverie_dreams.block.GensokyoAltarBlock;
import cc.thonly.reverie_dreams.block.MarisaHatBlock;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.block.base.ModelBlock;
import cc.thonly.reverie_dreams.block.kitchen.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.util.PlatformContext;
import eu.pb4.factorytools.api.block.model.SignModel;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
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

@SuppressWarnings("deprecation")
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
            case RailControllerBlock ignored -> RailPolymerBlock.INSTANCE;
            case SignalRailBlock ignored -> RailPolymerBlock.INSTANCE;
            case SignalDelayerBlock ignored -> BaseFactoryBlock.BARRIER;
            case RemoteClientBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case RemoteServerBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case SpeakerBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case FoodDisplayBlock ignored -> new ItemStackDisplayImpl();
            case AbstractCropBlock ignored -> new CropHolderImpl(ignored);
            case FruitLeavesBlock ignored -> new FruitLeavesImpl(ignored);
            case ModelBlock ignored -> new ModelFactoryImpl(ignored);
            case MarisaHatBlock ignored -> new FumoImpl(ignored);
            case BaseFumoBlock ignored -> new FumoImpl(ignored);
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
            case HorizontalDirectionalBlock ignored -> BaseFactoryBlock.BARRIER;
            case CarpetBlock ignored -> StateCopyFactoryBlock.CARPET;
            case ChainBlock ignored -> StateCopyFactoryBlock.CHAIN;
            case RotatedPillarBlock ignored -> BaseFactoryBlock.BARRIER;
            case WaterloggedTransparentBlock ignored -> BaseFactoryBlock.BARRIER;
            default -> {
                if (defaultState.isCollisionShapeFullBlock(PolymerCommonUtils.getFakeWorld(), BlockPos.ZERO)) {
                    yield StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
                } else {
                    yield BaseFactoryBlock.BARRIER;
                }
            }
        };
    }
}
