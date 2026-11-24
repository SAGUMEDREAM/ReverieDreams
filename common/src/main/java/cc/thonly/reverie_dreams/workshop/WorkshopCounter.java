package cc.thonly.reverie_dreams.workshop;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.entity.*;
import cc.thonly.reverie_dreams.entity.ai.work.*;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.gui.block.RestaurantGui;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import java.lang.ref.WeakReference;
import java.util.*;
import cc.thonly.reverie_dreams.restaurant.RestaurantService;

/** Restaurant identity and settlement remain owned by the existing restaurant system. */
public final class WorkshopCounter {
    public static final Identifier ACTION=ReverieDreams.id("workshop/counter");
    private record Session(WeakReference<WorkshopStorageBlockEntity> host,String nonce,long expires){}
    private static final Map<ServerPlayer,Session> SESSIONS=new WeakHashMap<>();
    private WorkshopCounter(){}
    public static void open(Player player,WorkshopStorageBlockEntity host){
        if(!(player instanceof ServerPlayer server))return;
        var session=new Session(new WeakReference<>(host),UUID.randomUUID().toString(),player.level().getGameTime()+2400);SESSIONS.put(server,session);
        var bell=host.restaurant();boolean manage=host.canManage(player)&&(bell==null||bell.canEdit(player));
        DialogBuilder.builder(builder->{
            builder.common(common->{common.title(WorkshopWriting.text("counter_title")).setPause(false).addTextBody(WorkshopWriting.text(bell==null?"counter_unbound":"counter_bound"),300);
                if(bell!=null&&bell.linked()!=null){var menu=bell.linked().menu();common.addTextBody(WorkshopWriting.text("menu_counts",menu.entries(false).size(),menu.entries(true).size()),300);}
            });
            builder.actions(buttons->{for(String action:manage?List.of("bind","menu","serve","storage","unbind"):List.of("preview")){
                CompoundTag tag=new CompoundTag();tag.putString("session",session.nonce);tag.putString("action",action);
                buttons.addButton(WorkshopWriting.text("counter_"+action),150,Optional.of(buttons.actionBuilder().customAction(ACTION,tag)));
            }});
            builder.exitAction(WorkshopWriting.text("close"),150,Optional.empty());
        }).get().buildOrThrow().open(server);
        FurnitureSounds.read(host.getLevel(),host.getBlockPos());
    }
    public static void handle(ServerPlayer player,ServerboundCustomClickActionPacket packet){
        if(player==null||!(packet.payload().orElse(null) instanceof CompoundTag tag))return;
        var session=SESSIONS.get(player);if(session==null||!session.nonce.equals(tag.getStringOr("session","")))return;
        SESSIONS.remove(player);var host=session.host.get();
        if(host==null||host.isRemoved()||!player.isAlive()||player.isSpectator()||host.getLevel()!=player.level()||player.level().getGameTime()>session.expires
                ||player.distanceToSqr(host.getBlockPos().getCenter())>64||!player.level().hasChunkAt(host.getBlockPos())||player.level().getBlockEntity(host.getBlockPos())!=host)return;
        var bell=host.restaurant();String action=tag.getStringOr("action","");
        if(action.equals("preview")){preview(player,bell);return;}
        if(!host.canManage(player)||bell!=null&&!bell.canEdit(player))return;
        switch(action){
            case "bind" -> {
                var candidate=WorkWorld.blockEntities((ServerLevel)player.level(),new WorkArea(host.getBlockPos(),16,8)).stream()
                        .filter(be->be instanceof RestaurantBlockEntity r&&r.isBell()&&r.ready()&&r.canEdit(player)&&r.area().contains(host.getBlockPos()))
                        .map(be->(RestaurantBlockEntity)be).min(Comparator.comparingDouble(r->r.getBlockPos().distSqr(host.getBlockPos()))).orElse(null);
                if(candidate!=null)host.bindRestaurant(candidate,player);else player.sendSystemMessage(WorkshopWriting.text("counter_no_shop"));
            }
            case "unbind" -> host.unbindRestaurant(player);
            case "menu" -> {if(bell!=null&&bell.linked()!=null){new RestaurantGui(player,bell.linked()).open();FurnitureSounds.read(host.getLevel(),host.getBlockPos());}return;}
            case "storage" -> {player.openMenu(host);return;}
            case "serve" -> {
                int served=0;
                if(bell!=null)for(var npc:player.level().getEntitiesOfClass(NPCSimpleEntity.class,new AABB(host.getBlockPos()).inflate(16,8,16)))
                    if(npc.getCustomerContainer().acceptFromCounter(player,host,bell))served++;
                player.sendSystemMessage(WorkshopWriting.text("counter_served",served));
            }
            default -> {return;}
        }
        open(player,host);
    }
    private static void preview(ServerPlayer player,RestaurantBlockEntity bell){
        if(bell==null||bell.linked()==null)return;
        DialogBuilder.builder(builder->{builder.common(common->{common.title(WorkshopWriting.text("counter_menu")).setPause(false);
            common.addTextBody(WorkshopWriting.text("menu_counts",bell.linked().menu().entries(false).size(),bell.linked().menu().entries(true).size()),280);
            for(boolean drinks:new boolean[]{false,true})for(var id:bell.linked().menu().entries(drinks))common.addTextBody(RestaurantService.item(id).getDefaultInstance().getHoverName(),280);
        });builder.actions(buttons->buttons.addButton(WorkshopWriting.text("close"),150,Optional.empty()));}).get().buildOrThrow().open(player);
        FurnitureSounds.read(player.level(),bell.getBlockPos());
    }
}
