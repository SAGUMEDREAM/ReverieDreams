package cc.thonly.reverie_dreams.fabric.datagen.generator;

import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
@Environment(value = EnvType.CLIENT)
public abstract class AbstractEquipmentAssetProvider implements DataProvider {
    public final FabricDataOutput output;
    public final CompletableFuture<HolderLookup.Provider> future;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public AbstractEquipmentAssetProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.output = output;
        this.future = future;
    }

    protected abstract void bootstrap(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> consumer);

    protected EquipmentClientInfo createHumanoidOnlyModel(String id) {
        return EquipmentClientInfo.builder().addHumanoidLayers(Identifier.withDefaultNamespace(id)).build();
    }

    protected EquipmentClientInfo createHumanoidAndHorseModel(String id) {
        return EquipmentClientInfo.builder().addHumanoidLayers(Identifier.withDefaultNamespace(id)).addLayers(EquipmentClientInfo.LayerType.HORSE_BODY, EquipmentClientInfo.Layer.leatherDyeable(Identifier.withDefaultNamespace(id), false)).build();
    }

    protected EquipmentClientInfo createHumanoidOnlyModel(Identifier id) {
        return EquipmentClientInfo.builder().addHumanoidLayers(id).build();
    }

    protected EquipmentClientInfo createHumanoidAndHorseModel(Identifier id) {
        return EquipmentClientInfo.builder().addHumanoidLayers(id).addLayers(EquipmentClientInfo.LayerType.HORSE_BODY, EquipmentClientInfo.Layer.leatherDyeable(id, false)).build();
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return CompletableFuture.runAsync(() -> this.export(writer));
    }

    public void export(CachedOutput writer) {
        try {
            Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
            HashMap<ResourceKey<EquipmentAsset>, EquipmentClientInfo> map = new HashMap<>();
            this.bootstrap((key, model) -> {
                if (map.putIfAbsent(key, model) != null) {
                    throw new IllegalStateException("Tried to register equipment asset twice for id: " + String.valueOf(key));
                }
                Identifier identifier = key.identifier();
                String namespaceRef = identifier.getNamespace();
                String pathRef = identifier.getPath();
                Path generatePath = DataGeneratorUtil.getAssets(path, namespaceRef, "equipment", null);

                DataResult<JsonElement> result = EquipmentClientInfo.CODEC.encodeStart(JsonOps.INSTANCE, model);
                Optional<JsonElement> optional = result.result();

                if (optional.isPresent()) {
                    JsonElement element = optional.get();
                    Path output = generatePath.resolve(pathRef + ".json");
                    String jsonString = this.gson.toJson(element);
                    byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
                    try {
                        Files.createDirectories(output.getParent());
                        writer.writeIfNeeded(output, bytes, HashCode.fromBytes(bytes));
                    } catch (IOException e) {
                        log.error("Can't generate equipment asset {}", identifier.toString());
                    }
                }
            });
        } catch (Exception err) {
            log.error("Fail to generate equipment provider");
        }
    }

    @Override
    public String getName() {
        return "Equipment Asset Definitions";
    }
}
