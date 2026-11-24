package cc.thonly.reverie_dreams.restaurant;

import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import cc.thonly.reverie_dreams.entity.ai.work.WorkWorld;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import java.util.*;
import java.util.function.Predicate;
import cc.thonly.reverie_dreams.entity.ai.work.WorkArea;

public final class RestaurantService {
    private RestaurantService() {}
    public static boolean isVisiting(BaseNPCLikeEntity npc) {
        return npc instanceof NPCSimpleEntity guest && guest.getCustomerContainer().hasVisit()
                && guest.getCustomerContainer().isCustomerMode();
    }

    public static boolean isRestaurantShift(BaseNPCLikeEntity npc) {
        var mode = npc.getWorkMode();
        return isVisiting(npc) || npc.getWorkController().working()
                && (mode == NPCWorkModes.COOKING || mode == NPCWorkModes.SERVING);
    }
    public record Shop(RestaurantBlockEntity bell, RestaurantBlockEntity book) {
        public List<Item> items(boolean beverages) { return book.menu().available(beverages, id -> valid(id, beverages)).stream().map(RestaurantService::item).toList(); }
    }
    public static Item item(Identifier id) { return BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR); }
    public static boolean valid(Identifier id, boolean beverages) { var item = item(id); return item != Items.AIR && item.builtInRegistryHolder().is(beverages ? RDItemTags.BEVERAGE : RDItemTags.CUISINE); }
    public static Optional<Shop> findForStaff(BaseNPCLikeEntity npc) {
        return find(npc, npc.getWorkingPos(), shop -> {
            if (shop.bell().open() && shop.bell().ready()) return true;
            if (!(npc.level() instanceof ServerLevel level)) return false;
            var scene = RestaurantSceneCache.get(level, shop.bell());
            for (UUID id : scene.customers()) {
                if (level.getEntity(id) instanceof NPCSimpleEntity guest && guest.getCustomerContainer().isAutomationEligible()) return true;
            }
            return false;
        });
    }

    public static Optional<Shop> find(BaseNPCLikeEntity npc, BlockPos anchor, Predicate<Shop> accepts) {
        if (!(npc.level() instanceof ServerLevel level)) return Optional.empty();
        return WorkWorld.blockEntities(level, new WorkArea(anchor, 16, 8)).stream().filter(b -> b instanceof RestaurantBlockEntity)
                .map(b -> (RestaurantBlockEntity) b).filter(b -> b.isBell() && b.area().contains(anchor))
                .sorted(Comparator.comparingDouble(b -> b.getBlockPos().distToCenterSqr(npc.position())))
                .map(b -> new Shop(b, b.linked())).filter(shop -> shop.book() != null && shop.book().linked() == shop.bell() && accepts.test(shop)).findFirst();
    }
}
