package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.PlayerEntityDataModifier;
import cc.thonly.reverie_dreams.entity.Ghost;
import cc.thonly.reverie_dreams.entity.ai.goal.GhostStatusEffectTargetGoal;
import cc.thonly.reverie_dreams.item.WingType;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.server.PlayerSettings;
import cc.thonly.reverie_dreams.world.RDBuiltInGameRules;
import com.mojang.authlib.GameProfile;
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

@SuppressWarnings("resource")
@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityDataModifier {

    @Shadow
    public abstract boolean hasInfiniteMaterials();

    @Unique
    private static final long MAX_NON_SLEEPING_TIME = (long) (2 * 10 * 60 * 20) / 2;
    @Unique
    private long reverie_dreams$nonSleepingTime = 0L;
    @Unique
    private boolean reverie_dreams$sleep = false;
    @Unique
    private WingType reverie_dreams$wingType = WingType.NONE;
    @Unique
    private PlayerSettings reverie_dreams$playerSettings;

    @Unique
    private static final Holder<MobEffect> MENTAL_DISORDER = RDStatusEffects.MENTAL_DISORDER;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void reverie_dreams$init(Level level, GameProfile gameProfile, CallbackInfo ci) {
        this.reverie_dreams$playerSettings = new PlayerSettings((Player) (Object) this);
    }

    @Inject(method = "playSound",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"),
            cancellable = true
    )
    public void reverie_dreams$playSound(SoundEvent sound, float f, float g, CallbackInfo ci) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            if (element.getClassName().startsWith("cc.thonly")) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundSource(), f, g);
                ci.cancel();
                return;
            }
        }
    }


    @Inject(method = "tick", at = @At("TAIL"))
    public void reverie_dreams$tick(CallbackInfo ci) {
        this.nonSleepingTick();
        if (this.isSleeping()) {
            if (this.hasEffect(MENTAL_DISORDER)) {
                this.removeEffect(MENTAL_DISORDER);
            }
            this.reverie_dreams$nonSleepingTime = 0;
            this.reverie_dreams$sleep = true;
        } else {
            this.reverie_dreams$sleep = false;
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
                this.reverie_dreams$nonSleepingTime = 0;
                return;
            }
            if (this.reverie_dreams$nonSleepingTime < MAX_NON_SLEEPING_TIME) {
                this.reverie_dreams$nonSleepingTime++;
            } else if (ReverieDreams.config().enableGhostSpawn && serverWorld.getGameRules().get(RDBuiltInGameRules.DO_GHOST.value())) {
                this.reverie_dreams$trySpawnGhost();
                this.addEffect(new MobEffectInstance(RDStatusEffects.MENTAL_DISORDER, 20 * 60 * 5));
                DelayedTask.whenTick(server, () -> this.reverie_dreams$sleep, 20 * 60 * 2, this::reverie_dreams$trySpawnGhost, () -> {

                });
                this.reverie_dreams$nonSleepingTime = 0;
            }
        }
    }

    @Unique
    private void reverie_dreams$trySpawnGhost() {
        if (!ReverieDreams.config().enableGhostSpawn) {
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
        boolean value = serverWorld.getGameRules().get(RDBuiltInGameRules.DO_GHOST.value());
        if (!value) {
            return;
        }
        if (world.equals(server.overworld())) {
            BlockPos origin = this.blockPosition();
            BlockPos a = this.reverie_dreams$getRandomPos(origin);
            BlockPos b = this.reverie_dreams$getRandomPos(origin);
            Ghost aMob = new Ghost(world);
            aMob.setPosRaw(a.getX(), a.getY(), a.getZ());
            Ghost bMob = new Ghost(world);
            bMob.setPosRaw(b.getX(), b.getY(), b.getZ());
            serverWorld.addFreshEntity(aMob);
            serverWorld.addFreshEntity(bMob);
        }
    }

    @Unique
    private BlockPos reverie_dreams$getRandomPos(BlockPos origin) {
        var world = this.level();
        RandomSource random = world.getRandom();

        int offsetX = random.nextInt(11) - 5; // 0~10 - 5 => -5~5
        int offsetZ = random.nextInt(11) - 5;

        int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE, origin.getX() + offsetX, origin.getZ() + offsetZ);

        return new BlockPos(origin.getX() + offsetX, surfaceY, origin.getZ() + offsetZ);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    protected void reverie_dreams$writeCustomData(ValueOutput view, CallbackInfo ci) {
        view.putLong("NonSleepingTime", this.reverie_dreams$nonSleepingTime);
        view.store("WingType", WingType.CODEC, this.reverie_dreams$wingType);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    protected void reverie_dreams$readCustomData(ValueInput view, CallbackInfo ci) {
        this.reverie_dreams$nonSleepingTime = view.getLongOr("NonSleepingTime", 0L);
        this.reverie_dreams$wingType = view.read("WingType", WingType.CODEC).orElse(WingType.NONE);
    }

    @Unique
    public PlayerSettings reverie_dreams$getPlayerSettings() {
        return this.reverie_dreams$playerSettings;
    }

    @Unique
    @Override
    public void reverie_dreams$setNonSleepingTime(long time) {
        this.reverie_dreams$nonSleepingTime = time;
    }

    @Unique
    @Override
    public long reverie_dreams$getNonSleepingTime() {
        return this.reverie_dreams$nonSleepingTime;
    }

    @Unique
    @Override
    public void reverie_dreams$setWingType(WingType wingType) {
        this.reverie_dreams$wingType = wingType;
    }

    @Unique
    @Override
    public WingType reverie_dreams$getWingType() {
        return this.reverie_dreams$wingType;
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
