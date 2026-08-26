package cc.thonly.reverie_dreams.entity.skill;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.BuiltinObject;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class Skill<T extends LivingEntity> implements BuiltinObject {
    public static final Skill<?> DEFAULT = new Skill<>() {
        @Override
        public void onStarted(SkillContainer<LivingEntity> container) {

        }

        @Override
        public void onTick(SkillContainer<LivingEntity> container) {

        }

        @Override
        public void onEnd(SkillContainer<LivingEntity> container) {

        }

        @Override
        public Identifier id() {
            return null;
        }
    };
    public static final Codec<Skill<?>> CODEC = Codec.lazyInitialized(() -> (Codec) Identifier.CODEC.xmap((Function<Identifier, Skill>) identifier -> {
        Skill<?> skill = BuiltInRegistryProviders.SKILL.getValue(identifier);
        if (skill == null) {
            return DEFAULT;
        }
        return skill;
    }, skill -> {
        Identifier key = BuiltInRegistryProviders.SKILL.getKey(skill);
        if (key == null) {
            return ReverieDreams.id("undefined");
        }
        return key;
    }));

    public abstract void onStarted(SkillContainer<T> container);

    public abstract void onTick(SkillContainer<T> container);

    public abstract void onEnd(SkillContainer<T> container);

    public T asEntity(LivingEntity livingEntity) {
        return (T) livingEntity;
    }

    public abstract Identifier id();

}
