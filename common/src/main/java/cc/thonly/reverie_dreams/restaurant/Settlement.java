package cc.thonly.reverie_dreams.restaurant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Outcome of one accepted restaurant order; can be paid immediately or stored until the owner claims it. */
public record Settlement(Optional<UUID> beneficiary, Outcome outcome, int coins, int experience, Optional<String> feedback) {
    public static final Codec<Settlement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).optionalFieldOf("beneficiary").forGetter(Settlement::beneficiary),
            Outcome.CODEC.optionalFieldOf("outcome", Outcome.SUCCESS).forGetter(Settlement::outcome),
            Codec.INT.optionalFieldOf("coins", 0).forGetter(Settlement::coins),
            Codec.INT.optionalFieldOf("experience", 0).forGetter(Settlement::experience),
            Codec.STRING.optionalFieldOf("feedback").forGetter(Settlement::feedback)
    ).apply(instance, Settlement::new));

    public Settlement {
        coins = Math.max(0, coins);
        experience = Math.max(0, experience);
        feedback = feedback == null ? Optional.empty() : feedback;
    }

    public boolean belongsTo(UUID player) {
        return beneficiary.isEmpty() || beneficiary.filter(player::equals).isPresent();
    }

    public enum Outcome implements StringRepresentable {
        SUCCESS,
        TOO_EXPENSIVE,
        ANGRY;

        public static final Codec<Outcome> CODEC = StringRepresentable.fromEnum(Outcome::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
