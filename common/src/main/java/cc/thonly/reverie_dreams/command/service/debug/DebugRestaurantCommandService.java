package cc.thonly.reverie_dreams.command.service.debug;

import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import cc.thonly.reverie_dreams.block.entity.KitchenwareBlockEntity;
import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import cc.thonly.reverie_dreams.block.entity.WorkshopStorageBlockEntity;
import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.BeverageProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseSet2Blocks;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.restaurant.RestaurantMenu;
import cc.thonly.reverie_dreams.restaurant.RestaurantService;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DebugRestaurantCommandService implements CommandService {
    private static final int RADIUS = 8;
    private static final List<KitchenRecipeType.TypeInstance> KITCHEN_TYPES = List.of(
            KitchenRecipeType.TypeInstance.STEAMER, KitchenRecipeType.TypeInstance.GRILL,
            KitchenRecipeType.TypeInstance.FRYING_PAN, KitchenRecipeType.TypeInstance.CUTTING_BOARD,
            KitchenRecipeType.TypeInstance.COOKING_POT);

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("debug_restaurant")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> generate(context.getSource()));
    }

    private static int generate(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();
        BlockPos origin = BlockPos.containing(source.getPosition()).above(3);
        BlockPos min = origin.offset(-RADIUS, 0, -RADIUS);
        BlockPos max = origin.offset(RADIUS, 3, RADIUS);
        if (!tickingArea(level, min, max) || level.isOutsideBuildHeight(min) || level.isOutsideBuildHeight(max)
                || !level.getWorldBorder().isWithinBounds(min) || !level.getWorldBorder().isWithinBounds(max)
                || !level.getEntities(null, AABB.encapsulatingFullBlocks(min, max)).isEmpty()) {
            source.sendFailure(Component.literal("The restaurant needs a ticking, clear 17×17×4 space three blocks above the command position."));
            return 0;
        }
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!level.getBlockState(pos).isAir()) {
                source.sendFailure(Component.literal("The restaurant would overwrite a block at " + pos.toShortString() + "."));
                return 0;
            }
        }

        var kitchen = KitchenRecipeType.getInstance();
        var recipes = KITCHEN_TYPES.stream().flatMap(type -> kitchen.getRecipeView(type).values().stream())
                .sorted(Comparator.comparingDouble(KitchenRecipe::getCostTime).thenComparing(recipe -> recipe.getId().toString()))
                .toList();
        var foods = recipes.stream().filter(recipe -> !recipe.getIngredients().isEmpty()
                        && recipe.getIngredients().size() <= KitchenwareBlockEntity.OUTPUT_SLOT
                        && recipe.getIngredients().stream().allMatch(ingredient -> ingredient.getCount() == 1))
                .map(recipe -> recipe.getOutput().getItem()).distinct()
                .filter(item -> RestaurantService.valid(BuiltInRegistries.ITEM.getKey(item), false))
                .limit(24).toList();
        var drinks = taggedItems(RDItemTags.BEVERAGE);
        var roles = BuiltInRegistryProviders.NPC_ROLE_TYPE.stream().filter(NPCRoleType::isPresent)
                .filter(role -> role.getSkinType() != null).toList();
        var guests = new ArrayList<>(roles.stream().filter(role -> canOrder(role, foods, drinks)).toList());
        if (roles.size() < 4 || guests.size() < 3 || foods.isEmpty() || drinks.isEmpty()) {
            source.sendFailure(Component.literal("Restaurant recipes, beverages or NPC customer profiles are missing."));
            return 0;
        }

        List<NPCSimpleEntity> npcs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            var role = guests.remove(level.getRandom().nextInt(guests.size()));
            var guest = createNPC(level, player, role, NPCWorkModes.COMBAT, origin.offset(-5 + i * 4, 1, 2));
            guest.setNPCState(NPCStates.NORMAL);
            guest.getCustomerContainer().allowVisitNow(level);
            npcs.add(guest);
        }
        var staff = new ArrayList<>(roles);
        staff.removeAll(npcs.stream().map(NPCSimpleEntity::getRoleType).toList());
        if (staff.size() < 4) staff = new ArrayList<>(roles);
        for (int i = 0; i < 2; i++) {
            npcs.add(createNPC(level, player, staff.remove(level.getRandom().nextInt(staff.size())), NPCWorkModes.COOKING, origin.offset(-5 + i * 9, 1, -4)));
            npcs.add(createNPC(level, player, staff.remove(level.getRandom().nextInt(staff.size())), NPCWorkModes.SERVING, origin.offset(-4 + i * 9, 1, 1)));
        }

        var stock = new ArrayList<ItemStack>();
        RDIngredientItems.INGREDIENTS.forEach(item -> addStock(stock, item.asItem().getDefaultInstance()));
        taggedItems(RDItemTags.INGREDIENT).forEach(item -> addStock(stock, item.getDefaultInstance()));
        recipes.forEach(recipe -> recipe.getIngredients().forEach(ingredient -> addStock(stock, ingredient.build())));
        int ingredientCount = stock.size();
        drinks.forEach(item -> addStock(stock, item.getDefaultInstance()));
        Map<BlockPos, Block> fixtures = new LinkedHashMap<>();
        var bellPos = origin.offset(0, 1, 2);
        var bookPos = bellPos.east();
        fixtures.put(bellPos, RDBlocks.MYSTIA_BELL.asBlock());
        fixtures.put(bookPos, RDBlocks.MENU_BOOK.asBlock());
        fixtures.put(origin.offset(6, 1, -2), RDBlocks.CUPBOARD.asBlock());
        List<BlockPos> counters = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            var counter = origin.offset(-3 + i * 3, 1, -2);
            counters.add(counter);
            fixtures.put(counter, RDJapaneseSet2Blocks.get(WorkshopKind.CONNECTED_COUNTER).asBlock());
            fixtures.put(counter.south(), RDBlocks.PLATE.asBlock());
            fixtures.put(origin.offset(-5 + i * 4, 1, 4), RDBlocks.CHAIR.asBlock());
            fixtures.put(origin.offset(-5 + i * 4, 1, 6), RDBlocks.TABLE.asBlock());
        }
        var cookware = List.of(RDKitchenBlocks.STEAMER, RDKitchenBlocks.GRILL, RDKitchenBlocks.FRYING_PAN,
                RDKitchenBlocks.CUTTING_BOARD, RDKitchenBlocks.COOKING_POT);
        for (int i = 0; i < cookware.size(); i++) {
            fixtures.put(origin.offset(-6 + i * 3, 1, -5), cookware.get(i).asBlock());
        }
        int chestCount = (stock.size() + 26) / 27;
        if (chestCount > 14) {
            source.sendFailure(Component.literal("The current ingredient and beverage catalogue exceeds the debug platform storage."));
            return 0;
        }
        List<BlockPos> chests = new ArrayList<>();
        for (int i = 0; i < chestCount; i++) {
            var pos = origin.offset(-6 + i % 7 * 2, 1, i < 7 ? -7 : 8);
            chests.add(pos);
            fixtures.put(pos, RDBlocks.SILVER_CHEST_BLOCK.getChestBlock().asBlock());
        }

        List<BlockPos> placed = new ArrayList<>();
        List<NPCSimpleEntity> spawned = new ArrayList<>();
        try {
            for (BlockPos pos : BlockPos.betweenClosed(min, max.atY(min.getY()))) {
                place(level, pos.immutable(), Blocks.SMOOTH_STONE, placed);
            }
            fixtures.forEach((pos, block) -> place(level, pos, block, placed));
            var bell = (RestaurantBlockEntity) level.getBlockEntity(bellPos);
            var book = (RestaurantBlockEntity) level.getBlockEntity(bookPos);
            bell.claim(player);
            bell.radius(10);
            book.menu().load(false, foods.stream().map(BuiltInRegistries.ITEM::getKey).toList());
            book.menu().load(true, drinks.stream().limit(RestaurantMenu.LIMIT).map(BuiltInRegistries.ITEM::getKey).toList());
            if (!bell.bind(book, player)) throw new IllegalStateException("Menu binding failed");
            book.menuChanged();
            for (int i = 0; i < counters.size(); i++) {
                var counter = (WorkshopStorageBlockEntity) level.getBlockEntity(counters.get(i));
                counter.bindRestaurant(bell, player);
                counter.setItem(0, drinks.get(i % drinks.size()).getDefaultInstance().copyWithCount(8));
            }
            for (int i = 0; i < stock.size(); i++) {
                var chest = (Container) level.getBlockEntity(chests.get(i / 27));
                chest.setItem(i % 27, stock.get(i));
            }
            bell.toggleBusiness(player);
            if (!bell.open()) throw new IllegalStateException("The bell did not open");
            for (NPCSimpleEntity npc : npcs) {
                if (!level.addFreshEntity(npc)) throw new IllegalStateException("NPC spawn refused");
                spawned.add(npc);
            }
        } catch (RuntimeException failure) {
            spawned.forEach(NPCSimpleEntity::discard);
            for (BlockPos pos : placed.reversed()) {
                if (level.getBlockEntity(pos) instanceof Container container) container.clearContent();
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
            source.sendFailure(Component.literal("Restaurant setup failed. " + failure));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Restaurant opened at " + bellPos.toShortString() + " with "
                + ingredientCount + " ingredient stacks, " + drinks.size() + " beverages, 3 customers, 2 waiters and 2 chefs."), false);
        return 1;
    }

    private static List<Item> taggedItems(net.minecraft.tags.TagKey<Item> tag) {
        return BuiltInRegistries.ITEM.stream().filter(item -> item.builtInRegistryHolder().is(tag)).toList();
    }

    private static boolean tickingArea(ServerLevel level, BlockPos min, BlockPos max) {
        for (int x = SectionPos.blockToSectionCoord(min.getX()); x <= SectionPos.blockToSectionCoord(max.getX()); x++) {
            for (int z = SectionPos.blockToSectionCoord(min.getZ()); z <= SectionPos.blockToSectionCoord(max.getZ()); z++) {
                var chunk = new BlockPos(SectionPos.sectionToBlockCoord(x), min.getY(), SectionPos.sectionToBlockCoord(z));
                if (!level.hasChunkAt(chunk) || !level.isPositionEntityTicking(chunk)) return false;
            }
        }
        return true;
    }

    private static boolean canOrder(NPCRoleType role, List<Item> foods, List<Item> drinks) {
        var profile = role.getCustomer();
        return !profile.isGeneric()
                && foods.stream().anyMatch(item -> FoodProperties.get(item.getDefaultInstance()).stream().anyMatch(profile.likes()::contains))
                && drinks.stream().limit(RestaurantMenu.LIMIT).anyMatch(item -> BeverageProperties.get(item.getDefaultInstance()).stream()
                        .anyMatch(property -> profile.beverages().stream().anyMatch(property::is)));
    }

    private static void addStock(List<ItemStack> stock, ItemStack item) {
        if (!item.isEmpty() && stock.stream().noneMatch(existing -> ItemStack.isSameItemSameComponents(existing, item))) {
            stock.add(item.copyWithCount(Math.min(16, item.getMaxStackSize())));
        }
    }

    private static NPCSimpleEntity createNPC(ServerLevel level, ServerPlayer player, NPCRoleType role, NPCWorkMode mode, BlockPos pos) {
        var npc = RDEntityTypes.NPC_SIMPLE_ENTITY.value().create(level, EntitySpawnReason.COMMAND);
        if (npc == null) throw new IllegalStateException("Cannot create a restaurant NPC");
        npc.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        npc.setRoleType(role);
        npc.setSkinType(role.getSkinType());
        npc.setCustomName(Component.translatable(role.translateKey()));
        npc.setPersistenceRequired();
        npc.setOwner(player);
        npc.setWorkMode(mode);
        npc.setNPCState(NPCStates.WORKING);
        return npc;
    }

    private static void place(ServerLevel level, BlockPos pos, Block block, List<BlockPos> placed) {
        if (!level.setBlockAndUpdate(pos, block.defaultBlockState())) {
            throw new IllegalStateException("Could not place " + block + " at " + pos.toShortString());
        }
        placed.add(pos);
    }
}
