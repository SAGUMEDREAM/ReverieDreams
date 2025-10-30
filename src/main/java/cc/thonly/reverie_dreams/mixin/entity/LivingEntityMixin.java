package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.effect.ModStatusEffects;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.interfaces.IBedBlockEntity;
import cc.thonly.reverie_dreams.interfaces.ILivingEntity;
import cc.thonly.reverie_dreams.interfaces.IWorld;
import cc.thonly.reverie_dreams.item.armor.DreamArmorItem;
import cc.thonly.reverie_dreams.item.armor.EarphoneItem;
import cc.thonly.reverie_dreams.item.armor.KoishiHatItem;
import cc.thonly.reverie_dreams.item.prop.DreamPillowItem;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingEntity {
    @Shadow
    public abstract boolean hasEffect(Holder<MobEffect> effect);

    @Shadow
    public abstract boolean addEffect(MobEffectInstance effect);

    @Shadow
    public abstract void setHealth(float health);

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract float getHealth();

    @Shadow
    @Nullable
    protected abstract SoundEvent getDeathSound();

    @Shadow
    protected abstract @Nullable SoundEvent getHurtSound(DamageSource source);

    @Shadow
    @Nullable
    public abstract AttributeInstance getAttribute(Holder<Attribute> attribute);

    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    @Shadow
    public abstract Optional<BlockPos> getSleepingPos();

    @Shadow
    public float yHeadRot;

    @Unique
    public double manpozuchiUsingState = 1;
    @Unique
    public float maxHealthModifier = 0f;
    @Unique
    public int deathCount = 0;
    @Unique
    private int deathCountResetTimer = 0;
    @Unique
    private ServerLevel kanjuWorld;
    @Unique
    private BlockPos kanjuBlockPos = new BlockPos(0, 0, 0);
    @Unique
    private BlockPos tempSleepPosition;


    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    public void setKanju(ServerLevel world, BlockPos blockPos) {
        this.kanjuWorld = world;
        this.kanjuBlockPos = blockPos;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void setMaxHealth(EntityType<? extends LivingEntity> entityType, Level world, CallbackInfo ci) {
        if (this.maxHealthModifier < 0) {
            this.maxHealthModifier = 0;
        }
        AttributeInstance maxHealthAttributeInstance = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttributeInstance != null) {
            maxHealthAttributeInstance.setBaseValue(Math.abs(this.getMaxHealth() + this.maxHealthModifier));
        }
        if (world instanceof ServerLevel) {
            this.kanjuWorld = (ServerLevel) world;
        }
    }

    @Inject(method = "stopSleeping", at = @At(value = "HEAD"))
    public void wakeUpHead(CallbackInfo ci) {
        Optional<BlockPos> blockPos = this.entityData.get(LivingEntity.SLEEPING_POS_ID);
        blockPos.ifPresent(pos -> this.tempSleepPosition = pos);
    }

    @Inject(method = "stopSleeping", at = @At(value = "TAIL"))
    public void wakeUp(CallbackInfo ci) {
        MinecraftServer server = this.getServer();
        if (server == null) {
            return;
        }
        Level world = this.level();
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }
        IWorld iWorld = (IWorld) world;
        ResourceKey<Level> dreamWorldKey = iWorld.getDreamWorld();
        ServerLevel dreamWorld = server.getLevel(dreamWorldKey);
        if (dreamWorld == null) {
            return;
        }
        ServerLevel overworld = server.overworld();
        if (serverWorld.equals(dreamWorld)) {
            BlockPos spawnPos = overworld.getSharedSpawnPos();
            this.teleportTo(server.overworld(), spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    EnumSet.noneOf(Relative.class), this.getYRot(), this.getXRot(), true);
            serverWorld.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
            return;
        }
        Optional<BlockPos> sleepingPosition = Optional.ofNullable(this.tempSleepPosition);
        sleepingPosition.ifPresent(pos -> {
            Tuple<Boolean, BlockPos> bedHead = DreamPillowItem.getBedHead(serverWorld, pos);
            if (
                    bedHead.getA() &&
                            this.level().getBlockEntity(bedHead.getB()) instanceof BedBlockEntity bedBlockEntity &&
                            this.level() == server.overworld()
            ) {
                IBedBlockEntity iBedBlockEntity = (IBedBlockEntity) bedBlockEntity;
                if (iBedBlockEntity.hasDreamPillow()) {
                    this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 5));
                    this.teleportTo(dreamWorld, this.getX() + 0.5, this.getY(), this.getZ() + 0.5,
                            EnumSet.noneOf(Relative.class), this.getYRot(), this.getXRot(), true);

                    BlockPos targetPos = findSafeTeleportPos(dreamWorld, new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ()));
                    this.teleportTo(dreamWorld, targetPos.getX() + 0.5, targetPos.getY() + 5, targetPos.getZ() + 0.5,
                            EnumSet.noneOf(Relative.class), this.getYRot(), this.getXRot(), true);
                    serverWorld.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.5, 0.5, 0.5, 0.1);

                }
            }
        });
    }

    @Unique
    private BlockPos findSafeTeleportPos(ServerLevel world, BlockPos pos) {
        return world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, pos);
    }


//    @Inject(method = "getMaxHealth", at = @At("RETURN"), cancellable = true)
//    public void getMaxHealth(CallbackInfoReturnable<Float> ci) {
//        float baseMaxHealth = ci.getReturnValue();
//        float modifiedMaxHealth = baseMaxHealth + this.maxHealthModifier;
//
//        if (modifiedMaxHealth < 1.0f) {
//            modifiedMaxHealth = 1.0f;
//        }
//
//        ci.setReturnValue(modifiedMaxHealth);
//    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickBefore(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
//        this.onEarphoneTick(livingEntity, ci);
//        this.onKoishiHeadTick(livingEntity, ci);
//        this.onDreamArmorTick(livingEntity, ci);
    }

//    @Unique
//    public void onEarphoneTick(LivingEntity livingEntity, CallbackInfo ci) {
//        ItemStack stack = this.getEquippedStack(EquipmentSlot.HEAD);
//        if (!stack.isEmpty() && stack.getItem() instanceof EarphoneItem) {
//            EarphoneItem.onUseTick(this.getWorld(), livingEntity, stack);
//        }
//    }
//
//    @Unique
//    public void onKoishiHeadTick(LivingEntity livingEntity, CallbackInfo ci) {
//        ItemStack stack = this.getEquippedStack(EquipmentSlot.HEAD);
//        if (!stack.isEmpty() && stack.getItem() instanceof KoishiHatItem) {
//            KoishiHatItem.onUseTick(this.getWorld(), livingEntity, stack);
//        }
//    }
//
//    @Unique
//    public void onDreamArmorTick(LivingEntity livingEntity, CallbackInfo ci) {
//
//    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (this.hasEffect(ModStatusEffects.ELIXIR_OF_LIFE)) {
            if (this.deathCount == 1) {
                this.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 0));
            }
            if (this.deathCount == 2) {
                this.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 1));
            }
            if (this.deathCount == 3) {
                this.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 2));
                this.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20, 0));
            }
            if (this.deathCount == 3) {
                this.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 3));
                this.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20, 1));
                this.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 0));
            }
            if (this.deathCount == 3) {
                this.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 3));
                this.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20, 2));
                this.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 1));
            }
            if (this.deathCount > 3) {
                this.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 3));
                this.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20, 2));
                this.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 2));
            }
        } else {
            this.deathCount = 0;
        }

        if (!this.level().isClientSide()) {
            MinecraftServer server = this.level().getServer();
            Level world = this.level();
            double mobY = this.getY();
            ResourceKey<Level> moonKey = ((IWorld) world).getMoon();
            ResourceKey<Level> registryKey = world.dimension();
            if (server != null) {
                ServerLevel moonWorld = server.getLevel(moonKey);
                ServerLevel endWorld = server.getLevel(Level.END);
                if (moonWorld != null && endWorld != null) {
                    if (registryKey.equals(Level.END)) {
                        if (mobY >= endWorld.getHeight()) {
                            this.teleportTo(moonWorld, this.getX(), moonWorld.getHeight() - 1, this.getZ(), EnumSet.noneOf(Relative.class), this.getYRot(), this.getXRot(), true);
                        }
                    } else if (registryKey.equals(moonKey)) {
                        this.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1, 0));
                        if (mobY >= moonWorld.getHeight()) {
                            this.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40 * 20, 0));
                            this.teleportTo(endWorld, this.getX(), endWorld.getHeight() - 1, this.getZ(), EnumSet.noneOf(Relative.class), this.getYRot(), this.getXRot(), true);
                        }
                    }
                }
            }
        }

        if (!this.level().isClientSide()) {
            this.deathCountResetTimer++;
            if (this.deathCountResetTimer >= 18000) {
                this.deathCount = Math.max(0, this.deathCount - 1);
                this.deathCountResetTimer = 0;
            }
        }
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    public void damage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        MinecraftServer server = this.getServer();
        if (server == null) {
            return;
        }
        boolean isPlayer = ((LivingEntity) (Object) this) instanceof Player;
        boolean deathInElixir = this.deathInElixir(world, source, amount, cir);
        boolean deathInKanju = this.deathInKanju(world, source, amount, cir);
        if (!deathInElixir && !deathInKanju) {
            this.deathByDanmakuEntity(world, source, amount, cir);
            if ((this.getHealth() - amount <= 0f)) {
                IWorld iWorld = (IWorld) world;
                ResourceKey<Level> dreamWorldKey = iWorld.getDreamWorld();
                ServerLevel dreamWorld = server.getLevel(dreamWorldKey);
                if (this.level().equals(dreamWorld) && isPlayer) {
                    this.setHealth(this.getMaxHealth());
                    this.fallDistance = 0;
                    this.teleportTo(
                            server.overworld(),
                            server.overworld().getSharedSpawnPos().getX() + 0.5,
                            server.overworld().getSharedSpawnPos().getY() + 1.5,
                            server.overworld().getSharedSpawnPos().getZ() + 0.5,
                            EnumSet.noneOf(Relative.class), this.getYRot(), this.getXRot(), true
                    );
                    return;
                }
                AttributeInstance maxHealthAttributeInstance = this.getAttribute(Attributes.MAX_HEALTH);
                if (maxHealthAttributeInstance != null) {
                    if (this.getMaxHealth() > 20) {
                        maxHealthAttributeInstance.setBaseValue(Math.abs(this.getMaxHealth() - 2));
                    }
                }
            }
        }
    }

    @Inject(method = "hurtServer", at = @At("RETURN"), cancellable = true)
    public void damageAfter(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        List<ItemStack> armorStacks = List.of(
                this.getItemBySlot(EquipmentSlot.HEAD),
                this.getItemBySlot(EquipmentSlot.CHEST),
                this.getItemBySlot(EquipmentSlot.LEGS),
                this.getItemBySlot(EquipmentSlot.FEET)
        );
        Stream<Item> itemStream = armorStacks.stream().filter(stack -> stack.is(ModTags.ItemTypeTag.DREAM_ARMOR)).map(ItemStack::getItem).filter(item -> item instanceof DreamArmorItem);
        if (!itemStream.toList().isEmpty()) {
            RandomSource random = RandomSource.create();
            if (random.nextIntBetweenInclusive(0, 100) < 39) {
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 5 * 20));
            }
        }
    }

    @Unique
    public boolean deathByDanmakuEntity(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((this.getHealth() - amount <= 0f) && source.getDirectEntity() instanceof DanmakuEntity) {
            Entity self = (Entity) this;
            self.playSound(SoundEventInit.BIU, 0.32F, 1.0F);
            return true;
        }
        return false;
    }

    public boolean deathInKanju(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.kanjuWorld == null) {
            return false;
        }
        if (this.kanjuWorld instanceof ServerLevel serverWorld && this.hasEffect(ModStatusEffects.KANJU_KUSURI) && (this.getHealth() - amount <= 0f)) {
            this.setHealth(1f);
            this.setHealth(this.getMaxHealth());
//            System.out.println(1);
            this.teleportTo(serverWorld, this.kanjuBlockPos.getX(), this.kanjuBlockPos.getY(), this.kanjuBlockPos.getZ(), EnumSet.noneOf(Relative.class), this.getYRot(), this.getXRot(), true);
            return true;
        }
        return false;
    }

//    @Inject(method = "onDeath", at = {
//            @At("HEAD"),
//            @At("TAIL")
//    })
//    public void test(CallbackInfo ci){
//        Thread.dumpStack();
//    }

    @Unique
    public boolean deathInElixir(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.hasEffect(ModStatusEffects.ELIXIR_OF_LIFE) && (this.getHealth() - amount <= 0f)) {
            this.deathCount++;
            this.setHealth(1f);
            this.setHealth(this.getMaxHealth());
            SoundEvent hurtSound = getHurtSound(source);
            SoundEvent deathSound = getDeathSound();
            this.playSound(hurtSound, 1.0f, 1.0f);
            this.playSound(deathSound, 1.0f, 1.0f);
            this.playSound(SoundEvents.TOTEM_USE, 1.0f, 1.0f);
            for (var player : world.players()) {
                world.sendParticles(player, ParticleTypes.TOTEM_OF_UNDYING, true, false, this.getX(), this.getY(), this.getZ(), 250, 1.5, 2, 1.5, 0.5);
            }
//            System.out.println("deathInElixir");
            cir.cancel();
            return true;
        }
        return false;
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    public void onDeath(CallbackInfo ci) {
        if (this.maxHealthModifier >= 1) {
            this.maxHealthModifier--;
        }
        if (this.maxHealthModifier < 0) {
            this.maxHealthModifier = 0;
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    public void writeCustomDataToNbt(ValueOutput view, CallbackInfo ci) {
        RegistryAccess registryManager = this.registryAccess();
        view.putFloat("MaxHealthModifier", this.maxHealthModifier);
        view.putInt("DeathCount", this.deathCount);
        view.putInt("DeathCountResetTimer", this.deathCountResetTimer);
        view.putDouble("ManpozuchiUsingState", this.manpozuchiUsingState);
        view.putString("KanjuWorld", this.kanjuWorld.dimension().location().toString());
        view.putLong("KanjuBlockPos", this.kanjuBlockPos.asLong());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    public void readCustomDataFromNbt(ValueInput view, CallbackInfo ci) {
        RegistryAccess registryManager = this.registryAccess();
        MinecraftServer server = this.getServer();
        this.maxHealthModifier = view.getFloatOr("MaxHealthModifier", 0.0f);
        this.deathCount = view.getIntOr("DeathCount", 0);
        this.deathCountResetTimer = view.getIntOr("DeathCountResetTimer", 0);
        this.manpozuchiUsingState = view.getDoubleOr("ManpozuchiUsingState", 0.0);
        String kanjuWorldStr = view.getStringOr("KanjuWorld", "");
        if (kanjuWorldStr != null && !kanjuWorldStr.isEmpty()) {
            if (server != null) {
                this.kanjuWorld = server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(kanjuWorldStr)));
            }
        }
        this.kanjuBlockPos = BlockPos.of(view.getLongOr("KanjuBlockPos", new BlockPos(0, 0, 0).asLong()));
    }

    @Override
    public void setDeathCount(int deathCount) {
        this.deathCount = deathCount;
    }

    @Override
    public int getDeathCount() {
        return deathCount;
    }

    @Override
    public void setMaxHealthModifier(float value) {
        this.maxHealthModifier = value;
    }

    @Override
    public float getMaxHealthModifier() {
        return this.maxHealthModifier;
    }

    @Override
    public void setManpozuchiUsingState(double value) {
        this.manpozuchiUsingState = value;
    }

    @Override
    public double getManpozuchiUsingState() {
        return this.manpozuchiUsingState;
    }
}

