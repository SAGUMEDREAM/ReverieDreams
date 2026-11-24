package cc.thonly.reverie_dreams.world.moon.signal;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinStructures;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

/**
 * Natural lunar signal stations. All inventory, scan and navigation actions are server-authoritative.
 */
@SuppressWarnings("resource")
public final class MoonSignalTowers {
    private static final Identifier ACTION = ReverieDreams.id("moon/signal_tower");
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final ArrayDeque<Job> JOBS = new ArrayDeque<>();

    private record Session(ResourceKey<Level> dimension, BlockPos pos, long tower, String token, long expires) {
    }

    private record Job(ServerLevel level, long tower, UUID owner, MoonSignalScan scan) {
    }

    private MoonSignalTowers() {
    }

    public static void initialize() {
        InteractionEvent.RIGHT_CLICK_BLOCK.register(MoonSignalTowers::interact);
        TickEvent.SERVER_POST.register(MoonSignalTowers::tick);
        LifecycleEvent.SERVER_STOPPED.register(server -> {
            SESSIONS.clear();
            JOBS.clear();
        });
        CustomClickActionRegistry.registerCustomAction(ACTION, MoonSignalTowers::handle);
    }

    public static MoonSignalData data(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(MoonSignalData.TYPE);
    }

    public static InteractionResult interact(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        if (hand != InteractionHand.MAIN_HAND || player.isSpectator() || !player.mayBuild()
                || !player.level().dimension().equals(RDBuiltinLevels.THE_MOON)
                || !player.level().getBlockState(pos).is(Blocks.LODESTONE))
            return InteractionResult.PASS;
        if (player.level().isClientSide())
            return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer))
            return InteractionResult.PASS;
        ServerLevel level = (ServerLevel) player.level();
        if (!level.mayInteract(player, pos) || player.blockPosition().distSqr(pos) > 64)
            return InteractionResult.PASS;
        Long tower = naturalTower(level, pos);
        if (tower == null)
            return InteractionResult.PASS;
        var session = new Session(level.dimension(), pos.immutable(), tower, UUID.randomUUID().toString(), level.getGameTime() + 2400);
        SESSIONS.put(player.getUUID(), session);
        open(serverPlayer, session);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static Long naturalTower(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos))
            return null;
        var structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(RDBuiltinStructures.SIGNAL_TOWER).value();
        var start = level.structureManager().getStructureAt(pos, structure);
        return start.isValid() ? ((long) start.getChunkPos().z() << 32) | (start.getChunkPos().x() & 0xffffffffL) : null;
    }

    @SuppressWarnings("deprecation")
    private static Session validate(ServerPlayer player, String token) {
        if (player == null || player.isSpectator() || !player.mayBuild())
            return null;
        var session = SESSIONS.get(player.getUUID());
        if (session == null || !session.token.equals(token) || !player.level().dimension().equals(session.dimension)
                || player.level().getGameTime() > session.expires || player.blockPosition().distSqr(session.pos) > 64
                || !player.level().hasChunkAt(session.pos) || !player.level().getBlockState(session.pos).is(Blocks.LODESTONE)
                || !player.level().mayInteract(player, session.pos))
            return null;
        Long tower = naturalTower((ServerLevel) player.level(), session.pos);
        return tower != null && tower == session.tower ? session : null;
    }

    private static ItemStack part(int stage) {
        return switch (stage) {
            case 0 -> RDItems.LUNAR_POWER_CELL.toStack();
            case 1 -> RDItems.LUNAR_SIGNAL_COIL.toStack();
            case 2 -> RDItems.LUNAR_SIGNAL_CRYSTAL.toStack(2);
            default -> ItemStack.EMPTY;
        };
    }

    private static Component text(String suffix, Object... args) {
        return Component.translatable("moon_signal." + suffix, args);
    }

    private static CompoundTag payload(Session session, String action, int stage) {
        var tag = new CompoundTag();
        tag.putString("token", session.token);
        tag.putString("action", action);
        tag.putInt("stage", stage);
        return tag;
    }

    private static void open(ServerPlayer player, Session session) {
        ServerLevel level = (ServerLevel) player.level();
        var tower = data(level).tower(session.tower);
        DialogBuilder.builder(builder -> {
            builder.columns(1);
            builder.common(common -> {
                common.title(text("title")).setPause(false).setCanCloseWithEscape(true);
                common.addTextBody(text(tower.repaired() ? "online" : "stage", tower.stage(), 3), 320);
                if (!tower.repaired()) {
                    var needed = part(tower.stage());
                    common.addTextBody(text("need", needed.getHoverName(), needed.getCount(), player.getInventory().countItem(needed.getItem())), 320);
                    common.addTextBody(text("parts_hint"), 320);
                    common.addTextBody(text("recipe." + tower.stage()), 320);
                } else {
                    common.addTextBody(text(isScanning(level, session.tower) ? "scanning" : "scan_hint", MoonSignalScan.RANGE), 320);
                    common.addTextBody(text("compass_hint"), 320);
                    for (var target : tower.targets())
                        common.addTextBody(text("coordinates", text("target." + target.kind()),
                                target.pos().getX(), target.pos().getZ(), (int) Math.sqrt(MoonSignalScan.horizontalDistanceSquared(session.pos, target.pos()))), 320);
                    if (tower.scannedAt() >= 0)
                        for (String kind : List.of("institute", "supply", "anomaly")) {
                            if (tower.targets().stream().noneMatch(target -> target.kind().equals(kind)))
                                common.addTextBody(text("not_found", text("target." + kind)), 320);
                        }
                }
            });
            builder.actions(actions -> {
                var actionBuilder = actions.actionBuilder();
                if (!tower.repaired())
                    actions.addButton(text("install"), Optional.of(actionBuilder.customAction(ACTION, payload(session, "install", tower.stage()))));
                else {
                    actions.addButton(text(isScanning(level, session.tower) ? "refresh" : "scan"), Optional.of(actionBuilder.customAction(ACTION, payload(session, "scan", 3))));
                    for (var target : tower.targets()) {
                        var tag = payload(session, "navigate", 3);
                        tag.putString("kind", target.kind());
                        actions.addButton(text("navigate", text("target." + target.kind())), Optional.of(actionBuilder.customAction(ACTION, tag)));
                    }
                }
                actions.addButton(text("close"), Optional.empty());
            });
        }).get().buildOrThrow().open(player);
    }

    public static void handle(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
        if (player == null)
            return;
        CompoundTag tag = packet.payload().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).orElse(null);
        if (tag == null)
            return;
        var session = validate(player, tag.getStringOr("token", ""));
        if (session == null)
            return;
        ServerLevel level = (ServerLevel) player.level();
        var ledger = data(level);
        var tower = ledger.tower(session.tower);
        switch (tag.getStringOr("action", "")) {
            case "install" -> {
                if (tower.repaired() || tag.getIntOr("stage", -1) != tower.stage()) {
                    open(player, session);
                    return;
                }
                var needed = part(tower.stage());
                if (!player.getAbilities().instabuild && !consume(player, needed)) {
                    player.sendSystemMessage(text("missing"), false);
                    open(player, session);
                    return;
                }
                var advanced = tower.advance();
                ledger.put(session.tower, advanced);
                level.playSound(null, session.pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.6f, 1.3f);
                if (advanced.repaired()) {
                    if (level.getBlockState(session.pos.below()).is(Blocks.CHISELED_POLISHED_BLACKSTONE))
                        level.setBlockAndUpdate(session.pos.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
                    give(player, new ItemStack(Items.COMPASS));
                    level.playSound(null, session.pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);
                    player.sendSystemMessage(text("repaired"), false);
                }
            }
            case "scan" -> {
                if (!tower.repaired())
                    return;
                if (!isScanning(level, session.tower)) {
                    long now = level.getGameTime();
                    if (now >= tower.scannedAt() && now - tower.scannedAt() < 1200)
                        player.sendSystemMessage(text("cooldown"), false);
                    else if (JOBS.size() >= 8)
                        player.sendSystemMessage(text("busy"), false);
                    else
                        try {
                            JOBS.addLast(new Job(level, session.tower, player.getUUID(), new MoonSignalScan(level, session.pos)));
                        } catch (RuntimeException error) {
                            player.sendSystemMessage(text("scan_failed"), false);
                        }
                }
            }
            case "navigate" -> {
                if (!tower.repaired())
                    return;
                var target = tower.targets().stream().filter(t -> t.kind().equals(tag.getStringOr("kind", ""))).findFirst().orElse(null);
                if (target == null)
                    return;
                ItemStack held = player.getMainHandItem();
                if (!held.is(Items.COMPASS)) {
                    player.sendSystemMessage(text("compass_hint"), false);
                    break;
                }
                ItemStack compass = held.getCount() == 1 ? held : held.split(1);
                compass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(), target.pos())), false));
                compass.set(DataComponents.CUSTOM_NAME, text("compass_name", text("target." + target.kind())));
                if (compass != held)
                    give(player, compass);
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
                player.sendSystemMessage(text("marked", text("target." + target.kind()), target.pos().getX(), target.pos().getZ()), false);
            }
            default -> {
                return;
            }
        }
        open(player, session);
    }

    private static boolean consume(ServerPlayer player, ItemStack needed) {
        var inventory = player.getInventory();
        if (inventory.countItem(needed.getItem()) < needed.getCount())
            return false;
        int remaining = needed.getCount();
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            var stack = inventory.getItem(slot);
            if (stack.is(needed.getItem()))
                remaining -= inventory.removeItem(slot, Math.min(stack.getCount(), remaining)).getCount();
        }
        inventory.setChanged();
        player.containerMenu.broadcastChanges();
        return remaining == 0;
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.addItem(stack))
            player.drop(stack, false);
    }

    private static boolean isScanning(ServerLevel level, long tower) {
        return JOBS.stream().anyMatch(j -> j.level == level && j.tower == tower);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 100 == 0)
            SESSIONS.entrySet().removeIf(entry -> {
                var session = entry.getValue();
                var level = server.getLevel(session.dimension);
                return level == null || level.getGameTime() > session.expires || server.getPlayerList().getPlayer(entry.getKey()) == null;
            });
        long deadline = System.nanoTime() + 6_000_000;
        for (int work = 0; work < 2 && !JOBS.isEmpty() && System.nanoTime() < deadline; work++) {
            Job job = JOBS.peekFirst();
            try {
                if (!job.scan.step(job.level))
                    continue;
                var ledger = data(job.level);
                var tower = ledger.tower(job.tower);
                ledger.put(job.tower, new MoonSignalData.Tower(tower.stage(), job.level.getGameTime(), job.scan.targets()));
                var owner = server.getPlayerList().getPlayer(job.owner);
                if (owner != null)
                    owner.sendSystemMessage(text("scan_done", job.scan.targets().size()), false);
            } catch (RuntimeException error) {
                var owner = server.getPlayerList().getPlayer(job.owner);
                if (owner != null)
                    owner.sendSystemMessage(text("scan_failed"), false);
            }
            JOBS.removeFirst();
        }
    }
}
