package cc.thonly.reverie_dreams.component.tooltip;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.item.callback.ItemEvent;
import cc.thonly.reverie_dreams.api.proxy.SafeClientAccess;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.BeverageProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.data.danmaku.trajectory.PlayerTrajectoryType;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import cc.thonly.reverie_dreams.item.danmaku.SpellcardItem;
import cc.thonly.reverie_dreams.item.prop.Cheque;
import cc.thonly.reverie_dreams.item.prop.FumoLicenseItem;
import cc.thonly.reverie_dreams.item.template.DanmakuShapeCreatorItem;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import cc.thonly.reverie_dreams.item.template.SpellCardTemplateItem;
import cc.thonly.reverie_dreams.item.weapon.MagazineItem;
import cc.thonly.reverie_dreams.item.weapon.Levatin;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.BeverageProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.ItemPropDescriptionConsts;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpellCharacters;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.world.RDBuiltInGameRules;
import dev.architectury.event.Event;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("resource")
public class InitTooltips {
    public static final Map<ItemLike, List<String>> ITEM_DESCRIPTION_RENDER_DATA = new Object2ObjectOpenHashMap<>(512);
    public static final Map<ItemLike, List<String>> ITEM_DESCRIPTION_FOR_DETAIL_RENDER_DATA = new Object2ObjectOpenHashMap<>(512);

    public static void addItem(ItemLike item, String component) {
        List<String> list = ITEM_DESCRIPTION_RENDER_DATA.computeIfAbsent(item, _ -> new ArrayList<>());
        list.add(component);
    }

    public static void addItem(ItemLike item, List<String> components) {
        List<String> list = ITEM_DESCRIPTION_RENDER_DATA.computeIfAbsent(item, _ -> new ArrayList<>());
        list.addAll(components);
    }

    public static void copyItemTooltip(ItemLike from, ItemLike to) {
        List<String> list = ITEM_DESCRIPTION_RENDER_DATA.get(from);
        if (list == null) {
            return;
        }
        ITEM_DESCRIPTION_RENDER_DATA.put(to, List.copyOf(list));
    }

    public static void addItemForDetail(ItemLike item, String component) {
        List<String> list = ITEM_DESCRIPTION_FOR_DETAIL_RENDER_DATA.computeIfAbsent(item, _ -> new ArrayList<>());
        list.add(component);
    }

    public static void addItemForDetail(ItemLike item, List<String> components) {
        List<String> list = ITEM_DESCRIPTION_FOR_DETAIL_RENDER_DATA.computeIfAbsent(item, _ -> new ArrayList<>());
        list.addAll(components);
    }

    public static void copyItemTooltipForDetail(ItemLike from, ItemLike to) {
        List<String> list = ITEM_DESCRIPTION_FOR_DETAIL_RENDER_DATA.get(from);
        if (list == null) {
            return;
        }
        ITEM_DESCRIPTION_FOR_DETAIL_RENDER_DATA.put(to, List.copyOf(list));
    }

    public static List<String> getItemTooltip(ItemLike item) {
        return ITEM_DESCRIPTION_RENDER_DATA.getOrDefault(item, List.of());
    }

    public static void bootstrap() {
        registerTooltip(InitTooltips::appendItemDescription);
        registerTooltip(InitTooltips::appendItemDescriptionForDetail);
        registerTooltip(InitTooltips::appendDanmakuTooltip);
        registerTooltip(InitTooltips::appendDanmakuShapeTooltip);
        registerTooltip(InitTooltips::appendDrinkItemTooltip);
        registerTooltip(InitTooltips::appendTagFoodTooltip);
        registerTooltip(InitTooltips::appendFumoLicenseTooltip);
        registerTooltip(InitTooltips::appendRoleCardItemTooltip);
        registerTooltip(InitTooltips::appendDanmakuPropertiesTooltip);
        registerTooltip(InitTooltips::appendSpellcardTooltip);
        registerTooltip(InitTooltips::appendFastRecipeBookTooltip);
        registerTooltip(InitTooltips::appendChequeItem);
        registerTooltip(InitTooltips::appendMagazineTooltip);
        registerTooltip(InitTooltips::appendLevatinTooltip);

        ReverieDreams.COMMON_LATE_INIT.add(() -> {
            // 食材 / 食物 / 饮品
            ItemDescriptionBindings.register();
            // 道具
            ItemPropDescriptionConsts.initialize();
        });
    }

    public static void appendChequeItem(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.getItem() instanceof Cheque)) {
            return;
        }
        Component component = itemStack.get(RDDataComponentTypes.CHEQUE_NAME.get());
        int amount = itemStack.getOrDefault(RDDataComponentTypes.CHEQUE_AMOUNT.get(), 0);
        if (component != null) {
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.cheque.owner", component)));
        }
        consumer.accept(Component.empty().append(Component.translatable("item.tooltip.cheque.amount", amount)));
    }

    public static void appendLevatinTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (itemStack.getItem() instanceof Levatin) {
            consumer.accept(Levatin.formDescription(itemStack).withStyle(ChatFormatting.GRAY));
        }
    }

    public static void appendMagazineTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (itemStack.getItem() instanceof MagazineItem magazine) {
            consumer.accept(Component.translatable("item.tooltip.magazine.rounds",
                    MagazineItem.getRounds(itemStack), magazine.capacity()));
            return;
        }
        if (itemStack.getItem() instanceof WeaponOfTheMoon weapon) {
            consumer.accept(Component.translatable("item.tooltip.magazine.loaded",
                    weapon.getRounds(itemStack), weapon.capacity()));
            int level = player == null ? 0 : Math.clamp(player.experienceLevel, 0, 30);
            consumer.accept(Component.translatable("item.tooltip.lunar.damage",
                    String.format(Locale.ROOT, "%.2f", weapon.getWeaponType().damageAtLevel(level)), weapon.getWeaponType().pellets()));
            consumer.accept(Component.translatable("item.tooltip.lunar.experience",
                    String.format(Locale.ROOT, "%.1f%%", level * 25.0 / 30), level));
        }
    }

    public static void appendItemDescription(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        List<String> list = ITEM_DESCRIPTION_RENDER_DATA.get(itemStack.getItem());
        if (list == null) {
            return;
        }
        for (String line : list) {
            consumer.accept(Component.translatable(line));
        }
    }

    public static void appendItemDescriptionForDetail(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        SafeClientAccess safeClientAccess = SafeClientAccess.safeClientAccess();
        if (!safeClientAccess.isShiftDown()) {
            return;
        }
        List<String> list = ITEM_DESCRIPTION_FOR_DETAIL_RENDER_DATA.get(itemStack.getItem());
        if (list == null) {
            return;
        }
        for (String line : list) {
            consumer.accept(Component.translatable(line));
        }
    }

    public static void appendDanmakuTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.getItem() instanceof AbstractDanmakuItem abstractDanmakuItem)) {
            return;
        }
        DanmakuProperties properties = itemStack.get(RDDataComponentTypes.DANMAKU_PROPERTIES.value());
        if (properties != null) {
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.damage")).append(String.valueOf(properties.damage())));
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.speed")).append(String.valueOf(properties.actualSpeed())));
            if (PlayerTrajectoryType.isPlayerTrajectory(properties.templateId())) {
                consumer.accept(Component.translatableWithFallback("item.tooltip.danmaku_volley_multiplier",
                        "Volley damage multiplier: ×%s", Math.min(2, properties.count())));
            } else {
                consumer.accept(Component.empty().append(Component.translatable("item.tooltip.count")).append(String.valueOf(properties.count())));
            }
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.base_type")).append(PlayerTrajectoryType.displayName(properties.templateId())));
        }
    }

    public static void appendDanmakuShapeTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.getItem() instanceof DanmakuShapeCreatorItem danmakuShapeCreatorItem)) {
            return;
        }
        IngredientStack ingredientStack = itemStack.getOrDefault(RDDataComponentTypes.DANMAKU_SHAPE.value(), IngredientStack.of(Items.AIR));
        ItemStack lazyStack = ingredientStack.getLazyStack();
        consumer.accept(Component.empty().append(Component.translatable("item.tooltip.shape")).append(lazyStack.getHoverName()));

    }

    public static void appendDrinkItemTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.has(RDDataComponentTypes.DRINK_ITEM_TYPE.value()))) {
            return;
        }
        List<BeverageProperty> allProperties = BeverageProperties.get(itemStack);
        if (!allProperties.isEmpty()) {
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.food_properties")));
            for (BeverageProperty property : allProperties) {
                consumer.accept(Component.empty().append("§b+").append(Component.translatable(property.translateKey())));
            }
        }
    }

    public static void appendTagFoodTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        Collection<FoodProperty> foodProperties = FoodProperties.get(itemStack);
        if (!foodProperties.isEmpty()) {
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.food_properties")));
            for (FoodProperty foodProperty : foodProperties) {
                consumer.accept(Component.empty().append(FoodProperty.getDisplayPrefix(itemStack, foodProperty)).append(foodProperty.getTooltip()));
            }
        }
    }

    public static void appendFumoLicenseTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.getItem() instanceof FumoLicenseItem)) {
            return;
        }
        consumer.accept(Component.translatable("item.tooltip.use.villager"));
    }

    public static void appendRoleCardItemTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.getItem() instanceof RoleCardItem roleCardItem)) {
            return;
        }
        boolean renderDisabled = false;
        Optional<RoleCard> roleCardComponent = roleCardItem.getRoleCardComponent(itemStack);
        if (roleCardComponent.isEmpty() || roleCardComponent.get().isEmpty()) {
            renderDisabled = true;
        }
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            GameRules gameRules = serverLevel.getGameRules();
            if (gameRules.get(RDBuiltInGameRules.FREE_CHOICE_OF_ROLE.value())) {
                renderDisabled = false;
            }
        }
        if (renderDisabled) {
            consumer.accept(Component.translatable("item.disabled"));
        }
        consumer.accept(Component.translatable("item.tooltip.use"));
    }

    public static void appendDanmakuPropertiesTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.getItem() instanceof SpellCardTemplateItem spellCardTemplateItem)) {
            return;
        }
        DanmakuProperties properties = itemStack.get(RDDataComponentTypes.DANMAKU_PROPERTIES.value());
        if (properties != null) {
            consumer.accept(Component.empty().append(Component.translatable("item.tooltip.base_type")).append(PlayerTrajectoryType.displayName(properties.templateId())));
        }
    }

    public static void appendSpellcardTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!(itemStack.getItem() instanceof SpellcardItem)) {
            return;
        }
        Identifier patternId = itemStack.get(RDDataComponentTypes.DANMAKU_PATTERN.value());
        if (patternId == null) {
            return;
        }
        String characterKey = TouhouSpellCharacters.translationKey(patternId);
        if (characterKey == null) {
            return;
        }
        consumer.accept(Component.translatable("item.tooltip.spellcard.character", Component.translatable(characterKey))
                .withStyle(ChatFormatting.GRAY));
    }

    public static void appendFastRecipeBookTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (!itemStack.has(RDDataComponentTypes.RECIPE_MEMORY.value())) {
            return;
        }
        KitchenRecipe.IdEntry recipeIdEntry = itemStack.get(RDDataComponentTypes.RECIPE_MEMORY.value());
        if (recipeIdEntry == null) {
            return;
        }
        recipeIdEntry.map((key, recipe) -> {
            MutableComponent component = Component.empty();
            component.append(Component.translatable(recipe.getTypeInstance().toTranslateKey()));
            component.append(" | ");
            ItemStack lazyStack = recipe.getOutput().getLazyStack();
            for (IngredientStack ingredient : recipe.getIngredients()) {
                component.append(ingredient.getLazyStack().getHoverName()).append(" ");
            }
            component.append("-> ").append(lazyStack.getHoverName());
            component.append(" | %ss".formatted(recipe.getCostTime()));
            consumer.accept(component);
        });
    }

    public static void registerTooltip(ItemEvent.ItemTooltip callback) {
        Event<ItemEvent.ItemTooltip> event = getTooltipEventBus();
        event.register((itemStack, tooltipContext, tooltipDisplay, player, consumer, tooltipFlag) -> {
            if (player == null) {
                return;
            }
            invokeBypassShowOnly(callback, itemStack, tooltipContext, tooltipDisplay, player, consumer, tooltipFlag);
        });
    }

    public static boolean isShowOnly(ItemStack itemStack) {
        return itemStack.has(RDDataComponentTypes.SHOW_ONLY.value());
    }

    private static void invokeBypassShowOnly(ItemEvent.ItemTooltip callback, ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag) {
        if (isShowOnly(itemStack)) {
            return;
        }
        callback.appendTooltip(itemStack, context, display, player, consumer, flag);
    }

    public static Event<ItemEvent.ItemTooltip> getTooltipEventBus() {
        return ItemEvent.ITEM_TOOLTIP;
    }

}
