package cc.thonly.reverie_dreams.util.codec;

import com.mojang.serialization.Codec;

import java.util.UUID;

public interface UUIDCodec {
    Codec<UUID> CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
}
