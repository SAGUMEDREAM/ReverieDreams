package cc.thonly.reverie_dreams.api.resource;

import cc.thonly.reverie_dreams.api.resource.impl.ArrpProvider;
import cc.thonly.reverie_dreams.api.resource.impl.CompositeResourcePack;
import cc.thonly.reverie_dreams.api.resource.impl.QslProvider;
import cc.thonly.reverie_dreams.api.resource.impl.RawFileProvider;
import dev.architectury.platform.Platform;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class ResourceLocatorApi {
   public static final Logger LOGGER = LoggerFactory.getLogger("resource-locator-api");
   private static Method addPacksMethod = null;

   public static AssetContainer createGlobalAssetContainer() {
      CompositeResourcePack compositePack = new CompositeResourcePack(PackType.CLIENT_RESOURCES);
      if (Platform.isModLoaded("advanced_runtime_resource_pack")) {
         ArrpProvider.addPacksBeforeVanilla(compositePack);
      }

      if (Platform.isModLoaded("quilt_resource_loader")) {
         QslProvider.addPacks(compositePack);
      } else if (addPacksMethod != null) {
         try {
            addPacksMethod.invoke(compositePack);
         } catch (Exception var2) {
         }
      }

      if (Platform.isModLoaded("advanced_runtime_resource_pack")) {
         ArrpProvider.addPacksAfterVanilla(compositePack);
      }

      RawFileProvider.addPacks(compositePack);
      return compositePack;
   }

   static {
      try {
         Class<?> clazz = Class.forName("cc.thonly.reverie_dreams.fabric.resource.FapiProvider");
         Method addPacks = clazz.getDeclaredMethod("addPacks", CompositeResourcePack.class);
         addPacks.setAccessible(true);
         addPacksMethod = addPacks;
      } catch (Exception var2) {
      }
   }
}
