package cc.thonly.reverie_dreams.api.registry.tag;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class ConventionalStructureTags {
   public static final TagKey<Structure> HIDDEN_FROM_DISPLAYERS = register("hidden_from_displayers");
   public static final TagKey<Structure> HIDDEN_FROM_LOCATOR_SELECTION = register("hidden_from_locator_selection");

   private ConventionalStructureTags() {
   }

   private static TagKey<Structure> register(String tagId) {
      return TagRegistration.STRUCTURE_TAG.registerC(tagId);
   }
}
