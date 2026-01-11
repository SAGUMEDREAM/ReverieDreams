package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public class GameRulesInit {
    public static final GameRule<Boolean> DO_GHOST = GameRuleBuilder.forBoolean(true)
            .category(GameRuleCategory.SPAWNING)
            .buildAndRegister(ReverieDreams.id("do_ghost"));

    public static void init() {

    }
}
