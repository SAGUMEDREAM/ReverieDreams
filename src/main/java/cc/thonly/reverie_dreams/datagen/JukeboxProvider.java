package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.datagen.generator.AbstractJukeboxProvider;
import cc.thonly.reverie_dreams.sound.JukeBoxEntry;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import java.util.concurrent.CompletableFuture;

public class JukeboxProvider extends AbstractJukeboxProvider {
    public JukeboxProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured() {
        for (JukeBoxEntry entry : JukeboxSongInit.ENTRIES) {
            this.add(ReverieDreams.id(entry.getId()), entry.getRef());
        }
    }
}
