package cc.thonly.reverie_dreams.sound;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.world.item.JukeboxSong;

import java.util.Optional;

@Getter
@Setter
public class JukeBoxEntry {
    private final String id;
    private final ResourceKey<JukeboxSong> jukeboxSongKey;
    private final ResourceKey<SoundEvent> soundEventKey;
    private final int length;
    private final int output;
    private JukeboxSong ref;

    public JukeBoxEntry(String id, ResourceKey<JukeboxSong> jukeboxSongKey, ResourceKey<SoundEvent> soundEventKey, int length, int output) {
        this.id = id;
        this.jukeboxSongKey = jukeboxSongKey;
        this.soundEventKey = soundEventKey;
        this.length = length;
        this.output = output;
    }

    public JukeboxSong getEntryByProvider() {
        if (this.ref == null) {
            this.ref = createEntry(this.jukeboxSongKey, this.soundEventKey, this.length, this.output);
        }
        return this.ref;
    }

    public SoundEvent getSoundEvent() {
        return new SoundEvent(this.soundEventKey.identifier(), Optional.empty());
    }

    private static JukeboxSong createEntry(ResourceKey<JukeboxSong> key, ResourceKey<SoundEvent> soundEvent, int lengthInSeconds, int comparatorOutput) {
        return new JukeboxSong(Holder.direct(new SoundEvent(soundEvent.identifier(), Optional.empty())), Component.translatable(Util.makeDescriptionId("jukebox_song", key.identifier())), lengthInSeconds, comparatorOutput);
    }

}
