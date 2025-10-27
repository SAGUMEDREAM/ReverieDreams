package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.EarphoneArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.server.ArmorAttributeManager;
import cc.thonly.reverie_dreams.server.ParticleTickerManager;
import cc.thonly.reverie_dreams.util.math.Vec3d2Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EarphoneItem extends ArmorItem {
    public static final List<Vec3d2Entity> VEC_3_DS = new ArrayList<>();

    public EarphoneItem(Item.Settings settings) {
        super(EarphoneArmorMaterial.INSTANCE, EquipmentType.HELMET, settings);
        ArmorAttributeManager.register(this::onAccept, this);

    }

    private void onAccept(LivingEntity livingEntity, ItemStack itemStack) {
        World world = livingEntity.getWorld();
        if (!world.isClient() && world instanceof ServerWorld sWorld && livingEntity instanceof ServerPlayerEntity player) {
            if (!VEC_3_DS.isEmpty()) {
                for (Vec3d2Entity vec3D2Entity : VEC_3_DS) {
                    if (vec3D2Entity.entity() == livingEntity) {
                        continue;
                    }
                    if (player.isSpectator()) {
                        continue;
                    }
                    if (vec3D2Entity.entity() instanceof PlayerEntity playerEntity) {
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
                        ParticleTickerManager.joinQueue(sWorld, ParticleTypes.SONIC_BOOM, 10, vec3D2Entity.vec3d(), livingEntity.getPos(), 1.0f);
                    }
                }
            }
        }
    }

//    public static synchronized void onUseTick(World world, LivingEntity user, ItemStack stack) {
//
//    }
}