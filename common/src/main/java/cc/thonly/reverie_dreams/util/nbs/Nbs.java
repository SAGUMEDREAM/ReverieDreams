package cc.thonly.reverie_dreams.util.nbs;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * NBS (Note Block Studio) file reader/writer.
 *
 * Based on the Python implementation provided by the user.
 */
public class Nbs {

    public static final int CURRENT_NBS_VERSION = 5;

    /**
     * Python:
     * value.encode("cp1252")
     */
    private static final Charset CP1252 = Charset.forName("windows-1252");

    // ============================================================
    // Data classes
    // ============================================================

    public static class Instrument {
        public int id;
        public String name;
        public String file;
        public int pitch;
        public boolean pressKey;

        public Instrument(int id, String name, String file) {
            this(id, name, file, 45, true);
        }

        public Instrument(
                int id,
                String name,
                String file,
                int pitch,
                boolean pressKey
        ) {
            this.id = id;
            this.name = name;
            this.file = file;
            this.pitch = pitch;
            this.pressKey = pressKey;
        }

        @Override
        public String toString() {
            return "Instrument{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", file='" + file + '\'' +
                    ", pitch=" + pitch +
                    ", pressKey=" + pressKey +
                    '}';
        }
    }

    public static class Note {
        public int tick;
        public int layer;
        public int instrument;
        public int key;
        public int velocity;
        public int panning;
        public int pitch;

        public Note(
                int tick,
                int layer,
                int instrument,
                int key
        ) {
            this(tick, layer, instrument, key, 100, 0, 0);
        }

        public Note(
                int tick,
                int layer,
                int instrument,
                int key,
                int velocity,
                int panning,
                int pitch
        ) {
            this.tick = tick;
            this.layer = layer;
            this.instrument = instrument;
            this.key = key;
            this.velocity = velocity;
            this.panning = panning;
            this.pitch = pitch;
        }

        @Override
        public String toString() {
            return "Note{" +
                    "tick=" + tick +
                    ", layer=" + layer +
                    ", instrument=" + instrument +
                    ", key=" + key +
                    ", velocity=" + velocity +
                    ", panning=" + panning +
                    ", pitch=" + pitch +
                    '}';
        }
    }

    public static class Layer {
        public int id;
        public String name;
        public boolean lock;
        public int volume;
        public int panning;

        public Layer(int id) {
            this(id, "", false, 100, 0);
        }

        public Layer(
                int id,
                String name,
                boolean lock,
                int volume,
                int panning
        ) {
            this.id = id;
            this.name = name;
            this.lock = lock;
            this.volume = volume;
            this.panning = panning;
        }

        @Override
        public String toString() {
            return "Layer{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", lock=" + lock +
                    ", volume=" + volume +
                    ", panning=" + panning +
                    '}';
        }
    }

    public static class Header {
        public int version = CURRENT_NBS_VERSION;
        public int defaultInstruments = 16;
        public int songLength = 0;
        public int songLayers = 0;

        public String songName = "";
        public String songAuthor = "";
        public String originalAuthor = "";
        public String description = "";

        public double tempo = 10.0;

        public boolean autoSave = false;
        public int autoSaveDuration = 10;
        public int timeSignature = 4;

        public long minutesSpent = 0;
        public long leftClicks = 0;
        public long rightClicks = 0;
        public long blocksAdded = 0;
        public long blocksRemoved = 0;

        public String songOrigin = "";

        public boolean loop = false;
        public int maxLoopCount = 0;
        public int loopStart = 0;

        public Header() {
        }

        public Header(Header other) {
            this.version = other.version;
            this.defaultInstruments = other.defaultInstruments;
            this.songLength = other.songLength;
            this.songLayers = other.songLayers;
            this.songName = other.songName;
            this.songAuthor = other.songAuthor;
            this.originalAuthor = other.originalAuthor;
            this.description = other.description;
            this.tempo = other.tempo;
            this.autoSave = other.autoSave;
            this.autoSaveDuration = other.autoSaveDuration;
            this.timeSignature = other.timeSignature;
            this.minutesSpent = other.minutesSpent;
            this.leftClicks = other.leftClicks;
            this.rightClicks = other.rightClicks;
            this.blocksAdded = other.blocksAdded;
            this.blocksRemoved = other.blocksRemoved;
            this.songOrigin = other.songOrigin;
            this.loop = other.loop;
            this.maxLoopCount = other.maxLoopCount;
            this.loopStart = other.loopStart;
        }
    }

    /**
     * 相当于 Python 中:
     *
     * for tick, chord in nbs_file:
     *
     * 这里把它表示为一个对象。
     */
    public static class Chord {
        public final int tick;
        public final List<Note> notes;

        public Chord(int tick, List<Note> notes) {
            this.tick = tick;
            this.notes = notes;
        }
    }

    /**
     * 相当于 Python 的 File。
     */
    public static class NbsFile implements Iterable<Chord> {

        public Header header;
        public List<Note> notes;
        public List<Layer> layers;
        public List<Instrument> instruments;

        public NbsFile(
                Header header,
                List<Note> notes,
                List<Layer> layers,
                List<Instrument> instruments
        ) {
            this.header = header;
            this.notes = notes;
            this.layers = layers;
            this.instruments = instruments;
        }

        /**
         * Python:
         *
         * def update_header(self, version):
         */
        public void updateHeader(int version) {
            header.version = version;

            if (!notes.isEmpty()) {
                // Python 使用 self.notes[-1].tick
                header.songLength = notes.get(notes.size() - 1).tick;
            }

            header.songLayers = layers.size();
        }

        /**
         * Python:
         *
         * def save(self, filename, version=CURRENT_NBS_VERSION)
         */
        public void save(String filename) throws IOException {
            save(filename, CURRENT_NBS_VERSION);
        }

        public void save(String filename, int version) throws IOException {
            updateHeader(version);

            try (OutputStream output = new BufferedOutputStream(
                    new FileOutputStream(filename)
            )) {
                new Writer(output).encodeFile(this, version);
            }
        }

        /**
         * Java 中为了保持 Iterable，
         * 这里返回按照 tick 分组后的 chord。
         */
        @Override
        public Iterator<Chord> iterator() {
            if (notes.isEmpty()) {
                return Collections.emptyIterator();
            }

            // Python:
            // sorted(self.notes, key=lambda n: n.tick)
            List<Note> sortedNotes = new ArrayList<>(notes);

            sortedNotes.sort(
                    Comparator.comparingInt(n -> n.tick)
            );

            List<Chord> chords = new ArrayList<>();

            int currentTick = sortedNotes.get(0).tick;
            List<Note> currentChord = new ArrayList<>();

            for (Note note : sortedNotes) {
                if (note.tick == currentTick) {
                    currentChord.add(note);
                } else {
                    // Python:
                    // chord.sort(key=lambda n: n.layer)
                    currentChord.sort(
                            Comparator.comparingInt(n -> n.layer)
                    );

                    chords.add(
                            new Chord(
                                    currentTick,
                                    currentChord
                            )
                    );

                    currentTick = note.tick;
                    currentChord = new ArrayList<>();
                    currentChord.add(note);
                }
            }

            currentChord.sort(
                    Comparator.comparingInt(n -> n.layer)
            );

            chords.add(
                    new Chord(
                            currentTick,
                            currentChord
                    )
            );

            return chords.iterator();
        }
    }

    // ============================================================
    // Public API
    // ============================================================

    /**
     * Python:
     *
     * def read(filename):
     *     with open(filename, "rb") as fileobj:
     *         return Parser(fileobj).read_file()
     */
    public static NbsFile read(String filename) throws IOException {
        try (InputStream input = new BufferedInputStream(
                new FileInputStream(filename)
        )) {
            return new Parser(input).readFile();
        }
    }

    /**
     * Python:
     *
     * def new_file(**header):
     *     return File(
     *         Header(**header),
     *         [],
     *         [Layer(0, "", False, 100, 0)],
     *         []
     *     )
     */
    public static NbsFile newFile() {
        Header header = new Header();

        List<Layer> layers = new ArrayList<>();
        layers.add(
                new Layer(
                        0,
                        "",
                        false,
                        100,
                        0
                )
        );

        return new NbsFile(
                header,
                new ArrayList<>(),
                layers,
                new ArrayList<>()
        );
    }

    // ============================================================
    // Parser
    // ============================================================

    public static class Parser {

        private final InputStream input;

        public Parser(InputStream input) {
            this.input = input;
        }

        public NbsFile readFile() throws IOException {

            Header header = parseHeader();
            int version = header.version;

            List<Note> notes = new ArrayList<>();
            for (Note note : parseNotes(version)) {
                notes.add(note);
            }

            List<Layer> layers = new ArrayList<>();
            for (Layer layer : parseLayers(header.songLayers, version)) {
                layers.add(layer);
            }

            List<Instrument> instruments = new ArrayList<>();
            for (Instrument instrument : parseInstruments(version)) {
                instruments.add(instrument);
            }

            return new NbsFile(
                    header,
                    notes,
                    layers,
                    instruments
            );
        }

        // --------------------------------------------------------
        // Numeric readers
        // --------------------------------------------------------

        /**
         * BYTE = Struct("<B")
         * Python returns 0..255.
         */
        private int readByte() throws IOException {
            int value = input.read();

            if (value < 0) {
                throw new EOFException("Unexpected end of file");
            }

            return value;
        }

        /**
         * SHORT = Struct("<H")
         *
         * unsigned little endian short
         * 0..65535
         */
        private int readUnsignedShortLE() throws IOException {

            int b0 = readByte();
            int b1 = readByte();

            return b0 | (b1 << 8);
        }

        /**
         * SSHORT = Struct("<h")
         *
         * signed little endian short
         * -32768..32767
         */
        private short readShortLE() throws IOException {

            int b0 = readByte();
            int b1 = readByte();

            return (short) (b0 | (b1 << 8));
        }

        /**
         * INT = Struct("<I")
         *
         * Python unsigned int.
         * Java long 用来保存 0..4294967295。
         */
        private long readUnsignedIntLE() throws IOException {

            long b0 = readByte();
            long b1 = readByte();
            long b2 = readByte();
            long b3 = readByte();

            return b0
                    | (b1 << 8)
                    | (b2 << 16)
                    | (b3 << 24);
        }

        // --------------------------------------------------------
        // Strings
        // --------------------------------------------------------

        private String readString() throws IOException {

            long length = readUnsignedIntLE();

            if (length > Integer.MAX_VALUE) {
                throw new IOException(
                        "String too large: " + length
                );
            }

            byte[] data = readFully((int) length);

            return new String(data, CP1252);
        }

        private byte[] readFully(int length) throws IOException {

            byte[] data = new byte[length];

            int offset = 0;

            while (offset < length) {

                int count = input.read(
                        data,
                        offset,
                        length - offset
                );

                if (count < 0) {
                    throw new EOFException(
                            "Unexpected end of file"
                    );
                }

                offset += count;
            }

            return data;
        }

        // --------------------------------------------------------
        // jump()
        // --------------------------------------------------------

        /**
         * Python:
         *
         * value = -1
         * while True:
         *     jump = read_numeric(SHORT)
         *     if not jump:
         *         break
         *     value += jump
         *     yield value
         */
        private Iterable<Integer> jump() throws IOException {

            List<Integer> result = new ArrayList<>();

            int value = -1;

            while (true) {

                int jump = readUnsignedShortLE();

                if (jump == 0) {
                    break;
                }

                value += jump;
                result.add(value);
            }

            return result;
        }

        // --------------------------------------------------------
        // Header
        // --------------------------------------------------------

        private Header parseHeader() throws IOException {

            int songLength = readUnsignedShortLE();

            int version;

            if (songLength == 0) {
                // Open Note Block Studio format
                version = readByte();
            } else {
                version = 0;
            }

            Header header = new Header();

            header.version = version;

            header.defaultInstruments =
                    version > 0
                            ? readByte()
                            : 10;

            header.songLength =
                    version >= 3
                            ? readUnsignedShortLE()
                            : songLength;

            header.songLayers =
                    readUnsignedShortLE();

            header.songName =
                    readString();

            header.songAuthor =
                    readString();

            header.originalAuthor =
                    readString();

            header.description =
                    readString();

            header.tempo =
                    readUnsignedShortLE() / 100.0;

            header.autoSave =
                    readByte() == 1;

            header.autoSaveDuration =
                    readByte();

            header.timeSignature =
                    readByte();

            header.minutesSpent =
                    readUnsignedIntLE();

            header.leftClicks =
                    readUnsignedIntLE();

            header.rightClicks =
                    readUnsignedIntLE();

            header.blocksAdded =
                    readUnsignedIntLE();

            header.blocksRemoved =
                    readUnsignedIntLE();

            header.songOrigin =
                    readString();

            if (version >= 4) {

                header.loop =
                        readByte() == 1;

                header.maxLoopCount =
                        readByte();

                header.loopStart =
                        readUnsignedShortLE();

            } else {

                header.loop = false;
                header.maxLoopCount = 0;
                header.loopStart = 0;
            }

            return header;
        }

        // --------------------------------------------------------
        // Notes
        // --------------------------------------------------------

        private Iterable<Note> parseNotes(int version)
                throws IOException {

            List<Note> result = new ArrayList<>();

            for (int currentTick : jump()) {

                for (int currentLayer : jump()) {

                    int instrument =
                            readByte();

                    int key =
                            readByte();

                    int velocity =
                            version >= 4
                                    ? readByte()
                                    : 100;

                    int panning =
                            version >= 4
                                    ? readByte() - 100
                                    : 0;

                    int pitch =
                            version >= 4
                                    ? readShortLE()
                                    : 0;

                    result.add(
                            new Note(
                                    currentTick,
                                    currentLayer,
                                    instrument,
                                    key,
                                    velocity,
                                    panning,
                                    pitch
                            )
                    );
                }
            }

            return result;
        }

        // --------------------------------------------------------
        // Layers
        // --------------------------------------------------------

        private Iterable<Layer> parseLayers(
                int layersCount,
                int version
        ) throws IOException {

            List<Layer> result = new ArrayList<>();

            for (int i = 0; i < layersCount; i++) {

                String name =
                        readString();

                boolean lock =
                        version >= 4
                                && readByte() == 1;

                int volume =
                        readByte();

                int panning =
                        version >= 2
                                ? readByte() - 100
                                : 0;

                result.add(
                        new Layer(
                                i,
                                name,
                                lock,
                                volume,
                                panning
                        )
                );
            }

            return result;
        }

        // --------------------------------------------------------
        // Instruments
        // --------------------------------------------------------

        private Iterable<Instrument> parseInstruments(
                int version
        ) throws IOException {

            List<Instrument> result =
                    new ArrayList<>();

            int count = readByte();

            for (int i = 0; i < count; i++) {

                String name =
                        readString();

                String soundFile =
                        readString();

                int pitch =
                        readByte();

                boolean pressKey =
                        readByte() == 1;

                result.add(
                        new Instrument(
                                i,
                                name,
                                soundFile,
                                pitch,
                                pressKey
                        )
                );
            }

            return result;
        }
    }

    // ============================================================
    // Writer
    // ============================================================

    public static class Writer {

        private final OutputStream output;

        public Writer(OutputStream output) {
            this.output = output;
        }

        public void encodeFile(
                NbsFile nbsFile,
                int version
        ) throws IOException {

            writeHeader(nbsFile, version);
            writeNotes(nbsFile, version);
            writeLayers(nbsFile, version);
            writeInstruments(nbsFile, version);
        }

        // --------------------------------------------------------
        // Numeric writers
        // --------------------------------------------------------

        private void writeByte(int value)
                throws IOException {

            output.write(value & 0xFF);
        }

        private void writeUnsignedShortLE(int value)
                throws IOException {

            output.write(value & 0xFF);
            output.write((value >>> 8) & 0xFF);
        }

        private void writeShortLE(int value)
                throws IOException {

            output.write(value & 0xFF);
            output.write((value >>> 8) & 0xFF);
        }

        private void writeUnsignedIntLE(long value)
                throws IOException {

            output.write((int) (value & 0xFF));
            output.write((int) ((value >>> 8) & 0xFF));
            output.write((int) ((value >>> 16) & 0xFF));
            output.write((int) ((value >>> 24) & 0xFF));
        }

        // --------------------------------------------------------
        // String
        // --------------------------------------------------------

        private void writeString(String value)
                throws IOException {

            if (value == null) {
                value = "";
            }

            byte[] data =
                    value.getBytes(CP1252);

            writeUnsignedIntLE(data.length);

            output.write(data);
        }

        // --------------------------------------------------------
        // Header
        // --------------------------------------------------------

        private void writeHeader(
                NbsFile nbsFile,
                int version
        ) throws IOException {

            Header header =
                    nbsFile.header;

            if (version > 0) {

                // SHORT = 0
                writeUnsignedShortLE(0);

                // BYTE version
                writeByte(version);

                // BYTE default instruments
                writeByte(header.defaultInstruments);

            } else {

                writeUnsignedShortLE(
                        header.songLength
                );
            }

            if (version >= 3) {

                writeUnsignedShortLE(
                        header.songLength
                );
            }

            writeUnsignedShortLE(
                    header.songLayers
            );

            writeString(
                    header.songName
            );

            writeString(
                    header.songAuthor
            );

            writeString(
                    header.originalAuthor
            );

            writeString(
                    header.description
            );

            writeUnsignedShortLE(
                    (int) (header.tempo * 100)
            );

            writeByte(
                    header.autoSave ? 1 : 0
            );

            writeByte(
                    header.autoSaveDuration
            );

            writeByte(
                    header.timeSignature
            );

            writeUnsignedIntLE(
                    header.minutesSpent
            );

            writeUnsignedIntLE(
                    header.leftClicks
            );

            writeUnsignedIntLE(
                    header.rightClicks
            );

            writeUnsignedIntLE(
                    header.blocksAdded
            );

            writeUnsignedIntLE(
                    header.blocksRemoved
            );

            writeString(
                    header.songOrigin
            );

            if (version >= 4) {

                writeByte(
                        header.loop ? 1 : 0
                );

                writeByte(
                        header.maxLoopCount
                );

                writeUnsignedShortLE(
                        header.loopStart
                );
            }
        }

        // --------------------------------------------------------
        // Notes
        // --------------------------------------------------------

        private void writeNotes(
                NbsFile nbsFile,
                int version
        ) throws IOException {

            int currentTick = -1;

            for (Chord chord : nbsFile) {

                int tickDelta =
                        chord.tick - currentTick;

                writeUnsignedShortLE(
                        tickDelta
                );

                currentTick =
                        chord.tick;

                int currentLayer = -1;

                for (Note note : chord.notes) {

                    int layerDelta =
                            note.layer - currentLayer;

                    writeUnsignedShortLE(
                            layerDelta
                    );

                    currentLayer =
                            note.layer;

                    writeByte(
                            note.instrument
                    );

                    writeByte(
                            note.key
                    );

                    if (version >= 4) {

                        writeByte(
                                note.velocity
                        );

                        writeByte(
                                note.panning + 100
                        );

                        writeShortLE(
                                note.pitch
                        );
                    }
                }

                // End of this tick
                writeUnsignedShortLE(0);
            }

            // End of notes
            writeUnsignedShortLE(0);
        }

        // --------------------------------------------------------
        // Layers
        // --------------------------------------------------------

        private void writeLayers(
                NbsFile nbsFile,
                int version
        ) throws IOException {

            for (Layer layer : nbsFile.layers) {

                writeString(
                        layer.name
                );

                if (version >= 4) {

                    writeByte(
                            layer.lock ? 1 : 0
                    );
                }

                writeByte(
                        layer.volume
                );

                if (version >= 2) {

                    writeByte(
                            layer.panning + 100
                    );
                }
            }
        }

        // --------------------------------------------------------
        // Instruments
        // --------------------------------------------------------

        private void writeInstruments(
                NbsFile nbsFile,
                int version
        ) throws IOException {

            writeByte(
                    nbsFile.instruments.size()
            );

            for (Instrument instrument :
                    nbsFile.instruments) {

                writeString(
                        instrument.name
                );

                writeString(
                        instrument.file
                );

                writeByte(
                        instrument.pitch
                );

                writeByte(
                        instrument.pressKey ? 1 : 0
                );
            }
        }
    }

    // ============================================================
    // Example
    // ============================================================

    public static void main(String[] args) throws Exception {

        // 创建一个新文件
        NbsFile file = Nbs.newFile();

        file.header.songName = "Java Test";
        file.header.songAuthor = "OpenAI";
        file.header.tempo = 10.0;

        // 添加一个乐器
        file.instruments.add(
                new Instrument(
                        0,
                        "Piano",
                        "harp.ogg",
                        45,
                        true
                )
        );

        // 添加音符
        file.notes.add(
                new Note(
                        0,
                        0,
                        0,
                        33,
                        100,
                        0,
                        0
                )
        );

        file.notes.add(
                new Note(
                        4,
                        0,
                        0,
                        37,
                        100,
                        0,
                        0
                )
        );

        file.notes.add(
                new Note(
                        8,
                        0,
                        0,
                        40,
                        100,
                        0,
                        0
                )
        );

        // 保存
        file.save("test.nbs");

        // 重新读取
        NbsFile loaded =
                Nbs.read("test.nbs");

        System.out.println(
                "Song: " +
                        loaded.header.songName
        );

        System.out.println(
                "Notes: " +
                        loaded.notes.size()
        );

        for (Chord chord : loaded) {

            System.out.println(
                    "Tick = " +
                            chord.tick +
                            ", notes = " +
                            chord.notes.size()
            );
        }
    }
}