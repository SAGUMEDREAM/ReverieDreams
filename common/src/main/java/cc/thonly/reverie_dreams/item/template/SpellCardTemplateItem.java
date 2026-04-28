package cc.thonly.reverie_dreams.item.template;

import net.minecraft.world.item.Item;

public class SpellCardTemplateItem extends Item {
    public SpellCardTemplateItem(Properties settings) {
        super(settings);
    }

//    @Override
//    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
//        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
//        Float damage = stack.getOrDefault(ModDataComponentTypes.Danmaku.DAMAGE, null);
//        Float scale = stack.getOrDefault(ModDataComponentTypes.Danmaku.SCALE, null);
//        Float speed = stack.getOrDefault(ModDataComponentTypes.Danmaku.SPEED, null);
//        Integer count = stack.getOrDefault(ModDataComponentTypes.Danmaku.COUNT, AbstractDanmakuItem.DEFAULT_COUNT);
//        String templateType = stack.getOrDefault(ModDataComponentTypes.Danmaku.TEMPLATE, Touhou.id("single").toString());
//
//        textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.base_type")).append(Text.translatable(Identifier.of(templateType).toTranslationKey())));
//    }
}
