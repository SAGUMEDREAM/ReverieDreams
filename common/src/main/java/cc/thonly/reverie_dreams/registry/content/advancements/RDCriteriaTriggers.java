package cc.thonly.reverie_dreams.registry.content.advancements;

import cc.thonly.reverie_dreams.advancement.*;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RDCriteriaTriggers {
    public static final List<Holder<CriterionTrigger<?>>> LIST = new ArrayList<>(64);
    public static Holder<UseItemTrigger> USE_ITEM;
    public static Holder<SimpleTrigger> SIMPLE_TRIGGER;

    public static void initialize(BalmRegistrar.Scoped<CriterionTrigger<?>> scoped) {
        USE_ITEM = register(scoped, UseItemTrigger.ID, UseItemTrigger::new);
        SIMPLE_TRIGGER = register(scoped, SimpleTrigger.ID, SimpleTrigger::new);

    }

    @SuppressWarnings("unchecked")
    public static <T extends CriterionTrigger<?>> Holder<T> register(BalmRegistrar.Scoped<CriterionTrigger<?>> scoped, Identifier key, Supplier<T> criterionTrigger) {
        Holder<CriterionTrigger<?>> holder = scoped.register(key.getPath(), id -> criterionTrigger.get());
        LIST.add(holder);
        return (Holder<T>) holder;
    }

}
