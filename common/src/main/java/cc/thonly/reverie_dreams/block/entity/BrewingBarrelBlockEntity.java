package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.BrewingBarrelRecipe;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BrewingBarrelBlockEntity extends BlockEntity {
    public static final int MAX_COUNT = 9;
    public static final int SIZE = 9;
    @Getter
    private SimpleContainer inventory = new SimpleContainer(SIZE);
    @Getter
    private Optional<BrewingBarrelRecipe.IdEntry> recipeEntry = Optional.empty();
    @Getter
    private int maxBrewingTick = 1;
    @Getter
    private int brewingTick = 0;
    @Getter
    Optional<IngredientStack> output = Optional.empty();
    @Getter
    int count = 0;
    @Getter
    DataComponentPatch patch = DataComponentPatch.EMPTY;

    public BrewingBarrelBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(RDBlockEntityTypes.BREWING_BARREL.get(), worldPosition, blockState);
    }

    public static void onBlockEntityTick(
            Level level,
            BlockPos pos,
            BlockState state,
            BrewingBarrelBlockEntity entity
    ) {
        if (level.isClientSide()) {
            return;
        }

        if (entity.recipeEntry.isEmpty()) {
            return;
        }

        if (entity.brewingTick >= entity.maxBrewingTick) {
            entity.finishBrewing();
            return;
        }

        entity.brewingTick++;
        entity.setChanged();

        if (entity.brewingTick >= entity.maxBrewingTick) {
            entity.finishBrewing();
        }
    }

    private void finishBrewing() {
        if (this.level != null) {
            SoundEventPlayUtils.playSound(this.level, this.getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS);
        }
        IngredientStack stack = IngredientStack.empty();
        if (this.recipeEntry.isPresent()) {
            BrewingBarrelRecipe.IdEntry idEntry = this.recipeEntry.get();
            if (idEntry.recipe().isPresent()) {
                BrewingBarrelRecipe brewingBarrelRecipe = idEntry.recipe().get();
                stack = brewingBarrelRecipe.getOutput().copy();
            }
        }
        this.output = Optional.ofNullable(stack.apply(this.patch));
        this.count = MAX_COUNT;
        this.brewingTick = 0;
        this.maxBrewingTick = 1;
        this.recipeEntry = Optional.empty();
        this.setChanged();
    }

    public boolean onUseItem(LivingEntity livingEntity, ItemStack itemStack) {
        if (!this.hasOutput()) {
            return false;
        }
        if (this.output.isEmpty()) {
            return false;
        }
        if (!itemStack.is(Items.GLASS_BOTTLE)) {
            if (livingEntity instanceof Player player) {
                player.sendOverlayMessage(Component.literal("message.reverie_dreams.no_bottle"));
            }
            return false;
        }
        boolean reduced = false;
        if (livingEntity instanceof Player player) {
            ItemStack build = this.output.get().build();
            if (!player.addItem(build)) {
                player.drop(build, false);
            }
            itemStack.shrink(1);
            reduced = true;
        } else if (livingEntity instanceof NPCSimpleEntity npc) {
            ItemStack build = this.output.get().build();
            if (!npc.addItem(build)) {
                npc.drop(build, false, false);
            }
            itemStack.shrink(1);
            reduced = true;
        }
        if (reduced) {
            this.count--;
            if (this.count <= 0) {
                this.clearOutput();
            }
            this.setChanged();
            return true;
        }
        return false;
    }

    public void clearOutput() {
        this.count = 0;
        this.output = Optional.empty();
        this.patch = DataComponentPatch.EMPTY;
        this.setChanged();
    }

    public boolean isBrewing() {
        return this.recipeEntry.isPresent() && this.brewingTick < this.maxBrewingTick;
    }

    public boolean hasOutput() {
        return this.count > 0 && this.output.isPresent() && !this.output.get().isEmpty();
    }

    public boolean startMatchesBrewing() {
        if (this.isBrewing() || this.hasOutput()) {
            return false;
        }

        List<BrewingBarrelRecipe> matches = RecipeManager.BREWING_BARREL.getMatches(
                this.getInputs().stream()
                        .map(IngredientStack::new)
                        .toList()
        );

        if (matches.isEmpty()) {
            return false;
        }

        BrewingBarrelRecipe first = matches.getFirst();
        Identifier recipeKey = RecipeManager.BREWING_BARREL.getRecipeKey(first);
        this.recipeEntry = Optional.of(
                new BrewingBarrelRecipe.IdEntry(
                        Optional.ofNullable(recipeKey),
                        Optional.of(first)
                )
        );

        this.brewingTick = 0;
        this.maxBrewingTick = Math.max(first.getCostTime(), 1);
        this.patch = this.buildComponentPatch(first);

        for (ItemStack itemStack : this.inventory) {
            if (!itemStack.isEmpty()) {
                itemStack.shrink(1);
            }
        }

        this.setChanged();
        return true;
    }

    private DataComponentPatch buildComponentPatch(BrewingBarrelRecipe recipe) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        List<ItemStack> inputs = this.getInputs();
        // 药水效果合并
        Map<MobEffect, MobEffectInstance> effects = new HashMap<>();
        for (ItemStack itemStack : inputs) {
            PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);

            if (potionContents == null) {
                continue;
            }

            for (MobEffectInstance effect : potionContents.getAllEffects()) {
                MobEffect effectType = effect.getEffect().value();

                MobEffectInstance existing = effects.get(effectType);

                if (existing == null) {
                    effects.put(effectType, effect);
                    continue;
                }

                int amplifier = Math.max(
                        existing.getAmplifier(),
                        effect.getAmplifier()
                ) + 1;

                int duration = Math.max(
                        existing.getDuration(),
                        effect.getDuration()
                );

                MobEffectInstance merged = new MobEffectInstance(
                        effect.getEffect(),
                        duration,
                        amplifier,
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                );

                effects.put(effectType, merged);
            }
        }
        List<MobEffectInstance> buildEffects = new ArrayList<>(effects.values());
        builder.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), buildEffects, Optional.empty()));
        Set<ConsumeEffect> consumeEffects = new LinkedHashSet<>();
        // 食物效果合并
        for (ItemStack itemStack : inputs) {
            if (!itemStack.has(DataComponents.CONSUMABLE)) {
                continue;
            }
            Consumable consumable = itemStack.get(DataComponents.CONSUMABLE);
            assert consumable != null;
            consumeEffects.addAll(consumable.onConsumeEffects());
        }
        IngredientStack output = recipe.getOutput();
        ItemStack buildStack = output.build();
        Consumable consumable;
        if (buildStack.has(DataComponents.CONSUMABLE)) {
            Consumable temp = buildStack.get(DataComponents.CONSUMABLE);
            Consumable.Builder consumableBuilder = Consumable.builder();
            assert temp != null;
            consumableBuilder.consumeSeconds(temp.consumeSeconds());
            consumableBuilder.animation(temp.animation());
            consumableBuilder.sound(temp.sound());
            consumableBuilder.hasConsumeParticles(temp.hasConsumeParticles());
            for (ConsumeEffect onConsumeEffect : temp.onConsumeEffects()) {
                consumableBuilder.onConsume(onConsumeEffect);
            }
            for (ConsumeEffect consumeEffect : consumeEffects) {
                consumableBuilder.onConsume(consumeEffect);
            }
            consumable = consumableBuilder.build();
        } else {
            Consumable.Builder consumableBuilder = Consumables.defaultDrink();
            for (ConsumeEffect consumeEffect : consumeEffects) {
                consumableBuilder.onConsume(consumeEffect);
            }
            consumable = consumableBuilder.build();
        }
        builder.set(DataComponents.CONSUMABLE, consumable);
        return builder.build();
    }

    public List<ItemStack> getInputs() {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            list.add(this.inventory.getItem(i));
        }
        return list;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        SimpleContainer inventory = new SimpleContainer(SIZE);
        ContainerHelper.loadAllItems(view, inventory.getItems());
        this.inventory = inventory;
        this.recipeEntry = view.read("RecipeTarget", BrewingBarrelRecipe.IdEntry.CODEC);
        this.brewingTick = view.getIntOr("BrewingTick", 0);
        this.maxBrewingTick = view.getIntOr("MaxBrewingTick", 1);
        this.output = view.read("Output", IngredientStack.CODEC);
        this.patch = view.read("OutputPatch", DataComponentPatch.CODEC).orElse(DataComponentPatch.EMPTY);
    }

    @Override
    public void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, this.inventory.getItems());
        this.recipeEntry.ifPresent(idEntry -> view.storeNullable("RecipeTarget", BrewingBarrelRecipe.IdEntry.CODEC, idEntry));
        view.putInt("BrewingTick", this.brewingTick);
        view.putInt("MaxBrewingTick", this.maxBrewingTick);
        this.output.ifPresent(stack -> view.storeNullable("Output", IngredientStack.CODEC, stack));
        view.storeNullable("OutputPatch", DataComponentPatch.CODEC, this.patch);
    }
}
