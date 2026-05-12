package cc.thonly.reverie_dreams.api.entity;

import cc.thonly.reverie_dreams.item.WingType;

public interface PlayerEntityDataModifier {
    long reverie_dreams$getNonSleepingTime();

    void reverie_dreams$setNonSleepingTime(long time);

    WingType reverie_dreams$getWingType();

    void reverie_dreams$setWingType(WingType wingType);
}
