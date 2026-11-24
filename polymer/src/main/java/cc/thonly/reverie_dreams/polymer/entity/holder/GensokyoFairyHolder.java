package cc.thonly.reverie_dreams.polymer.entity.holder;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.model.Model;

public final class GensokyoFairyHolder<E extends BaseNPCLikeEntity, A extends AnimatedEntity>
        extends EquippedHumanoidHolder<E, A> {
    public GensokyoFairyHolder(E entity, A animatedEntity, Model model) {
        super(entity, animatedEntity, model);
    }
}
