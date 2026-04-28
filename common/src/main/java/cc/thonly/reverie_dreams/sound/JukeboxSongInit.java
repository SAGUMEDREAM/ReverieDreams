package cc.thonly.reverie_dreams.sound;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.blay09.mods.balm.core.BalmRegistrars;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

import java.util.LinkedList;
import java.util.List;

public class JukeboxSongInit {
    public static final List<JukeBoxEntry> ENTRIES = new LinkedList<>();
    public static final JukeBoxEntry HR01_01 = createJukeBoxEntry("records/hr01_01", 233, 6);
    public static final JukeBoxEntry HR02_08 = createJukeBoxEntry("records/hr02_08", 296, 6);
    public static final JukeBoxEntry HR03_01 = createJukeBoxEntry("records/hr03_01", 309, 6);
    public static final JukeBoxEntry MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS = createJukeBoxEntry("records/melodic-taste-nightmare-before-crossroads", 231, 6);
    public static final JukeBoxEntry YV_FLOWER_CLOCK_AND_DREAMS = createJukeBoxEntry("records/yv_flower_clock_and_dreams", 357, 6);
    public static final JukeBoxEntry GLOWING_NEEDLES_LITTLE_PEOPLE = createJukeBoxEntry("records/glowing_needles_little_people", 242, 6);
    public static final JukeBoxEntry COOKIE = createJukeBoxEntry("records/cookie", 72, 6);
    public static final JukeBoxEntry BAD_APPLE = createJukeBoxEntry("records/badapple", 219, 6);

    private static JukeBoxEntry createJukeBoxEntry(String id, int length, int output) {
        ResourceKey<JukeboxSong> jukeboxSongRegistryKey = createJukeBoxSongRegistryKey(id);
        JukeBoxEntry entry = new JukeBoxEntry(id, jukeboxSongRegistryKey, ResourceKey.create(Registries.SOUND_EVENT, ReverieDreams.id(id)), length, output);
        ENTRIES.add(entry);
        return entry;
    }

    private static ResourceKey<JukeboxSong> createJukeBoxSongRegistryKey(String id) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, ReverieDreams.id(id));
    }

    public static void initialize() {

    }

}
