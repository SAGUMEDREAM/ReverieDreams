package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.Scarecrow;
import com.geckolib.model.DefaultedEntityGeoModel;
import net.minecraft.resources.Identifier;

public class ScarecrowModel extends DefaultedEntityGeoModel<Scarecrow> {
    public static final Identifier NORMAL = ReverieDreams.id("scarecrow");

    public ScarecrowModel() {
        super(NORMAL);
    }
}
