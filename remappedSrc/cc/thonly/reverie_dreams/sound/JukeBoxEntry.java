package cc.thonly.reverie_dreams.sound;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;

@Getter
@Setter
public class JukeBoxEntry {
    private final String id;
    private final ResourceKey<JukeboxSong> jukeboxSongRegistryKey;
    private final Holder.Reference<SoundEvent> soundEventReference;
    private final int length;
    private final int output;
    private JukeboxSong ref;

    public JukeBoxEntry(String id, ResourceKey<JukeboxSong> jukeboxSongRegistryKey, Holder.Reference<SoundEvent> soundEventReference, int length, int output) {
        this.id = id;
        this.jukeboxSongRegistryKey = jukeboxSongRegistryKey;
        this.soundEventReference = soundEventReference;
        this.length = length;
        this.output = output;
        this.ref = createEntry(this.jukeboxSongRegistryKey, this.soundEventReference, length, output);
    }

    private static JukeboxSong createEntry(ResourceKey<JukeboxSong> key, Holder.Reference<SoundEvent> soundEvent, int lengthInSeconds, int comparatorOutput) {
        return new JukeboxSong(soundEvent, Component.translatable(Util.makeDescriptionId("jukebox_song", key.location())), lengthInSeconds, comparatorOutput);
    }

}
