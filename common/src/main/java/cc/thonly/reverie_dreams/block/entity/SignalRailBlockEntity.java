package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.api.entity.CartSignal;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Objects;

import cc.thonly.reverie_dreams.block.redstone.RedstoneSettings;

@Setter
@Getter
public class SignalRailBlockEntity extends BlockEntity {
    private String signName = "";

    public SignalRailBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(RDBlockEntityTypes.SIGNAL_RAIL_BLOCK_ENTITY.value(), blockPos, blockState);
    }

    public boolean testSignName(AbstractMinecart abstractMinecart) {
        if (abstractMinecart instanceof CartSignal cartSignal) {
            return RedstoneSettings.matches(this.signName, cartSignal.reverie_dreams$getSignName());
        }
        return false;
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        if (this.signName != null && !this.signName.isEmpty()) {
            view.putString("SignName", this.signName);
        }
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.signName = view.getStringOr("SignName", "");
    }
}
