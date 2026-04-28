package cc.thonly.reverie_dreams.common;

import cc.thonly.reverie_dreams.block.entity.FoodDisplayBlockEntity;
import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public class MultiPlatformEvents {

    @FunctionalInterface
    public interface TenguCameraItemUseCallback {
        Event<TenguCameraItemUseCallback> EVENT = create(
                TenguCameraItemUseCallback.class,
                listeners -> (level, player, hand) -> {
                    for (TenguCameraItemUseCallback listener : listeners) {
                        InteractionResult handle = listener.handle(level, player, hand);
                        if (handle != InteractionResult.PASS) {
                            return handle;
                        }
                    }
                    return InteractionResult.PASS;
                }
        );

        InteractionResult handle(Level level, Player player, InteractionHand hand);
    }

    @FunctionalInterface
    public interface FoodDisplayBlockEntityTicker {
        Event<FoodDisplayBlockEntityTicker> EVENT = create(
                FoodDisplayBlockEntityTicker.class,
                listeners -> (world, pos, state, blockEntity) -> {
                    for (FoodDisplayBlockEntityTicker listener : listeners) {
                        listener.handle(world, pos, state, blockEntity);
                    }
                }
        );

        void handle(Level world, BlockPos pos, BlockState state, FoodDisplayBlockEntity blockEntity);
    }

    @FunctionalInterface
    public interface FoodDisplayBlockEntityUpdater {
        Event<FoodDisplayBlockEntityUpdater> EVENT = create(
                FoodDisplayBlockEntityUpdater.class,
                listeners -> blockEntity -> {
                    for (FoodDisplayBlockEntityUpdater listener : listeners) {
                        listener.handle(blockEntity);
                    }
                }
        );

        void handle(FoodDisplayBlockEntity blockEntity);
    }

    @FunctionalInterface
    public interface GensokyoAltarBlockEntityTicker {
        Event<GensokyoAltarBlockEntityTicker> EVENT = create(
                GensokyoAltarBlockEntityTicker.class,
                listeners -> (world, pos, state, blockEntity) -> {
                    for (GensokyoAltarBlockEntityTicker listener : listeners) {
                        listener.handle(world, pos, state, blockEntity);
                    }
                }
        );

        void handle(Level world, BlockPos pos, BlockState state, GensokyoAltarBlockEntity blockEntity);
    }

    public static void initialize() {

    }

    public static <T> Event<T> create(
            Class<T> tClass,
            Function<T[], T> invokerFactory
    ) {
        return EventFactory.createArrayBacked(tClass, invokerFactory);
    }
}