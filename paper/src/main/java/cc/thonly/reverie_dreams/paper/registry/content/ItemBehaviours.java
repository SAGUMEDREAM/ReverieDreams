package cc.thonly.reverie_dreams.paper.registry.content;

import cc.thonly.reverie_dreams.paper.item.armor.*;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorType;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviors;

public class ItemBehaviours {

    public static final ItemBehaviorType<CrownOfTheUnderworldItemBehaviour> CROWN_OF_THE_UNDER_WORLD_ARMOR = ItemBehaviors.register(ItemBehaviourKey.CROWN_OF_THE_UNDER_WORLD_ARMOR.get(), CrownOfTheUnderworldItemBehaviour.FACTORY);
    public static final ItemBehaviorType<DreamArmorItemBehaviour> DREAM_ARMOR = ItemBehaviors.register(ItemBehaviourKey.DREAM_ARMOR.get(), DreamArmorItemBehaviour.FACTORY);
    public static final ItemBehaviorType<EarphoneItemBehaviour> EARPHONE_ARMOR = ItemBehaviors.register(ItemBehaviourKey.EARPHONE_ARMOR.get(), EarphoneItemBehaviour.FACTORY);
    public static final ItemBehaviorType<KoishiHatItemBehaviour> KOISHI_HAT_ARMOR = ItemBehaviors.register(ItemBehaviourKey.KOISHI_HAT_ARMOR.get(), KoishiHatItemBehaviour.FACTORY);
    public static final ItemBehaviorType<LowGravityBootItemBehaviour> LOW_GRAVITY_BOOT_ARMOR = ItemBehaviors.register(ItemBehaviourKey.LOW_GRAVITY_BOOT_ARMOR.get(), LowGravityBootItemBehaviour.FACTORY);
    public static final ItemBehaviorType<WaterproofArmorItemBehaviour> WATERPROOF_ARMOR = ItemBehaviors.register(ItemBehaviourKey.WATERPROOF_ARMOR.get(), WaterproofArmorItemBehaviour.FACTORY);
    public static final ItemBehaviorType<SilverArmorItemBehaviour> SILVER_ARMOR = ItemBehaviors.register(ItemBehaviourKey.SILVER_ARMOR.get(), SilverArmorItemBehaviour.FACTORY);

    public static void initialize() {

    }

}
