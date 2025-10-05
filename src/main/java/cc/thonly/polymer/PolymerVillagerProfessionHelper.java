package cc.thonly.polymer;

import cc.thonly.reverie_dreams.entity.villager.ModVillagerProfessions;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.entity.PolymerVillagerProfession;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.village.VillagerProfession;
import xyz.nucleoid.packettweaker.PacketContext;

public class PolymerVillagerProfessionHelper {
    public static void bootstrap() {
        registerOverlay(ModVillagerProfessions.HAWKERS, new PolymerVillagerProfession() {
            @Override
            public VillagerProfession getPolymerReplacement(VillagerProfession object, PacketContext context) {
                return Registries.VILLAGER_PROFESSION.get(VillagerProfession.BUTCHER);
            }
        });
        registerOverlay(ModVillagerProfessions.PRIEST, new PolymerVillagerProfession() {
            @Override
            public VillagerProfession getPolymerReplacement(VillagerProfession object, PacketContext context) {
                return Registries.VILLAGER_PROFESSION.get(VillagerProfession.CLERIC);
            }
        });
        registerOverlay(ModVillagerProfessions.MONEY_SHOP_CLERK, new PolymerVillagerProfession() {
            @Override
            public VillagerProfession getPolymerReplacement(VillagerProfession object, PacketContext context) {
                return Registries.VILLAGER_PROFESSION.get(VillagerProfession.LIBRARIAN);
            }
        });
    }

    public static void registerOverlay(RegistryKey<VillagerProfession> registryKey, PolymerSyncedObject<VillagerProfession> overlay) {
        VillagerProfession profession = Registries.VILLAGER_PROFESSION.get(registryKey);
        PolymerEntityUtils.registerProfession(profession, overlay);
    }
}
