package cc.thonly.reverie_dreams.util.nbs;

import javax.sound.midi.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Self-contained Java implementation of NBSTool's midi2nbs.py conversion logic.
 * Requires only the Java standard library (javax.sound.midi).
 */
public final class Midi2Nbs {
    private Midi2Nbs() {}

    public static final int NOTE_POS_MULTIPLIER = 18;
    public static final int TRAILING_NOTE_STEREO_PAN = 50;
    public static final double NBS_PITCH_IN_MIDI_PITCHBEND = 40.96;
    private static final Set<Integer> PERC_INSTS = new HashSet<>(Arrays.asList(2, 3, 4));

    private static final MidiInstrument[] MIDI_INSTRUMENTS = buildMidiInstruments();
    private static final MidiDrum[] MIDI_DRUMS = buildMidiDrums();

    public static final class Options {
        public double expandMultiplier = 1;
        public boolean importDurations = false;
        public int durationSpacing = 1;
        public int trailingVelocities = 50;
        public boolean percentTrailingVelocities = true;
        public boolean fadeOutTrailingNotes = true;
        public boolean applyStereo = true;
        public boolean importVelocities = true;
        public boolean importPanning = true;
        public boolean importPitches = true;

        public Options() {}
    }

    public static final class NbsSong {
        public final Header header = new Header();
        public final List<Layer> layers = new ArrayList<>();
        public final List<Note> notes = new ArrayList<>();

        void correctData() {
            notes.sort(Comparator
                    .comparingInt((Note n) -> n.tick)
                    .thenComparingInt(n -> n.layer)
                    .thenComparingInt(n -> n.key)
                    .thenComparingInt(n -> n.inst));
            header.length = notes.isEmpty() ? 0 : notes.get(notes.size() - 1).tick + 1;
            header.height = layers.size();
            header.vaniInst = 20;
        }

        public void write(Path output) throws IOException {
            correctData();
            if (notes.isEmpty()) {
                throw new IllegalStateException("MIDI contains no convertible notes");
            }
            try (OutputStream out = Files.newOutputStream(output)) {
                LittleEndian.writeU16(out, 0);
                LittleEndian.writeU8(out, 6);              // version
                LittleEndian.writeU8(out, header.vaniInst); // vanilla instruments
                LittleEndian.writeU16(out, header.length);
                LittleEndian.writeU16(out, header.height);
                LittleEndian.writeString(out, header.name);
                LittleEndian.writeString(out, header.author);
                LittleEndian.writeString(out, header.origAuthor);
                LittleEndian.writeString(out, header.description);
                LittleEndian.writeU16(out, clampU16((int) (header.tempo * 100.0)));
                LittleEndian.writeU8(out, header.autoSave ? 1 : 0);
                LittleEndian.writeU8(out, header.autoSaveTime);
                LittleEndian.writeU8(out, header.timeSign);
                LittleEndian.writeU32(out, header.minutesSpent);
                LittleEndian.writeU32(out, header.leftClicks);
                LittleEndian.writeU32(out, header.rightClicks);
                LittleEndian.writeU32(out, header.blockAdded);
                LittleEndian.writeU32(out, header.blockRemoved);
                LittleEndian.writeString(out, header.importName);
                LittleEndian.writeU8(out, header.loop ? 1 : 0);
                LittleEndian.writeU8(out, header.loopMax);
                LittleEndian.writeU16(out, header.loopStart);

                int previousTick = -1;
                int previousLayer = -1;
                Note first = notes.get(0);
                for (Note note : notes) {
                    if (previousTick != note.tick) {
                        if (note != first) {
                            LittleEndian.writeU16(out, 0); // end previous tick's layer list
                            previousLayer = -1;
                        }
                        LittleEndian.writeU16(out, note.tick - previousTick);
                        previousTick = note.tick;
                    }
                    if (previousLayer != note.layer) {
                        LittleEndian.writeU16(out, note.layer - previousLayer);
                        previousLayer = note.layer;
                        LittleEndian.writeU8(out, note.inst);
                        LittleEndian.writeU8(out, note.key);
                        LittleEndian.writeU8(out, note.vel);
                        LittleEndian.writeU8(out, clamp(note.pan + 100, 0, 200));
                        LittleEndian.writeS16(out, note.pitch);
                    }
                }
                LittleEndian.writeU16(out, 0);
                LittleEndian.writeU16(out, 0);

                for (Layer layer : layers) {
                    LittleEndian.writeString(out, layer.name);
                    LittleEndian.writeU8(out, layer.lock ? 1 : 0);
                    LittleEndian.writeU8(out, clamp(layer.volume, 0, 100));
                    LittleEndian.writeU8(out, clamp(layer.pan + 100, 0, 200));
                }
                LittleEndian.writeU8(out, 0); // custom instrument count
            }
        }
    }

    public static final class Header {
        int vaniInst = 20;
        int length = 0;
        int height = 0;
        String name = "";
        String author = "";
        String origAuthor = "";
        String description = "";
        boolean autoSave = true;
        int autoSaveTime = 10;
        double tempo = 10.0;
        int timeSign = 4;
        int minutesSpent = 0;
        int leftClicks = 0;
        int rightClicks = 0;
        int blockAdded = 0;
        int blockRemoved = 0;
        String importName = "";
        boolean loop = false;
        int loopMax = 0;
        int loopStart = 0;
    }

    public static final class Layer {
        String name;
        boolean lock;
        int volume;
        int pan;

        Layer(String name, boolean lock, int volume) {
            this(name, lock, volume, 0);
        }
        Layer(String name, boolean lock, int volume, int pan) {
            this.name = name == null ? "" : name;
            this.lock = lock;
            this.volume = volume;
            this.pan = pan;
        }
    }

    public static final class Note {
        int tick;
        int layer;
        int inst;
        int key;
        int vel;
        int pan;
        int pitch;

        Note(int tick, int layer, int inst, int key, int vel, int pan, int pitch) {
            this.tick = tick;
            this.layer = layer;
            this.inst = inst;
            this.key = key;
            this.vel = vel;
            this.pan = pan;
            this.pitch = pitch;
        }

        boolean isPerc() { return PERC_INSTS.contains(inst); }
    }

    private static final class MidiNoteKey {
        final int note;
        final int channel;
        MidiNoteKey(int note, int channel) { this.note = note; this.channel = channel; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof MidiNoteKey)) return false;
            MidiNoteKey k = (MidiNoteKey)o;
            return note == k.note && channel == k.channel;
        }
        @Override public int hashCode() { return note * 31 + channel; }
    }

    private static final class Change {
        final long tick;
        Integer pan;
        Integer pitch;
        Change(long tick, Integer pan, Integer pitch) {
            this.tick = tick; this.pan = pan; this.pitch = pitch;
        }
    }

    private static final class PlayingNote {
        final Note note;
        int duration = 1;
        final List<Change> changes = new ArrayList<>();
        PlayingNote(Note note) { this.note = note; }
    }

    private static final class MidiInstrument {
        final String name; final int nbsInstrument; final int octaveShift; final String shortName;
        MidiInstrument(String name, int nbsInstrument, int octaveShift, String shortName) {
            this.name=name; this.nbsInstrument=nbsInstrument; this.octaveShift=octaveShift; this.shortName=shortName;
        }
    }

    private static final class MidiDrum {
        final int pitch; final int nbsInstrument; final int nbsPitch;
        MidiDrum(int pitch, int nbsInstrument, int nbsPitch) {
            this.pitch=pitch; this.nbsInstrument=nbsInstrument; this.nbsPitch=nbsPitch;
        }
    }

    /** Converts a MIDI file to NBS. Output parent directories are created automatically. */
    public static NbsSong midi2nbs(Path midiFile, Path nbsFile, Options options) throws Exception {
        Objects.requireNonNull(midiFile, "midiFile");
        Objects.requireNonNull(nbsFile, "nbsFile");
        Objects.requireNonNull(options, "options");
        if (options.durationSpacing <= 0) throw new IllegalArgumentException("durationSpacing must be > 0");
        if (options.expandMultiplier < 0) throw new IllegalArgumentException("expandMultiplier must be >= 0");

        Sequence sequence = MidiSystem.getSequence(midiFile.toFile());
        int tpb = sequence.getResolution();
        if (tpb <= 0) throw new IllegalArgumentException("Invalid MIDI ticks-per-beat: " + tpb);

        final int timeSign = 4;
        double tempo = -1;
        int detectedTimeSign = 4;

        List<MidiMessageWithTick> merged = mergeTracks(sequence);
        for (MidiMessageWithTick e : merged) {
            MidiMessage msg = e.message;
            if (!(msg instanceof MetaMessage)) continue;
            MetaMessage meta = (MetaMessage) msg;
            if (meta.getType() == 0x58 && meta.getData().length >= 1) {
                detectedTimeSign = meta.getData()[0] & 0xFF;
            } else if (meta.getType() == 0x51 && tempo < 0 && meta.getData().length >= 3) {
                int mpqn = ((meta.getData()[0] & 0xFF) << 16)
                        | ((meta.getData()[1] & 0xFF) << 8)
                        | (meta.getData()[2] & 0xFF);
                tempo = 60_000_000.0 / mpqn;
            }
        }
        if (tempo < 0) tempo = 120.0;

        boolean autoExpand = options.expandMultiplier == 0;
        double expandMultiplier = autoExpand ? 1.0 : options.expandMultiplier;
        if (autoExpand) {
            long notePosGcd = -1;
            for (MidiMessageWithTick e : merged) {
                ShortMessage sm = asShort(e.message);
                if (sm == null) continue;
                int command = sm.getCommand();
                if (command == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                    int notePos = pyRound(e.tick * (double) timeSign * NOTE_POS_MULTIPLIER / tpb);
                    if ((notePos & 1) != 0) notePos++;
                    notePosGcd = notePosGcd == -1 ? notePos : gcd(notePosGcd, notePos);
                }
            }
            expandMultiplier = notePosGcd > 0 ? (NOTE_POS_MULTIPLIER / (int) notePosGcd) : 1;
            if (expandMultiplier <= 0) expandMultiplier = 1.0;
        }

        NbsSong nbs = new NbsSong();
        nbs.header.importName = midiFile.getFileName().toString();
        nbs.header.timeSign = (int) Math.round(detectedTimeSign * expandMultiplier);
        nbs.header.tempo = tempo * timeSign / 60.0 * expandMultiplier;

        boolean[] emptyTracks = new boolean[sequence.getTracks().length];
        boolean[] percussionTracks = new boolean[sequence.getTracks().length];
        for (int i = 0; i < sequence.getTracks().length; i++) {
            Track track = sequence.getTracks()[i];
            boolean hasNoteOn = false;
            boolean percOnly = true;
            for (int j = 0; j < track.size(); j++) {
                ShortMessage sm = asShort(track.get(j).getMessage());
                if (sm != null && sm.getCommand() == ShortMessage.NOTE_ON) {
                    hasNoteOn = true;
                    if (sm.getChannel() != 9) percOnly = false;
                }
            }
            emptyTracks[i] = !hasNoteOn;
            percussionTracks[i] = percOnly;
        }

        int baseLayer = -1;
        int ceilingLayer = -1;
        for (int i = 0; i < sequence.getTracks().length; i++) {
            if (emptyTracks[i]) continue;
            Track track = sequence.getTracks()[i];
            long absTime = 0;
            int trackInst = 0;
            int keyShift = 0;
            int trackVel = 100;
            boolean isPerc = percussionTracks[i];
            String trackName = isPerc ? "Percussion" : "";
            if (isPerc) nbs.layers.add(new Layer(trackName, false, trackVel));

            int pan = 0;
            int pitch = 0;
            baseLayer = ceilingLayer + 1;
            int layer = baseLayer;
            int lastTick = -1;
            boolean isNoteEnd = false;
            Map<MidiNoteKey, PlayingNote> playingNotes = new HashMap<>();
            List<Note> currentNotes = new ArrayList<>();
            int innerBaseLayer = baseLayer;

            for (int j = 0; j < track.size(); j++) {
                MidiEvent event = track.get(j);
                MidiMessage msg = event.getMessage();
                absTime = event.getTick();
                long tick = scaleTick(absTime, timeSign, tpb, expandMultiplier);

                if (msg instanceof MetaMessage) {
                    MetaMessage meta = (MetaMessage) msg;
                    if (meta.getType() == 0x03 && trackName.isEmpty()) {
                        trackName = decodeAscii(meta.getData());
                        nbs.layers.add(new Layer(trackName, false, trackVel));
                    }
                    continue;
                }

                ShortMessage sm = asShort(msg);
                if (sm == null) continue;

                int command = sm.getCommand();
                int channel = sm.getChannel();
                int data1 = sm.getData1();
                int data2 = sm.getData2();

                if (command == ShortMessage.NOTE_ON) {
                    KeyInst ki = extractKeyAndInst(data1, channel, trackInst, keyShift);
                    if (ki == null) continue;
                    int velocity = options.importVelocities ? (int)(data2 * 100L / 127L) : 100;
                    if (data2 > 0 && velocity > 0) {
                        boolean enoughSpace;
                        if (options.importDurations && !PERC_INSTS.contains(ki.inst)) {
                            enoughSpace = playingNotes.isEmpty();
                        } else {
                            enoughSpace = tick != lastTick;
                        }
                        if (!enoughSpace) {
                            layer++;
                            while (layer >= nbs.layers.size()) {
                                nbs.layers.add(new Layer(trackName + " (" + (layer - innerBaseLayer + 1) + ")", false, trackVel));
                            }
                        } else {
                            layer = innerBaseLayer;
                        }
                        Note note = new Note(safeInt(tick), layer, ki.inst, ki.key, velocity, pan, pitch);
                        nbs.notes.add(note);
                        currentNotes.add(note);
                        if (options.importDurations) {
                            playingNotes.put(new MidiNoteKey(data1, channel), new PlayingNote(note));
                        }
                        ceilingLayer = Math.max(ceilingLayer, layer);
                        lastTick = safeInt(tick);
                    } else if (options.importDurations && data2 == 0) {
                        PlayingNote playing = playingNotes.get(new MidiNoteKey(data1, channel));
                        if (playing != null) {
                            playing.duration = Math.max(0, safeInt(tick) - playing.note.tick);
                            isNoteEnd = true;
                        }
                    }
                } else if (options.importDurations && command == ShortMessage.NOTE_OFF) {
                    PlayingNote playing = playingNotes.get(new MidiNoteKey(data1, channel));
                    if (playing != null) {
                        playing.duration = Math.max(0, safeInt(tick) - playing.note.tick);
                        isNoteEnd = true;
                    }
                } else if (command == ShortMessage.PROGRAM_CHANGE && !isPerc) {
                    MidiInstrument mi = MIDI_INSTRUMENTS[Math.min(127, data1)];
                    trackInst = mi.nbsInstrument;
                    if (trackInst == -1) trackInst = 0;
                    keyShift = mi.octaveShift * 12;
                    if (trackName.isEmpty()) {
                        trackName = !mi.shortName.isEmpty() ? mi.shortName : mi.name;
                        nbs.layers.add(new Layer(trackName, false, trackVel));
                    }
                } else if (command == ShortMessage.CONTROL_CHANGE) {
                    if (options.importPanning && data1 == 10) {
                        pan = (int) interp(data2, 0, 127, -100, 100);
                        for (PlayingNote playing : playingNotes.values()) {
                            if (playing.note.tick == tick) playing.note.pan = pan;
                            else playing.changes.add(new Change(tick, pan, null));
                        }
                    } else if (options.importVelocities && data1 == 7) {
                        trackVel = data2 * 100 / 127;
                    }
                } else if (options.importPitches && msg instanceof ShortMessage && command == 0xE0) {
                    int bend = (data1 | (data2 << 7)) - 8192;
                    pitch = (int)(bend / NBS_PITCH_IN_MIDI_PITCHBEND);
                    for (PlayingNote playing : playingNotes.values()) {
                        if (playing.note.tick == tick) playing.note.pitch = pitch;
                        else playing.changes.add(new Change(tick, null, pitch));
                    }
                }

                if (isNoteEnd && !currentNotes.isEmpty()) {
                    MidiNoteKey key = new MidiNoteKey(data1, channel);
                    PlayingNote playing = playingNotes.get(key);
                    if (playing != null) {
                        KeyInst ki = extractKeyAndInst(data1, channel, trackInst, keyShift);
                        int duration = playing.duration;
                        if (ki != null && duration > 1 && !playing.note.isPerc()) {
                            for (Note trailing : generateTrailingNotes(playing, safeInt(tick), options)) {
                                nbs.notes.add(trailing);
                            }
                            currentNotes.remove(playing.note);
                        }
                        playingNotes.remove(key);
                        layer--;
                    }
                    isNoteEnd = false;
                }
            }
        }

        nbs.correctData();
        Files.createDirectories(nbsFile.toAbsolutePath().getParent() == null
                ? Paths.get(".") : nbsFile.toAbsolutePath().getParent());
        nbs.write(nbsFile);
        return nbs;
    }

    private static Iterable<Note> generateTrailingNotes(PlayingNote playing, int endTick, Options o) {
        List<Note> result = new ArrayList<>();
        boolean odd = true;
        Note base = playing.note;
        int duration = playing.duration;
        int pan = base.pan;
        int pitch = base.pitch;
        for (int index = 1; index < duration; index++) {
            int newTick = endTick - duration + index;
            if (index % o.durationSpacing != 0) continue;
            int vel = base.vel;
            if (o.fadeOutTrailingNotes) {
                vel = (int)(vel * (duration - index) / (double)duration);
            } else if (o.percentTrailingVelocities) {
                vel = base.vel * o.trailingVelocities / 100;
            } else {
                vel = o.trailingVelocities;
            }
            vel = clamp(vel, 0, 100);

            Change change = findMidNoteChange(playing.changes, newTick);
            if (o.applyStereo) {
                pan = base.pan;
                if (odd) pan = (int)(pan - TRAILING_NOTE_STEREO_PAN * (100 - Math.abs(pan)) / 100.0);
                else pan = (int)(pan + TRAILING_NOTE_STEREO_PAN * (100 - Math.abs(pan)) / 100.0);
                if (base.vel > 50) pan = (int)(pan * (1 - (duration - index) / (double)duration));
            } else if (change != null && change.pan != null) {
                pan = change.pan;
            }
            pan = clamp(pan, -100, 100);
            if (change != null && change.pitch != null) pitch = change.pitch;
            odd = !odd;
            if (vel > 1) result.add(new Note(newTick, base.layer, base.inst, base.key, vel, pan, pitch));
        }
        return result;
    }

    private static Change findMidNoteChange(List<Change> changes, long tick) {
        if (changes.isEmpty() || tick < changes.get(0).tick) return null;
        for (int i = 0; i < changes.size() - 1; i++) {
            Change c = changes.get(i), next = changes.get(i + 1);
            if (c.tick <= tick && tick < next.tick) return c;
        }
        return null; // same behavior as the Python implementation for the final entry
    }

    private static KeyInst extractKeyAndInst(int midiNote, int channel, int trackInst, int keyShift) {
        if (channel == 9) {
            MidiDrum drum = MIDI_DRUMS[Math.max(0, Math.min(MIDI_DRUMS.length - 1, midiNote - 24))];
            // Python falls back to drum 27 when note is outside the table.
            if (midiNote < 24 || midiNote > 87) drum = findDrum(27);
            if (drum == null || drum.nbsInstrument == -1) return null;
            return new KeyInst(drum.nbsPitch + 36, drum.nbsInstrument);
        }
        return new KeyInst(Math.max(0, (midiNote - 21) + keyShift), trackInst);
    }

    private static MidiDrum findDrum(int pitch) {
        for (MidiDrum d : MIDI_DRUMS) if (d.pitch == pitch) return d;
        return null;
    }

    private static final class KeyInst {
        final int key; final int inst;
        KeyInst(int key, int inst) { this.key=key; this.inst=inst; }
    }

    private static final class MidiMessageWithTick {
        final long tick; final MidiMessage message;
        MidiMessageWithTick(long tick, MidiMessage message) { this.tick=tick; this.message=message; }
    }

    private static List<MidiMessageWithTick> mergeTracks(Sequence sequence) {
        List<MidiMessageWithTick> out = new ArrayList<>();
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent e = track.get(i);
                out.add(new MidiMessageWithTick(e.getTick(), e.getMessage()));
            }
        }
        out.sort(Comparator.comparingLong(e -> e.tick));
        return out;
    }

    private static ShortMessage asShort(MidiMessage msg) {
        return (msg instanceof ShortMessage) ? (ShortMessage) msg : null;
    }

    private static long scaleTick(long absTime, int timeSign, int tpb, double expand) {
        double v = absTime * (double) timeSign / tpb * expand;
        return pyRound(v);
    }

    private static int pyRound(double v) {
        double floor = Math.floor(v);
        double frac = v - floor;
        if (frac > 0.5) return (int) Math.ceil(v);
        if (frac < 0.5) return (int) floor;
        long f = (long) floor;
        return (int) ((f & 1L) == 0 ? f : f + 1);
    }

    private static long gcd(long a, long b) {
        a = Math.abs(a); b = Math.abs(b);
        while (b != 0) { long t = a % b; a = b; b = t; }
        return a;
    }

    private static double interp(double x, double a, double b, double c, double d) {
        if (b == a) return c;
        return c + (x - a) * (d - c) / (b - a);
    }

    private static String decodeAscii(byte[] data) {
        int len = 0;
        while (len < data.length && data[len] != 0) len++;
        return new String(data, 0, len, StandardCharsets.ISO_8859_1);
    }

    private static int safeInt(long v) {
        if (v > Integer.MAX_VALUE) throw new ArithmeticException("NBS tick exceeds signed int range: " + v);
        return (int) v;
    }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private static int clampU16(int v) { return clamp(v, 0, 0xFFFF); }

    private static final class LittleEndian {
        static void writeU8(OutputStream out, int v) throws IOException { out.write(v & 0xFF); }
        static void writeU16(OutputStream out, int v) throws IOException { out.write(v & 0xFF); out.write((v >>> 8) & 0xFF); }
        static void writeS16(OutputStream out, int v) throws IOException { writeU16(out, v); }
        static void writeU32(OutputStream out, int v) throws IOException {
            out.write(v & 0xFF); out.write((v >>> 8) & 0xFF); out.write((v >>> 16) & 0xFF); out.write((v >>> 24) & 0xFF);
        }
        static void writeString(OutputStream out, String s) throws IOException {
            if (s == null) s = "";
            byte[] bytes = s.replaceAll("[^\\x00-\\x7F]", "?").getBytes(StandardCharsets.US_ASCII);
            writeU32(out, bytes.length);
            out.write(bytes);
        }
    }

    private static MidiInstrument[] buildMidiInstruments() {
        // Exactly the mapping used by NBSTool/common.py.
        String[] n = {
                "Acoustic Grand Piano","Bright Acoustic Piano","Electric Grand Piano","Honky-tonk Piano","Electric Piano 1","Electric Piano 2","Harpsichord","Clavinet","Celesta","Glockenspiel","Music Box","Vibraphone","Marimba","Xylophone","Tubular Bells","Dulcimer",
                "Drawbar Organ","Percussive Organ","Rock Organ","Church Organ","Reed Organ","Accordion","Harmonica","Tango Accordion","Acoustic Guitar (nylon)","Acoustic Guitar (steel)","Electric Guitar (jazz)","Electric Guitar (clean)","Electric Guitar (muted)","Overdriven Guitar","Distortion Guitar","Guitar Harmonics",
                "Acoustic Bass","Electric Bass (finger)","Electric Bass (pick)","Fretless Bass","Slap Bass 1","Slap Bass 2","Synth Bass 1","Synth Bass 2","Violin","Viola","Cello","Contrabass","Tremolo Strings","Pizzicato Strings","Orchestral Harp","Timpani",
                "String Ensemble 1","String Ensemble 2","Synth Strings 1","Synth Strings 2","Choir Aahs","Voice Oohs","Synth Choir","Orchestra Hit","Trumpet","Trombone","Tuba","Muted Trumpet","French Horn","Brass Section","Synth Brass 1","Synth Brass 2",
                "Soprano Sax","Alto Sax","Tenor Sax","Baritone Sax","Oboe","English Horn","Bassoon","Clarinet","Piccolo","Flute","Recorder","Pan Flute","Blown Bottle","Shakuhachi","Whistle","Ocarina",
                "Lead 1 (square)","Lead 2 (sawtooth)","Lead 3 (calliope)","Lead 4 (chiff)","Lead 5 (charang)","Lead 6 (voice)","Lead 7 (fifths)","Lead 8 (bass + lead)",
                "Pad 1 (new age)","Pad 2 (warm)","Pad 3 (polysynth)","Pad 4 (choir)","Pad 5 (bowed)","Pad 6 (metallic)","Pad 7 (halo)","Pad 8 (sweep)",
                "FX 1 (rain)","FX 2 (soundtrack)","FX 3 (crystal)","FX 4 (atmosphere)","FX 5 (brightness)","FX 6 (goblins)","FX 7 (echoes)","FX 8 (sci-fi)",
                "Sitar","Banjo","Shamisen","Koto","Kalimba","Bag pipe","Fiddle","Shanai","Tinkle Bell","Agogo","Steel Drums","Woodblock","Taiko Drum","Melodic Tom","Synth Drum","Reverse Cymbal","Guitar Fret Noise","Breath Noise","Seashore","Bird Tweet","Telephone Ring","Helicopter","Applause","Gunshot","Percussion"
        };
        int[] inst = {0,15,15,15,15,15,5,14,7,7,7,10,10,9,7,5,6,10,6,6,6,6,6,6,5,5,0,5,1,12,12,5,1,1,1,1,5,5,1,15,6,6,6,6,6,1,0,3,6,6,6,6,6,6,6,3,16,18,17,16,6,12,12,6,19,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,13,6,6,6,5,6,6,1,7,6,6,6,6,6,6,6,8,8,6,8,5,15,6,6,6,14,14,14,5,10,6,6,6,8,11,10,9,2,3,3,8,4,6,8,6,2,3,3,-1};
        int[] shift = {
                0,0,0,0,0,0,1,0,-2,-2,-2,0,0,-2,-2,1,
                -1,0,-1,-1,-1,-1,-1,-1,1,1,0,1,2,2,2,3,
                2,2,2,2,1,1,2,0,-1,-1,-1,-1,-1,2,0,0,
                -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,2,-1,2,-1,-1,
                -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
                0,-1,-1,-1,1,-1,-1,2,-2,-1,-1,-1,-1,-1,-1,-2,
                -2,-1,-2,1,0,-1,-1,-1,0,0,0,1,0,-1,-1,-1,
                -2,-1,0,-2,0,0,0,-2,1,-1,-2,1,2,0,0,0
        };
        String[] shortNames = new String[128];
        // Populate the non-empty short names from common.py.
        String[] pairs = {
                "0=Grand Piano","1=Acoustic Piano","2=E. Grand Piano","3=H.T. Piano","4=E. Piano 1","5=E. Piano 2","14=D. Organ","23=Bandoneon","24=A.Guitar (nylon)","25=A.Guitar (steel)","26=E.Guitar (jazz)","27=E.Guitar (clean)","28=E.Guitar (mute)","29=OD Guitar","30=Dist. Guitar","31=Guitar H.","32=A. Bass","33=E.Bass (finger)","34=E.Bass (pick)","38=P. Strings","39=O. Harp","41=T. Strings","45=P. Strings","48=String E. 1","49=String E. 2","50=S. Strings 1","51=S. Strings 2","55=O. Hit","62=T. Bells","80=L.1 (square)","81=L.2 (sawtooth)","82=L.3 (calliope)","83=L.4 (chiff)","84=L.5 (charang)","85=L.6 (voice)","86=L.7 (fifths)","87=L.8 (bass + lead)","88=P.1 (new age)","89=P.2 (warm)","90=P.3 (polysynth)","91=P.4 (choir)","92=P.5 (bowed)","93=P.6 (metallic)","94=P.7 (halo)","95=P.8 (sweep)","96=Fx (rain)","97=Fx (strack)","98=Fx (crystal)","99=Fx (atmosph.)","100=Fx (bright.)","101=Fx (goblins)","102=Fx (echoes)","103=Fx (sci-fi)","111=Telephone","118=Rev. Cymbal","119=Guitar F. Noise"};
        for (String p : pairs) { int eq=p.indexOf('='); shortNames[Integer.parseInt(p.substring(0,eq))]=p.substring(eq+1); }
        MidiInstrument[] out = new MidiInstrument[128];
        for (int i=0;i<128;i++) out[i]=new MidiInstrument(n[i],inst[i],shift[i],shortNames[i]==null?"":shortNames[i]);
        return out;
    }

    private static MidiDrum[] buildMidiDrums() {
        int[] inst = {-1,3,4,3,3,4,4,4,4,4,4,2,2,4,4,3,3,3,2,3,2,2,3,3,2,3,4,4,4,4,4,4,3,4,3,4,2,2,2,3,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2};
        int[] pitch = {0,8,25,18,27,16,13,9,6,2,17,10,6,6,22,13,15,18,20,23,17,23,24,8,13,18,1,13,2,13,9,2,8,15,13,8,3,20,23,23,17,11,18,10,5,4,16,16,22,22,0,0,21,14,7,0,0,0,0,0,0,0,0,0};
        int[] midi = new int[64];
        for (int i=0;i<64;i++) midi[i]=24+i;
        MidiDrum[] out = new MidiDrum[64];
        for (int i=0;i<64;i++) out[i]=new MidiDrum(midi[i],inst[i],pitch[i]);
        return out;
    }

    /** Minimal CLI: java Midi2Nbs input.mid output.nbs */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java Midi2Nbs <input.mid> <output.nbs>");
            System.exit(2);
        }
        midi2nbs(Paths.get(args[0]), Paths.get(args[1]), new Options());
        System.out.println("Converted: " + args[0] + " -> " + args[1]);
    }
}
