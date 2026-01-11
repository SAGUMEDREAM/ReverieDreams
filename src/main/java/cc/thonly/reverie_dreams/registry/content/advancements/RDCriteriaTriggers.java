package cc.thonly.reverie_dreams.registry.content.advancements;

import cc.thonly.reverie_dreams.advancement.*;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class RDCriteriaTriggers {
    public static final List<CriterionTrigger<?>> LIST = new ArrayList<>(64);
    public static final UseItemTrigger USE_ITEM = register(UseItemTrigger.ID, new UseItemTrigger());
    public static final SimpleTrigger SIMPLE_TRIGGER = register(SimpleTrigger.ID, new SimpleTrigger());

    public static void registerCriteria() {

    }

    public static <T extends CriterionTrigger<?>> T register(Identifier name, T criterionTrigger) {
        LIST.add(criterionTrigger);
        return (T) Registry.register(BuiltInRegistries.TRIGGER_TYPES, name, criterionTrigger);
    }
}
