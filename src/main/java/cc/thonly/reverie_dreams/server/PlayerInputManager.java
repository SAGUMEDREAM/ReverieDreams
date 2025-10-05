package cc.thonly.reverie_dreams.server;


import cc.thonly.reverie_dreams.util.PairWrapper;
import lombok.Getter;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PlayerInput;

import java.util.*;

@Getter
public class PlayerInputManager {
    private static PlayerInputManager INSTANCE = null;
    public static Stack<PairWrapper<ServerPlayerEntity, Packet<?>>> TICK_PLAYER_QUEUE = new Stack<>();
    private final List<ServerPlayerEntity> FORWARDS = new ArrayList<>();
    private final List<ServerPlayerEntity> BACKWARDS = new ArrayList<>();
    private final List<ServerPlayerEntity> LEFTS = new ArrayList<>();
    private final List<ServerPlayerEntity> RIGHTS = new ArrayList<>();
    private final List<ServerPlayerEntity> JUMPS = new ArrayList<>();
    private final List<ServerPlayerEntity> SNEAKS = new ArrayList<>();
    private final List<ServerPlayerEntity> SPRINTS = new ArrayList<>();
    private final List<List<ServerPlayerEntity>> LIST = List.of(
            FORWARDS,
            BACKWARDS,
            LEFTS,
            RIGHTS,
            JUMPS,
            SNEAKS,
            SPRINTS
    );
    private final Map<ServerPlayerEntity, PlayerInput> currentInputs = new HashMap<>();

    private PlayerInputManager() {
    }

    public static void tick(MinecraftServer server) {
        PlayerInputManager inputManager = PlayerInputManager.getInstance();
        inputManager.tickServer(server);
    }

    public synchronized void tickServer(MinecraftServer server) {
        for (List<ServerPlayerEntity> playerEntities : LIST) {
            playerEntities.clear();
        }
        while (!TICK_PLAYER_QUEUE.isEmpty()) {
            PairWrapper<ServerPlayerEntity, Packet<?>> wrapper = TICK_PLAYER_QUEUE.pop();
            ServerPlayerEntity player = wrapper.getKey();
            Packet<?> packet = wrapper.getValue();
            if (packet instanceof PlayerInputC2SPacket inputC2SPacket) {
                PlayerInput input = inputC2SPacket.input();
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
                if (input.sneak()) {
                    SNEAKS.add(player);
                }
                if (input.sprint()) {
                    SPRINTS.add(player);
                }
            }
            if (packet instanceof ClientCommandC2SPacket cCC2SPacket) {
                ClientCommandC2SPacket.Mode mode = cCC2SPacket.getMode();
//                if (mode.equals(ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY)) {
//                    SNEAKS.add(entity);
//                }
//                if (mode.equals(ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY)) {
//                    SNEAKS.remove(entity);
//                }
                if (player.isSneaking()) {
                    SNEAKS.add(player);
                } else {
                    SNEAKS.remove(player);
                }
                if (mode.equals(ClientCommandC2SPacket.Mode.START_SPRINTING)) {
                    SPRINTS.add(player);
                }
                if (mode.equals(ClientCommandC2SPacket.Mode.STOP_SPRINTING)) {
                    SPRINTS.remove(player);
                }
            }
        }
    }

    public static synchronized boolean isKeyPressed(ServerPlayerEntity player, InputKey key) {
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

    public static synchronized boolean isKeyDown(ServerPlayerEntity player, InputKey key) {
        PlayerInputManager inputManager = PlayerInputManager.getInstance();
        PlayerInput input = inputManager.currentInputs.get(player);
        if (input == null) return false;

        return switch (key) {
            case FORWARD -> input.forward();
            case BACKWARD -> input.backward();
            case LEFT -> input.left();
            case RIGHT -> input.right();
            case JUMP -> input.jump();
            case SNEAK -> input.sneak();
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
