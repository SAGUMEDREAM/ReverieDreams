package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.skin.SkinType;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class NPCRoleFastEntity extends MobEntity {
    private ReadView view;
    private SkinType skinType;

    public NPCRoleFastEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    public NPCRoleFastEntity(EntityType<? extends MobEntity> entityType, World world, SkinType skinType) {
        super(entityType, world);
        this.skinType = skinType;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.getWorld() instanceof ServerWorld)) {
            return;
        }
        BaseNPCLikeEntity baseNPCRole = ModEntities.NPC_ROLE_ENTITY.create(this.getWorld(), SpawnReason.MOB_SUMMONED);
        if (baseNPCRole == null) {
            return;
        }
        if (this.view != null) {
            baseNPCRole.readData(this.view);
        }
        baseNPCRole.setPosition(this.getPos());
        baseNPCRole.setPitch(this.getPitch());
        baseNPCRole.setYaw(this.getYaw());
        baseNPCRole.setUuid(this.getUuid());
        baseNPCRole.setSkinType(this.skinType);
        baseNPCRole.setCustomName(Text.translatable(this.getType().getTranslationKey()));
        this.discard();
        this.getWorld().spawnEntity(baseNPCRole);
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);
        this.view = view;
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }
}
