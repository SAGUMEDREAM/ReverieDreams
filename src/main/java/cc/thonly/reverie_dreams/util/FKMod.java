package cc.thonly.reverie_dreams.util;

import cc.thonly.reverie_dreams.Touhou;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.*;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import net.fabricmc.loader.impl.metadata.AbstractModMetadata;
import net.fabricmc.loader.impl.metadata.EntrypointMetadata;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.fabricmc.loader.impl.metadata.NestedJarEntry;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public class FKMod {
    static {
        addMod("Nota","nota");
    }

    public static void addMod(String modName, String mod_id) {
        try {
            FabricLoaderInterface.addMod(FabricLoaderImpl.INSTANCE,
                    FabricLoaderInterface.createPlain(
                            Path.of("/"),
                            new V1ModMetadata(
                                    mod_id,
                                    Version.parse(Touhou.VERSION),
                                    List.of(),
                                    ModEnvironment.UNIVERSAL,
                                    Map.of(),
                                    List.of(),
                                    List.of(),
                                    null,
                                    List.of(),
                                    false,
                                    modName,
                                    "Runtime-generated mod",
                                    List.of(),
                                    List.of(),
                                    ContactInformation.EMPTY,
                                    List.of(),
                                    V1ModMetadata.NO_ICON,
                                    Map.of(),
                                    Map.of()),
                            false,
                            List.of()
                    )
            );
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    public static class FabricLoaderInterface {
        private static final Method ADD_MOD_METHOD;
        private static final Method CREATE_PLAIN_METHOD;
        private static final Field MODS_FIELD;

        static {
            try {
                ADD_MOD_METHOD = FabricLoaderImpl.class.getDeclaredMethod("addMod", ModCandidateImpl.class);
                ADD_MOD_METHOD.setAccessible(true);

                MODS_FIELD = FabricLoaderImpl.class.getDeclaredField("mods");
                MODS_FIELD.setAccessible(true);

                CREATE_PLAIN_METHOD = ModCandidateImpl.class.getDeclaredMethod("createPlain", List.class, LoaderModMetadata.class, boolean.class, Collection.class);
                CREATE_PLAIN_METHOD.setAccessible(true);
            } catch (NoSuchMethodException | NoSuchFieldException e) {
                throw new IllegalStateException("failed to reflect addMod/createPlain/mods - fabric loader unsupported?", e);
            }
        }

        public static void addMod(FabricLoaderImpl fabricLoader, ModCandidateImpl candidate) {
            try {
                ADD_MOD_METHOD.invoke(fabricLoader, candidate);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new IllegalStateException("Failed to inject mod", e);
            }
        }

        public static ModCandidateImpl createPlain(Path path, LoaderModMetadata metadata, boolean requiresRemap, Collection<ModCandidateImpl> nestedMods) {
            try {
                return (ModCandidateImpl) CREATE_PLAIN_METHOD.invoke(null, List.of(path), metadata, requiresRemap, nestedMods);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to create plain mod container", e);
            }
        }
    }
    public static final class V1ModMetadata extends AbstractModMetadata implements LoaderModMetadata {
        static final IconEntry NO_ICON = size -> Optional.empty();

        // Required values
        private final String id;
        private Version version;

        // Optional (id provides)
        private final Collection<String> provides;

        // Optional (mod loading)
        private final ModEnvironment environment;
        private final Map<String, List<EntrypointMetadata>> entrypoints;
        private final Collection<NestedJarEntry> jars;
        private final Collection<MixinEntry> mixins;
        /* @Nullable */
        private final String accessWidener;

        // Optional (dependency resolution)
        private Collection<ModDependency> dependencies;
        // Happy little accidents
        private final boolean hasRequires;

        // Optional (metadata)
        /* @Nullable */
        private final String name;
        private final String description;
        private final Collection<Person> authors;
        private final Collection<Person> contributors;
        private final ContactInformation contact;
        private final Collection<String> license;
        private final IconEntry icon;

        // Optional (language adapter providers)
        private final Map<String, String> languageAdapters;

        // Optional (custom values)
        private final Map<String, CustomValue> customValues;

        public V1ModMetadata(String id, Version version, Collection<String> provides,
                             ModEnvironment environment, Map<String, List<EntrypointMetadata>> entrypoints, Collection<NestedJarEntry> jars,
                             Collection<MixinEntry> mixins, /* @Nullable */ String accessWidener,
                             Collection<ModDependency> dependencies, boolean hasRequires,
                /* @Nullable */ String name, /* @Nullable */String description,
                             Collection<Person> authors, Collection<Person> contributors, /* @Nullable */ContactInformation contact, Collection<String> license, IconEntry icon,
                             Map<String, String> languageAdapters,
                             Map<String, CustomValue> customValues) {
            this.id = id;
            this.version = version;
            this.provides = Collections.unmodifiableCollection(provides);
            this.environment = environment;
            this.entrypoints = Collections.unmodifiableMap(entrypoints);
            this.jars = Collections.unmodifiableCollection(jars);
            this.mixins = Collections.unmodifiableCollection(mixins);
            this.accessWidener = accessWidener;
            this.dependencies = Collections.unmodifiableCollection(dependencies);
            this.hasRequires = hasRequires;
            this.name = name;

            // Empty description if not specified
            if (description != null) {
                this.description = description;
            } else {
                this.description = "";
            }

            this.authors = Collections.unmodifiableCollection(authors);
            this.contributors = Collections.unmodifiableCollection(contributors);

            if (contact != null) {
                this.contact = contact;
            } else {
                this.contact = ContactInformation.EMPTY;
            }

            this.license = Collections.unmodifiableCollection(license);

            if (icon != null) {
                this.icon = icon;
            } else {
                this.icon = V1ModMetadata.NO_ICON;
            }

            this.languageAdapters = Collections.unmodifiableMap(languageAdapters);
            this.customValues = Collections.unmodifiableMap(customValues);
        }

        @Override
        public int getSchemaVersion() {
            return 1;
        }

        @Override
        public String getType() {
            return TYPE_FABRIC_MOD; // Fabric Mod
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public Collection<String> getProvides() {
            return this.provides;
        }

        @Override
        public Version getVersion() {
            return this.version;
        }

        @Override
        public void setVersion(Version version) {
            this.version = version;
        }

        @Override
        public ModEnvironment getEnvironment() {
            return this.environment;
        }

        @Override
        public boolean loadsInEnvironment(EnvType type) {
            return this.environment.matches(type);
        }

        @Override
        public Collection<ModDependency> getDependencies() {
            return dependencies;
        }

        @Override
        public void setDependencies(Collection<ModDependency> dependencies) {
            this.dependencies = Collections.unmodifiableCollection(dependencies);
        }

        // General metadata

        @Override
        public String getName() {
            if (this.name == null || this.name.isEmpty()) {
                return this.id;
            }

            return this.name;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public Collection<Person> getAuthors() {
            return this.authors;
        }

        @Override
        public Collection<Person> getContributors() {
            return this.contributors;
        }

        @Override
        public ContactInformation getContact() {
            return this.contact;
        }

        @Override
        public Collection<String> getLicense() {
            return this.license;
        }

        @Override
        public Optional<String> getIconPath(int size) {
            return this.icon.getIconPath(size);
        }

        @Override
        public Map<String, CustomValue> getCustomValues() {
            return this.customValues;
        }

        // Internal stuff

        @Override
        public Map<String, String> getLanguageAdapterDefinitions() {
            return this.languageAdapters;
        }

        @Override
        public Collection<NestedJarEntry> getJars() {
            return this.jars;
        }

        @Override
        public Collection<String> getMixinConfigs(EnvType type) {
            final List<String> mixinConfigs = new ArrayList<>();

            // This is only ever called once, so no need to store the result of this.
            for (MixinEntry mixin : this.mixins) {
                if (mixin.environment.matches(type)) {
                    mixinConfigs.add(mixin.config);
                }
            }

            return mixinConfigs;
        }

        @Override
        public String getAccessWidener() {
            return this.accessWidener;
        }

        @Override
        public Collection<String> getOldInitializers() {
            return Collections.emptyList(); // Not applicable in V1
        }

        @Override
        public List<EntrypointMetadata> getEntrypoints(String type) {
            if (type == null) {
                return Collections.emptyList();
            }

            final List<EntrypointMetadata> entrypoints = this.entrypoints.get(type);

            if (entrypoints != null) {
                return entrypoints;
            }

            return Collections.emptyList();
        }

        @Override
        public Collection<String> getEntrypointKeys() {
            return this.entrypoints.keySet();
        }

        @Override
        public void emitFormatWarnings() {
            if (hasRequires) {
                Log.warn(LogCategory.METADATA, "Mod `%s` (%s) uses 'requires' key in fabric.mod.json, which is not supported - use 'depends'", this.id, this.version);
            }
        }

        public static final class EntrypointMetadataImpl implements EntrypointMetadata {
            private final String adapter;
            private final String value;

            EntrypointMetadataImpl(String adapter, String value) {
                this.adapter = adapter;
                this.value = value;
            }

            @Override
            public String getAdapter() {
                return this.adapter;
            }

            @Override
            public String getValue() {
                return this.value;
            }
        }

        static final class JarEntry implements NestedJarEntry {
            private final String file;

            JarEntry(String file) {
                this.file = file;
            }

            @Override
            public String getFile() {
                return this.file;
            }
        }

        static final class MixinEntry {
            private final String config;
            private final ModEnvironment environment;

            MixinEntry(String config, ModEnvironment environment) {
                this.config = config;
                this.environment = environment;
            }
        }

        interface IconEntry {
            Optional<String> getIconPath(int size);
        }

        static final class Single implements IconEntry {
            private final String icon;

            Single(String icon) {
                this.icon = icon;
            }

            @Override
            public Optional<String> getIconPath(int size) {
                return Optional.of(this.icon);
            }
        }

        static final class MapEntry implements IconEntry {
            private final SortedMap<Integer, String> icons;

            MapEntry(SortedMap<Integer, String> icons) {
                this.icons = icons;
            }

            @Override
            public Optional<String> getIconPath(int size) {
                int iconValue = -1;

                for (int i : icons.keySet()) {
                    iconValue = i;

                    if (iconValue >= size) {
                        break;
                    }
                }

                return Optional.of(icons.get(iconValue));
            }
        }
    }
}
