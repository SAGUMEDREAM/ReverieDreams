package cc.thonly.reverie_dreams.workshop;

import cc.thonly.reverie_dreams.block.entity.WorkshopStationBlockEntity;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import cc.thonly.reverie_dreams.gui.block.WorkshopRecipeGui;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.*;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class WorkshopInteraction {
    private WorkshopInteraction(){}
    public static void inspect(Player player,WorkshopStationBlockEntity host){
        if(!(player instanceof ServerPlayer server))return;
        switch(host.workshopKind()){
            case YAGEN,STONE_MILL,HERB_DRYING_TRAY -> {new WorkshopRecipeGui(server,host).open();FurnitureSounds.read(host.getLevel(),host.getBlockPos());}
            case EMA_RACK,OFUDA_RACK,BOOK_STACK -> readBooks(player,host);
            default -> player.sendSystemMessage(WorkshopWriting.text(host.workshopKind()==WorkshopKind.SUZURI?"write_help":"place_help"));
        }
    }
    public static List<Component> pages(Container inventory){
        var pages=new ArrayList<Component>();
        for(int slot=0;slot<inventory.getContainerSize()&&pages.size()<12;slot++){
            ItemStack item=inventory.getItem(slot);String inscription=WorkshopItems.text(item);
            if(!inscription.isBlank())pages.add(Component.literal(inscription));
            var written=item.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if(written!=null)for(var page:written.pages())if(pages.size()<12)pages.add(Component.literal(page.raw().getString().substring(0,Math.min(1200,page.raw().getString().length()))));
            var draft=item.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if(draft!=null)for(var page:draft.pages())if(pages.size()<12)pages.add(Component.literal(page.raw().substring(0,Math.min(1200,page.raw().length()))));
        }
        return List.copyOf(pages);
    }
    public static boolean readBooks(Player player,Container inventory){
        if(!(player instanceof ServerPlayer server))return false;
        var pages=pages(inventory);if(pages.isEmpty()){server.sendSystemMessage(WorkshopWriting.text("empty_reading"));return false;}
        DialogBuilder.builder(builder->{builder.common(common->{common.title(WorkshopWriting.text("reading")).setPause(false);for(var page:pages)common.addTextBody(page,300);});
            builder.actions(buttons->buttons.addButton(WorkshopWriting.text("close"),150,Optional.empty()));
        }).get().buildOrThrow().open(server);
        if(inventory instanceof BlockEntity host)FurnitureSounds.read(host.getLevel(),host.getBlockPos());
        return true;
    }
}
