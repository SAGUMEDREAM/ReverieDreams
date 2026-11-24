package cc.thonly.reverie_dreams.paper.worldbridge;

import cc.thonly.reverie_dreams.paper.util.Ids;

import cc.thonly.reverie_dreams.paper.world.GensokyoWorldPlugin;
import com.google.gson.GsonBuilder;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import cc.thonly.reverie_dreams.paper.farming.FarmingModule;
import java.util.HexFormat;
import java.util.LinkedHashSet;

/** Optional deferred startup resolver and live reload hook. Native identities always come from CE. */
public final class CraftEngineWorldBridge {
    private CraftEngineWorldBridge() { }

    private record Verified(Map<String, String> states, Map<String, Object> owners) { }

    /** Registration during onLoad does not access CE; Paper invokes the resolver when selecting the generator. */
    public static void registerResolver() {
        var target = (GensokyoWorldPlugin) Bukkit.getPluginManager().getPlugin("ReverieDreamsWorld");
        if (target == null) throw new IllegalStateException("World bootstrap plugin is absent");
        target.setVerifiedBindingResolver(requested -> verify(requested).states());
    }

    private static Verified verify(Set<String> requested) {
        if (BukkitCraftEngine.instance().isReloading()) return new Verified(Map.of(), Map.of());
        Map<String, String> states = new LinkedHashMap<>();
        Map<String, Object> owners = new LinkedHashMap<>();
        for (String source : requested) {
            // Source property expressions require a dedicated parser; an unresolved source must remain blocked.
            if (source.indexOf('[') >= 0) continue;
            var definition = CraftEngineBlocks.byId(Key.of(source));
            if (definition == null) continue;
            var state = definition.defaultState();
            if (state == null || state.customBlockState() == null || state.owner().value() != definition) continue;
            if (!(state.customBlockState().platformState() instanceof BlockData nativeState)) continue;
            String encoded = nativeState.getAsString();
            BlockData parsed = Bukkit.createBlockData(encoded);
            if (!encoded.equals(parsed.getAsString()) || !nativeState.equals(parsed)) continue;
            if (source.equals(Ids.MOD_ID + ":paddy_field")) {
                FarmingModule.bindPaddy();
                FarmingModule.assertPaddyRoundTrip(parsed);
            }
            states.put(source, encoded);
            owners.put(source, definition);
        }
        return new Verified(Map.copyOf(states), Map.copyOf(owners));
    }

    public static BukkitTask connect(JavaPlugin main) {
        var target = (GensokyoWorldPlugin) Bukkit.getPluginManager().getPlugin("ReverieDreamsWorld");
        if (target == null) throw new IllegalStateException("World bootstrap plugin is absent");
        return Bukkit.getScheduler().runTaskTimer(main, new Runnable() {
            private Map<String, String> installed = Map.of();
            private final Map<String, Object> semanticOwners = new LinkedHashMap<>();
            private String previousFailure;
            @Override public void run() {
                if (!target.isEnabled()) return;
                if (BukkitCraftEngine.instance().isReloading()) return;
                var surfaces = target.requestedCustomStates();
                var features = target.requestedFeatureStates();
                var requested = new LinkedHashSet<>(surfaces);
                requested.addAll(features);
                if (requested.isEmpty()) return;
                try {
                    var resolution = verify(requested);
                    var verified = resolution.states();
                    var owners = resolution.owners();
                    if (!verified.keySet().containsAll(requested)) return;
                    boolean changed = owners.entrySet().stream().anyMatch(entry -> semanticOwners.get(entry.getKey()) != entry.getValue());
                    if (verified.equals(installed) && !changed) return;
                    Map<String, String> surfaceStates = new LinkedHashMap<>(), featureStates = new LinkedHashMap<>();
                    verified.forEach((source, nativeState) -> {
                        if (surfaces.contains(source)) surfaceStates.put(source, nativeState);
                        if (features.contains(source)) featureStates.put(source, nativeState);
                    });
                    target.installVerifiedBindings(Map.copyOf(surfaceStates));
                    target.installVerifiedFeatureBindings(Map.copyOf(featureStates));
                    installed = Map.copyOf(verified);
                    semanticOwners.clear(); semanticOwners.putAll(owners);
                    previousFailure = null;
                    main.getSLF4JLogger().info("World generator bound {} verified CraftEngine surface state(s)", installed.size());
                    record(main, target, installed);
                } catch (RuntimeException rejected) {
                    if (!rejected.toString().equals(previousFailure)) {
                        main.getSLF4JLogger().warn("World binding refused; unresolved chunks remain blocked: {}", rejected.toString());
                        previousFailure = rejected.toString();
                    }
                }
            }
        }, 1L, 20L);
    }

    private static void record(JavaPlugin main, GensokyoWorldPlugin world, Map<String, String> bindings) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("schema", 1);
        evidence.put("mode", "verified live binding after CE parsing; never a cold-start alias or ordinal restore");
        evidence.put("paper", Bukkit.getVersion());
        evidence.put("states", bindings);
        List<Path> files = new ArrayList<>();
        var ce = Bukkit.getPluginManager().getPlugin("CraftEngine");
        Path ceData = ce.getDataFolder().toPath().toAbsolutePath();
        files.add(ceData.resolve("config.yml"));
        files.add(ceData.resolve("cache/custom_block_states.json"));
        files.add(main.getDataFolder().toPath().resolve("config.yml"));
        for (var plugin : List.of(main, ce, world)) {
            try { files.add(Path.of(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI())); }
            catch (Exception unavailable) { /* Missing fingerprints remain explicit in the record. */ }
        }
        Path output = world.getDataFolder().toPath().resolve("ce-live-bindings.json");
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            try {
                try (var paths = Files.walk(ceData.resolve("resources"))) {
                    files.addAll(paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml") || path.toString().endsWith(".json")).sorted().toList());
                }
                Map<String, String> hashes = new LinkedHashMap<>();
                for (Path file : files) {
                    hashes.put(file.toAbsolutePath().toString(), Files.isRegularFile(file) ? sha256(file) : "MISSING");
                }
                evidence.put("fingerprints", hashes);
                Files.createDirectories(output.getParent());
                Path temporary = output.resolveSibling(output.getFileName() + ".tmp");
                Files.writeString(temporary, new GsonBuilder().setPrettyPrinting().create().toJson(evidence) + "\n", StandardCharsets.UTF_8);
                Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception failure) {
                main.getSLF4JLogger().warn("Cannot save world binding audit record: {}", failure.toString());
            }
        });
    }

    private static String sha256(Path file) throws Exception {
        var digest = MessageDigest.getInstance("SHA-256");
        try (var input = Files.newInputStream(file)) {
            byte[] bytes = new byte[64 * 1024];
            int count;
            while ((count = input.read(bytes)) >= 0) if (count > 0) digest.update(bytes, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
