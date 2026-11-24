package cc.thonly.reverie_dreams.entity.npc.work;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;

/**
 * A furnace owns one issued input batch. Output credit survives NPC unload/restart.
 */
public final class NPCFurnaceBatch {
    private NPCProduction.Source source;
    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int remaining;
    private int pending;

    public boolean issuedTo(NPCSimpleEntity npc) {
        return remaining > 0 && source != null && source.equals(NPCProduction.source(npc));
    }

    public boolean available(AbstractFurnaceBlockEntity furnace, BaseNPCLikeEntity npc) {
        if (source == null || source.equals(NPCProduction.source(npc)))
            return true;
        // A player may reclaim an abandoned furnace by emptying it; never leave an empty workstation locked forever.
        if (furnace.getItem(0).isEmpty() && furnace.getItem(2).isEmpty() && remaining == 0) {
            clear();
            return true;
        }
        var entity = furnace.getLevel() instanceof ServerLevel level ? level.getEntity(source.npc()) : null;
        if (entity instanceof NPCSimpleEntity previous && (!previous.isAlive() || !previous.getSkillTask().active()
                || !source.task().equals(previous.getSkillTask().id()))) {
            clear();
            return true;
        }
        return false;
    }

    public void inputChanged() {
        remaining = 0;
        input = ItemStack.EMPTY;
    }

    public void issued(NPCSimpleEntity npc, ItemStack stack, int amount) {
        source = NPCProduction.source(npc);
        input = stack.copyWithCount(1);
        remaining = amount;
        pending = 0;
        output = ItemStack.EMPTY;
    }

    public void cooked(AbstractFurnaceBlockEntity furnace, RecipeHolder<?> recipe) {
        if (source == null || remaining <= 0 || input.isEmpty() || !(recipe.value() instanceof AbstractCookingRecipe cooking))
            return;
        ItemStack actual = cooking.assemble(new SingleRecipeInput(input));
        if (actual.isEmpty())
            return;
        if (!output.isEmpty() && !ItemStack.isSameItemSameComponents(output, actual))
            return;
        output = actual.copyWithCount(1);
        remaining--;
        pending = (int) Math.min(1_000_000L, (long) pending + actual.getCount());
        if (furnace.getLevel() instanceof ServerLevel level
                && level.getEntity(source.npc()) instanceof NPCSimpleEntity npc)
            flush(npc);
    }

    public void flush(NPCSimpleEntity npc) {
        if (source == null || !source.equals(NPCProduction.source(npc)) || pending == 0 || output.isEmpty())
            return;
        if (npc.getSkillTask().recordProduction(npc, source.task(), "smelt", BuiltInRegistries.ITEM.getKey(output.getItem()).toString(), pending, false))
            pending = 0;
    }

    private void clear() {
        source = null;
        remaining = pending = 0;
        input = output = ItemStack.EMPTY;
    }

    public void save(ValueOutput out) {
        if (source == null)
            return;
        out.putString("NPCProduction.Producer", source.npc().toString());
        out.putString("NPCProduction.Task", source.task().toString());
        out.putInt("NPCProduction.Remaining", remaining);
        out.putInt("NPCProduction.Pending", pending);
        if (!input.isEmpty())
            out.store("NPCProduction.Input", ItemStack.CODEC, input);
        if (!output.isEmpty())
            out.store("NPCProduction.Output", ItemStack.CODEC, output);
    }

    public void load(ValueInput in) {
        clear();
        try {
            String producer = in.getStringOr("NPCProduction.Producer", "");
            if (producer.isEmpty())
                return;
            source = new NPCProduction.Source(UUID.fromString(producer), UUID.fromString(in.getStringOr("NPCProduction.Task", "")));
            remaining = Math.clamp(in.getIntOr("NPCProduction.Remaining", 0), 0, 64);
            pending = Math.clamp(in.getIntOr("NPCProduction.Pending", 0), 0, 1_000_000);
            input = in.read("NPCProduction.Input", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            output = in.read("NPCProduction.Output", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        } catch (RuntimeException invalid) {
            LogUtils.getLogger().error("Failed to load NPC furnace production batch (source={}); clearing batch", source, invalid);
            clear();
        }
    }
}
