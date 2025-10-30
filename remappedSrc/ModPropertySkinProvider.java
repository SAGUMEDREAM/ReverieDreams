//package cc.thonly.reverie_dreams.datagen;
//
//import cc.thonly.reverie_dreams.datagen.generator.PropertySkinProvider;
//import cc.thonly.reverie_dreams.entity.skin.NPCSkin;
//import cc.thonly.reverie_dreams.registry.RegistryManager;
//import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
//import net.minecraft.registry.RegistryWrapper;
//
//import java.util.concurrent.CompletableFuture;
//
//public class ModPropertySkinProvider extends PropertySkinProvider {
//    public ModPropertySkinProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> future) {
//        super(output, future);
//    }
//
//    @Override
//    public void configured() {
//        for (NPCSkin skin : RegistryManager.ROLE_SKIN) {
//            this.addProperty(skin);
//        }
//    }
//}
