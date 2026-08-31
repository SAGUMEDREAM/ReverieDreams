package cc.thonly.reverie_dreams.fabric.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.TextElement;
import snownee.jade.impl.ui.TextElementImpl;

import java.util.Optional;

public class MusicBlockComponentProvider implements IBlockComponentProvider {
    public static final MusicBlockComponentProvider INSTANCE = new MusicBlockComponentProvider();

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        Optional<MusicBlockData> data = MusicBlockServerDataProvider.INSTANCE.decodeFromData(blockAccessor);
        if (data.isEmpty())
            return;
        MusicBlockData musicBlockData = data.get();
        TextElement text = new TextElementImpl(Component.literal(musicBlockData.midiName()));
        iTooltip.add(text);
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.MUSIC_BLOCK_DATA_PROVIDER;
    }
}
