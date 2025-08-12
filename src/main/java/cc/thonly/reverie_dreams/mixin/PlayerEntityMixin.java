package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.effect.ModStatusEffects;
import cc.thonly.reverie_dreams.entity.GhostEntity;
import cc.thonly.reverie_dreams.entity.ai.goal.GhostStatusEffectTargetGoal;
import cc.thonly.reverie_dreams.interfaces.IPlayerEntity;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.world.GameRulesInit;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IPlayerEntity {

    @Shadow
    public abstract boolean isInCreativeMode();

    @Unique
    private static final long MAX_NON_SLEEPING_TIME = (long) (2 * 10 * 60 * 20) / 2;
    @Unique
    private long nonSleepingTime = 0L;
    @Unique
    private boolean sleep = false;
    @Unique
    private static final RegistryEntry<StatusEffect> MENTAL_DISORDER = ModStatusEffects.MENTAL_DISORDER;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        this.nonSleepingTick();
        if (this.isSleeping()) {
            if (this.hasStatusEffect(MENTAL_DISORDER)) {
                this.removeStatusEffect(MENTAL_DISORDER);
            }
            this.nonSleepingTime = 0;
            this.sleep = true;
        } else {
            this.sleep = false;
        }
    }

    @Unique
    public void nonSleepingTick() {
        if (this.isInCreativeMode()) return;
        MinecraftServer server = this.getServer();
        World world = this.getWorld();
        if (server == null) return;
        if (world instanceof ServerWorld serverWorld) {
            if (GhostStatusEffectTargetGoal.hasSilverArmor(this)) {
                this.nonSleepingTime = 0;
                return;
            }
            if (this.nonSleepingTime < MAX_NON_SLEEPING_TIME) {
                this.nonSleepingTime++;
            } else {
                this.trySpawnGhost();
                this.addStatusEffect(new StatusEffectInstance(ModStatusEffects.MENTAL_DISORDER, 20 * 60 * 5));
                DelayedTask.whenTick(server, () -> this.sleep, 20 * 60 * 2, this::trySpawnGhost, () -> {

                });
                this.nonSleepingTime = 0;
            }
        }
    }

    @Unique
    private void trySpawnGhost() {
        var server = this.getServer();
        if (server == null) return;
        var world = this.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) return;
        boolean value = serverWorld.getGameRules().getBoolean(GameRulesInit.DO_GHOST);
        if (!value) return;
        if (world.equals(server.getOverworld())) {
            BlockPos origin = this.getBlockPos();
            BlockPos a = this.getRandomPos(origin);
            BlockPos b = this.getRandomPos(origin);
            GhostEntity aMob = new GhostEntity(world);
            aMob.setPos(a.getX(), a.getY(), a.getZ());
            GhostEntity bMob = new GhostEntity(world);
            bMob.setPos(b.getX(), b.getY(), b.getZ());
            serverWorld.spawnEntity(aMob);
            serverWorld.spawnEntity(bMob);
        }
    }

    @Unique
    private BlockPos getRandomPos(BlockPos origin) {
        var world = this.getWorld();
        Random random = world.getRandom();

        int offsetX = random.nextInt(11) - 5; // 0~10 - 5 => -5~5
        int offsetZ = random.nextInt(11) - 5;

        int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE, origin.getX() + offsetX, origin.getZ() + offsetZ);

        return new BlockPos(origin.getX() + offsetX, surfaceY, origin.getZ() + offsetZ);
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    protected void writeCustomData(WriteView view, CallbackInfo ci) {
        super.writeCustomData(view);
        view.putLong("NonSleepingTime", this.nonSleepingTime);
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    protected void readCustomData(ReadView view, CallbackInfo ci) {
        super.readCustomData(view);
        this.nonSleepingTime = view.getLong("NonSleepingTime", 0L);
    }

    @Unique
    @Override
    public void setNonSleepingTime(long time) {
        this.nonSleepingTime = time;
    }

    @Unique
    @Override
    public long getNonSleepingTime() {
        return this.nonSleepingTime;
    }

    //    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
//    public void damage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
//        Entity attacker = source.getAttacker();
//        if(attacker == null) return;
//        if (!attacker.isAttackable()) {
//            return;
//        }
//        if(!world.isClient()) {
//            List<NPCEntityImpl> npcList = world.getEntitiesByClass(
//                            NPCEntityImpl.class,
//                            new Box(this.getX() + 8, this.getY() + 8, this.getZ() + 8,
//                                    this.getX() - 8, this.getY() - 8, this.getZ() - 8
//                            ),
//                            entity -> true)
//                    .stream()
//                    .filter(entity -> entity.getUuidAsString().equalsIgnoreCase(this.getUuidAsString()))
//                    .toList();
//            if(!npcList.isEmpty()) {
//                for (var npc: npcList) {
//                    if(npc.isSit()) continue;
//                    npc.setTarget((LivingEntity) attacker);
//                }
//            }
//        }
//    }
//
//    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
//    public void attack(Entity target, CallbackInfo ci) {
//        World world = this.getEntityWorld();
//        if (!target.isAttackable()) {
//            return;
//        }
//        if(!world.isClient()) {
//            List<NPCEntityImpl> npcList = world.getEntitiesByClass(
//                            NPCEntityImpl.class,
//                            new Box(this.getX() + 8, this.getY() + 8, this.getZ() + 8,
//                                    this.getX() - 8, this.getY() - 8, this.getZ() - 8
//                            ),
//                            entity -> true)
//                    .stream()
//                    .filter(entity -> entity.getUuidAsString().equalsIgnoreCase(this.getUuidAsString()))
//                    .toList();
//            if(!npcList.isEmpty()) {
//                for (var npc: npcList) {
//                    if(npc.isSit()) continue;
//                    npc.setTarget((LivingEntity) target);
//                }
//            }
//        }
//    }
}
