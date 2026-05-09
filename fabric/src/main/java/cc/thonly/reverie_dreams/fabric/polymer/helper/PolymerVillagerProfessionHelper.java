package cc.thonly.reverie_dreams.fabric.polymer.helper;

import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerProfessions;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.entity.PolymerVillagerProfession;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class PolymerVillagerProfessionHelper {
    public static void bootstrap() {
        registerOverlay(RDVillagerProfessions.HAWKERS_KEY, new PolymerVillagerProfession() {
            @Override
            public VillagerProfession getPolymerReplacement(VillagerProfession object, PacketContext context) {
                return BuiltInRegistries.VILLAGER_PROFESSION.getValue(VillagerProfession.BUTCHER);
            }
        });
        registerOverlay(RDVillagerProfessions.PRIEST_KEY, new PolymerVillagerProfession() {
            @Override
            public VillagerProfession getPolymerReplacement(VillagerProfession object, PacketContext context) {
                return BuiltInRegistries.VILLAGER_PROFESSION.getValue(VillagerProfession.CLERIC);
            }
        });
        registerOverlay(RDVillagerProfessions.MONEY_SHOP_CLERK_KEY, new PolymerVillagerProfession() {
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
