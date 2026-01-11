package cc.thonly.reverie_dreams.advancement;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class SimpleTrigger extends SimpleCriterionTrigger<SimpleTrigger.Condition> {
    public static final Identifier ID = ReverieDreams.id("simple_trigger");

    public static Criterion<Condition> of(Identifier powerHandCrank) {
        return RDCriteriaTriggers.SIMPLE_TRIGGER.createCriterion(new Condition(powerHandCrank));
    }

    public static void trigger(ServerPlayer player, Identifier identifier) {
        RDCriteriaTriggers.SIMPLE_TRIGGER.trigger(player, (condition) -> {
            return condition.location.equals(identifier);
        });
    }

    public Codec<SimpleTrigger.Condition> codec() {
        return SimpleTrigger.Condition.CODEC;
    }

    public record Condition(Identifier location) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<SimpleTrigger.Condition> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Identifier.CODEC.fieldOf("trigger").forGetter(SimpleTrigger.Condition::identifier)).apply(instance, SimpleTrigger.Condition::new);
        });

        public Condition(Identifier location) {
            this.location = location;
        }

        public Optional<ContextAwarePredicate> player() {
            return Optional.empty();
        }

        public Identifier identifier() {
            return this.location;
        }
    }
}
