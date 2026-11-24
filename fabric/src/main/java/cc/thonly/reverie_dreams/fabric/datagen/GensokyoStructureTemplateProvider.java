package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.data.gensokyo.GensokyoAdventure;
import cc.thonly.reverie_dreams.data.gensokyo.GensokyoStructureMetadata;
import cc.thonly.reverie_dreams.fabric.util.DataProviderHelper;
import com.mojang.serialization.Codec;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import net.minecraft.nbt.CompoundTag;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Exports registry configuration from final authored NBT assets. */
public final class GensokyoStructureTemplateProvider implements DataProvider {
    private final CompletableFuture<HolderLookup.Provider> lookup;

    public GensokyoStructureTemplateProvider(FabricPackOutput output,
                                             CompletableFuture<HolderLookup.Provider> lookup) {
        this.lookup = lookup;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return lookup.thenAcceptAsync(provider -> {
            Path directory = structureDirectory();
            try (var files = Files.list(directory)) {
                for (Path file : files.filter(p -> p.toString().endsWith(".nbt")).sorted().toList()) {
                    var template = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
                    String name = file.getFileName().toString().replaceFirst("\\.nbt$", "");
                    Identifier id = Identifier.fromNamespaceAndPath("reverie_dreams", "gensokyo/" + name);
                    var metadata = GensokyoStructureMetadata.CODEC.parse(NbtOps.INSTANCE,
                            template.getCompoundOrEmpty("reverie_structure_metadata")).getOrThrow();
                    metadata = metadata.withFingerprint(fingerprint(template));
                    write(cache, id, metadata, GensokyoStructureMetadata.CODEC, "gensokyo_structure_metadata");
                    if (template.contains("reverie_adventure")) {
                        var adventure = GensokyoAdventure.CODEC.parse(NbtOps.INSTANCE,
                                template.getCompoundOrEmpty("reverie_adventure")).getOrThrow();
                        write(cache, Identifier.fromNamespaceAndPath(id.getNamespace(), name),
                                adventure, GensokyoAdventure.CODEC, "gensokyo_adventure");
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Could not read final Gensokyo structures", e);
            }
        });
    }

    private static String fingerprint(CompoundTag template) throws IOException {
        var metadata = template.getCompoundOrEmpty("reverie_structure_metadata");
        var baseline = metadata.get("template_sha256");
        var content = metadata.get("content_sha256");
        metadata.remove("template_sha256");
        metadata.remove("content_sha256");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var stream = new DigestOutputStream(OutputStream.nullOutputStream(), digest)) {
                NbtIo.writeCompressed(template, stream);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } finally {
            if (baseline != null) {
                metadata.put("template_sha256", baseline);
            }
            if (content != null) {
                metadata.put("content_sha256", content);
            }
        }
    }

    private static <T> void write(CachedOutput cache, Identifier id, T value, Codec<T> codec, String directory) {
        DataProviderHelper.outputFile(cache, Map.of(id, value), codec, directory);
    }

    private static Path structureDirectory() {
        String override = System.getProperty("reverie_dreams.structure-directory");
        if (override != null) {
            return Path.of(override);
        }
        for (Path parent = Path.of("").toAbsolutePath(); parent != null; parent = parent.getParent()) {
            Path directory = parent.resolve("common/src/main/resources/data/reverie_dreams/structure/gensokyo");
            if (Files.isDirectory(directory)) {
                return directory;
            }
        }
        throw new IllegalStateException("Final Gensokyo structure directory was not found");
    }

    @Override
    public String getName() {
        return "Gensokyo structure registry configuration";
    }
}
