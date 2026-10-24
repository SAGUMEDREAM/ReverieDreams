package cc.thonly.reverie_dreams.api.resource.impl;

import net.minecraft.server.packs.PackResources;

import java.util.ArrayList;

public class QslProvider {
   public static void addPacks(CompositeResourcePack pack) {
      ArrayList<PackResources> packs = new ArrayList<>();
      packs.forEach(pack::append);
   }
}
