package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuDefaults;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.item.danmaku.SpellcardItem;
import cc.thonly.reverie_dreams.item.danmaku.CombatTechniqueItem;
import cc.thonly.reverie_dreams.registry.content.danmaku.soku.SokuMoves;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.util.command.PermissionPredicates;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class GetPatternCardCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("get_pattern_card")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        BuiltInRegistryProviders.getSuggestProvider(this::execute, ResourceKey.createRegistryKey(ReverieDreams.id("danmaku_pattern")))
                );
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        assert player != null;
        Identifier id = IdentifierArgument.getId(context, "id");
        DanmakuPattern pattern = BuiltInRegistryProviders.DANMAKU_PATTERN.findValue(id).orElse(null);
        if (pattern == null) {
            source.sendFailure(Component.literal("Invalid danmaku pattern id."));
            return 0;
        }
        ItemStack itemStack = SokuMoves.find(id) == null
                ? SpellcardItem.createCard(id) : CombatTechniqueItem.createTechnique(id);
        player.addItem(itemStack);
        source.sendSuccess(() -> Component.translatableWithFallback("command.touhou.pattern_card.given", "Received %s", DanmakuDefaults.itemName(id)), false);
        return 1;
    }
}
