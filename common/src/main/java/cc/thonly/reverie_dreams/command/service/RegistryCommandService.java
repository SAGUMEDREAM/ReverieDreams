package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.api.registry.RegistryEntryTranslatable;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.util.command.PermissionPredicates;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class RegistryCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("registry")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        BuiltInRegistryProviders.getSuggestProvider(this::execute)
                );
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Identifier registryKeyId = IdentifierArgument.getId(context, "registry_key");
        Identifier id = IdentifierArgument.getId(context, "id");

        ResourceKey<Registry<Object>> registryKey =
                ResourceKey.createRegistryKey(registryKeyId);

        RegistryProvider<?> registry = BuiltInRegistryProviders.ROOT.get(registryKey);

        if (registry == null) {
            source.sendFailure(Component.translatable("command.touhou.registry.not_found", registryKey));
            return 0;
        }

        Object value = registry.findValue(id).orElse(null);

        MutableComponent msg = Component.translatable("command.touhou.registry.header", ResourceKey.create(registryKey, id));
        if (value == null) {
            msg.append(Component.translatable("command.touhou.registry.entry_not_found").withStyle(ChatFormatting.RED));
            source.sendSystemMessage(msg);
            return 0;
        }

        if (value instanceof RegistryEntryTranslatable translatable) {
            msg.append(Component.translatable("command.touhou.registry.translation").withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable(translatable.translateKey()).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("\n"));
        }

        msg.append(Component.translatable("command.touhou.registry.object").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(value.toString()).withStyle(ChatFormatting.AQUA));

        source.sendSystemMessage(msg);
        return 1;
    }
}
