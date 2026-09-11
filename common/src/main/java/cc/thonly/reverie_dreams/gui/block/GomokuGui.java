package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.block.entity.GomokuBlockEntity;
import net.minecraft.core.Holder;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class GomokuGui {

    public static Holder<Dialog> create() {
        return Holder.direct(null);
    }

    public static class Instance {
        private final GomokuBlockEntity blockEntity;

        public Instance(GomokuBlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        public void init() {

        }

        public void onClick(LivingEntity entity, int x, int y) {

        }

        public void render() {

        }

        public void onClose() {

        }

        public BlockEntity getBlockEntity() {
            return this.blockEntity;
        }
    }
}
