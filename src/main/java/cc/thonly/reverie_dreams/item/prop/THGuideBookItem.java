package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.ReverieDreams;
import eu.pb4.booklet.api.item.GuideBookItem;

public class THGuideBookItem extends GuideBookItem {
    public THGuideBookItem(Properties settings) {
        super(settings, ReverieDreams.id("welcome"));
    }

}
