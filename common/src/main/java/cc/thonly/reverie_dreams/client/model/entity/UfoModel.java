package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.UFO;
import com.geckolib.model.DefaultedEntityGeoModel;
import net.minecraft.resources.Identifier;

public class UfoModel extends DefaultedEntityGeoModel<UFO> {
    public static final Identifier NORMAL = ReverieDreams.id("ufo");
    public UfoModel() {
        super(NORMAL);
    }
}
