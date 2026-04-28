package cc.thonly.reverie_dreams.fabric.datagen.generator;//package cc.thonly.reverie_dreams.datagen.generator;
//
//import cc.thonly.reverie_dreams.entity.skin.NPCSkin;
//import com.google.common.hash.HashCode;
//import com.google.gson.*;
//import com.mojang.authlib.properties.Property;
//import lombok.extern.slf4j.Slf4j;
//import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
//import net.minecraft.data.DataProvider;
//import net.minecraft.data.DataWriter;
//import net.minecraft.registry.RegistryWrapper;
//import net.minecraft.util.Identifier;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.Base64;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//@Slf4j
//public abstract class PropertySkinProvider implements DataProvider {
//    public final FabricDataOutput output;
//    public final CompletableFuture<RegistryWrapper.WrapperLookup> future;
//    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
//    private final OkHttpClient client = new OkHttpClient.Builder()
//            .retryOnConnectionFailure(true)
//            .build();
//    private final List<NPCSkin> properties = new ArrayList<>();
//
//    public PropertySkinProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> future) {
//        this.output = output;
//        this.future = future;
//    }
//
//    public abstract void configured();
//
//    protected void addProperty(NPCSkin property) {
//        this.properties.add(property);
//    }
//
//    @Override
//    public CompletableFuture<?> run(DataWriter writer) {
//        return CompletableFuture.runAsync(() -> {
//            this.configured();
//            this.export(writer);
//        });
//    }
//
//    public void export(DataWriter writer) {
//        Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
//        Base64.Decoder decoder = Base64.getDecoder();
//        List<String> lines = new ArrayList<>();
//        for (NPCSkin skin : this.properties) {
//            Property property = skin.get();
//            try {
//                byte[] bytes = decoder.decode(property.value());
//                String jsonString = new String(bytes, StandardCharsets.UTF_8);
//                JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
//                JsonObject textures = jsonObject.getAsJsonObject("textures");
//                JsonObject SKIN = textures.getAsJsonObject("SKIN");
//                boolean has = false;
//                if (SKIN.has("metadata")) {
//                    JsonObject metadata = SKIN.getAsJsonObject("metadata");
//                    String model = metadata.get("model").getAsString();
//                    if (model.equalsIgnoreCase("slim")) {
//                        has = true;
//                    }
//                }
//                String url = SKIN.get("url").getAsString();
//                byte[] imgBytes = this.downloadImage(url);
//                Path imgPath = DataGeneratorUtil.getAssets(path, skin.getId().getNamespace(), "textures/entity/player/skin/", null);
//                Path output = imgPath.resolve(skin.getId().getPath() + ".png");
//                Files.createDirectories(output.getParent());
//                String name = "RoleSkins.%s".formatted(skin.getId().getPath().toUpperCase());
//                if (has) {
//                    lines.add("this.addConfig(%s, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));".formatted(name));
//                } else {
//                    lines.add("this.addConfig(%s, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));".formatted(name));
//                }
////                Identifier profileId = Identifier.of(skin.getId().getNamespace(), skin.getId().getPath() + ".json");
////                Path dataPath = DataGeneratorUtil.getAssets(path, skin.getId().getNamespace(), "skin_config/", null);
//
//                writer.write(output, imgBytes, HashCode.fromBytes(bytes));
//                System.out.println(jsonString);
//            } catch (Exception err) {
//                log.error("Error: ", err);
//            }
//        }
//        for (String line : lines) {
//            System.out.println(line);
//        }
//    }
//
//    public byte[] downloadImage(String imageUrl) {
//        Request request = new Request.Builder()
//                .url(imageUrl)
//                .build();
//        try (Response response = this.client.newCall(request).execute()) {
//            if (response.isSuccessful()) {
//                return response.body().bytes();
//            }
//        } catch (Exception e) {
//            return null;
//        }
//        return null;
//    }
//
//    @Override
//    public String getName() {
//        return "NPC Skin updater";
//    }
//}
