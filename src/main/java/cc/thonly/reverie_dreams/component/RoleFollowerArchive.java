package cc.thonly.reverie_dreams.component;

import cc.thonly.minecraft.inventory.InventoriesImpl;
import cc.thonly.minecraft.inventory.Slot2ItemStack;
import cc.thonly.minecraft.text.TextUtil;
import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.gui.NPCGui;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import com.google.gson.Gson;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

@Getter
public class RoleFollowerArchive {
    public static final Codec<RoleFollowerArchive> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("entity_type_id").forGetter(RoleFollowerArchive::getEntityTypeId),
            Codec.STRING.fieldOf("name").forGetter(RoleFollowerArchive::getNameJson),
            Codec.STRING.fieldOf("inventory_nbt").forGetter(RoleFollowerArchive::getInventoryJson)
    ).apply(instance, RoleFollowerArchive::new));

    private final Identifier entityTypeId;
    private final String nameJson;
    private final String inventoryJson;


    public RoleFollowerArchive(Identifier entityTypeId, String nameJson, String inventoryJson) {
        this.entityTypeId = entityTypeId;
        this.nameJson = nameJson;
        this.inventoryJson = inventoryJson;
    }

    public RoleFollowerArchive(EntityType<?> entityType, NPCEntityImpl entity, RegistryWrapper.WrapperLookup registries) {
        this.entityTypeId = Registries.ENTITY_TYPE.getId(entityType);
        this.nameJson = entity.hasCustomName() ? TextUtil.encode(entity.getName()) : "";
        NPCInventoryImpl inventory = entity.getInventory();
        this.inventoryJson = InventoriesImpl.toJson(inventory.heldStacks);
    }

    public NPCEntityImpl respawn(ServerWorld world, BlockPos pos, RegistryWrapper.WrapperLookup registries) {
        EntityType<?> entityType = Registries.ENTITY_TYPE.get(this.entityTypeId);
        Entity entity = entityType.spawn(world, pos, SpawnReason.MOB_SUMMONED);

        if (!(entity instanceof NPCEntityImpl impl)) {
            return null;
        }

        NPCInventoryImpl inventory = new NPCInventoryImpl(NPCInventoryImpl.MAX_SIZE);
        DefaultedList<ItemStack> heldStacks = inventory.heldStacks;
        Optional<List<Slot2ItemStack>> slot2ItemStacks = InventoriesImpl.parseJson(this.inventoryJson);
        if (slot2ItemStacks.isPresent()) {
            List<Slot2ItemStack> list = slot2ItemStacks.get();
            for (Slot2ItemStack slot2ItemStack : list) {
                int index = slot2ItemStack.index();
                if (index < 0 || index >= heldStacks.size()) continue;
                heldStacks.set(slot2ItemStack.index(), slot2ItemStack.itemStack());
            }
        }
        impl.setInventory(inventory);

        if (!this.nameJson.isEmpty()) {
            Optional<Text> decode = TextUtil.decode(this.nameJson);
            decode.ifPresent(entity::setCustomName);
        }

        return impl;
    }
}
