package cc.thonly.reverie_dreams.polymer.block;

import cc.thonly.reverie_dreams.workshop.*;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.*;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.*;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopRackBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Actual item components and the native layout are shared; no polling inventory scans. */
public final class WorkshopImpl implements FactoryBlock,PolymerTexturedBlock,BSMMParticleBlock {
    private final BaseFactoryBlock carrier;
    public WorkshopImpl(BaseFactoryBlock carrier){this.carrier=carrier;}
    @Override public BlockState getPolymerBlockState(BlockState state,PacketContext context){return carrier.getPolymerBlockState(state,context);}
    @Override public ElementHolder createElementHolder(ServerLevel world,BlockPos pos,BlockState state){return new Model(world,pos,state);}
    @Override public boolean tickElementHolder(ServerLevel world,BlockPos pos,BlockState state){return true;}
    public static final class Model extends BlockStateModel {
        private final ServerLevel world;private final BlockPos pos;
        private final boolean hasInventory;
        private BlockEntity bound;private WorkshopDisplayHost host;
        private final List<ItemDisplayElement> items=new ArrayList<>();
        private final Runnable callback=this::update;
        Model(ServerLevel world,BlockPos pos,BlockState state){super(state,pos,.5f);this.world=world;this.pos=pos;
            hasInventory=!(state.getBlock() instanceof WorkshopRackBlock
                    &&state.getValue(WorkshopRackBlock.HALF)==DoubleBlockHalf.UPPER);
        }
        private void bind(){
            if(!hasInventory)return;
            var chunk=world.getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4);
            if(chunk!=null&&chunk.getBlockEntity(pos) instanceof WorkshopDisplayHost next&&chunk.getBlockEntity(pos)!=bound){
                if(host!=null)host.clearDisplayUpdate(callback);bound=chunk.getBlockEntity(pos);host=next;host.setDisplayUpdate(callback);
            }
        }
        private void update(){
            if(bound==null)return;var parts=WorkshopDisplays.parts(bound,4);
            while(items.size()>parts.size())removeElement(items.removeLast());
            for(int i=0;i<parts.size();i++){
                var part=parts.get(i);ItemDisplayElement element;
                if(i==items.size()){element=ItemDisplayElementUtil.createSimple(part.stack());items.add(element);addElement(element);}else element=items.get(i);
                element.setInterpolationDuration(4);element.setItem(part.stack());element.setItemDisplayContext(ItemDisplayContext.NONE);element.setScale(new Vector3f(part.scale()));element.setOffset(new Vec3(part.x()-.5,part.y()-.5,part.z()-.5));
                element.setYaw(part.yaw()+180);element.setLeftRotation(new Quaternionf().rotationX((float)Math.toRadians(part.pitch())));element.startInterpolationIfDirty();element.tick();
            }
        }
        @Override protected void onTick(){super.onTick();if(bound==null||bound.isRemoved())bind();}
        @Override public void notifyUpdate(HolderAttachment.UpdateType type){super.notifyUpdate(type);bind();update();}
        @Override public void destroy(){if(host!=null)host.clearDisplayUpdate(callback);items.clear();bound=null;host=null;super.destroy();}
    }
}
