package cc.thonly.reverie_dreams.registry.content.advancements;

import cc.thonly.reverie_dreams.advancement.SimpleTrigger;
import cc.thonly.reverie_dreams.advancement.UseItemTrigger;
import cc.thonly.reverie_dreams.registry.ReverieDreamsRegistries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RDCriteriaTriggers {
    public static final List<Holder<CriterionTrigger<?>>> LIST = new ArrayList<>(64);
    public static RegistrySupplier<UseItemTrigger> USE_ITEM = register(UseItemTrigger.ID, UseItemTrigger::new);
    public static RegistrySupplier<SimpleTrigger> SIMPLE_TRIGGER = register(SimpleTrigger.ID, SimpleTrigger::new);

    public static void initialize() {

    }

    @SuppressWarnings("unchecked")
    public static <T extends CriterionTrigger<?>> RegistrySupplier<T> register(Identifier key, Supplier<T> criterionTrigger) {
        Holder<CriterionTrigger<?>> holder = ReverieDreamsRegistries.CRITERION_TRIGGER.register(key.getPath(), criterionTrigger);
        LIST.add(holder);
        return (RegistrySupplier<T>) holder;
    }

}
