package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.LegacySpellcardDefinition;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfig;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
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

import java.util.List;

public final class GetSpellcardWithConfigCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("get_spellcard_with_config")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        BuiltInRegistryProviders.getSuggestProvider(this::execute, ResourceKey.createRegistryKey(ReverieDreams.id("danmaku_config")))
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

        SpellCardFrameConfig config = BuiltInRegistryProviders.DANMAKU_CONFIG.getValue(id);
        if (config == null) {
            source.sendFailure(Component.literal("Invalid resource key."));
            return 0;
        }
        ItemStack itemStack = RDItems.SPELLCARD.createStack();
        itemStack.set(RDDataComponentTypes.SPELL_CARD_COMPONENT.value(), new LegacySpellcardDefinition(List.of(List.of(config))));
        player.addItem(itemStack);
        return 1;
    }
}
