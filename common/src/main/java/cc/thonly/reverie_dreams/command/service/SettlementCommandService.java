package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.util.command.PermissionPredicates;
import cc.thonly.reverie_dreams.world.settlement.SettlementLandmarkIntegration;
import cc.thonly.reverie_dreams.world.settlement.SettlementNPCExchange;
import cc.thonly.reverie_dreams.world.settlement.SettlementPilotData;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public final class SettlementCommandService implements CommandService {
    private final CommandBuildContext commandContext;

    public SettlementCommandService(CommandBuildContext commandContext) {
        this.commandContext = commandContext;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("settlement")
                .requires(PermissionPredicates.isGameMasters())
                .then(Commands.literal("install")
                        .then(Commands.argument("landmark", IdentifierArgument.id())
                                .then(Commands.argument("post", BlockPosArgument.blockPos())
                                        .then(Commands.argument("second", BlockPosArgument.blockPos())
                                                .executes(this::executeInstall)))))
                .then(Commands.literal("enroll")
                        .then(Commands.argument("landmark", IdentifierArgument.id())
                                .then(Commands.argument("resident", EntityArgument.entity())
                                        .executes(context -> executeEnroll(context, null, false))
                                        .then(Commands.argument("bed", BlockPosArgument.blockPos())
                                                .executes(context -> executeEnroll(
                                                        context, BlockPosArgument.getBlockPos(context, "bed"), false))))))
                .then(Commands.literal("enroll_trader")
                        .then(Commands.argument("landmark", IdentifierArgument.id())
                                .then(Commands.argument("resident", EntityArgument.entity())
                                        .executes(context -> executeEnroll(context, null, true))
                                        .then(Commands.argument("bed", BlockPosArgument.blockPos())
                                                .executes(context -> executeEnroll(
                                                        context, BlockPosArgument.getBlockPos(context, "bed"), true))))))
                .then(Commands.literal("exchange")
                        .then(Commands.argument("seller", EntityArgument.entity())
                                .then(Commands.argument("buyer", EntityArgument.entity())
                                        .then(Commands.argument("payment_box", BlockPosArgument.blockPos())
                                                .then(Commands.argument("receive_box", BlockPosArgument.blockPos())
                                                        .executes(this::executeExchange))))))
                .then(Commands.literal("trade")
                        .then(Commands.argument("seller", EntityArgument.entity())
                                .then(Commands.argument("site", IdentifierArgument.id())
                                        .then(Commands.argument("goods_box", BlockPosArgument.blockPos())
                                                .then(Commands.argument("till_box", BlockPosArgument.blockPos())
                                                        .then(Commands.argument("payment_item", ItemArgument.item(commandContext))
                                                                .then(Commands.argument("payment_count", IntegerArgumentType.integer(1))
                                                                        .then(Commands.argument("result_item", ItemArgument.item(commandContext))
                                                                                .then(Commands.argument("result_count", IntegerArgumentType.integer(1))
                                                                                        .executes(this::executeTrade))))))))))
                .then(Commands.literal("haul")
                        .then(Commands.argument("carrier", EntityArgument.entity())
                                .then(Commands.argument("source_site", IdentifierArgument.id())
                                        .then(Commands.argument("source_box", BlockPosArgument.blockPos())
                                                .then(Commands.argument("destination_site", IdentifierArgument.id())
                                                        .then(Commands.argument("destination_box", BlockPosArgument.blockPos())
                                                                .then(Commands.argument("item", ItemArgument.item(commandContext))
                                                                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                                                .executes(this::executeHaul)))))))))
                .then(Commands.literal("task")
                        .then(Commands.literal("visit")
                                .then(Commands.argument("resident", EntityArgument.entity())
                                        .then(Commands.argument("site", IdentifierArgument.id())
                                                .executes(this::executeTaskVisit))))
                        .then(Commands.literal("cancel")
                                .then(Commands.argument("resident", EntityArgument.entity())
                                        .executes(this::executeTaskCancel))))
                .then(Commands.literal("add_site")
                        .then(Commands.argument("landmark", IdentifierArgument.id())
                                .then(Commands.argument("site", IdentifierArgument.id())
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(this::executeAddSite)))))
                .then(Commands.literal("connect")
                        .then(Commands.argument("landmark", IdentifierArgument.id())
                                .then(Commands.argument("from", IdentifierArgument.id())
                                        .then(Commands.argument("to", IdentifierArgument.id())
                                                .executes(this::executeConnect)))))
                .then(Commands.literal("status")
                        .then(Commands.argument("landmark", IdentifierArgument.id())
                                .executes(this::executeStatus)));
    }

    private int executeInstall(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Identifier landmark = IdentifierArgument.getId(context, "landmark");
        BlockPos post = BlockPosArgument.getBlockPos(context, "post");
        BlockPos second = BlockPosArgument.getBlockPos(context, "second");
        boolean installed = SettlementLandmarkIntegration.install(level, landmark, post, second);
        return finish(source, installed,
                "Installed settlement " + landmark,
                "Failed to install settlement " + landmark);
    }

    private int executeEnroll(CommandContext<CommandSourceStack> context, BlockPos bed, boolean trader)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Identifier landmark = IdentifierArgument.getId(context, "landmark");
        Mob resident = mob(context, "resident");
        if (resident == null) {
            return 0;
        }
        boolean enrolled = trader
                ? SettlementLandmarkIntegration.enroll(level, landmark, resident, bed, true)
                : SettlementLandmarkIntegration.enroll(level, landmark, resident, bed);
        String name = landmark + (trader ? " trader " : " resident ") + resident.getUUID();
        return finish(source, enrolled,
                (trader ? "Enrolled " : "Enrolled ") + name,
                (trader ? "Failed to enroll trader " : "Failed to enroll resident ") + resident.getUUID());
    }

    private int executeExchange(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Mob seller = mob(context, "seller");
        Mob buyer = mob(context, "buyer");
        if (seller == null || buyer == null) {
            return 0;
        }
        BlockPos paymentBox = BlockPosArgument.getBlockPos(context, "payment_box");
        BlockPos receiveBox = BlockPosArgument.getBlockPos(context, "receive_box");
        boolean exchanged = SettlementNPCExchange.exchange(seller, buyer, paymentBox, receiveBox);
        return finish(source, exchanged,
                "Exchange completed",
                "Exchange failed");
    }

    private int executeTrade(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Mob seller = mob(context, "seller");
        if (seller == null) {
            return 0;
        }
        Identifier site = IdentifierArgument.getId(context, "site");
        BlockPos goodsBox = BlockPosArgument.getBlockPos(context, "goods_box");
        BlockPos tillBox = BlockPosArgument.getBlockPos(context, "till_box");
        ItemStack payment = ItemArgument.getItem(context, "payment_item")
                .createItemStack(IntegerArgumentType.getInteger(context, "payment_count"));
        ItemStack result = ItemArgument.getItem(context, "result_item")
                .createItemStack(IntegerArgumentType.getInteger(context, "result_count"));
        boolean registered = SettlementPilotData.registerStockTrade(
                seller, site, payment, result, goodsBox, tillBox);
        return finish(source, registered,
                "Registered stock trade at " + site,
                "Failed to register stock trade at " + site);
    }

    private int executeHaul(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Mob carrier = mob(context, "carrier");
        if (carrier == null) {
            return 0;
        }
        Identifier sourceSite = IdentifierArgument.getId(context, "source_site");
        BlockPos sourceBox = BlockPosArgument.getBlockPos(context, "source_box");
        Identifier destinationSite = IdentifierArgument.getId(context, "destination_site");
        BlockPos destinationBox = BlockPosArgument.getBlockPos(context, "destination_box");
        var item = ItemArgument.getItem(context, "item").item().value();
        int count = IntegerArgumentType.getInteger(context, "count");
        boolean queued = SettlementPilotData.queueHaul(
                carrier, sourceSite, sourceBox, destinationSite, destinationBox, item, count);
        return finish(source, queued,
                "Queued haul " + sourceSite + " -> " + destinationSite,
                "Failed to queue haul " + sourceSite + " -> " + destinationSite);
    }

    private int executeTaskVisit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Mob resident = mob(context, "resident");
        if (resident == null) return 0;
        Identifier site = IdentifierArgument.getId(context, "site");
        return finish(context.getSource(), SettlementPilotData.queueTask(resident, site),
                "Queued visit to " + site, "Failed to queue visit to " + site);
    }

    private int executeTaskCancel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Mob resident = mob(context, "resident");
        if (resident == null) return 0;
        return finish(context.getSource(), SettlementPilotData.cancelTask(resident),
                "Cancelled resident task", "Resident has no task to cancel");
    }

    private int executeAddSite(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Identifier landmark = IdentifierArgument.getId(context, "landmark");
        Identifier site = IdentifierArgument.getId(context, "site");
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        boolean added = SettlementLandmarkIntegration.addSite(level, landmark, site, pos);
        return finish(source, added,
                "Added site " + site + " at " + pos.toShortString(),
                "Failed to add site " + site);
    }

    private int executeConnect(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Identifier landmark = IdentifierArgument.getId(context, "landmark");
        Identifier from = IdentifierArgument.getId(context, "from");
        Identifier to = IdentifierArgument.getId(context, "to");
        boolean connected = SettlementLandmarkIntegration.connect(level, landmark, from, to);
        return finish(source, connected,
                "Connected " + from + " to " + to,
                "Failed to connect " + from + " to " + to);
    }

    private int executeStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Identifier landmark = IdentifierArgument.getId(context, "landmark");
        SettlementPilotData data = level.getDataStorage().get(SettlementPilotData.TYPE);
        SettlementPilotData.Layout layout = data == null ? null : data.layout(landmark);
        if (layout == null) {
            source.sendFailure(Component.literal("Settlement " + landmark + " is not installed"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Settlement " + landmark + ": "
                + layout.sites().size() + " sites, " + layout.roles().size() + " roles"), false);
        return 1;
    }

    private static Mob mob(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, name);
        ServerLevel level = context.getSource().getLevel();
        if (entity.level() != level) {
            context.getSource().sendFailure(Component.literal(name + " is not in the current level"));
            return null;
        }
        if (!(entity instanceof Mob mob)) {
            context.getSource().sendFailure(Component.literal(name + " is not a mob"));
            return null;
        }
        return mob;
    }

    private static int finish(CommandSourceStack source, boolean success,
                              String successMessage, String failureMessage) {
        if (success) {
            source.sendSuccess(() -> Component.literal(successMessage), false);
            return 1;
        }
        source.sendFailure(Component.literal(failureMessage));
        return 0;
    }
}
