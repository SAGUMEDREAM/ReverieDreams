package cc.thonly.reverie_dreams.server.input;


import cc.thonly.reverie_dreams.api.player.PlayerInputManagerAccess;
import cc.thonly.reverie_dreams.server.InputKey;
import cc.thonly.reverie_dreams.util.PairWrapper;
import lombok.Getter;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Input;

import java.util.*;

@Getter
public class ServerPlayerInputManagerAccess implements PlayerInputManagerAccess {
    private static ServerPlayerInputManagerAccess INSTANCE = null;
    public static Stack<PairWrapper<ServerPlayer, Packet<?>>> TICK_PLAYER_QUEUE = new Stack<>();
    private final List<ServerPlayer> FORWARDS = new ArrayList<>();
    private final List<ServerPlayer> BACKWARDS = new ArrayList<>();
    private final List<ServerPlayer> LEFTS = new ArrayList<>();
    private final List<ServerPlayer> RIGHTS = new ArrayList<>();
    private final List<ServerPlayer> JUMPS = new ArrayList<>();
    private final List<ServerPlayer> SNEAKS = new ArrayList<>();
    private final List<ServerPlayer> SPRINTS = new ArrayList<>();
    private final List<List<ServerPlayer>> LIST = List.of(
            FORWARDS,
            BACKWARDS,
            LEFTS,
            RIGHTS,
            JUMPS,
            SNEAKS,
            SPRINTS
    );
    private final Map<ServerPlayer, Input> currentInputs = new HashMap<>();

    private ServerPlayerInputManagerAccess() {
    }

    public static void tick(MinecraftServer server) {
        ServerPlayerInputManagerAccess inputManager = ServerPlayerInputManagerAccess.getInstance();
        inputManager.tickServer(server);
    }

    public void tickServer(MinecraftServer server) {
        for (List<ServerPlayer> playerEntities : LIST) {
            playerEntities.clear();
        }
        while (!TICK_PLAYER_QUEUE.isEmpty()) {
            PairWrapper<ServerPlayer, Packet<?>> wrapper = TICK_PLAYER_QUEUE.pop();
            ServerPlayer player = wrapper.getKey();
            Packet<?> packet = wrapper.getValue();
            if (packet instanceof ServerboundPlayerInputPacket(Input input)) {
                currentInputs.put(player, input);
                if (input.forward()) {
                    FORWARDS.add(player);
                }
                if (input.backward()) {
                    BACKWARDS.add(player);
                }
                if (input.left()) {
                    LEFTS.add(player);
                }
                if (input.right()) {
                    RIGHTS.add(player);
                }
                if (input.jump()) {
                    JUMPS.add(player);
                }
                if (input.shift()) {
                    SNEAKS.add(player);
                }
                if (input.sprint()) {
                    SPRINTS.add(player);
                }
            }
            if (packet instanceof ServerboundPlayerCommandPacket cCC2SPacket) {
                ServerboundPlayerCommandPacket.Action mode = cCC2SPacket.getAction();
//                if (mode.equals(ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY)) {
//                    SNEAKS.add(entity);
//                }
//                if (mode.equals(ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY)) {
//                    SNEAKS.remove(entity);
//                }
                if (player.isShiftKeyDown()) {
                    SNEAKS.add(player);
                } else {
                    SNEAKS.remove(player);
                }
                if (mode.equals(ServerboundPlayerCommandPacket.Action.START_SPRINTING)) {
                    SPRINTS.add(player);
                }
                if (mode.equals(ServerboundPlayerCommandPacket.Action.STOP_SPRINTING)) {
                    SPRINTS.remove(player);
                }
            }
        }
    }

    public synchronized boolean isKeyPressed(ServerPlayer player, InputKey key) {
        ServerPlayerInputManagerAccess inputManager = ServerPlayerInputManagerAccess.getInstance();
        return switch (key) {
            case InputKey.FORWARD -> inputManager.FORWARDS.contains(player);
            case InputKey.BACKWARD -> inputManager.BACKWARDS.contains(player);
            case InputKey.LEFT -> inputManager.LEFTS.contains(player);
            case InputKey.RIGHT -> inputManager.RIGHTS.contains(player);
            case InputKey.JUMP -> inputManager.JUMPS.contains(player);
//            case InputKey.SNEAK -> inputManager.SNEAKS.contains(player);
            case InputKey.SPRINT -> inputManager.SPRINTS.contains(player);
            default -> false;
        };
    }

    public synchronized boolean isKeyDown(ServerPlayer player, InputKey key) {
        ServerPlayerInputManagerAccess inputManager = ServerPlayerInputManagerAccess.getInstance();
        Input input = inputManager.currentInputs.get(player);
        if (input == null) return false;

        return switch (key) {
            case FORWARD -> input.forward();
            case BACKWARD -> input.backward();
            case LEFT -> input.left();
            case RIGHT -> input.right();
            case JUMP -> input.jump();
            case SNEAK -> input.shift();
            case SPRINT -> input.sprint();
        };
    }

    public static synchronized ServerPlayerInputManagerAccess getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerPlayerInputManagerAccess();
        }
        return INSTANCE;
    }

    public synchronized PlayerInputManagerAccess reload() {
        return (INSTANCE = new ServerPlayerInputManagerAccess());
    }

}
