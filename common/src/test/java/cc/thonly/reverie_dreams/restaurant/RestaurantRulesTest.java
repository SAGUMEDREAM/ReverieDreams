package cc.thonly.reverie_dreams.restaurant;

import cc.thonly.reverie_dreams.data.*;
import cc.thonly.reverie_dreams.entity.ai.work.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.classfile.ClassFile;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import static org.junit.jupiter.api.Assertions.*;

class RestaurantRulesTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }
    @Test void genericMeansMissingDefinitionNotRareFlagOrAnEmptyPreferenceList() throws Exception {
        // Run the real Customer class/methods, isolating only the global role-registration bootstrap.
        // Plain JUnit has no Architectury loader; the fallback's decorative role reference is irrelevant here.
        String customerName = "cc.thonly.reverie_dreams.data.Customer";
        String rolesName = "cc.thonly.reverie_dreams.registry.content.NPCRoleTypes";
        byte[] roleBootstrap = ClassFile.of().build(ClassDesc.of(rolesName), builder -> builder
                .withFlags(ClassFile.ACC_PUBLIC)
                .withSuperclass(ConstantDescs.CD_Object)
                .withField("REIMU", ClassDesc.of("cc.thonly.reverie_dreams.data.npc.NPCRoleType"),
                        ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC));
        var isolated = new ClassLoader(getClass().getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!name.equals(rolesName) && !name.equals(customerName) && !name.startsWith(customerName + "$")) return super.loadClass(name, resolve);
                synchronized (getClassLoadingLock(name)) {
                    var loaded = findLoadedClass(name);
                    if (loaded == null) try {
                        byte[] bytes;
                        if (name.equals(rolesName)) bytes = roleBootstrap;
                        else try (var input = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                            assertNotNull(input); bytes = input.readAllBytes();
                        }
                        loaded = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException error) { throw new ClassNotFoundException(name, error); }
                    if (resolve) resolveClass(loaded); return loaded;
                }
            }
        };
        var actual = Class.forName(customerName, true, isolated);
        var isGeneric = actual.getMethod("isGeneric");
        assertEquals(true, isGeneric.invoke(actual.getField("UNDEFINED").get(null)));
        for (boolean rare : List.of(false, true)) {
            var defined = actual.getConstructors()[0].newInstance(null, rare, new CustomerBudget(20, 30),
                    List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), new CustomerEvaluation());
            assertEquals(rare, actual.getMethod("rare").invoke(defined));
            assertEquals(false, isGeneric.invoke(defined));
        }
    }
    @Test void dedicatedGuestsNeverFallBackToGenericWhenTheMenuCannotMeetTheirPreferences() {
        assertEquals(List.of("soup"), CustomerOrderChoice.candidates(List.of("soup", "rice"), false, true, "soup"::equals));
        assertTrue(CustomerOrderChoice.candidates(List.of("rice"), false, true, "soup"::equals).isEmpty());
        assertEquals(List.of("rice"), CustomerOrderChoice.candidates(List.of("rice"), false, false, s -> false));
        assertEquals(List.of("rice"), CustomerOrderChoice.candidates(List.of("rice"), true, true, s -> false));
    }
    @Test void roleLookupUsesAliasAwareExactLookupInsteadOfTheRegistryDefault() throws Exception {
        try (var input = NPCRoleType.class.getResourceAsStream("NPCRoleType.class")) {
            assertNotNull(input);
            var model = ClassFile.of().parse(input.readAllBytes());
            var code = model.methods().stream().filter(m -> m.methodName().equalsString("getCustomer")).findFirst().orElseThrow().code().orElseThrow();
            var calls = code.elementList().stream().filter(e -> e instanceof InvokeInstruction)
                    .map(e -> (InvokeInstruction) e).toList();
            assertTrue(calls.stream().anyMatch(c -> c.owner().asInternalName().endsWith("/RegistryProvider") && c.name().equalsString("get")));
            assertFalse(calls.stream().anyMatch(c -> c.name().equalsString("getValue")), "A fallback entry must not masquerade as a dedicated role definition");
        }
    }
    @Test void menuRequiresValidFoodAndDrinkAndNeverSuppliesAnUnlistedItem() {
        var menu = new RestaurantMenu(); var food = Identifier.parse("test:soup"); var drink = Identifier.parse("test:tea");
        menu.toggle(false, food); assertFalse(menu.ready(food::equals, drink::equals));
        menu.toggle(true, drink); assertTrue(menu.ready(food::equals, drink::equals));
        assertFalse(menu.ready(i -> false, drink::equals));
        assertFalse(menu.contains(false, Identifier.parse("test:unlisted")));
        menu.toggle(false, food); assertFalse(menu.ready(food::equals, drink::equals));
    }
    @Test void catalogueIsBoundedDeduplicatedAndSnapshotCannotBeMutated() {
        var menu = new RestaurantMenu(); var ids = new ArrayList<Identifier>();
        for (int i = 0; i < 200; i++) ids.add(Identifier.parse("test:food_" + i));
        menu.load(false, ids); assertEquals(128, menu.entries(false).size());
        assertFalse(menu.toggle(false, Identifier.parse("test:extra")));
        var snapshot = menu.entries(false); menu.toggle(false, ids.getFirst());
        assertEquals(128, snapshot.size()); assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
    }
    private RestaurantTicket ticket() { return new RestaurantTicket(BlockPos.ZERO, UUID.randomUUID(), new BlockPos(2,0,0), UUID.randomUUID(), List.of(Identifier.parse("test:soup")), List.of(Identifier.parse("test:tea"))); }
    @Test void unloadedFacilitiesPauseWithoutReadingOrLoadingTheWorld() {
        var ticket = ticket(); var reads = new AtomicInteger();
        assertEquals(RestaurantTicket.Availability.UNLOADED, ticket.availability(p -> false, p -> { reads.incrementAndGet(); return null; }));
        assertEquals(0, reads.get());
    }
    @Test void replacingEitherFacilityOrRebindingTheMenuInvalidatesTheOldOrder() {
        var ticket = ticket();
        var live = new HashMap<BlockPos, RestaurantTicket.Facility>();
        live.put(ticket.bellPos(), new RestaurantTicket.Facility(ticket.bellId(), ticket.bookId(), true));
        live.put(ticket.bookPos(), new RestaurantTicket.Facility(ticket.bookId(), ticket.bellId(), false));
        assertEquals(RestaurantTicket.Availability.READY, ticket.availability(p -> true, live::get));
        live.put(ticket.bookPos(), new RestaurantTicket.Facility(UUID.randomUUID(), ticket.bellId(), false));
        assertEquals(RestaurantTicket.Availability.REMOVED, ticket.availability(p -> true, live::get));
        live.put(ticket.bookPos(), new RestaurantTicket.Facility(ticket.bookId(), UUID.randomUUID(), false));
        assertEquals(RestaurantTicket.Availability.REMOVED, ticket.availability(p -> true, live::get));
        live.clear(); assertEquals(RestaurantTicket.Availability.REMOVED, ticket.availability(p -> true, live::get));
    }
    @Test void acceptedMenuIsStableWhileTheLiveMenuIsEdited() {
        var foods = new ArrayList<>(List.of(Identifier.parse("test:soup")));
        var ticket = new RestaurantTicket(BlockPos.ZERO, UUID.randomUUID(), new BlockPos(2,0,0), UUID.randomUUID(), foods, List.of(Identifier.parse("test:tea")));
        foods.clear();
        assertTrue(ticket.allows(Identifier.parse("test:soup"), false));
        assertFalse(ticket.allows(Identifier.parse("test:tea"), false));
        assertFalse(ticket.allows(Identifier.parse("test:unlisted"), false));
    }
    @Test void overlappingRestaurantsHaveIndependentQueues() {
        var queue = CustomerQueue.of(new Object()); var site = new WorkArea(BlockPos.ZERO, 8, 8);
        var first = UUID.randomUUID(); var second = UUID.randomUUID();
        queue.join(first, site, 0, UUID.randomUUID()); queue.join(second, site, 0, UUID.randomUUID());
        assertTrue(queue.turn(second, BlockPos.ZERO, 0)); assertEquals(1, queue.position(second, 0));
    }
    @Test void orderBindingsAndMenuSnapshotSurviveSaveReload() {
        var ticket = ticket();
        var registry = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registry);
        ticket.save(output);
        var input = TagValueInput.create(ProblemReporter.DISCARDING, registry, output.buildResult());
        assertEquals(ticket, RestaurantTicket.load(input));
    }
    @Test void originalAssetsParseThroughTheCurrentMinecraftModelParser() throws Exception {
        for (String name : List.of("mystia_bell", "mystia_bell_open", "menu_book", "menu_book_open")) {
            try (var input = getClass().getResourceAsStream("/assets/reverie_dreams/models/block/" + name + ".json")) {
                assertNotNull(input, name);
                var reader = new InputStreamReader(input, StandardCharsets.UTF_8);
                var model = CuboidModel.fromStream(reader);
                assertNotNull(model.geometry(), name);
                assertEquals(Identifier.parse("minecraft:block/block"), model.parent());
            }
        }
    }
}
