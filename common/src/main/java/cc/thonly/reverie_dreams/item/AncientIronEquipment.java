package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.item.base.PickaxeItem;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.item.base.CustomSpear;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Independent iron equipment. Its appearance never grants netherite's material properties.
 */
public final class AncientIronEquipment {
    public static final ResourceKey<EquipmentAsset> ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, ReverieDreams.id("ancient_iron"));
    private static final ArmorMaterial MATERIAL = new ArmorMaterial(15,
            Map.of(ArmorType.HELMET, 2, ArmorType.CHESTPLATE, 6, ArmorType.LEGGINGS, 5, ArmorType.BOOTS, 2, ArmorType.BODY, 5),
            9, SoundEvents.ARMOR_EQUIP_IRON, 0, 0, ItemTags.REPAIRS_IRON_ARMOR, ASSET);
    public static final Map<String, Item> ITEMS = new LinkedHashMap<>();
    public static final Item HELMET = armor("helmet", ArmorType.HELMET);
    public static final Item CHESTPLATE = armor("chestplate", ArmorType.CHESTPLATE);
    public static final Item LEGGINGS = armor("leggings", ArmorType.LEGGINGS);
    public static final Item BOOTS = armor("boots", ArmorType.BOOTS);
    public static final Item SWORD = item("sword", p -> new SwordItem(ToolMaterial.IRON, 3, -2.4F, p));
    public static final Item PICKAXE = item("pickaxe", p -> new PickaxeItem(ToolMaterial.IRON, 1, -2.8F, p));
    public static final Item AXE = item("axe", p -> new AxeItem(ToolMaterial.IRON, 6, -3.1F, p));
    public static final Item SHOVEL = item("shovel", p -> new ShovelItem(ToolMaterial.IRON, 1.5F, -3, p));
    public static final Item HOE = item("hoe", p -> new HoeItem(ToolMaterial.IRON, -2, -1, p));
    public static final Item SPEAR = item("spear", p -> new CustomSpear(p.spear(ToolMaterial.IRON, .95F, .95F, .6F, 2.5F, 11, 6.75F, 5.1F, 11.25F, 4.6F)));

    private AncientIronEquipment() {
    }

    public static void initialize() {
    }

    private static Item armor(String name, ArmorType type) {
        return item(name, p -> new ArmorItem(MATERIAL, type, p.durability(type.getDurability(15))));
    }

    private static Item item(String name, Function<Item.Properties, Item> factory) {
        Item value = RDItems.registerItem("ancient_iron_" + name, factory, new Item.Properties()).asItem();
        ITEMS.put(name, value);
        return value;
    }

    public static Item replacement(Item source) {
        var id = BuiltInRegistries.ITEM.getKey(source);
        return id.getNamespace().equals("minecraft") && id.getPath().startsWith("netherite_")
                ? ITEMS.get(id.getPath().substring("netherite_".length())) : null;
    }

    /**
     * Integer bounds guarantee the actual remaining ratio is inside [70%,88%].
     */
    public static int displayDamage(int maximum, long salt) {
        int low = (int) Math.ceil(maximum * .70), high = (int) Math.floor(maximum * .88);
        long mixed = (salt ^ (salt >>> 33)) * 0xff51afd7ed558ccdL;
        return maximum - (low + Math.floorMod(mixed, high - low + 1));
    }

    public static ItemStack convertDisplay(ItemStack original, long salt) {
        Item target = replacement(original.getItem());
        if (target == null)
            return original;
        ItemStack result = new ItemStack(target, original.getCount());
        // Source display stacks are decorative: do not carry over material overrides or enchantments.
        result.setDamageValue(displayDamage(result.getMaxDamage(), salt));
        return result;
    }

    /**
     * Called only for the mansion's authored armor stands, including both hand slots.
     */
    public static void convertTemplateEquipment(CompoundTag entity, long salt) {
        if (!entity.getStringOr("id", "").equals("minecraft:armor_stand"))
            return;
        entity.putBoolean("ShowArms", true);
        convertTags(entity.getCompoundOrEmpty("equipment"), salt);
        for (String key : new String[]{"ArmorItems", "HandItems"})
            for (Tag tag : entity.getListOrEmpty(key))
                if (tag instanceof CompoundTag stack)
                    convertStackTag(stack, salt++);
    }

    private static void convertTags(CompoundTag equipment, long salt) {
        for (String slot : equipment.keySet())
            if (equipment.get(slot) instanceof CompoundTag stack)
                convertStackTag(stack, salt ^ slot.hashCode());
    }

    private static void convertStackTag(CompoundTag stack, long salt) {
        String id = stack.getStringOr("id", "");
        if (!id.startsWith("minecraft:netherite_"))
            return;
        Item target = ITEMS.get(id.substring("minecraft:netherite_".length()));
        if (target == null)
            return;
        stack.putString("id", BuiltInRegistries.ITEM.getKey(target).toString());
        String type = id.substring("minecraft:netherite_".length());
        int maximum = switch (type) {
            case "helmet" -> 165;
            case "chestplate" -> 240;
            case "leggings" -> 225;
            case "boots" -> 195;
            default -> 250;
        };
        CompoundTag components = new CompoundTag();
        components.putInt("minecraft:damage", displayDamage(maximum, salt));
        stack.put("components", components);
    }
}
