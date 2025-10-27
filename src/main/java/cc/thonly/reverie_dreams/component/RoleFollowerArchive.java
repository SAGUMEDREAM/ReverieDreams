package cc.thonly.reverie_dreams.component;

import cc.thonly.minecraft.inventory.InventoriesImpl;
import cc.thonly.minecraft.inventory.Slot2ItemStack;
import cc.thonly.minecraft.text.TextUtil;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

@Getter
public class RoleFollowerArchive {
    public static final Codec<RoleFollowerArchive> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextCodecs.CODEC.fieldOf("Name").forGetter(RoleFollowerArchive::getName),
            Codec.FLOAT.fieldOf("MaxHealth").forGetter(RoleFollowerArchive::getMaxHealth),
            NbtCompound.CODEC.fieldOf("Nbt").forGetter(RoleFollowerArchive::getNbt)
    ).apply(instance, RoleFollowerArchive::new));

    private final Text name;
    private final float maxHealth;
    private final NbtCompound nbt;

    public RoleFollowerArchive(Text name, float maxHealth, NbtCompound nbt) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.nbt = nbt;
    }

    public BaseNPCLikeEntity respawn(ServerWorld world, BlockPos pos, RegistryWrapper.WrapperLookup registries) {
        NPCRoleEntity npcLikeEntity = new NPCRoleEntity(ModEntities.NPC_ROLE_ENTITY, world);
        npcLikeEntity.setPos(pos.getX(), pos.getY(), pos.getZ());
        npcLikeEntity.setCustomName(this.name);
        npcLikeEntity.setOwner((LivingEntity) null);
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(npcLikeEntity.getErrorReporterContext(), LogUtils.getLogger())) {
            ReadView view = NbtReadView.create(logging, registries, this.nbt);
            npcLikeEntity.readCustomData(view);
        }
        AttributeContainer attributes = npcLikeEntity.getAttributes();
        EntityAttributeInstance attributeInstance = attributes.getCustomInstance(EntityAttributes.MAX_HEALTH);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(this.maxHealth > 20 ? this.maxHealth - 2 : this.maxHealth);
        }
        npcLikeEntity.setHealth(npcLikeEntity.getMaxHealth());
        world.spawnEntity(npcLikeEntity);
        return npcLikeEntity;
    }
}
