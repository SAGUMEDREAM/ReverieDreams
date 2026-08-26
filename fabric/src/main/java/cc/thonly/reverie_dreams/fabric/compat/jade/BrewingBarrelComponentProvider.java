package cc.thonly.reverie_dreams.fabric.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.ResizeableElement;
import snownee.jade.api.view.ProgressView;

import java.util.Optional;

public class BrewingBarrelComponentProvider implements IBlockComponentProvider {
    public static final BrewingBarrelComponentProvider INSTANCE = new BrewingBarrelComponentProvider();


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        Optional<BrewingBarrelData> data = BrewingBarrelServerDataProvider.INSTANCE.decodeFromData(blockAccessor);
        if (data.isEmpty())
            return;
        BrewingBarrelData brewingBarrelData = data.get();
        if (brewingBarrelData.brewing()) {
            float v;
            if (brewingBarrelData.maxBrewingTick() != 0 && brewingBarrelData.maxBrewingTick() != 1) {
                v = (float) brewingBarrelData.brewingTick() / brewingBarrelData.maxBrewingTick();

                ResizeableElement progress = JadeUI.progress(new ProgressView(ProgressView.Part.of(v), Component.translatable("gui.reverie_dreams.progress"), JadeUI.progressStyle(), BoxStyle.nestedBox()));
                iTooltip.add(progress);
            }
        } else if (brewingBarrelData.output().isPresent() && brewingBarrelData.count() > 0) {
            float v = (float) brewingBarrelData.count() / brewingBarrelData.maxCount();
            ResizeableElement progress = JadeUI.progress(new ProgressView(ProgressView.Part.of(v), Component.translatable("gui.reverie_dreams.count"), JadeUI.progressStyle(), BoxStyle.nestedBox()));
            iTooltip.add(progress);

            Element item = JadeUI.item(brewingBarrelData.output().get().getLazyStack());
            iTooltip.add(item);
        }
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.BREWING_BARREL_PROVIDER;
    }
}
