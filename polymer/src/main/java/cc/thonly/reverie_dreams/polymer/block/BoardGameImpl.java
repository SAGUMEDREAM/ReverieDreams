package cc.thonly.reverie_dreams.polymer.block;

import cc.thonly.reverie_dreams.board.BoardDisplays;
import cc.thonly.reverie_dreams.block.entity.BoardGameBlockEntity;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.*;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.*;
import eu.pb4.polymer.virtualentity.api.attachment.*;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import java.util.*;
import java.util.function.Consumer;

public final class BoardGameImpl implements FactoryBlock,PolymerTexturedBlock,BSMMParticleBlock {
    private final BaseFactoryBlock carrier;
    public BoardGameImpl(boolean table){carrier=table?BaseFactoryBlock.LOW_TABLE:BaseFactoryBlock.TRIPWIRE_FLAT;}
    @Override public BlockState getPolymerBlockState(BlockState state,PacketContext context){return carrier.getPolymerBlockState(state,context);}
    @Override public ElementHolder createElementHolder(ServerLevel world,BlockPos pos,BlockState state){return new Model(world,pos,state);}
    @Override public boolean tickElementHolder(ServerLevel world,BlockPos pos,BlockState state){return true;}
    public static final class Model extends BlockStateModel {
        private final ServerLevel world;private final BlockPos pos;
        private BoardGameBlockEntity bound;
        private final List<ItemDisplayElement> pieces=new ArrayList<>();
        private final Consumer<BoardGameBlockEntity> callback=this::update;
        Model(ServerLevel world,BlockPos pos,BlockState state){super(state,pos,.5f);this.world=world;this.pos=pos;}
        private void bind(){
            // Holders are also created inside LevelChunk's constructor. Never synchronously request that same chunk.
            var chunk=world.getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4);
            if(chunk!=null&&chunk.getBlockEntity(pos) instanceof BoardGameBlockEntity host&&bound!=host) {
                if(bound!=null)bound.clearDisplayUpdate(callback);bound=host;host.setDisplayUpdate(callback);
            }
        }
        private void update(BoardGameBlockEntity host) {
            var parts=BoardDisplays.parts(host);
            while(pieces.size()>parts.size())removeElement(pieces.removeLast());
            for(int i=0;i<parts.size();i++) {
                var part=parts.get(i);ItemDisplayElement element;
                if(i==pieces.size()){element=ItemDisplayElementUtil.createSimple(part.stack());pieces.add(element);addElement(element);}else element=pieces.get(i);
                element.setItem(part.stack());element.setItemDisplayContext(ItemDisplayContext.NONE);element.setScale(new Vector3f(part.scale()));
                element.setOffset(new Vec3(part.x()-.5,part.y()-.5,part.z()-.5));element.setYaw(part.yaw()+180);element.tick();
            }
        }
        @Override protected void onTick(){super.onTick();if(bound==null||bound.isRemoved())bind();}
        @Override public void notifyUpdate(HolderAttachment.UpdateType type){super.notifyUpdate(type);bind();if(bound!=null)update(bound);}
        @Override public void destroy(){if(bound!=null)bound.clearDisplayUpdate(callback);pieces.clear();super.destroy();}
    }
}
