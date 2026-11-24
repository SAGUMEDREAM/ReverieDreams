package cc.thonly.reverie_dreams.workshop;

import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import cc.thonly.reverie_dreams.registry.content.item.RDWorkshopItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;

public final class WorkshopItems {
    public static final String TEXT="WorkshopText",AUTHOR="WorkshopAuthor";
    private WorkshopItems(){}
    public static boolean document(ItemStack stack){
        if(stack.isEmpty())return false;var key=BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key!=null&&key.getNamespace().equals("reverie_dreams")&&(key.getPath().equals("ema")||key.getPath().equals("ofuda"));
    }
    public static boolean book(ItemStack stack){return stack.is(Items.BOOK)||stack.is(Items.WRITABLE_BOOK)||stack.is(Items.WRITTEN_BOOK)||stack.is(Items.ENCHANTED_BOOK);}
    public static boolean displayable(WorkshopKind kind,ItemStack stack){
        if(stack.isEmpty())return false;
        return switch(kind){
            case EMA_RACK -> stack.is(RDWorkshopItems.EMA.asItem());
            case OFUDA_RACK -> stack.is(RDWorkshopItems.OFUDA.asItem())||stack.is(RDItems.EXORCISM_PAPER.asItem())||stack.is(RDItems.BOUNDARY_OFUDA.asItem());
            case FLOWER_VASE -> stack.is(ItemTags.FLOWERS);
            case BOOK_STACK,SCROLL_RACK,BOOK_BOX -> book(stack);
            case SUZURI -> stack.is(Items.INK_SAC);
            case OMAMORI_CABINET -> stack.is(RDWorkshopItems.OMAMORI.asItem())||document(stack);
            case SAKE_CASK_RACK -> stack.is(RDItemTags.BEVERAGE);
            case INGREDIENT_BASKET -> stack.has(DataComponents.FOOD)||stack.is(RDItemTags.CUISINE);
            default -> true;
        };
    }
    public static String text(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr(TEXT,"");}
    public static ItemStack inscribed(ItemStack source,String text,String author){
        if(!document(source)||text.isBlank()||text.length()>160)throw new IllegalArgumentException("Invalid inscription");
        var result=source.copyWithCount(1);CompoundTag data=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        data.putString(TEXT,text);data.putString(AUTHOR,author);result.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
        result.set(DataComponents.LORE,new ItemLore(List.of(Component.literal(text),Component.literal(author))));return result;
    }
    public static void give(Player player,ItemStack stack){if(!stack.isEmpty()&&!player.getInventory().add(stack))player.drop(stack,false);}
}
