package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.entity.GhostEntity;
import cc.thonly.reverie_dreams.entity.ai.goal.GhostStatusEffectTargetGoal;
import cc.thonly.reverie_dreams.inf.IPlayerEntity;
import cc.thonly.reverie_dreams.item.WingType;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.world.GameRulesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IPlayerEntity {

    @Shadow
    public abstract boolean hasInfiniteMaterials();

    @Unique
    private static final long MAX_NON_SLEEPING_TIME = (long) (2 * 10 * 60 * 20) / 2;
    @Unique
    private long nonSleepingTime = 0L;
    @Unique
    private boolean sleep = false;
    @Unique
    private WingType wingType = WingType.NONE;
    @Unique
    private static final Holder<MobEffect> MENTAL_DISORDER = RDStatusEffects.MENTAL_DISORDER;


    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "playSound",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"),
            cancellable = true
    )
    public void playSound(SoundEvent soundEvent, float f, float g, CallbackInfo ci) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            if (element.getClassName().startsWith("cc.thonly")) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), f, g);
                ci.cancel();
                return;
            }
        }
    }


    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        this.nonSleepingTick();
        if (this.isSleeping()) {
            if (this.hasEffect(MENTAL_DISORDER)) {
                this.removeEffect(MENTAL_DISORDER);
            }
            this.nonSleepingTime = 0;
            this.sleep = true;
        } else {
            this.sleep = false;
        }
    }

    @Unique
    public void nonSleepingTick() {
        if (this.hasInfiniteMaterials()) {
            return;
        }
        MinecraftServer server = this.level().getServer();
        Level world = this.level();
        if (server == null) {
            return;
        }
        if (world instanceof ServerLevel serverWorld) {
            if (GhostStatusEffectTargetGoal.hasSilverArmor(this)) {
                this.nonSleepingTime = 0;
                return;
            }
            if (this.nonSleepingTime < MAX_NON_SLEEPING_TIME) {
                this.nonSleepingTime++;
            } else if (ReverieDreamsConfiguration.ENABLE_GHOST_SPAWN && serverWorld.getGameRules().get(GameRulesInit.DO_GHOST)){
                this.trySpawnGhost();
                this.addEffect(new MobEffectInstance(RDStatusEffects.MENTAL_DISORDER, 20 * 60 * 5));
                DelayedTask.whenTick(server, () -> this.sleep, 20 * 60 * 2, this::trySpawnGhost, () -> {

                });
                this.nonSleepingTime = 0;
            }
        }
    }

    @Unique
    private void trySpawnGhost() {
        if (!ReverieDreamsConfiguration.ENABLE_GHOST_SPAWN) {
            return;
        }
        var server = this.level().getServer();
        if (server == null) {
            return;
        }
        var world = this.level();
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }
        boolean value = serverWorld.getGameRules().get(GameRulesInit.DO_GHOST);
        if (!value) {
            return;
        }
        if (world.equals(server.overworld())) {
            BlockPos origin = this.blockPosition();
            BlockPos a = this.getRandomPos(origin);
            BlockPos b = this.getRandomPos(origin);
            GhostEntity aMob = new GhostEntity(world);
            aMob.setPosRaw(a.getX(), a.getY(), a.getZ());
            GhostEntity bMob = new GhostEntity(world);
            bMob.setPosRaw(b.getX(), b.getY(), b.getZ());
            serverWorld.addFreshEntity(aMob);
            serverWorld.addFreshEntity(bMob);
        }
    }

    @Unique
    private BlockPos getRandomPos(BlockPos origin) {
        var world = this.level();
        RandomSource random = world.getRandom();

        int offsetX = random.nextInt(11) - 5; // 0~10 - 5 => -5~5
        int offsetZ = random.nextInt(11) - 5;

        int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE, origin.getX() + offsetX, origin.getZ() + offsetZ);

        return new BlockPos(origin.getX() + offsetX, surfaceY, origin.getZ() + offsetZ);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    protected void writeCustomData(ValueOutput view, CallbackInfo ci) {
        view.putLong("NonSleepingTime", this.nonSleepingTime);
        view.store("WingType", WingType.CODEC, this.wingType);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    protected void readCustomData(ValueInput view, CallbackInfo ci) {
        this.nonSleepingTime = view.getLongOr("NonSleepingTime", 0L);
        this.wingType = view.read("WingType", WingType.CODEC).orElse(WingType.NONE);
    }

    @Unique
    @Override
    public void reverie_dreams$setNonSleepingTime(long time) {
        this.nonSleepingTime = time;
    }

    @Unique
    @Override
    public long reverie_dreams$getNonSleepingTime() {
        return this.nonSleepingTime;
    }

    @Unique
    @Override
    public void reverie_dreams$setWingType(WingType wingType) {
        this.wingType = wingType;
    }

    @Unique
    @Override
    public WingType reverie_dreams$getWingType() {
        return this.wingType;
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
