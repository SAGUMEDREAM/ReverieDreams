package cc.thonly.reverie_dreams.sound;

import cc.thonly.reverie_dreams.Touhou;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;

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
        RegistryKey<JukeboxSong> jukeboxSongRegistryKey = createJukeBoxSongRegistryKey(id);
        RegistryEntry.Reference<SoundEvent> soundEventReference = registerReference(id);
        JukeBoxEntry entry = new JukeBoxEntry(id, jukeboxSongRegistryKey, soundEventReference, length, output);
        ENTRIES.add(entry);
        return entry;
    }

    private static RegistryKey<JukeboxSong> createJukeBoxSongRegistryKey(String id) {
        return RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Touhou.id(id));
    }

    public static void init() {

    }

}
