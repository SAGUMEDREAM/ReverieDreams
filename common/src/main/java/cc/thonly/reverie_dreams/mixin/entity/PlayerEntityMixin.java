package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.api.entity.PlayerEntityDataModifier;
import cc.thonly.reverie_dreams.entity.Ghost;
import cc.thonly.reverie_dreams.entity.ai.goal.GhostStatusEffectTargetGoal;
import cc.thonly.reverie_dreams.item.WingType;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.server.PlayerSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
    private static final long MAX_NON_SLEEPING_TIME = 10 * 60 * 20;
    @Unique
    private long reverie_dreams$nonSleepingTime = 0L;
    @Unique
    private long reverie_dreams$nextHauntAttempt;
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
        if (!(this.level() instanceof ServerLevel level) || !((Object) this instanceof ServerPlayer player)) {
            return;
        }
        if (this.isSleeping()) {
            this.removeEffect(MENTAL_DISORDER);
            this.reverie_dreams$nonSleepingTime = 0;
            return;
        }
        if (this.hasInfiniteMaterials() || this.isSpectator() || !this.isAlive()) {
            return;
        }
        if (GhostStatusEffectTargetGoal.hasSilverArmor(this)) {
            this.reverie_dreams$nonSleepingTime = 0;
            return;
        }
        if (this.reverie_dreams$nonSleepingTime < MAX_NON_SLEEPING_TIME) {
            this.reverie_dreams$nonSleepingTime++;
        } else if (Ghost.canStartHaunt(level) && level.getGameTime() >= this.reverie_dreams$nextHauntAttempt) {
            this.reverie_dreams$nextHauntAttempt = level.getGameTime() + 100;
            if (Ghost.tryHaunt(level, player)) {
                this.reverie_dreams$nonSleepingTime = 0;
            }
        }
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
