package cc.thonly.polymer;

import cc.thonly.mystias_izakaya.block.kitchenware.AbstractKitchenwareBlock;
import cc.thonly.polymer.block.*;
import cc.thonly.reverie_dreams.LateLoaderInit;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.BaseFumoBlock;
import cc.thonly.reverie_dreams.block.GensokyoAltarBlock;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import eu.pb4.factorytools.api.block.model.SignModel;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class PolymerBlockHelper {
    public static void registerOverlay(Block block) {
        if (!ReverieDreamsConfiguration.POLYMER_PATCH) {
            return;
        }
        Identifier id = Registries.BLOCK.getId(block);
        PolymerBlock polymerBlock = requestBlockOverlay(block);
        PolymerBlock.registerOverlay(block, polymerBlock);
        if (polymerBlock instanceof BlockWithElementHolder blockWithElementHolder) {
            BlockWithElementHolder.registerOverlay(block, blockWithElementHolder);
        }

        if (block instanceof AbstractSignBlock) {
            LateLoaderInit.LATE_INIT.add(() -> SignModel.setModel(block, Touhou.id("block_sign/" + id.getPath())));
        }
    }

    public static PolymerBlock requestBlockOverlay(Block block) {
        Identifier id = Registries.BLOCK.getId(block);
        BlockState defaultState = block.getDefaultState();

        LateLoaderInit.LATE_INIT.add(() -> BlockStateModelManager.addBlock(id, block));

        return switch (block) {
            case AbstractCropBlock ignored -> new CropBlockImpl(ignored);
            case FruitLeavesBlock ignored -> new FruitLeavesImpl(ignored);
            case BaseFumoBlock ignored -> new FumoImpl(ignored);
            case GensokyoAltarBlock ignored -> new GensokyoAltarImpl();
            case AbstractKitchenwareBlock ignored -> new KitchenwareImpl(ignored);
            case RedstoneLampBlock ignored -> StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
            case StairsBlock ignored -> StateCopyFactoryBlock.STAIR;
            case SlabBlock ignored -> SlabFactoryBlock.INSTANCE;
            case FenceGateBlock ignored -> StateCopyFactoryBlock.FENCE_GATE;
            case FenceBlock ignored -> StateCopyFactoryBlock.FENCE;
            case WallBlock ignored -> StateCopyFactoryBlock.WALL;
            case LeavesBlock ignored -> RealSingleStatePolymerBlock.of(block, BlockModelType.TRANSPARENT_BLOCK);
            case SignBlock ignored -> StateCopyFactoryBlock.SIGN;
            case WallSignBlock ignored -> StateCopyFactoryBlock.WALL_SIGN;
            case HangingSignBlock ignored -> StateCopyFactoryBlock.HANGING_SIGN;
            case WallHangingSignBlock ignored -> StateCopyFactoryBlock.HANGING_WALL_SIGN;
            case DoorBlock ignored -> DoorPolymerBlock.INSTANCE;
            case TrapdoorBlock ignored -> TrapdoorPolymerBlock.INSTANCE;
            case ButtonBlock ignored -> StateCopyFactoryBlock.BUTTON;
            case PressurePlateBlock ignored -> StateCopyFactoryBlock.PRESSURE_PLATE;
            case PlantBlock ignored -> BaseFactoryBlock.SAPLING;
            case FlowerPotBlock ignored -> new PottedPlantPolymerBlock(id);
            case PaneBlock ignored -> StateCopyFactoryBlock.PANE;
            case LanternBlock ignored -> StateCopyFactoryBlock.LANTERN;
            case HorizontalFacingBlock ignored -> BaseFactoryBlock.BARRIER;
            case CarpetBlock ignored -> StateCopyFactoryBlock.CARPET;
            case ChainBlock ignored -> StateCopyFactoryBlock.CHAIN;
            case PillarBlock ignored -> BaseFactoryBlock.BARRIER;
            case GrateBlock ignored -> BaseFactoryBlock.BARRIER;
            default -> {
                if (defaultState.isFullCube(PolymerCommonUtils.getFakeWorld(), BlockPos.ORIGIN)) {
                    yield StatePolymerBlock.of(block, BlockModelType.FULL_BLOCK);
                } else {
                    yield BaseFactoryBlock.BARRIER;
                }
            }
        };
    }
}
