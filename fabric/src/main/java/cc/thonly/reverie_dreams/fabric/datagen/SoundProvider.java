package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractSoundProvider;
import cc.thonly.reverie_dreams.registry.content.sound.RDJukeboxSongs;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class SoundProvider extends AbstractSoundProvider {
    public SoundProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured() {
        this.addWithRecords(RDJukeboxSongs.HR01_01, null);
        this.addWithRecords(RDJukeboxSongs.HR02_08, null);
        this.addWithRecords(RDJukeboxSongs.HR03_01, null);
        this.addWithRecords(RDJukeboxSongs.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS, null);
        this.addWithRecords(RDJukeboxSongs.YV_FLOWER_CLOCK_AND_DREAMS, null);
        this.addWithRecords(RDJukeboxSongs.GLOWING_NEEDLES_LITTLE_PEOPLE, null);
        this.addWithRecords(RDJukeboxSongs.COOKIE, null);
        this.addWithRecords(RDJukeboxSongs.BAD_APPLE, null);
        this.addWithRecords(RDJukeboxSongs.A_MELODY_BLOOMING_ON_THE_EARTH, null);
        this.addWithRecords(RDJukeboxSongs.SPRAWLING_EARTH, null);
        for (var soundEvent : RDSoundEvents.SOUND_EVENTS) {
            this.addWithSoundEvent(soundEvent.value(), null);
        }
    }
}
