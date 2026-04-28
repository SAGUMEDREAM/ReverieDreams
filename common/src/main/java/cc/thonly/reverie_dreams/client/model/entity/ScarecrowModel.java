package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.Scarecrow;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class ScarecrowModel extends DefaultedEntityGeoModel<Scarecrow> {
    public static final Identifier NORMAL = ReverieDreams.id("scarecrow");

    public ScarecrowModel() {
        super(NORMAL);
    }
}
