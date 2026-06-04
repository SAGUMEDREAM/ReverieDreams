package cc.thonly.reverie_dreams.registry.content.armor;

import cc.thonly.reverie_dreams.armor.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RDArmorMaterials {
    public static void initialize() {
        register(SilverArmorMaterial.class);
        register(MagicIceArmorMaterial.class);
        register(EarphoneArmorMaterial.class);
        register(KoishiHatArmorMaterial.class);
        register(LowGravityBootArmorMaterial.class);
        register(CrownOfTheUnderworldArmorMaterial.class);
        register(MaidArmorMaterial.class);
        register(DreamArmorMaterial.class);
    }

    public static boolean register(Class<?> clazz) {
        try {
            Class.forName(clazz.getName());
            return true;
        } catch (Exception err) {
            log.error("Can't register Armor Material Type: ", err);
            return false;
        }
    }
}
