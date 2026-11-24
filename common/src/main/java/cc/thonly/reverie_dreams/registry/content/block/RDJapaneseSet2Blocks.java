package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.decor.workshop.*;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class RDJapaneseSet2Blocks {
    public static final Map<WorkshopKind,BlockDelegate> ENTRIES=register();
    public static final List<BlockDelegate> ALL=List.copyOf(ENTRIES.values());
    private static Map<WorkshopKind,BlockDelegate> register() {
        Map<WorkshopKind,BlockDelegate> entries=new EnumMap<>(WorkshopKind.class);
        for(var kind:WorkshopKind.values()){
            Block source=kind==WorkshopKind.KOMAINU_A||kind==WorkshopKind.KOMAINU_UN||kind==WorkshopKind.SUZURI||kind==WorkshopKind.STONE_MILL?Blocks.STONE:Blocks.SPRUCE_PLANKS;
            var properties=BlockBehaviour.Properties.ofFullCopy(source).noOcclusion();
            if(!kind.collision())properties.noCollision();
            entries.put(kind,RDBlocks.registerBlock(kind.id(),settings->switch(kind.mode()){
                case STORAGE -> new WorkshopStorageBlock(kind,settings);
                case DESK -> new WorkshopDeskBlock(settings);
                case COUNTER -> new WorkshopCounterBlock(settings);
                case BENCH -> new WorkshopBenchBlock(settings);
                case BRIDGE -> new WorkshopBridgeBlock(kind,settings);
                case RAILING -> new FenceBlock(settings);
                default -> switch(kind) {
                    case EMA_RACK,OFUDA_RACK -> new WorkshopRackBlock(kind,settings);
                    case SUZURI,BOOK_STACK,YAGEN,STONE_MILL,HERB_DRYING_TRAY,FLOWER_VASE -> new WorkshopStationBlock(kind,settings);
                    default -> new WorkshopDecorBlock(kind,settings);
                };
            },properties));
        }
        return Collections.unmodifiableMap(entries);
    }
    public static BlockDelegate get(WorkshopKind kind) { return ENTRIES.get(kind); }
    public static List<BlockDelegate> storageBlocks() { return ENTRIES.entrySet().stream().filter(e->e.getKey().storage()).map(Map.Entry::getValue).toList(); }
    public static List<BlockDelegate> stationBlocks() { return ENTRIES.entrySet().stream().filter(e -> switch(e.getKey()) {
        case EMA_RACK,OFUDA_RACK,SUZURI,BOOK_STACK,YAGEN,STONE_MILL,HERB_DRYING_TRAY,FLOWER_VASE -> true;default -> false;
    }).map(Map.Entry::getValue).toList(); }
    public static void initialize() {}
    private RDJapaneseSet2Blocks() {}
}
