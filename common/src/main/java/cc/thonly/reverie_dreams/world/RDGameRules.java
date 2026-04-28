package cc.thonly.reverie_dreams.world;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

public class RDGameRules {
    public static Holder<GameRule<Boolean>> DO_GHOST;

    @SuppressWarnings("unchecked")
    public static void initialize(BalmRegistrar.Scoped<GameRule<?>> scoped) {
        DO_GHOST = (Holder<GameRule<Boolean>>) (Object) scoped.register("do_ghost", key -> new GameRule<>(
                GameRuleCategory.SPAWNING,
                GameRuleType.BOOL,
                BoolArgumentType.bool(),
                GameRuleTypeVisitor::visitBoolean,
                Codec.BOOL,
                value -> value ? 1 : 0,
                true,
                FeatureFlagSet.of())
        );
    }
}
