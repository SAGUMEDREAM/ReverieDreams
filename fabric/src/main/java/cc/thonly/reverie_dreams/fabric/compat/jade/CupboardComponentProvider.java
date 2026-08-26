package cc.thonly.reverie_dreams.fabric.compat.jade;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.impl.ui.TextElementImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CupboardComponentProvider implements IBlockComponentProvider {
    public static final CupboardComponentProvider INSTANCE = new CupboardComponentProvider();

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        Optional<CupboardData> data = CupboardServerDataProvider.INSTANCE.decodeFromData(blockAccessor);
        if (data.isEmpty()) {
            return;
        }

        Optional<List<ItemStack>> itemStacks = data.get().stacks();

        itemStacks.ifPresent(list -> {
            List<Element> elements = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                ItemStack itemStack = list.get(i);

                if (itemStack.isEmpty()) {
                    continue;
                }

                elements.add(JadeUI.smallItem(itemStack));

                if ((elements.size() % 9) == 0 || i == list.size() - 1) {
                    iTooltip.add(elements);
                    elements = new ArrayList<>();
                }
            }

            if (!elements.isEmpty()) {
                iTooltip.add(elements);
            }
        });
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.CUPBOARD_PROVIDER;
    }
}
