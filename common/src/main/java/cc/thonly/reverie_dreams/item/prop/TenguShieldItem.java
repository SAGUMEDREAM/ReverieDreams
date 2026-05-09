package cc.thonly.reverie_dreams.item.prop;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.world.item.ShieldItem;

@Setter
@Getter
@ToString
public class TenguShieldItem extends ShieldItem {
    public TenguShieldItem(Properties settings) {
        super(settings);
    }

}
