package cc.thonly.reverie_dreams.polymer.entity;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ThrownItemProjectileImplTest {
    private static Unsafe unsafe;

    @BeforeAll
    static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
        for (var item : new Item[]{Items.PAPER, Items.ICE, Items.SNOWBALL}) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build());
            }
        }
    }

    @Test
    void initialPaperMetadataAppliesToActualSnowballSchemaWithoutOverflow() throws Exception {
        var source = (PaperFixture) unsafe.allocateInstance(PaperFixture.class);
        var adapter = new ThrownItemProjectileImpl(source);
        assertSame(EntityType.SNOWBALL, adapter.getPolymerEntityType(null));
        var values = new ArrayList<SynchedEntityData.DataValue<?>>();
        adapter.modifyRawTrackedData(values, null, true);
        var client = new Snowball(EntityType.SNOWBALL, null);
        var data = client.getEntityData();
        assertDoesNotThrow(() -> data.assignValues(values));
        assertTrue(client.getItem().is(Items.PAPER));
    }

    @Test
    void initialItemReplacesExistingEntryWithoutDuplicatingTheField() throws Exception {
        var source = (PaperFixture) unsafe.allocateInstance(PaperFixture.class);
        var values = new ArrayList<SynchedEntityData.DataValue<?>>();
        values.add(SynchedEntityData.DataValue.create(ThrowableItemProjectile.DATA_ITEM_STACK, new ItemStack(Items.ICE)));
        new ThrownItemProjectileImpl(source).modifyRawTrackedData(values, null, true);
        assertEquals(1, values.size());
        assertTrue(((ItemStack) values.getFirst().value()).is(Items.PAPER));
    }

    @Test
    void laterItemChangesArePreserved() throws Exception {
        var source = (PaperFixture) unsafe.allocateInstance(PaperFixture.class);
        var values = new ArrayList<SynchedEntityData.DataValue<?>>();
        var update = SynchedEntityData.DataValue.create(ThrowableItemProjectile.DATA_ITEM_STACK, new ItemStack(Items.ICE));
        values.add(update);
        new ThrownItemProjectileImpl(source).modifyRawTrackedData(values, null, false);
        assertEquals(1, values.size());
        assertSame(update, values.getFirst());
    }

    private static class PaperFixture extends ThrowableItemProjectile {
        private PaperFixture() { super(EntityType.SNOWBALL, null); }
        @Override protected Item getDefaultItem() { return Items.PAPER; }
        @Override public ItemStack getItem() { return new ItemStack(Items.PAPER); }
    }
}
