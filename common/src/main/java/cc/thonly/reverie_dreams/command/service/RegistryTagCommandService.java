package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.util.command.PermissionPredicates;
import cc.thonly.reverie_dreams.util.math.ModMth;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class RegistryTagCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("registry_tag")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        BuiltInRegistryProviders.getSuggestTagProvider(this::execute)
                );
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Identifier registryKeyId = IdentifierArgument.getId(context, "registry_key");
        Identifier id = IdentifierArgument.getId(context, "id");

        ResourceKey<Registry<Object>> registryKey = ResourceKey.createRegistryKey(registryKeyId);
        RegistryProvider<?> registry = BuiltInRegistryProviders.ROOT.get(registryKey);

        if (registry == null) {
            source.sendFailure(Component.translatable("command.touhou.registry_tag.not_found", registryKey));
            return 0;
        }

        TagKey<?> tagKey = TagKey.create(registry.key(), id);

        List<Holder> list = ModMth.toList(
                registry.getTagOrEmpty((TagKey) tagKey)
        );

        source.sendSystemMessage(Component.translatable("command.touhou.registry_tag.name", id));
        List<Identifier> ids = new ArrayList<>();

        for (Holder holder : list) {
            holder.unwrapKey().ifPresent(key -> {
                if (key instanceof ResourceKey resourceKey) {
                    ids.add(resourceKey.identifier());
                }
            });
        }

        source.sendSystemMessage(
                Component.translatable("command.touhou.registry_tag.entries", ids)
        );

        return 1;
    }
}
