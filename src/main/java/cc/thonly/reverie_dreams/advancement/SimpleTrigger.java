package cc.thonly.reverie_dreams.advancement;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class SimpleTrigger extends SimpleCriterionTrigger<SimpleTrigger.Condition> {
    public static final ResourceLocation ID = ReverieDreams.id("simple_trigger");

    public static Criterion<Condition> of(ResourceLocation powerHandCrank) {
        return RDCriteriaTriggers.SIMPLE_TRIGGER.createCriterion(new Condition(powerHandCrank));
    }

    public static void trigger(ServerPlayer player, ResourceLocation identifier) {
        RDCriteriaTriggers.SIMPLE_TRIGGER.trigger(player, (condition) -> {
            return condition.location.equals(identifier);
        });
    }

    public Codec<SimpleTrigger.Condition> codec() {
        return SimpleTrigger.Condition.CODEC;
    }

    public record Condition(ResourceLocation location) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<SimpleTrigger.Condition> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ResourceLocation.CODEC.fieldOf("trigger").forGetter(SimpleTrigger.Condition::identifier)).apply(instance, SimpleTrigger.Condition::new);
        });

        public Condition(ResourceLocation location) {
            this.location = location;
        }

        public Optional<ContextAwarePredicate> player() {
            return Optional.empty();
        }

        public ResourceLocation identifier() {
            return this.location;
        }
    }
}
