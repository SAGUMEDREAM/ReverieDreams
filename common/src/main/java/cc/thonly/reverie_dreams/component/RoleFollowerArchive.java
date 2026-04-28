package cc.thonly.reverie_dreams.component;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

public record RoleFollowerArchive(Component name, float maxHealth, CompoundTag nbt) {
    public static final Codec<RoleFollowerArchive> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("Name").forGetter(RoleFollowerArchive::name),
            Codec.FLOAT.fieldOf("MaxHealth").forGetter(RoleFollowerArchive::maxHealth),
            CompoundTag.CODEC.fieldOf("Nbt").forGetter(RoleFollowerArchive::nbt)
    ).apply(instance, RoleFollowerArchive::new));

    public BaseNPCLikeEntity respawn(ServerLevel world, BlockPos pos, HolderLookup.Provider registries) {
        NPCRoleEntity npcLikeEntity = new NPCRoleEntity(RDEntityTypes.NPC_ROLE.asHolder().value(), world);
        npcLikeEntity.setPosRaw(pos.getX(), pos.getY(), pos.getZ());
        npcLikeEntity.setCustomName(this.name);
        npcLikeEntity.setOwner((LivingEntity) null);
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(npcLikeEntity.problemPath(), LogUtils.getLogger())) {
            ValueInput view = TagValueInput.create(logging, registries, this.nbt);
            npcLikeEntity.readAdditionalSaveData(view);
        }
        AttributeMap attributes = npcLikeEntity.getAttributes();
        AttributeInstance attributeInstance = attributes.getInstance(Attributes.MAX_HEALTH);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(this.maxHealth > 20 ? this.maxHealth - 2 : this.maxHealth);
        }
        npcLikeEntity.setHealth(npcLikeEntity.getMaxHealth());
        world.addFreshEntity(npcLikeEntity);
        return npcLikeEntity;
    }
}
