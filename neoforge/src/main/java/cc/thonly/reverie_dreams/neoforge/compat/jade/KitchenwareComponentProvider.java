package cc.thonly.reverie_dreams.neoforge.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.*;
import snownee.jade.api.config.*;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.*;
import snownee.jade.api.view.*;

import java.util.Optional;

public class KitchenwareComponentProvider implements IBlockComponentProvider {
    public static final KitchenwareComponentProvider INSTANCE = new KitchenwareComponentProvider();

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        Optional<KitchenwareData> data = KitchenwareServerDataProvider.INSTANCE.decodeFromData(blockAccessor);
        if (data.isEmpty())
            return;
        KitchenwareData kitchenwareData = data.get();

        if (kitchenwareData.target().isPresent()) {
            Element item = JadeUI.item(kitchenwareData.target().get());
            iTooltip.add(item);
        }
        float v;
        if (kitchenwareData.maxCookingTime() != 0) {
            v = ((float) kitchenwareData.maxCookingTime() - kitchenwareData.cookingTime()) / kitchenwareData.maxCookingTime();
        } else {
            v = 0;
        }

        ResizeableElement progress = JadeUI.progress(new ProgressView(ProgressView.Part.of(v), Component.translatable("gui.reverie_dreams.progress"), JadeUI.progressStyle(), BoxStyle.nestedBox()));
        iTooltip.add(progress);
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.KITCHENWARE_DATA_PROVIDER;
    }
}
