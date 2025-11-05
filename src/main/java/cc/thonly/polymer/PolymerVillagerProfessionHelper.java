package cc.thonly.polymer;

import cc.thonly.reverie_dreams.entity.villager.RDVillagerProfessions;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.entity.PolymerVillagerProfession;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import xyz.nucleoid.packettweaker.PacketContext;

public class PolymerVillagerProfessionHelper {
    public static void bootstrap() {
        registerOverlay(RDVillagerProfessions.HAWKERS, new PolymerVillagerProfession() {
            @Override
            public VillagerProfession getPolymerReplacement(VillagerProfession object, PacketContext context) {
                return BuiltInRegistries.VILLAGER_PROFESSION.getValue(VillagerProfession.BUTCHER);
            }
        });
        registerOverlay(RDVillagerProfessions.PRIEST, new PolymerVillagerProfession() {
            @Override
            public VillagerProfession getPolymerReplacement(VillagerProfession object, PacketContext context) {
                return BuiltInRegistries.VILLAGER_PROFESSION.getValue(VillagerProfession.CLERIC);
            }
        });
        registerOverlay(RDVillagerProfessions.MONEY_SHOP_CLERK, new PolymerVillagerProfession() {
            @Override
            public VillagerProfession getPolymerReplacement(VillagerProfession object, PacketContext context) {
                return BuiltInRegistries.VILLAGER_PROFESSION.getValue(VillagerProfession.LIBRARIAN);
            }
        });
    }

    public static void registerOverlay(ResourceKey<VillagerProfession> registryKey, PolymerSyncedObject<VillagerProfession> overlay) {
        VillagerProfession profession = BuiltInRegistries.VILLAGER_PROFESSION.getValue(registryKey);
        PolymerEntityUtils.registerProfession(profession, overlay);
    }
}
