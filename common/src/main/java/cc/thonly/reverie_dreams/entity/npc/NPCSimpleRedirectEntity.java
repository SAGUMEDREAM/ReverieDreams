package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.data.npc.RoleType;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("resource")
public class NPCSimpleRedirectEntity extends Mob {
    private ValueInput view;
    private RoleType roleType;
    private SkinType skinType;

    public NPCSimpleRedirectEntity(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    public NPCSimpleRedirectEntity(EntityType<? extends Mob> entityType, Level world, RoleType roleType) {
        super(entityType, world);
        this.roleType = roleType;
        this.skinType = roleType.getSkinType();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }
        NPCCompanionEntity baseNPCRole = RDEntityTypes.NPC_SIMPLE_ENTITY.value().create(this.level(), EntitySpawnReason.MOB_SUMMONED);
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
        baseNPCRole.setRoleType(this.roleType);
        this.discard();
        this.level().addFreshEntity(baseNPCRole);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData) {
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData);
    }

    @Override
    public void load(ValueInput view) {
        super.load(view);
        this.view = view;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}
