package cc.thonly.reverie_dreams.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.ToString;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Random;

public record CustomerBudget(int min, int max)  {
    private static final Random rand = new Random();
    public static final Codec<CustomerBudget> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("min").forGetter(CustomerBudget::min),
                    Codec.INT.fieldOf("max").forGetter(CustomerBudget::max)
            ).apply(instance, CustomerBudget::new)
    );

    public static final StreamCodec<ByteBuf, CustomerBudget> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CustomerBudget::min,
            ByteBufCodecs.INT, CustomerBudget::max,
            CustomerBudget::new
    );

    public CustomerBudget {
        if (this.min() > this.max()) {
            throw new IllegalArgumentException("The value of %s cannot be greater than the value of %s.".formatted(min(), max()));
        }
    }

//    public int nextBudget() {
//        return rand.nextInt(this.max - this.min + 1) + this.min;
//    }

    public static CustomerBudget of(int min, int max) {
        return new CustomerBudget(min, max);
    }

    public static final CustomerBudget MAX = new CustomerBudget(0, Integer.MAX_VALUE);
}
