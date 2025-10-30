package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.EarphoneArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.server.ArmorAttributeManager;
import cc.thonly.reverie_dreams.server.ParticleTickerManager;
import cc.thonly.reverie_dreams.util.math.Vec3d2Entity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;

public class EarphoneItem extends ArmorItem {
    public static final List<Vec3d2Entity> VEC_3_DS = new ArrayList<>();

    public EarphoneItem(Item.Properties settings) {
        super(EarphoneArmorMaterial.INSTANCE, ArmorType.HELMET, settings);
        ArmorAttributeManager.register(this::onAccept, this);

    }

    private void onAccept(LivingEntity livingEntity, ItemStack itemStack) {
        Level world = livingEntity.level();
        if (!world.isClientSide() && world instanceof ServerLevel sWorld && livingEntity instanceof ServerPlayer player) {
            if (!VEC_3_DS.isEmpty()) {
                for (Vec3d2Entity vec3D2Entity : VEC_3_DS) {
                    if (vec3D2Entity.entity() == livingEntity) {
                        continue;
                    }
                    if (player.isSpectator()) {
                        continue;
                    }
                    if (vec3D2Entity.entity() instanceof Player playerEntity) {
                        if (playerEntity.isSpectator()) {
                            continue;
                        }
                    }
                    double x1 = vec3D2Entity.vec3d().x;
                    double y1 = vec3D2Entity.vec3d().y;
                    double z1 = vec3D2Entity.vec3d().z;
                    double x2 = livingEntity.getX();
                    double y2 = livingEntity.getY();
                    double z2 = livingEntity.getZ();
                    double distance = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2) + Math.pow((z2 - z1), 2));
                    if (distance < 32) {
                        ParticleTickerManager.joinQueue(sWorld, ParticleTypes.SONIC_BOOM, 10, vec3D2Entity.vec3d(), livingEntity.position(), 1.0f);
                    }
                }
            }
        }
    }

//    public static synchronized void onUseTick(World world, LivingEntity user, ItemStack stack) {
//
//    }
}