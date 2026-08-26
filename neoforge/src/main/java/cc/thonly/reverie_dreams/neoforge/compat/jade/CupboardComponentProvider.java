package cc.thonly.reverie_dreams.neoforge.compat.jade;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.impl.ui.TextElementImpl;

import java.util.List;
import java.util.Optional;

public class CupboardComponentProvider implements IBlockComponentProvider {
    public static final CupboardComponentProvider INSTANCE = new CupboardComponentProvider();

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        Optional<CupboardData> data = CupboardServerDataProvider.INSTANCE.decodeFromData(blockAccessor);
        if (data.isEmpty())
            return;
        CupboardData cupboardData = data.get();
        Optional<List<ItemStack>> itemStacks = cupboardData.stacks();
        itemStacks.ifPresent(list -> {
            for (ItemStack itemStack : list) {
                Element item = JadeUI.item(itemStack);
                iTooltip.add(item);
                iTooltip.add(new TextElementImpl(itemStack.getDisplayName()));
            }
        });
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.CUPBOARD_PROVIDER;
    }
}
