package cc.thonly.reverie_dreams.item.base;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public abstract class BasicPolymerMiningToolItem extends MiningToolItem implements PolymerItem, PolymerClientDecoded, PolymerKeepModel, IdentifierGetter {
    final Identifier identifier;
    final Item vanillaItem = Items.TRIAL_KEY;
    public static final List<MiningToolItem> ITEMS = new ArrayList<>();

    public BasicPolymerMiningToolItem(String path, ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        this(Touhou.id(path), material, attackDamage, attackSpeed, settings);
    }

    public BasicPolymerMiningToolItem(Identifier identifier, ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(material, ModTags.BlockTypeTag.MIN_TOOL, attackDamage, attackSpeed, material.applySwordSettings(settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, identifier)), attackDamage, attackSpeed));
        this.identifier = identifier;
        ITEMS.add(this);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return this.vanillaItem;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context) {
        ItemStack stack = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context);
        return stack;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return this.identifier;
    }

}
