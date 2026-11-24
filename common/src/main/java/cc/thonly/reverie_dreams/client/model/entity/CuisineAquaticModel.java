package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.aquatic.AquaticSpecies;
import cc.thonly.reverie_dreams.entity.aquatic.CuisineAquaticEntity;
import com.geckolib.model.DefaultedEntityGeoModel;

public final class CuisineAquaticModel extends DefaultedEntityGeoModel<CuisineAquaticEntity> {
    public CuisineAquaticModel(AquaticSpecies species) {
        super(ReverieDreams.id("aquatic/" + species.key()));
    }
}
