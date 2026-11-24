package cc.thonly.reverie_dreams.world.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

/**
 * A finite, explicitly queued movement command for one enrolled resident.
 */
public record SettlementTask(Identifier settlement, Identifier role, Type type, Identifier site, BlockPos position) {
    public enum Type implements StringRepresentable {
        VISIT_SITE("visit_site");

        private static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        private final String id;

        Type(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    static final Codec<SettlementTask> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("settlement").forGetter(SettlementTask::settlement),
            Identifier.CODEC.fieldOf("role").forGetter(SettlementTask::role),
            Type.CODEC.fieldOf("type").forGetter(SettlementTask::type),
            Identifier.CODEC.fieldOf("site").forGetter(SettlementTask::site),
            BlockPos.CODEC.fieldOf("position").forGetter(SettlementTask::position)
    ).apply(instance, SettlementTask::new));

    public SettlementTask {
        position = position.immutable();
    }

    boolean belongsTo(SettlementPilotData.Member member) {
        return member != null && this.settlement.equals(member.settlement())
                && this.role.equals(member.role());
    }

    boolean matches(SettlementPilotData.Layout layout) {
        return layout != null && layout.roles().containsKey(this.role)
                && this.position.equals(layout.sites().get(this.site));
    }

    boolean isValid(SettlementPilotData.Member member, SettlementPilotData.Layout layout) {
        return this.type == Type.VISIT_SITE && this.belongsTo(member) && this.matches(layout);
    }
}
