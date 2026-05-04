package cc.thonly.reverie_dreams.server;

import java.util.Optional;

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
