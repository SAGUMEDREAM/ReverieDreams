package cc.thonly.reverie_dreams.component;

import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public class OverTooltipAppender implements TooltipProvider {
    public static final Codec<OverTooltipAppender> CODEC = UnitCodec.unit(OverTooltipAppender::new);

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
//        Identifier itemId = components.get(ModDataComponentTypes.REGISTRY_KEY);
//        Item item = Registries.ITEM.get(itemId);

    }
}
