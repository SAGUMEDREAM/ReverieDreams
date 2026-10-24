package cc.thonly.reverie_dreams.api.registry.tag;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class ConventionalFluidTags {
   public static final TagKey<Fluid> LAVA = register("lava");
   public static final TagKey<Fluid> WATER = register("water");
   public static final TagKey<Fluid> MILK = register("milk");
   public static final TagKey<Fluid> HONEY = register("honey");
   public static final TagKey<Fluid> GASEOUS = register("gaseous");
   public static final TagKey<Fluid> EXPERIENCE = register("experience");
   public static final TagKey<Fluid> POTION = register("potion");
   public static final TagKey<Fluid> SUSPICIOUS_STEW = register("suspicious_stew");
   public static final TagKey<Fluid> MUSHROOM_STEW = register("mushroom_stew");
   public static final TagKey<Fluid> RABBIT_STEW = register("rabbit_stew");
   public static final TagKey<Fluid> BEETROOT_SOUP = register("beetroot_soup");
   public static final TagKey<Fluid> HIDDEN_FROM_RECIPE_VIEWERS = register("hidden_from_recipe_viewers");

   private ConventionalFluidTags() {
   }

   private static TagKey<Fluid> register(String tagId) {
      return TagRegistration.FLUID_TAG.registerC(tagId);
   }
}
