package cc.thonly.reverie_dreams.api.client;

import cc.thonly.reverie_dreams.registry.delegate.RegistryDelegate;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;

public class BlockEntityRendererRegistry {
    public static final List<Entry<?,?>> ENTRIES = new ArrayList<>();

    public static <E extends BlockEntity, S extends BlockEntityRenderState> void register(Holder<BlockEntityType<E>> type, BlockEntityRendererProvider<E, S> provider) {
        ENTRIES.add(new Entry<>(type, provider));
    }

    public static <E extends BlockEntity, S extends BlockEntityRenderState> void register(RegistryDelegate<BlockEntityType<E>> type, BlockEntityRendererProvider<E, S> provider) {
        ENTRIES.add(new Entry<>(type, provider));
    }

    public record Entry<E extends BlockEntity, S>(Holder<BlockEntityType<E>> type,
                                                  BlockEntityRendererProvider<? super E, ? super S> provider) {

    }
}
