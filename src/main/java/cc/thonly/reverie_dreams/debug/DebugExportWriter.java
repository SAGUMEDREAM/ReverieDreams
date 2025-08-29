package cc.thonly.reverie_dreams.debug;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Map;

@Slf4j
public class DebugExportWriter {
    private static final Map<String,DebugExportWriter> INSTANCES = new Object2ObjectOpenHashMap<>();
    public static final String RDDE = "reverie_dreams_debug_export.txt";
    public static final DebugExportWriter OUTPUT = createInstance(RDDE);
    private final String filename;
    private final LinkedList<String> lines = new LinkedList<>();

    private DebugExportWriter(String filename) {
        this.filename = filename;
    }

    protected static DebugExportWriter createInstance(String filename) {
        return INSTANCES.computeIfAbsent(filename, x-> new DebugExportWriter(filename));
    }

    public static DebugExportWriter getInstance(String filename) {
        return INSTANCES.get(filename);
    }

    public DebugExportWriter write(String format, Object... args) {
        this.lines.add(String.format(format, args));
        return this;
    }

    protected boolean export() {
        try {
            Path path = Path.of("./" + this.filename);
            Files.createDirectories(path.getParent() != null ? path.getParent() : Path.of("."));

            Files.write(path.toAbsolutePath(), this.lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Success to export debug write in {}", path.toAbsolutePath());
            return true;
        } catch (IOException e) {
            log.error("Can't export debug writer", e);
            return false;
        }
    }
}
