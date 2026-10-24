package cc.thonly.reverie_dreams.api.registry.tag;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ConventionalEntityTypeTags {
   public static final TagKey<EntityType<?>> BOSSES = register("bosses");
   public static final TagKey<EntityType<?>> MINECARTS = register("minecarts");
   public static final TagKey<EntityType<?>> BOATS = register("boats");
   public static final TagKey<EntityType<?>> CAPTURING_NOT_SUPPORTED = register("capturing_not_supported");
   public static final TagKey<EntityType<?>> TELEPORTING_NOT_SUPPORTED = register("teleporting_not_supported");

   private ConventionalEntityTypeTags() {
   }

   private static TagKey<EntityType<?>> register(String tagId) {
      return TagRegistration.ENTITY_TYPE_TAG.registerC(tagId);
   }
}
