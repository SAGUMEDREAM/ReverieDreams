package cc.thonly.reverie_dreams.workshop;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.decor.workshop.*;
import cc.thonly.reverie_dreams.block.entity.WorkshopStationBlockEntity;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.lang.ref.WeakReference;
import java.util.*;
import net.minecraft.world.Container;

public final class WorkshopWriting {
    public static final Identifier ACTION=ReverieDreams.id("workshop/write");
    private record Session(WeakReference<BlockEntity> host,InteractionHand hand,ItemStack original,String token,long expires){}
    private static final Map<ServerPlayer,Session> SESSIONS=new WeakHashMap<>();
    private WorkshopWriting(){}
    static Component text(String key,Object... args){return Component.translatable("workshop.reverie_dreams."+key,args);}
    public static boolean writingStation(BlockEntity host){
        var state=host.getBlockState();return state.getBlock() instanceof WorkshopStationBlock station&&station.kind()==WorkshopKind.SUZURI
                ||state.getBlock() instanceof WorkshopDeskBlock&&state.getValue(WorkshopDeskBlock.ACCESSORY)==WorkshopDeskBlock.Accessory.INKSTONE;
    }
    public static InteractionResult open(Level level,BlockPos pos,Player player,InteractionHand hand){
        if(player.isSpectator()||!player.mayBuild()||!WorkshopItems.document(player.getItemInHand(hand)))return InteractionResult.FAIL;
        if(!(player instanceof ServerPlayer server))return InteractionResult.SUCCESS;
        var host=level.getBlockEntity(pos);if(host==null||!writingStation(host))return InteractionResult.FAIL;
        var session=new Session(new WeakReference<>(host),hand,player.getItemInHand(hand).copyWithCount(1),UUID.randomUUID().toString(),level.getGameTime()+2400);
        SESSIONS.put(server,session);CompoundTag token=new CompoundTag();token.putString("session",session.token);
        DialogBuilder.builder(builder->{
            builder.key(ACTION).common(common->common.title(text("write_title")).setPause(false).setAfterAction(DialogAction.CLOSE)
                    .addTextBody(text("write_help"),300).input((inputs,factory)->inputs.add(factory.textInput("message",300,text("message"),true,WorkshopItems.text(session.original),160))));
            builder.actions(buttons->buttons.addButton(text("write"),150,Optional.of(buttons.actionBuilder().customAll(ACTION,token))));
            builder.exitAction(text("cancel"),150,Optional.empty());
        }).get().buildOrThrow().open(server);
        FurnitureSounds.read(level,pos);
        return InteractionResult.SUCCESS;
    }
    public static String clean(String value){return value.codePoints().filter(c->c=='\n'||c>=32&&c!=127).collect(StringBuilder::new,StringBuilder::appendCodePoint,StringBuilder::append).toString().strip();}
    public static void handle(ServerPlayer player,ServerboundCustomClickActionPacket packet){
        if(player==null||!(packet.payload().orElse(null) instanceof CompoundTag tag))return;
        var session=SESSIONS.get(player);if(session==null||!session.token.equals(tag.getStringOr("session","")))return;
        SESSIONS.remove(player);var host=session.host.get();
        if(host==null||host.isRemoved()||player.isSpectator()||!player.mayBuild()||!player.isAlive()||host.getLevel()!=player.level()
                ||player.level().getGameTime()>session.expires||player.distanceToSqr(host.getBlockPos().getCenter())>64
                ||!player.level().hasChunkAt(host.getBlockPos())||player.level().getBlockEntity(host.getBlockPos())!=host||!writingStation(host))return;
        String value=tag.getStringOr("message","");if(value.length()>160)return;value=clean(value);if(value.isBlank())return;
        var held=player.getItemInHand(session.hand);if(held.isEmpty()||!ItemStack.isSameItemSameComponents(held,session.original))return;
        if(!consumeInk(player,host)){player.sendSystemMessage(text("need_ink"));return;}
        var written=WorkshopItems.inscribed(held,value,player.getGameProfile().name());held.consume(1,player);WorkshopItems.give(player,written);FurnitureSounds.write(host);player.sendSystemMessage(text("written"));
    }
    private static boolean consumeInk(Player player,BlockEntity host){
        if(player.hasInfiniteMaterials())return true;
        if(host instanceof Container station)for(int i=0;i<station.getContainerSize();i++)if(station.getItem(i).is(Items.INK_SAC)){station.removeItem(i,1);return true;}
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(Items.INK_SAC)){player.getInventory().removeItem(i,1);return true;}
        return false;
    }
}
