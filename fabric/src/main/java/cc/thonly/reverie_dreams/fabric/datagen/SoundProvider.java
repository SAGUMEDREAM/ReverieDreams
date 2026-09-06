package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractSoundProvider;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class SoundProvider extends AbstractSoundProvider {
    public SoundProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured() {
        this.addWithRecords(JukeboxSongInit.HR01_01, null);
        this.addWithRecords(JukeboxSongInit.HR02_08, null);
        this.addWithRecords(JukeboxSongInit.HR03_01, null);
        this.addWithRecords(JukeboxSongInit.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS, null);
        this.addWithRecords(JukeboxSongInit.YV_FLOWER_CLOCK_AND_DREAMS, null);
        this.addWithRecords(JukeboxSongInit.GLOWING_NEEDLES_LITTLE_PEOPLE, null);
        this.addWithRecords(JukeboxSongInit.COOKIE, null);
        this.addWithRecords(JukeboxSongInit.BAD_APPLE, null);
        this.addWithRecords(JukeboxSongInit.A_MELODY_BLOOMING_ON_THE_EARTH, null);
        this.addWithRecords(JukeboxSongInit.SPRAWLING_EARTH, null);
        for (var soundEvent : RDSoundEvents.SOUND_EVENTS) {
            this.addWithSoundEvent(soundEvent.value(), null);
        }
    }
}
