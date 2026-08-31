package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class SpeakerBlockEntity extends BlockEntity {
    private List<String> texts = new ArrayList<>();
    private boolean bellSound = false;

    public SpeakerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(RDBlockEntityTypes.SPEAKER_BLOCK_ENTITY.value(), blockPos, blockState);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        view.read("Texts", Codec.list(Codec.STRING)).ifPresent(value -> this.texts = new ArrayList<>(value));
        this.bellSound = view.getBooleanOr("BellSound", false);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.storeNullable("Texts", Codec.list(Codec.STRING), this.texts);
        view.putBoolean("BellSound", this.bellSound);
    }
}
