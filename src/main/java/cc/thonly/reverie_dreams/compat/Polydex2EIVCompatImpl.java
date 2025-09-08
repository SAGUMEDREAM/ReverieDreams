package cc.thonly.reverie_dreams.compat;

import cc.thonly.polydex2eiv.api.ItemViewServerModifier;
import cc.thonly.reverie_dreams.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.danmaku.SpellCardTemplates;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Polydex2EIVCompatImpl {
    public static void bootstrap(
    ) {
        ItemViewServerModifier.MODIFIER.register(() -> {
            List<ItemStack> stacks = new ArrayList<>();
            Collection<DanmakuType> danmakuTypes = RegistryManager.DANMAKU_TYPE.values();
            for (DanmakuType danmakuType : danmakuTypes) {
                List<Pair<Item, ItemStack>> pairs = danmakuType.getColorPairs();
                for (Pair<Item, ItemStack> pair : pairs) {
                    stacks.add(pair.getRight());
                }
            }

            Collection<ItemStack> spellCardTemplates = SpellCardTemplates.getRegistryItemStackView().values();
            stacks.addAll(spellCardTemplates);

            Collection<RoleCard> roleCards = RegistryManager.ROLE_CARD.values();
            for (RoleCard instance : roleCards) {
                stacks.add(instance.itemStack());
            }

            return stacks;
        });
    }
}
