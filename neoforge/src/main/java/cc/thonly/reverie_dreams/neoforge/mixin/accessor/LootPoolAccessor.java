package cc.thonly.reverie_dreams.neoforge.mixin.accessor;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LootPool.class)
public interface LootPoolAccessor {
    @Accessor("rolls")
    NumberProvider fabric_getRolls();

    @Accessor("bonusRolls")
    NumberProvider fabric_getBonusRolls();

    @Accessor("entries")
    List<LootPoolEntryContainer> fabric_getEntries();

    @Accessor("conditions")
    List<LootItemCondition> fabric_getConditions();

    @Accessor("functions")
    List<LootItemFunction> fabric_getFunctions();

    @Accessor("name")
    @Nullable
    String reverie_dreams$getName();
}
