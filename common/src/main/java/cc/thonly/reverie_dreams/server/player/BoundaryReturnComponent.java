package cc.thonly.reverie_dreams.server.player;

import cc.thonly.reverie_dreams.api.player.PlayerComponent;
import cc.thonly.reverie_dreams.api.player.PlayerComponentInitializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/**
 * Remembers where a player stepped through a boundary, so that the next
 * {@code boundary_ofuda} used on the other side can hand them back to the
 * same place instead of a random spot.
 */
@Getter
@Setter
@ToString
public class BoundaryReturnComponent implements PlayerComponent<BoundaryReturnComponent> {
    public static final Codec<BoundaryReturnComponent> CODEC = RecordCodecBuilder.create(x -> x.group(
            Codec.STRING.optionalFieldOf("ReturnDimension", "").forGetter(BoundaryReturnComponent::getReturnDimension),
            BlockPos.CODEC.optionalFieldOf("ReturnPos", BlockPos.ZERO).forGetter(BoundaryReturnComponent::getReturnPos)
    ).apply(x, BoundaryReturnComponent::new));

    private String returnDimension = "";
    private BlockPos returnPos = BlockPos.ZERO;
    private Player player;

    public BoundaryReturnComponent(String returnDimension, BlockPos returnPos) {
        this.returnDimension = returnDimension;
        this.returnPos = returnPos;
    }

    public boolean hasReturn() {
        return !this.returnDimension.isEmpty();
    }

    public void clearReturn() {
        this.returnDimension = "";
        this.returnPos = BlockPos.ZERO;
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public Codec<BoundaryReturnComponent> getCodec() {
        return CODEC;
    }

    public static class BoundaryReturnComponentInitializer implements PlayerComponentInitializer<BoundaryReturnComponent> {
        @Override
        public PlayerComponent<BoundaryReturnComponent> create() {
            return new BoundaryReturnComponent("", BlockPos.ZERO);
        }

        @Override
        public Codec<BoundaryReturnComponent> getCodec() {
            return CODEC;
        }
    }
}
