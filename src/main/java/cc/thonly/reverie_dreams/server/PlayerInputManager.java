package cc.thonly.reverie_dreams.server;


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
public class PlayerInputManager {
    private static PlayerInputManager INSTANCE = null;
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

    private PlayerInputManager() {
    }

    public static void tick(MinecraftServer server) {
        PlayerInputManager inputManager = PlayerInputManager.getInstance();
        inputManager.tickServer(server);
    }

    public synchronized void tickServer(MinecraftServer server) {
        for (List<ServerPlayer> playerEntities : LIST) {
            playerEntities.clear();
        }
        while (!TICK_PLAYER_QUEUE.isEmpty()) {
            PairWrapper<ServerPlayer, Packet<?>> wrapper = TICK_PLAYER_QUEUE.pop();
            ServerPlayer player = wrapper.getKey();
            Packet<?> packet = wrapper.getValue();
            if (packet instanceof ServerboundPlayerInputPacket inputC2SPacket) {
                Input input = inputC2SPacket.input();
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

    public static synchronized boolean isKeyPressed(ServerPlayer player, InputKey key) {
        PlayerInputManager inputManager = PlayerInputManager.getInstance();
        return switch (key) {
            case InputKey.FORWARD -> inputManager.FORWARDS.contains(player);
            case InputKey.BACKWARD -> inputManager.BACKWARDS.contains(player);
            case InputKey.LEFT -> inputManager.LEFTS.contains(player);
            case InputKey.RIGHT -> inputManager.RIGHTS.contains(player);
            case InputKey.JUMP -> inputManager.JUMPS.contains(player);
//            case InputKey.SNEAK -> inputManager.SNEAKS.contains(entity);
            case InputKey.SPRINT -> inputManager.SPRINTS.contains(player);
            default -> false;
        };
    }

    public static synchronized boolean isKeyDown(ServerPlayer player, InputKey key) {
        PlayerInputManager inputManager = PlayerInputManager.getInstance();
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


    public static synchronized PlayerInputManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerInputManager();
        }
        return INSTANCE;
    }

    public synchronized PlayerInputManager reload() {
        return (INSTANCE = new PlayerInputManager());
    }

    public enum InputKey {
        FORWARD(),
        BACKWARD(),
        LEFT(),
        RIGHT(),
        JUMP(),
        SNEAK(),
        SPRINT();

        public static Optional<InputKey> fromString(String str) {
            try {
                return Optional.of(InputKey.valueOf(str.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }
}
