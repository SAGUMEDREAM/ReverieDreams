package cc.thonly.reverie_dreams.paper.world;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.List;
import java.util.Locale;

/** Separately packaged STARTUP plugin. It never creates, regenerates or pre-generates a world. */
public final class GensokyoWorldPlugin extends JavaPlugin {
    private final AtomicBoolean preparing = new AtomicBoolean();
    private volatile GensokyoGenerator generator;
    private volatile Function<Set<String>, Map<String, String>> verifiedBindingResolver;

    @Override public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new GensokyoBoundaryListener(this), this);
        getLogger().info("World adapter is opt-in; no WorldCreator or automatic map pre-generation runs.");
    }

    @Override public synchronized ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (!getConfig().getBoolean("enabled", false) || !getConfig().getBoolean("acknowledge-terrain-only-gaps", false))
            throw new IllegalStateException("Gensokyo world generator is disabled or its terrain-only gaps are unacknowledged");
        if (!worldName.equals(getConfig().getString("world-name")) || !(id == null || id.equals("gensokyo")))
            throw new IllegalArgumentException("Refusing unexpected world/id: " + worldName + "/" + id);
        if (generator != null) return generator;
        try {
            GensokyoMapCache cache = GensokyoMapCache.open(source(), cacheDirectory());
            try {
            if (!cache.map().id().equals("reverie_dreams:gensokyo")) throw new IOException("Unexpected source map ID");
            GensokyoOreFeatures oreFeatures = null;
            boolean relativeOres = getConfig().getBoolean("features.terrain-relative-ores", false);
            boolean sourceOrePlacements = getConfig().getBoolean("features.source-ore-placements", false);
            if (relativeOres || sourceOrePlacements) {
                Path override = getDataFolder().toPath().resolve("ore-features.json");
                if (Files.exists(override)) {
                    try (var reader = Files.newBufferedReader(override, StandardCharsets.UTF_8)) { oreFeatures = GensokyoOreFeatures.read(reader); }
                } else oreFeatures = GensokyoOreFeatures.bundled();
            }
            GensokyoGenerator candidate = new GensokyoGenerator(cache, getConfig().getBoolean("features.native-biome-decoration", false),
                    oreFeatures, relativeOres, sourceOrePlacements, getConfig().getBoolean("features.source-forest-trees", false));
            Function<Set<String>, Map<String, String>> resolver = verifiedBindingResolver;
            if (resolver != null) candidate.installInitialBindings(resolver);
            candidate.unresolved().forEach(issue -> getLogger().warning(issue));
            getLogger().warning(candidate.sourceForestStatus());
            getLogger().warning("World adapter stages: native-decoration=" + getConfig().getBoolean("features.native-biome-decoration", false)
                    + ", relative-ores=" + relativeOres + ", source-ore-placements=" + sourceOrePlacements
                    + ". Mod custom flora, structure anchors and other dimensions remain incomplete.");
            // Ownership transfers only after construction, binding and logging
            // succeed. A World can retain this generator after plugin disable.
            generator = candidate;
            return candidate;
            } catch (Throwable failure) {
                try { cache.close(); } catch (Throwable cleanup) { failure.addSuppressed(cleanup); }
                throw failure;
            }
        } catch (IOException error) { throw new IllegalStateException("Prepare the map cache before selecting this world generator", error); }
    }

    /** Exact nonzero custom surface IRs requested by the active source map. Empty until its generator is selected. */
    public Set<String> requestedCustomStates() {
        GensokyoGenerator active = generator;
        return active == null ? Set.of() : active.requestedCustomStates();
    }
    /** Exact custom outputs of the enabled feature stage; separate from active source surface IRs. */
    public Set<String> requestedFeatureStates() {
        GensokyoGenerator active = generator;
        return active == null ? Set.of() : active.requestedFeatureStates();
    }
    public void installVerifiedFeatureBindings(Map<String, String> sourceToNativeEncoded) {
        GensokyoGenerator active = generator;
        if (active == null) throw new IllegalStateException("Select the prepared source world generator before installing feature bindings");
        active.installVerifiedFeatureBindings(Objects.requireNonNull(sourceToNativeEncoded));
    }

    /**
     * Registers a deferred resolver; this setter never invokes it or touches its
     * content engine. The factory invokes it once after reading the source map,
     * before publishing the first generator. An empty/partial map leaves all
     * other sources unresolved. Registering after construction does not perform
     * a reload; use installVerifiedBindings for the post-load readiness check.
     * Resolver failures propagate without installing a fallback.
     */
    public void setVerifiedBindingResolver(Function<Set<String>, Map<String, String>> resolver) {
        verifiedBindingResolver = Objects.requireNonNull(resolver);
    }

    /**
     * Installs native BlockData encodings only after the caller has verified that
     * its content engine has fully bound the corresponding source semantics.
     * This API neither discovers nor accepts guessed ordinal/vanilla mappings.
     * All entries parse before one immutable map is published. Existing native
     * identities cannot be changed during this generator's lifetime.
     */
    public void installVerifiedBindings(Map<String, String> sourceToNativeEncoded) {
        GensokyoGenerator active = generator;
        if (active == null) throw new IllegalStateException("Select the prepared source world generator before installing bindings");
        active.installVerifiedBindings(Objects.requireNonNull(sourceToNativeEncoded));
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("reveriedreams.world.admin")) { sender.sendMessage("Missing permission: reveriedreams.world.admin"); return true; }
        String action = args.length == 0 ? "audit" : args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "prepare" -> prepare(sender);
            case "audit" -> audit(sender);
            case "enter" -> enter(sender);
            default -> sender.sendMessage("/rdworld <prepare|audit|enter>");
        }
        return true;
    }

    private void prepare(CommandSender sender) {
        if (generator != null) { sender.sendMessage("A map generator is active. Prepare/rebuild only with its world deselected on the next stopped-server setup."); return; }
        if (!preparing.compareAndSet(false, true)) { sender.sendMessage("Map preparation is already running."); return; }
        sender.sendMessage("Preparing source delta/pal4 cache asynchronously; original map geometry is unchanged and no world chunks are generated.");
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            String message;
            try { message = "Prepared map cache: " + GensokyoMapCache.prepare(source(), cacheDirectory()); }
            catch (Exception error) { message = "Map preparation failed: " + error.getMessage(); getLogger().warning(message); }
            finally { preparing.set(false); }
            String result = message;
            if (isEnabled()) getServer().getScheduler().runTask(this, () -> sender.sendMessage(result));
        });
    }

    private void audit(CommandSender sender) {
        try {
            GensokyoMap map = GensokyoMap.read(source());
            sender.sendMessage("Source " + map.width() + "x" + map.depth() + ", y=[" + map.minY() + "," + (map.minY() + map.height())
                    + "), sea=" + map.seaLevel() + ", spawn=" + map.spawn() + ", authored underground="
                    + map.underground().values().stream().mapToInt(List::size).sum());
            sender.sendMessage("Source custom surface: " + map.surfacePalette().stream().filter(s -> !s.startsWith("minecraft:")).toList());
            sender.sendMessage("Whole-map native/CE palette preflight: " + (generator == null ? "not bound; paddy must not be assumed ready" : generator.unresolved()));
            sender.sendMessage("Enabled feature custom outputs: " + requestedFeatureStates());
            if (generator != null) sender.sendMessage(generator.sourceForestStatus() + "; generated trees=" + generator.sourceForestTreesGenerated());
            World world = getServer().getWorld(NamespacedKey.fromString(map.id()));
            sender.sendMessage(world == null ? "Datapack world is not loaded." : "Loaded " + world.getName() + " key=" + world.getKey()
                    + " height=[" + world.getMinHeight() + "," + world.getMaxHeight() + ") sea=" + world.getSeaLevel()
                    + " mapGenerator=" + (world.getGenerator() instanceof GensokyoGenerator));
            sender.sendMessage("Optional stages: native-biome-decoration=" + getConfig().getBoolean("features.native-biome-decoration", false)
                    + ", terrain-relative-ores=" + getConfig().getBoolean("features.terrain-relative-ores", false)
                    + ", source-ore-placements=" + getConfig().getBoolean("features.source-ore-placements", false)
                    + ". Mod custom flora/anchors/other dimensions still need separate acceptance.");
        } catch (Exception error) { sender.sendMessage("Map audit failed: " + error.getMessage()); }
    }

    private void enter(CommandSender sender) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Run enter as a player; audit is available to console."); return; }
        if (generator == null || !generator.unresolved().isEmpty()) {
            sender.sendMessage("Entry refused: whole-map palette is unresolved. " + (generator == null ? "World generator is not bound." : generator.unresolved()));
            return;
        }
        World world = getServer().getWorld(NamespacedKey.fromString(generator.terrain().map().id()));
        if (world == null || world.getGenerator() != generator || world.getMinHeight() != 0 || world.getMaxHeight() != 1536) {
            sender.sendMessage("Entry refused: exact-height datapack world/generator is not verified."); return;
        }
        var spawn = generator.terrain().map().spawn();
        player.teleportAsync(new Location(world, spawn.x() + 0.5, spawn.y(), spawn.z() + 0.5, spawn.yaw(), spawn.pitch()));
    }

    boolean outside(World world, int x, int z) {
        return generator != null && world.getKey().toString().equals(generator.terrain().map().id())
                && !generator.terrain().map().contains(x, z);
    }
    void initializeSpawn(World world) {
        if (generator == null || world.getGenerator() != generator || !world.getKey().toString().equals(generator.terrain().map().id())) return;
        if (world.getMinHeight() != 0 || world.getMaxHeight() != 1536) throw new IllegalStateException("Refusing wrong-height Gensokyo world");
        var spawn = generator.terrain().map().spawn();
        world.setSpawnLocation(spawn.x(), spawn.y(), spawn.z(), spawn.yaw());
    }
    private Path source() { return resolve(getConfig().getString("source-directory", "map")); }
    private Path cacheDirectory() { return resolve(getConfig().getString("cache-directory", "map-cache")); }
    private Path resolve(String configured) { Path path = Path.of(configured); return (path.isAbsolute() ? path : getDataFolder().toPath().resolve(path)).toAbsolutePath().normalize(); }
    @Override public void onDisable() {
        // Paper74 disables plugins before draining generation/saving worlds.
        // The World's generator owns its readable cache until those references
        // disappear; FileChannel.open has a JDK Cleaner, and process exit also
        // releases its descriptor. Closing it here races pending generation.
        generator = null;
        verifiedBindingResolver = null;
    }
}
