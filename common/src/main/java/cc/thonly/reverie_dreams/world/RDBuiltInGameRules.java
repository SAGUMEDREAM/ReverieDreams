package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

public class RDBuiltInGameRules {
    public static final Holder<GameRule<Boolean>> DO_GHOST = MCBuiltInRegistries.GAME_RULE.register("do_ghost", () -> new GameRule<>(
            GameRuleCategory.SPAWNING,
            GameRuleType.BOOL,
            BoolArgumentType.bool(),
            GameRuleTypeVisitor::visitBoolean,
            Codec.BOOL,
            value -> value ? 1 : 0,
            true,
            FeatureFlagSet.of())
    );

    public static void initialize() {
    }
}
