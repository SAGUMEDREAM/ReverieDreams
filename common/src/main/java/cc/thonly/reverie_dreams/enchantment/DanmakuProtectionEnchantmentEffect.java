package cc.thonly.reverie_dreams.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record DanmakuProtectionEnchantmentEffect(LevelBasedValue level) implements EnchantmentEntityEffect {
    public static final MapCodec<DanmakuProtectionEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance->instance.group(
            LevelBasedValue.CODEC.fieldOf("level").forGetter(DanmakuProtectionEnchantmentEffect::level)
    ).apply(instance, DanmakuProtectionEnchantmentEffect::new));

    @Override
    public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity target, Vec3 vec3) {
        if (target instanceof LivingEntity living) {

        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
