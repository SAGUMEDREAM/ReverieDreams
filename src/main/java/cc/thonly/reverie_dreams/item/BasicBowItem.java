package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

@Setter
@Getter
@ToString
public class BasicBowItem extends BowItem implements PolymerItem, PolymerClientDecoded, PolymerKeepModel, IdentifierGetter {
    final Identifier identifier;

    public BasicBowItem(String path, Settings settings) {
        this(Touhou.id(path), settings);
    }

    public BasicBowItem(Identifier identifier, Settings settings) {
        super(settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, identifier)));
        this.identifier = identifier;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.BOW;
    }
}
