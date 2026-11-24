package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.redstone.RemoteClientBlock;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.server.RemoteSignalManager;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

import cc.thonly.reverie_dreams.block.redstone.RemoteServerBlock;

public class RemoteBlockEntity extends BlockEntity {
    @NotNull
    private RemoteType remoteType;
    @NotNull
    private String signalName = "";
    @NotNull
    private String signalToken = "";
    @NotNull
    private String uid = UUID.randomUUID().toString();
    private int tK = 0;
    private boolean first = true;

    public RemoteBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(blockState.getBlock() instanceof RemoteServerBlock ? RemoteType.SERVER : RemoteType.CLIENT, blockPos, blockState);
    }

    public RemoteBlockEntity(@NotNull RemoteType remoteType, BlockPos blockPos, BlockState blockState) {
        super(RDBlockEntityTypes.REMOTE_BLOCK_ENTITY.value(), blockPos, blockState);
        this.remoteType = remoteType;

    }

    public boolean isEmpty() {
        return this.signalName.isEmpty() || this.signalToken.isEmpty();
    }

    public boolean test(@NotNull RemoteBlockEntity other) {
        return Objects.equals(other.signalName, this.signalName)
                && Objects.equals(other.signalToken, this.signalToken);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (this.remoteType == RemoteType.CLIENT) {
            return;
        }
        if (this.first) {
            this.first = false;
            boolean hasSignal = level.hasNeighborSignal(pos);
            if (state.getValue(RemoteServerBlock.OCCUPIED) != hasSignal)
                level.setBlock(pos, state.setValue(RemoteServerBlock.OCCUPIED, hasSignal), 3);
            RemoteSignalManager.access().setValue(this, hasSignal);
        }
    }

    public void clientTick(Level level, BlockPos pos, BlockState state) {
        if (this.remoteType == RemoteType.SERVER) {
            return;
        }
        if (this.first) {
            this.first = false;
            RemoteSignalManager.access().watch(this);
        }
    }

    public void receive(boolean powered) {
        if (level == null || level.isClientSide() || isRemoved())
            return;
        BlockState state = getBlockState();
        if (state.getBlock() instanceof RemoteClientBlock && state.getValue(RemoteClientBlock.POWERED) != powered)
            level.setBlock(worldPosition, state.setValue(RemoteClientBlock.POWERED, powered), 3);
    }

    public void configure(String name, String token) {
        RemoteSignalManager.access().remove(this);
        this.signalName = name.strip();
        this.signalToken = token.strip();
        this.first = true;
        setChanged();
        if (level != null && !level.isClientSide()) {
            if (remoteType == RemoteType.SERVER)
                serverTick(level, worldPosition, getBlockState());
            else
                clientTick(level, worldPosition, getBlockState());
        }
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        this.first = true;
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.store("RemoteType", RemoteType.CODEC, this.remoteType);
        view.putString("SignalName", this.signalName);
        view.putString("SignalToken", this.signalToken);
        view.putString("UID", this.uid);
        view.putInt("Ticker", this.tK);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.remoteType = getBlockState().getBlock() instanceof RemoteServerBlock ? RemoteType.SERVER : RemoteType.CLIENT;
        this.signalName = view.getStringOr("SignalName", "");
        this.signalToken = view.getStringOr("SignalToken", "");
        this.uid = view.getStringOr("UID", "");
        if (this.uid.isEmpty()) {
            this.uid = UUID.randomUUID().toString();
        }
        this.tK = view.getIntOr("Ticker", 0);
        this.first = true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide())
            RemoteSignalManager.access().remove(this);
    }

    public @NotNull String getUid() {
        return this.uid;
    }

    public void setSignalName(@NotNull String signalName) {
        configure(signalName, signalToken);
    }

    public @NotNull String getSignalName() {
        return this.signalName;
    }

    public void setSignalToken(@NotNull String signalToken) {
        configure(signalName, signalToken);
    }

    public @NotNull String getSignalToken() {
        return this.signalToken;
    }

    public void setRemoteType(@NotNull RemoteType remoteType) {
        this.remoteType = remoteType;
    }

    public @NotNull RemoteType getRemoteType() {
        return this.remoteType;
    }

    public enum RemoteType implements StringRepresentable {
        CLIENT("client"),
        SERVER("server");

        public static final Codec<RemoteType> CODEC = StringRepresentable.fromEnum(RemoteType::values);
        private final String name;

        RemoteType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}
