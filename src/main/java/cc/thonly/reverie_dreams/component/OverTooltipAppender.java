package cc.thonly.reverie_dreams.component;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class OverTooltipAppender implements TooltipAppender {
    public static final Codec<OverTooltipAppender> CODEC = Codec.unit(OverTooltipAppender::new);

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
//        Identifier itemId = components.get(ModDataComponentTypes.REGISTRY_KEY);
//        Item item = Registries.ITEM.get(itemId);

    }
}
