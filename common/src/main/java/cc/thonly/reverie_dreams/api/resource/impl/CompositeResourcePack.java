package cc.thonly.reverie_dreams.api.resource.impl;

import cc.thonly.reverie_dreams.api.resource.AssetContainer;
import cc.thonly.reverie_dreams.api.resource.ResourceLocatorApi;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.*;

public class CompositeResourcePack implements AssetContainer {
   private final Set<PackResources> resourcePacks = new LinkedHashSet<>();
   private final Map<String, List<PackResources>> packsPerNamespace = new HashMap<>();
   private final PackType type;

   public CompositeResourcePack(PackType type) {
      this.type = type;
   }

   public void append(Pack packProfile) {
      this.append(packProfile.open());
   }

   public void append(PackResources pack) {
      if (!this.resourcePacks.contains(pack)) {
         this.resourcePacks.add(pack);

         for (String namespace : pack.getNamespaces(this.type)) {
            this.packsPerNamespace.computeIfAbsent(namespace, value -> new ArrayList<>()).add(pack);
         }
      }
   }

   @Nullable
   @Override
   public IoSupplier<InputStream> getAsset(String namespace, String path) {
      List<PackResources> packs = this.packsPerNamespace.get(namespace);
      if (packs == null) {
         return null;
      } else {
         Identifier id;
         try {
            id = Identifier.fromNamespaceAndPath(namespace, path);
         } catch (IdentifierException var8) {
            ResourceLocatorApi.LOGGER.warn("Trying to retrieve asset at an invalid location: " + var8.getMessage());
            return null;
         }

         for (PackResources pack : packs) {
            IoSupplier<InputStream> asset = pack.getResource(this.type, id);
            if (asset != null) {
               return asset;
            }
         }

         return null;
      }
   }

   @NotNull
   @Override
   public List<IoSupplier<InputStream>> getAllAssets(String namespace, String path) {
      List<PackResources> packs = this.packsPerNamespace.get(namespace);
      if (packs == null) {
         return Collections.emptyList();
      } else {
         Identifier id;
         try {
            id = Identifier.fromNamespaceAndPath(namespace, path);
         } catch (IdentifierException var9) {
            ResourceLocatorApi.LOGGER.warn("Trying to lookup assets at an invalid location: " + var9.getMessage());
            return Collections.emptyList();
         }

         ArrayList<IoSupplier<InputStream>> list = new ArrayList<>();

         for (PackResources pack : packs) {
            IoSupplier<InputStream> asset = pack.getResource(this.type, id);
            if (asset != null) {
               list.add(asset);
            }
         }

         return list;
      }
   }

   @NotNull
   @Override
   public Set<String> getNamespaces() {
      return this.packsPerNamespace.keySet();
   }

   @Override
   public boolean containsAsset(String namespace, String path) {
      List<PackResources> packs = this.packsPerNamespace.get(namespace);
      if (packs == null) {
         return false;
      } else {
         try {
            Identifier id = Identifier.fromNamespaceAndPath(namespace, path);

            for (PackResources pack : packs) {
               if (pack.getResource(this.type, id) != null) {
                  return true;
               }
            }
         } catch (IdentifierException var7) {
            ResourceLocatorApi.LOGGER.warn("Trying to check if an invalid location contains an asset: " + var7.getMessage());
         }

         return false;
      }
   }

   @NotNull
   @Override
   public Set<Tuple<Identifier, IoSupplier<InputStream>>> locateFiles(String prefix) {
      ObjectArraySet<Tuple<Identifier, IoSupplier<InputStream>>> returnSet = new ObjectArraySet<>();

      for (PackResources pack : this.resourcePacks) {
         for (String namespace : pack.getNamespaces(this.type)) {
            pack.listResources(this.type, namespace, prefix, (identifier, inputStreamSupplier) -> returnSet.add(new Tuple<>(identifier, inputStreamSupplier)));
         }
      }

      return returnSet;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Resource Locator API - Composite resource pack");

      for (PackResources pack : this.resourcePacks) {
         String name;
         if (pack instanceof MoreContextPack moreContextPack) {
            name = moreContextPack.keine$getFullName();
         } else {
            name = pack.packId();
         }

         builder.append("\n - ").append(name);
      }

      return builder.toString();
   }

   @Override
   public void close() throws Exception {
      for (PackResources pack : this.resourcePacks) {
         pack.close();
      }
   }
}
