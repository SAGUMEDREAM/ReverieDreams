package cc.thonly.reverie_dreams.api.player;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

/**
 * Stable identity of a player component.
 *
 * <p>The {@link #id()} is what gets written to disk and sent over the network, so it must never
 * change — renaming or moving the component class no longer breaks existing player data.</p>
 *
 * <p>{@code shared} components are also sent to other clients (e.g. display names); everything else
 * is only synchronized back to the player it belongs to.</p>
 */
@SuppressWarnings("rawtypes")
public record PlayerComponentKey<T extends PlayerComponent>(
        Identifier id,
        Class<T> type,
        PlayerComponentInitializer<T> initializer,
        boolean shared
) {
    public PlayerComponentKey(Identifier id, Class<T> type, PlayerComponentInitializer<T> initializer) {
        this(id, type, initializer, false);
    }

    public Codec<T> codec() {
        return this.initializer.getCodec();
    }
}
