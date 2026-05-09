package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.MushroomMonster;
import com.geckolib.model.DefaultedEntityGeoModel;
import net.minecraft.resources.Identifier;

public class MushroomMonsterModel extends DefaultedEntityGeoModel<MushroomMonster> {
    public static final Identifier NORMAL = ReverieDreams.id("mushroom_monster");

    public MushroomMonsterModel() {
        super(NORMAL);
    }
}
