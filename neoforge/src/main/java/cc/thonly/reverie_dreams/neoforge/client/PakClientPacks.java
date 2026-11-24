package cc.thonly.reverie_dreams.neoforge.client;

import cc.thonly.reverie_dreams.client.pak.PakSkinPacks;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public final class PakClientPacks {
    private PakClientPacks() {
    }

    public static void addPacks(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES && !event.isTrusted()) {
            event.addRepositorySource(PakSkinPacks.SOURCE);
        }
    }
}
