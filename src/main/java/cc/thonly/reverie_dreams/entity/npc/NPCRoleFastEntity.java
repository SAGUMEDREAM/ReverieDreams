package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.minecraft.util.tvio.TagValueFunction;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class NPCRoleFastEntity extends Mob {
    private CompoundTag view;
    private SkinType skinType;

    public NPCRoleFastEntity(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    public NPCRoleFastEntity(EntityType<? extends Mob> entityType, Level world, SkinType skinType) {
        super(entityType, world);
        this.skinType = skinType;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }
        BaseNPCLikeEntity baseNPCRole = RDEntityTypes.NPC_ROLE.create(this.level(), EntitySpawnReason.MOB_SUMMONED);
        if (baseNPCRole == null) {
            return;
        }
        if (this.view != null) {
            baseNPCRole.load(this.view);
        }
        baseNPCRole.setPos(this.position());
        baseNPCRole.setXRot(this.getXRot());
        baseNPCRole.setYRot(this.getYRot());
        baseNPCRole.setUUID(this.getUUID());
        baseNPCRole.setSkinType(this.skinType);
        baseNPCRole.setCustomName(Component.translatable(this.getType().getDescriptionId()));
        this.discard();
        this.level().addFreshEntity(baseNPCRole);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData) {
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.view = compoundTag;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}
