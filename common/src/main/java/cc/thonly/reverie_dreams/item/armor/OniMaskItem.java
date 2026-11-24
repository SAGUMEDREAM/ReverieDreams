package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.armor.OniMaskArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 鬼面：佩戴后散发「鬼之威压」，使周围的敌对怪物心生畏惧而虚弱。
 * 具体数值：半径 8 格内的敌对生物每 2 秒获得 4 秒的虚弱 I。
 */
public class OniMaskItem extends ArmorItem {
    public static final double INTIMIDATION_RADIUS = 8.0D;
    public static final int INTIMIDATION_INTERVAL = 40;
    public static final int WEAKNESS_DURATION = 80;

    public OniMaskItem(Properties settings) {
        super(OniMaskArmorMaterial.INSTANCE, ArmorType.HELMET, settings);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel serverLevel, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemStack, serverLevel, entity, slot);
        if (!(entity instanceof LivingEntity wearer)) {
            return;
        }
        if (!wearer.getItemBySlot(EquipmentSlot.HEAD).is(itemStack.getItem())) {
            return;
        }
        if (serverLevel.getGameTime() % INTIMIDATION_INTERVAL != 0) {
            return;
        }
        AABB area = wearer.getBoundingBox().inflate(INTIMIDATION_RADIUS);
        List<Monster> monsters = serverLevel.getEntitiesOfClass(Monster.class, area,
                mob -> mob.isAlive() && !mob.isAlliedTo(wearer) && !(mob instanceof FriendlyFaction));
        for (Monster monster : monsters) {
            monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION, 0, false, false, false));
        }
    }
}