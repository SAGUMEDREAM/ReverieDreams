package cc.thonly.reverie_dreams.api.entity;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Server-owned provenance on a dropped item, never on the reusable inventory stack.
 */
public interface YoukaiFeedingSource {
    @Nullable UUID reverie_dreams$getYoukaiFeeder();

    void reverie_dreams$setYoukaiFeeder(@Nullable UUID playerId);
}
