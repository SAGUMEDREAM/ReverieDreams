package cc.thonly.reverie_dreams.sound;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;

import java.util.LinkedList;
import java.util.List;

import static cc.thonly.reverie_dreams.sound.SoundEventInit.registerReference;

public class JukeboxSongInit {
    public static final List<JukeBoxEntry> ENTRIES = new LinkedList<>();
    public static final JukeBoxEntry HR01_01 = registerJukeBoxEntry("hr01_01", 233, 6);
    public static final JukeBoxEntry HR02_08 = registerJukeBoxEntry("hr02_08", 296, 6);
    public static final JukeBoxEntry HR03_01 = registerJukeBoxEntry("hr03_01", 309, 6);
    public static final JukeBoxEntry MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS = registerJukeBoxEntry("melodic-taste-nightmare-before-crossroads", 231, 6);
    public static final JukeBoxEntry YV_FLOWER_CLOCK_AND_DREAMS = registerJukeBoxEntry("yv_flower_clock_and_dreams", 357, 6);
    public static final JukeBoxEntry GLOWING_NEEDLES_LITTLE_PEOPLE = registerJukeBoxEntry("glowing_needles_little_people", 242, 6);
    public static final JukeBoxEntry COOKIE = registerJukeBoxEntry("cookie", 72, 6);
    public static final JukeBoxEntry BAD_APPLE = registerJukeBoxEntry("badapple", 219,6);

    private static JukeBoxEntry registerJukeBoxEntry(String id, int length, int output) {
        ResourceKey<JukeboxSong> jukeboxSongRegistryKey = createJukeBoxSongRegistryKey(id);
        Holder.Reference<SoundEvent> soundEventReference = registerReference(id);
        JukeBoxEntry entry = new JukeBoxEntry(id, jukeboxSongRegistryKey, soundEventReference, length, output);
        ENTRIES.add(entry);
        return entry;
    }

    private static ResourceKey<JukeboxSong> createJukeBoxSongRegistryKey(String id) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, ReverieDreams.id(id));
    }

    public static void init() {

    }

}
