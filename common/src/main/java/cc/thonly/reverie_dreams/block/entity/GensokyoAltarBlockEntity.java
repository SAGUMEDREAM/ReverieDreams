package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.common.RDMPHooks;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

@Getter
public class GensokyoAltarBlockEntity extends BlockEntity {
    private SimpleContainer inventory = new SimpleContainer(9);
    public int tick = 0;

    public GensokyoAltarBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.GENSOKYO_ALTAR.value(), pos, state);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, GensokyoAltarBlockEntity blockEntity) {
        RDMPHooks.GensokyoAltarBlockEntityTicker.EVENT.invoker().handle(world, pos, state, blockEntity);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, this.inventory.getItems());
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        SimpleContainer inventory = new SimpleContainer(9);
        ContainerHelper.loadAllItems(view, inventory.getItems());
        this.inventory = inventory;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(this::toString, LogUtils.getLogger())) {
            TagValueOutput tagValue = TagValueOutput.createWithContext(logging, registries);
            this.saveAdditional(tagValue);
            return tagValue.buildResult();
        } catch (Exception any) {
            return super.getUpdateTag(registries);
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
