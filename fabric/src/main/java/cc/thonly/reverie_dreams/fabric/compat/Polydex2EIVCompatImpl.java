package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.polydex2eiv.api.ItemViewServerModifier;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTemplates;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Polydex2EIVCompatImpl {
    public static void bootstrap(
    ) {
        ItemViewServerModifier.MODIFIER.register(() -> {
            List<ItemStack> stacks = new ArrayList<>();
            Collection<DanmakuType> danmakuTypes = RegistryImpls.DANMAKU_TYPE.values();
            for (DanmakuType danmakuType : danmakuTypes) {
                List<Tuple<Item, ItemStack>> pairs = danmakuType.getColorPairs();
                for (Tuple<Item, ItemStack> pair : pairs) {
                    stacks.add(pair.getB());
                }
            }

            Collection<ItemStack> spellCardTemplates = DanmakuTemplates.getRegistryItemStackView().values();
            stacks.addAll(spellCardTemplates);

            Collection<RoleCard> roleCards = RegistryImpls.ROLE_CARD.values();
            for (RoleCard instance : roleCards) {
                stacks.add(instance.itemStack());
            }

            return stacks;
        });
    }
}
