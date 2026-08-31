package cc.thonly.reverie_dreams.fabric.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.impl.ui.TextElementImpl;

import java.util.Optional;

public class GensokyoAltarComponentProvider implements IBlockComponentProvider {
    public static final GensokyoAltarComponentProvider INSTANCE = new GensokyoAltarComponentProvider();

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        Optional<GensokyoAltarData> data = GensokyoAltarServerDataProvider.INSTANCE.decodeFromData(blockAccessor);
        if (data.isEmpty())
            return;
        GensokyoAltarData gensokyoAltarData = data.get();

        if (gensokyoAltarData.itemStack().isPresent()) {
            ItemStack itemStack = gensokyoAltarData.itemStack().get();
            Element item = JadeUI.item(itemStack);
            iTooltip.add(item);
            iTooltip.add(new TextElementImpl(itemStack.getDisplayName()));
        }
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.GENSOKYO_ALTAR_DATA_PROVIDER;
    }
}
