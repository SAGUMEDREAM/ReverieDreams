package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.UFO;
import net.minecraft.resources.Identifier;
import com.geckolib.model.DefaultedEntityGeoModel;

public class UfoModel extends DefaultedEntityGeoModel<UFO> {
    public static final Identifier NORMAL = ReverieDreams.id("ufo");
    public UfoModel() {
        super(NORMAL);
    }
}
