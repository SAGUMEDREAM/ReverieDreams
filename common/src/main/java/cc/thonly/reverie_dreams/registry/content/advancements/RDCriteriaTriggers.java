package cc.thonly.reverie_dreams.registry.content.advancements;

import cc.thonly.reverie_dreams.advancement.SimpleTrigger;
import cc.thonly.reverie_dreams.advancement.UseItemTrigger;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.delegate.RegistryDelegate;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RDCriteriaTriggers {
    public static final List<Holder<CriterionTrigger<?>>> LIST = new ArrayList<>(64);
    public static RegistryDelegate<UseItemTrigger> USE_ITEM = register(UseItemTrigger.ID, UseItemTrigger::new);
    public static RegistryDelegate<SimpleTrigger> SIMPLE_TRIGGER = register(SimpleTrigger.ID, SimpleTrigger::new);

    public static void initialize() {

    }

    @SuppressWarnings("unchecked")
    public static <T extends CriterionTrigger<?>> RegistryDelegate<T> register(Identifier key, Supplier<T> criterionTrigger) {
        Holder<CriterionTrigger<?>> holder = MCBuiltInRegistries.CRITERION_TRIGGER.register(key.getPath(), criterionTrigger);
        LIST.add(holder);
        return (RegistryDelegate<T>) holder;
    }

}
