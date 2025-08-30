package cc.thonly.mystias_izakaya.entity;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.reverie_dreams.mixin.accessor.PigEntityAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.VillagerEntityAccessor;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PigVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class WildPigEntity extends PigEntity {
    public static final RegistryKey<PigVariant> VARIANT = RegistryKey.of(RegistryKeys.PIG_VARIANT, MystiasIzakaya.id("wild_pig"));
    public WildPigEntity(EntityType<? extends PigEntity> entityType, World world) {
        super(entityType, world);
    }

    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
//        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.25));

        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.add(3, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(4, new TemptGoal(this, 1.2, (stack) -> {
            return stack.isOf(Items.CARROT_ON_A_STICK);
        }, false));
        this.goalSelector.add(4, new TemptGoal(this, 1.2, (stack) -> {
            return stack.isIn(ItemTags.PIG_FOOD);
        }, false));
        this.goalSelector.add(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.targetSelector.add(3, new RevengeGoal(this).setGroupRevenge());
    }

    @Nullable
    public WildPigEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        return MIEntities.WILD_PIG_ENTITY_TYPE.create(serverWorld, SpawnReason.BREEDING);
    }


}
