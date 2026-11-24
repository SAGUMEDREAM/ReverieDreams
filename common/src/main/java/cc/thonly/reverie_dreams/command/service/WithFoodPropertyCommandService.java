package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class WithFoodPropertyCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("with_food_property")
                .requires(PermissionPredicates.isGameMasters())
                .then(
                        BuiltInRegistryProviders.getSuggestProvider(this::execute, ResourceKey.createRegistryKey(ReverieDreams.id("food_property")))
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
        FoodProperty property = BuiltInRegistryProviders.FOOD_PROPERTY.getValue(id);
        if (property == null) {
            source.sendFailure(Component.literal("Invalid resource key."));
            return 0;
        }
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.isEmpty()) {
            source.sendFailure(Component.literal("§cYour slot item is empty."));
            return 0;
        }
        List<FoodProperty> props = new ArrayList<>(itemStack.getOrDefault(RDDataComponentTypes.FOOD_PROPERTIES.value(), new ArrayList<>()));
        if (!props.contains(property)) {
            props.add(property);
        }
        itemStack.set(RDDataComponentTypes.FOOD_PROPERTIES.value(), props);
        return 1;
    }
}
