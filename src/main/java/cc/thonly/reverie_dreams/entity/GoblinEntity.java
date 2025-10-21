package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.skin.MobSkins;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class GoblinEntity extends NPCEntityImpl {
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

    public GoblinEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world, MobSkins.GOBLIN::get);
        this.inventory.setMainHand(this.getRandomPickaxe());
        this.inventory.setOffHand(this.getRandomOffHand());
        if (GOLDEN_ITEMS.isEmpty()) {
            this.tryGetGoldenItem();
        }
    }

    private void tryGetGoldenItem() {
        Set<Identifier> ids = Registries.ITEM.getIds();
        Set<Identifier> collect = ids.stream().filter(id -> {
                    String path = id.getPath();
                    return path.startsWith("gold_") || path.startsWith("golden_");
                })
                .collect(Collectors.toSet());
        collect.forEach((id) -> GOLDEN_ITEMS.add(Registries.ITEM.get(id)));
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));

        this.goalSelector.add(4, new TemptGoal(this, 1.2, stack -> GOLDEN_ITEMS.contains(stack.getItem()), false));

        this.goalSelector.add(7, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));

        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        this.goalSelector.add(10, new LookAtEntityGoal(this, NPCEntityImpl.class, 8.0f));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.targetSelector.add(3, new RevengeGoal(this).setGroupRevenge());
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
            itemStack.damage(itemStack.getMaxDamage() - 50, this, Hand.MAIN_HAND);
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
            itemStack.damage(itemStack.getMaxDamage() - 50, this, Hand.OFF_HAND);
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
    public boolean cannotDespawn() {
        return false;
    }
}
