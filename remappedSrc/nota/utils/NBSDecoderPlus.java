package nota.utils;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import nota.model.CustomInstrument;
import nota.model.Layer;
import nota.model.Note;
import nota.model.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NBSDecoderPlus extends NBSDecoder {
    private static final Map<NoteBlockInstrument, Byte> instrumentToByteMap = new HashMap<>();

    static {
        instrumentToByteMap.put(NoteBlockInstrument.HARP, (byte) 0);
        instrumentToByteMap.put(NoteBlockInstrument.BASS, (byte) 1);
        instrumentToByteMap.put(NoteBlockInstrument.BASEDRUM, (byte) 2);
        instrumentToByteMap.put(NoteBlockInstrument.SNARE, (byte) 3);
        instrumentToByteMap.put(NoteBlockInstrument.HAT, (byte) 4);
        instrumentToByteMap.put(NoteBlockInstrument.GUITAR, (byte) 5);
        instrumentToByteMap.put(NoteBlockInstrument.FLUTE, (byte) 6);
        instrumentToByteMap.put(NoteBlockInstrument.BELL, (byte) 7);
        instrumentToByteMap.put(NoteBlockInstrument.CHIME, (byte) 8);
        instrumentToByteMap.put(NoteBlockInstrument.XYLOPHONE, (byte) 9);
        instrumentToByteMap.put(NoteBlockInstrument.IRON_XYLOPHONE, (byte) 10);
        instrumentToByteMap.put(NoteBlockInstrument.COW_BELL, (byte) 11);
        instrumentToByteMap.put(NoteBlockInstrument.DIDGERIDOO, (byte) 12);
        instrumentToByteMap.put(NoteBlockInstrument.BIT, (byte) 13);
        instrumentToByteMap.put(NoteBlockInstrument.BANJO, (byte) 14);
        instrumentToByteMap.put(NoteBlockInstrument.PLING, (byte) 15);
    }

    public static Song parse(File songFile, NoteBlockInstrument instrument) {
        try {
            Song song = parse(new FileInputStream(songFile), songFile);
            if (song == null) {
                log.error("Failed to parse NBS file: {}", songFile.getName());
                return null;
            }
            CustomInstrument[] customInstruments = song.getCustomInstruments();
            for (int i = 0; i < customInstruments.length; i++) {
                customInstruments[i].setSound(instrument.getSoundEvent().value());
            }
            HashMap<Integer, Layer> layerHashMap = song.getLayerHashMap();
            for (var entry : layerHashMap.entrySet()) {
                Layer layer = entry.getValue();
                for (var lay:layer.getNotesAtTicks().entrySet()) {
                    Note note = lay.getValue();
                    note.setInstrument(getByteFromInstrument(instrument));
                }
            }
            return song;
        } catch (FileNotFoundException e) {
            log.error("Failed to parse NBS File: ", e);
        }
        return null;
    }

    public static byte getByteFromInstrument(NoteBlockInstrument instrument) {
        return instrumentToByteMap.getOrDefault(instrument, (byte) 0); // 默认为 0：HARP
    }

}
