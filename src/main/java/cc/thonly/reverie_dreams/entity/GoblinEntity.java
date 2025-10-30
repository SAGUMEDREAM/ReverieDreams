package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import java.util.*;
import java.util.stream.Collectors;

public class GoblinEntity extends BaseNPCLikeEntity {
    public static final Set<Item> GOLDEN_ITEMS = new HashSet<>();
    public static List<Item> PICKAXE_POOL = new ArrayList<>();
    public static List<Item> OFFHAND_POOL = new ArrayList<>();

    static {
        PICKAXE_POOL.add(Items.AIR);
        PICKAXE_POOL.add(Items.WOODEN_PICKAXE);
        PICKAXE_POOL.add(Items.STONE_PICKAXE);
        PICKAXE_POOL.add(Items.IRON_PICKAXE);
        PICKAXE_POOL.add(Items.GOLDEN_PICKAXE);
        PICKAXE_POOL.add(Items.DIAMOND_PICKAXE);
        PICKAXE_POOL.add(Items.NETHERITE_PICKAXE);
        OFFHAND_POOL.add(Items.AIR);
        OFFHAND_POOL.add(Items.AIR);
        OFFHAND_POOL.add(Items.TORCH);
        OFFHAND_POOL.add(Items.SHIELD);
    }

    public GoblinEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world, MobSkinTypes.GOBLIN);
        this.inventory.setMainHand(this.getRandomPickaxe());
        this.inventory.setOffHand(this.getRandomOffHand());
        if (GOLDEN_ITEMS.isEmpty()) {
            this.tryGetGoldenItem();
        }
    }

    private void tryGetGoldenItem() {
        Set<ResourceLocation> ids = BuiltInRegistries.ITEM.keySet();
        Set<ResourceLocation> collect = ids.stream().filter(id -> {
                    String path = id.getPath();
                    return path.startsWith("gold_") || path.startsWith("golden_");
                })
                .collect(Collectors.toSet());
        collect.forEach((id) -> GOLDEN_ITEMS.add(BuiltInRegistries.ITEM.getValue(id)));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));

        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, stack -> GOLDEN_ITEMS.contains(stack.getItem()), false));

        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));

        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, BaseNPCLikeEntity.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    public ItemStack getRandomPickaxe() {
        final Random random = new Random();
        if (PICKAXE_POOL == null || PICKAXE_POOL.isEmpty()) {
            return null;
        }
        Item item = PICKAXE_POOL.get(random.nextInt(PICKAXE_POOL.size()));
        ItemStack itemStack = new ItemStack(item);
//        itemStack.set(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE);
        if (itemStack.isDamaged()) {
            itemStack.hurtAndBreak(itemStack.getMaxDamage() - 50, this, InteractionHand.MAIN_HAND);
        }
        return itemStack;
    }

    public ItemStack getRandomOffHand() {
        final Random random = new Random();
        if (OFFHAND_POOL == null || OFFHAND_POOL.isEmpty()) {
            return null;
        }
        Item item = OFFHAND_POOL.get(random.nextInt(OFFHAND_POOL.size()));
        ItemStack itemStack = new ItemStack(item);
        if (itemStack.isDamaged()) {
            itemStack.hurtAndBreak(itemStack.getMaxDamage() - 50, this, InteractionHand.OFF_HAND);
        }
        return itemStack;
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public Set<Integer> getDonDropSlotIndex() {
        return Set.of(NPCInventoryImpl.MAIN_HAND, NPCInventoryImpl.OFF_HAND);
    }

    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.NOT_DROP_ANY;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return false;
    }
}
