package cc.thonly.reverie_dreams.entity.npc;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractNPCEntity extends TamableAnimal implements ClientNPCSimulator, NPCMorphData {
    public static final EntityDataAccessor<String> YSM_MODEL_ID =
            SynchedEntityData.defineId(
                    AbstractNPCEntity.class,
                    EntityDataSerializers.STRING
            );

    public static final EntityDataAccessor<String> YSM_TEXTURE =
            SynchedEntityData.defineId(
                    AbstractNPCEntity.class,
                    EntityDataSerializers.STRING
            );

    protected final ServerAvatarState avatarState = new ServerAvatarStateImpl();

    protected AbstractNPCEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(YSM_MODEL_ID, "");
        builder.define(YSM_TEXTURE, "");
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.putString(
                "YsmModelId",
                this.reverie_dreams$getModelId()
        );

        output.putString(
                "YsmTexture",
                this.reverie_dreams$getTexture()
        );
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        this.reverie_dreams$setModelId(
                input.getString("YsmModelId").orElse("")
        );

        this.reverie_dreams$setTexture(
                input.getString("YsmTexture").orElse("")
        );
    }

    @Override
    public abstract @Nullable LivingEntity getOwner();

    public abstract Property getSkin();

    @Override
    public ServerAvatarState avatarState() {
        return this.avatarState;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public String reverie_dreams$getModelId() {
        return this.entityData.get(YSM_MODEL_ID);
    }

    @Override
    public synchronized void reverie_dreams$setModelId(String modelId) {
        this.entityData.set(YSM_MODEL_ID, modelId == null ? "" : modelId);
    }

    @Override
    public String reverie_dreams$getTexture() {
        return this.entityData.get(YSM_TEXTURE);
    }

    @Override
    public synchronized void reverie_dreams$setTexture(String texture) {
        this.entityData.set(YSM_TEXTURE, texture == null ? "" : texture);
    }

}
