package cc.thonly.reverie_dreams.world.gensokyo.update;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.TicketType;

/**
 * Unique nonpersistent, loading-only ticket. Removing it never removes another mod's ticket.
 */
public final class StructureUpdateTickets {
    public static final TicketType UPDATE = Registry.register(BuiltInRegistries.TICKET_TYPE, ReverieDreams.id("structure_update"), new TicketType(0L, TicketType.FLAG_LOADING));

    private StructureUpdateTickets() {
    }

    public static void initialize() {
    }
}
