package cc.thonly.reverie_dreams.server.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.network.ServerPlayerEntity;

@Getter
@Setter
public class FaithComponent implements PlayerComponent<FaithComponent> {
    private static final Codec<FaithComponent> CODEC = RecordCodecBuilder.create(x -> x.group(
            Codec.INT.optionalFieldOf("FaithValue", 0).forGetter(FaithComponent::getFaithValue),
            Codec.LONG.optionalFieldOf("DateOfLastPrayer", -1L).forGetter(FaithComponent::getDateOfLastPrayer)
    ).apply(x, FaithComponent::new));
    public static final int MAX_VALUE = 200;
    private ServerPlayerEntity player;
    private int faithValue = 0;
    private long dateOfLastPrayer = -1L;

    public FaithComponent(int faithValue, long dateOfLastPrayer) {
        this.faithValue = faithValue;
        this.dateOfLastPrayer = dateOfLastPrayer;
    }

    @Override
    public void onLoad() {

    }

    public void setFaithValue(int faithValue) {
        if (faithValue > MAX_VALUE) {
            return;
        }
        this.faithValue = faithValue;
    }

    @Override
    public void setPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public Codec<FaithComponent> getCodec() {
        return CODEC;
    }

    public static class FaithComponentInitializer implements PlayerComponentInitializer<FaithComponent> {

        @Override
        public PlayerComponent<FaithComponent> create(ServerPlayerEntity player) {
            return new FaithComponent(0, -1L);
        }

        @Override
        public Codec<FaithComponent> getCodec() {
            return CODEC;
        }
    }
}
