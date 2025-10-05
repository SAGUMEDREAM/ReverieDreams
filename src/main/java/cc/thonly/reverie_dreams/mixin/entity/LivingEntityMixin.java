package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.effect.ModStatusEffects;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.interfaces.IDreamPillowManager;
import cc.thonly.reverie_dreams.interfaces.ILivingEntity;
import cc.thonly.reverie_dreams.interfaces.IWorld;
import cc.thonly.reverie_dreams.item.armor.DreamArmorItem;
import cc.thonly.reverie_dreams.item.armor.EarphoneItem;
import cc.thonly.reverie_dreams.item.armor.KoishiHatItem;
import cc.thonly.reverie_dreams.item.prop.DreamPillowItem;
import cc.thonly.reverie_dreams.server.DreamPillowManager;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
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
    public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);

    @Shadow
    public abstract boolean addStatusEffect(StatusEffectInstance effect);

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
    public abstract EntityAttributeInstance getAttributeInstance(RegistryEntry<EntityAttribute> attribute);

    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Shadow
    public abstract Optional<BlockPos> getSleepingPosition();

    @Shadow
    public float headYaw;

    @Unique
    public double manpozuchiUsingState = 1;
    @Unique
    public float maxHealthModifier = 0f;
    @Unique
    public int deathCount = 0;
    @Unique
    private int deathCountResetTimer = 0;
    @Unique
    private ServerWorld kanjuWorld;
    @Unique
    private BlockPos kanjuBlockPos = new BlockPos(0, 0, 0);
    @Unique
    private BlockPos tempSleepPosition;


    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void setKanju(ServerWorld world, BlockPos blockPos) {
        this.kanjuWorld = world;
        this.kanjuBlockPos = blockPos;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void setMaxHealth(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        EntityAttributeInstance maxHealthAttributeInstance = this.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (maxHealthAttributeInstance != null) {
            maxHealthAttributeInstance.setBaseValue(Math.abs(this.getMaxHealth() + this.maxHealthModifier));
        }
        if (world instanceof ServerWorld) {
            this.kanjuWorld = (ServerWorld) world;
        }
    }

    @Inject(method = "wakeUp", at = @At(value = "HEAD"))
    public void wakeUpHead(CallbackInfo ci) {
        Optional<BlockPos> blockPos = this.dataTracker.get(LivingEntity.SLEEPING_POSITION);
        blockPos.ifPresent(pos -> this.tempSleepPosition = pos);
    }

    @Inject(method = "wakeUp", at = @At(value = "TAIL"))
    public void wakeUp(CallbackInfo ci) {
        MinecraftServer server = this.getServer();
        if (server == null) {
            return;
        }
        World world = this.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }
        IWorld iWorld = (IWorld) world;
        RegistryKey<World> dreamWorldKey = iWorld.getDreamWorld();
        ServerWorld dreamWorld = server.getWorld(dreamWorldKey);
        if (dreamWorld == null) {
            return;
        }
        ServerWorld overworld = server.getOverworld();
        if (serverWorld.equals(dreamWorld)) {
            BlockPos spawnPos = overworld.getSpawnPos();
            this.teleport(server.getOverworld(), spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    EnumSet.noneOf(PositionFlag.class), this.getYaw(), this.getPitch(), true);
            serverWorld.spawnParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
            return;
        }
        Optional<BlockPos> sleepingPosition = Optional.ofNullable(this.tempSleepPosition);
        sleepingPosition.ifPresent(pos -> {
            IDreamPillowManager iDreamPillowManager = (IDreamPillowManager) server;
            DreamPillowManager dreamPillowManager = iDreamPillowManager.getDreamPillowManager();
            Pair<Boolean, BlockPos> bedHead = DreamPillowItem.getBedHead(serverWorld, pos);
            if (bedHead.getLeft() && dreamPillowManager.get(serverWorld).contains(bedHead.getRight()) && this.getWorld() == server.getOverworld()) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 5));
                this.teleport(dreamWorld, this.getX() + 0.5, this.getY(), this.getZ() + 0.5,
                        EnumSet.noneOf(PositionFlag.class), this.getYaw(), this.getPitch(), true);

                BlockPos targetPos = findSafeTeleportPos(dreamWorld, new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ()));
                this.teleport(dreamWorld, targetPos.getX() + 0.5, targetPos.getY() + 5, targetPos.getZ() + 0.5,
                        EnumSet.noneOf(PositionFlag.class), this.getYaw(), this.getPitch(), true);
                serverWorld.spawnParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
            }
        });
    }

    @Unique
    private BlockPos findSafeTeleportPos(ServerWorld world, BlockPos pos) {
        return world.getTopPosition(Heightmap.Type.WORLD_SURFACE_WG, pos);
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
        this.onEarphoneTick(livingEntity, ci);
        this.onKoishiHeadTick(livingEntity, ci);
        this.onDreamArmorTick(livingEntity, ci);
    }

    @Unique
    public void onEarphoneTick(LivingEntity livingEntity, CallbackInfo ci) {
        ItemStack stack = this.getEquippedStack(EquipmentSlot.HEAD);
        if (!stack.isEmpty() && stack.getItem() instanceof EarphoneItem) {
            EarphoneItem.onUseTick(this.getWorld(), livingEntity, stack);
        }
    }

    @Unique
    public void onKoishiHeadTick(LivingEntity livingEntity, CallbackInfo ci) {
        ItemStack stack = this.getEquippedStack(EquipmentSlot.HEAD);
        if (!stack.isEmpty() && stack.getItem() instanceof KoishiHatItem) {
            KoishiHatItem.onUseTick(this.getWorld(), livingEntity, stack);
        }
    }

    @Unique
    public void onDreamArmorTick(LivingEntity livingEntity, CallbackInfo ci) {

    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (this.hasStatusEffect(ModStatusEffects.ELIXIR_OF_LIFE)) {
            if (this.deathCount == 1) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 0));
            }
            if (this.deathCount == 2) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1));
            }
            if (this.deathCount == 3) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 2));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, 0));
            }
            if (this.deathCount == 3) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 3));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, 1));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20, 0));
            }
            if (this.deathCount == 3) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 3));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, 2));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20, 1));
            }
            if (this.deathCount > 3) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 3));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, 2));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20, 2));
            }
        } else {
            this.deathCount = 0;
        }

        if (!this.getWorld().isClient()) {
            MinecraftServer server = this.getWorld().getServer();
            World world = this.getWorld();
            double mobY = this.getY();
            RegistryKey<World> moonKey = ((IWorld) world).getMoon();
            RegistryKey<World> registryKey = world.getRegistryKey();
            if (server != null) {
                ServerWorld moonWorld = server.getWorld(moonKey);
                ServerWorld endWorld = server.getWorld(World.END);
                if (moonWorld != null && endWorld != null) {
                    if (registryKey.equals(World.END)) {
                        if (mobY >= endWorld.getHeight()) {
                            this.teleport(moonWorld, this.getX(), moonWorld.getHeight() - 1, this.getZ(), EnumSet.noneOf(PositionFlag.class), this.getYaw(), this.getPitch(), true);
                        }
                    } else if (registryKey.equals(moonKey)) {
                        this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 1, 0));
                        if (mobY >= moonWorld.getHeight()) {
                            this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40 * 20, 0));
                            this.teleport(endWorld, this.getX(), endWorld.getHeight() - 1, this.getZ(), EnumSet.noneOf(PositionFlag.class), this.getYaw(), this.getPitch(), true);
                        }
                    }
                }
            }
        }

        if (!this.getWorld().isClient()) {
            this.deathCountResetTimer++;
            if (this.deathCountResetTimer >= 18000) {
                this.deathCount = Math.max(0, this.deathCount - 1);
                this.deathCountResetTimer = 0;
            }
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void damage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        MinecraftServer server = this.getServer();
        if (server == null) {
            return;
        }
        boolean isPlayer = ((LivingEntity) (Object) this) instanceof PlayerEntity;
        boolean deathInElixir = this.deathInElixir(world, source, amount, cir);
        boolean deathInKanju = this.deathInKanju(world, source, amount, cir);
        if (!deathInElixir && !deathInKanju) {
            this.deathByDanmakuEntity(world, source, amount, cir);
            if ((this.getHealth() - amount <= 0f)) {
                IWorld iWorld = (IWorld) world;
                RegistryKey<World> dreamWorldKey = iWorld.getDreamWorld();
                ServerWorld dreamWorld = server.getWorld(dreamWorldKey);
                if (this.getWorld().equals(dreamWorld) && isPlayer) {
                    this.setHealth(this.getMaxHealth());
                    this.fallDistance = 0;
                    this.teleport(
                            server.getOverworld(),
                            server.getOverworld().getSpawnPos().getX() + 0.5,
                            server.getOverworld().getSpawnPos().getY() + 1.5,
                            server.getOverworld().getSpawnPos().getZ() + 0.5,
                            EnumSet.noneOf(PositionFlag.class), this.getYaw(), this.getPitch(), true
                    );
                    return;
                }
                EntityAttributeInstance maxHealthAttributeInstance = this.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                if (maxHealthAttributeInstance != null) {
                    if (this.getMaxHealth() > 20) {
                        maxHealthAttributeInstance.setBaseValue(Math.abs(this.getMaxHealth() - 2));
                    }
                }
            }
        }
    }

    @Inject(method = "damage", at = @At("RETURN"), cancellable = true)
    public void damageAfter(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        List<ItemStack> armorStacks = List.of(
                this.getEquippedStack(EquipmentSlot.HEAD),
                this.getEquippedStack(EquipmentSlot.CHEST),
                this.getEquippedStack(EquipmentSlot.LEGS),
                this.getEquippedStack(EquipmentSlot.FEET)
        );
        Stream<Item> itemStream = armorStacks.stream().filter(stack -> stack.isIn(ModTags.ItemTypeTag.DREAM_ARMOR)).map(ItemStack::getItem).filter(item -> item instanceof DreamArmorItem);
        if (!itemStream.toList().isEmpty()) {
            Random random = Random.create();
            if (random.nextBetween(0, 100) < 39) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 5 * 20));
            }
        }
    }

    @Unique
    public boolean deathByDanmakuEntity(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((this.getHealth() - amount <= 0f) && source.getSource() instanceof DanmakuEntity) {
            Entity self = (Entity) this;
            self.playSound(SoundEventInit.BIU, 0.32F, 1.0F);
            return true;
        }
        return false;
    }

    public boolean deathInKanju(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.kanjuWorld == null) {
            return false;
        }
        if (this.kanjuWorld instanceof ServerWorld serverWorld && this.hasStatusEffect(ModStatusEffects.KANJU_KUSURI) && (this.getHealth() - amount <= 0f)) {
            this.setHealth(1f);
            this.setHealth(this.getMaxHealth());
            this.teleport(serverWorld, this.kanjuBlockPos.getX(), this.kanjuBlockPos.getY(), this.kanjuBlockPos.getZ(), EnumSet.noneOf(PositionFlag.class), this.getYaw(), this.getPitch(), true);
            return true;
        }
        return false;
    }

    @Unique
    public boolean deathInElixir(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.hasStatusEffect(ModStatusEffects.ELIXIR_OF_LIFE) && (this.getHealth() - amount <= 0f)) {
            this.deathCount++;
            this.setHealth(1f);
            this.setHealth(this.getMaxHealth());
            SoundEvent hurtSound = getHurtSound(source);
            SoundEvent deathSound = getDeathSound();
            this.playSound(hurtSound, 1.0f, 1.0f);
            this.playSound(deathSound, 1.0f, 1.0f);
            this.playSound(SoundEvents.ITEM_TOTEM_USE, 1.0f, 1.0f);
            for (var player : world.getPlayers()) {
                world.spawnParticles(player, ParticleTypes.TOTEM_OF_UNDYING, true, false, this.getX(), this.getY(), this.getZ(), 250, 1.5, 2, 1.5, 0.5);
            }
            cir.cancel();
            return true;
        }
        return false;
    }

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    public void onDeath(CallbackInfo ci) {
        if (this.maxHealthModifier >= 1) {
            this.maxHealthModifier--;
        }
    }

    @Inject(method = "writeCustomData", at = @At("HEAD"))
    public void writeCustomDataToNbt(WriteView view, CallbackInfo ci) {
        DynamicRegistryManager registryManager = this.getRegistryManager();
        view.putFloat("MaxHealthModifier", this.maxHealthModifier);
        view.putInt("DeathCount", this.deathCount);
        view.putInt("DeathCountResetTimer", this.deathCountResetTimer);
        view.putDouble("ManpozuchiUsingState", this.manpozuchiUsingState);
        view.putString("KanjuWorld", this.kanjuWorld.getRegistryKey().getValue().toString());
        view.putLong("KanjuBlockPos", this.kanjuBlockPos.asLong());
    }

    @Inject(method = "readCustomData", at = @At("HEAD"))
    public void readCustomDataFromNbt(ReadView view, CallbackInfo ci) {
        DynamicRegistryManager registryManager = this.getRegistryManager();
        MinecraftServer server = this.getServer();
        this.maxHealthModifier = view.getFloat("MaxHealthModifier", 0.0f);
        this.deathCount = view.getInt("DeathCount", 0);
        this.deathCountResetTimer = view.getInt("DeathCountResetTimer", 0);
        this.manpozuchiUsingState = view.getDouble("ManpozuchiUsingState", 0.0);
        String kanjuWorldStr = view.getString("KanjuWorld", "");
        if (kanjuWorldStr != null && !kanjuWorldStr.isEmpty()) {
            if (server != null) {
                this.kanjuWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(kanjuWorldStr)));
            }
        }
        this.kanjuBlockPos = BlockPos.fromLong(view.getLong("KanjuBlockPos", new BlockPos(0, 0, 0).asLong()));
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

