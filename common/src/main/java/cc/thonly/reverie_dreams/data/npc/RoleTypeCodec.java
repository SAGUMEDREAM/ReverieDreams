package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.api.registry.RegistryProviderLookup;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.Identifier;

final class RoleTypeCodec {
    private RoleTypeCodec() {
    }

    static Codec<RoleType> byId(RegistryProviderLookup<NPCRoleType> spawnable,
                                RegistryProviderLookup<NPCSimpleRoleType> simple) {
        return Identifier.CODEC.flatXmap(id -> spawnable.findValue(id).<RoleType>map(role -> role)
                        .or(() -> simple.findValue(id))
                        .filter(role -> !role.isUnit())
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown NPC role: " + id)),
                role -> encodeId(role, spawnable, simple));
    }

    private static DataResult<Identifier> encodeId(RoleType role, RegistryProviderLookup<NPCRoleType> spawnable,
                                                   RegistryProviderLookup<NPCSimpleRoleType> simple) {
        if (role.isUnit()) {
            return DataResult.error(() -> "An empty NPC role cannot be serialized");
        }
        Identifier id = switch (role) {
            case NPCRoleType npc -> spawnable.getKey(npc);
            case NPCSimpleRoleType data -> simple.getKey(data);
            default -> null;
        };
        if (id == null) {
            return DataResult.error(() -> "Unregistered NPC role: " + role.identify());
        }
        if (role instanceof NPCSimpleRoleType && spawnable.findValue(id).isPresent()) {
            return DataResult.error(() -> "Simple NPC role ID is already used by a spawnable role: " + id);
        }
        return DataResult.success(id);
    }
}
