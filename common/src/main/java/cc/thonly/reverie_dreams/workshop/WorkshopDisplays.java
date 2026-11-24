package cc.thonly.reverie_dreams.workshop;

import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.*;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.entity.WorkshopStationBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;

/** Shared local-space placements for native BER and Polymer item displays. */
public final class WorkshopDisplays {
    public record Part(ItemStack stack,double x,double y,double z,float scale,float yaw,float pitch){}
    private WorkshopDisplays(){}
    public static List<Part> parts(BlockEntity blockEntity){return parts(blockEntity,0);}
    public static List<Part> parts(BlockEntity blockEntity,float partialTick){
        if(!(blockEntity instanceof WorkshopDisplayHost host))return List.of();
        var contents=host.displayItems();var parts=new ArrayList<Part>();WorkshopKind kind=host.workshopKind();
        float yaw=switch(blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING)){case EAST->90;case SOUTH->180;case WEST->270;default->0;};
        int limit=switch(kind){case FLOWER_VASE,YAGEN,STONE_MILL->1;case EMA_RACK,OFUDA_RACK->6;case BOOK_STACK->3;case HERB_DRYING_TRAY->4;default->6;};
        if(kind==WorkshopKind.YAGEN){
            var wheel=new ItemStack(Items.PAPER);wheel.set(DataComponents.ITEM_MODEL,ReverieDreams.id("workshop/yagen_wheel"));
            float angle=blockEntity instanceof WorkshopStationBlockEntity station?station.grindRotation(partialTick):0;
            parts.add(new Part(wheel,.5,7.0/16,.5,1,yaw,angle));limit=2;
        }
        for(int i=0;i<contents.size()&&parts.size()<limit;i++){
            if(kind==WorkshopKind.YAGEN&&i>0)break;
            var stack=contents.get(i);if(stack.isEmpty())continue;
            double x=.5,y=.5,z=.15;float scale=.28f,pitch=0;
            switch(kind){
                case FLOWER_VASE -> {y=1.0;z=.5;scale=.75f;}
                case EMA_RACK -> {x=(4+i%3*4)/16.0;y=(i<3?25:10)/16.0;z=5.0/16;scale=.34f;}
                case OFUDA_RACK -> {x=(4+i%3*4)/16.0;y=(i<3?25:10)/16.0;z=5.0/16;scale=.44f;}
                case HERB_DRYING_TRAY -> {x=(i%2==0?4:12)/16.0;y=.15;z=(i<2?4:12)/16.0;scale=.3f;pitch=90;}
                case YAGEN -> {y=.18;z=.5;scale=.25f;pitch=90;}
                case STONE_MILL -> {y=.79;z=.5;scale=.25f;pitch=90;}
                case SUZURI -> {x=(3+i*3)/16.0;y=.17;z=.5;scale=.15f;pitch=90;}
                case BOOK_STACK -> {y=(7+i*2)/16.0;z=.5;scale=.55f;pitch=90;}
                case FUMIZUKUE -> {x=(i%2==0?4:12)/16.0;y=.54+(i/2)*.08;z=.5;scale=.27f;pitch=90;}
                case BOOK_BOX -> {x=(i%2==0?5:11)/16.0;y=.69+(i/2)*.055;z=.5;scale=.3f;pitch=90;}
                case INGREDIENT_BASKET -> {x=(i%2==0?5:11)/16.0;y=.26+(i/2)*.08;z=.5;scale=.28f;pitch=90;}
                case CONNECTED_COUNTER -> {x=(i%3*5+3)/16.0;y=1.08;z=(i<3?5:11)/16.0;scale=.22f;pitch=90;}
                case MEDICINE_CABINET -> {x=(i%3*5+3)/16.0;y=(i<3?5:11)/16.0;z=-.03;scale=.15f;}
                default -> {x=(i%3*5+3)/16.0;y=(i<3?4:11)/16.0;z=.05;scale=.23f;}
            }
            double angle=Math.toRadians(yaw),dx=x-.5,dz=z-.5;
            parts.add(new Part(stack,.5+dx*Math.cos(angle)-dz*Math.sin(angle),y,.5+dx*Math.sin(angle)+dz*Math.cos(angle),scale,yaw,pitch));
        }
        // Yagen output remains visible and can be taken independently of an empty input slot.
        if(kind==WorkshopKind.YAGEN&&contents.size()>4&&!contents.get(4).isEmpty())parts.add(new Part(contents.get(4),.5,.18,.5,.3f,yaw,90));
        return List.copyOf(parts);
    }
}
