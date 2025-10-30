package cc.thonly.reverie_dreams.item.base;

import cc.thonly.reverie_dreams.data.ModTags;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import java.util.Set;

@Setter
@Getter
@ToString
public abstract class MiningToolItem extends Item {
    public static final Set<MiningToolItem> MINING_TOOLS = new ObjectOpenHashSet<>();
    public static final Set<MiningToolItem> PICKAXES = new ObjectOpenHashSet<>();
    public static final Set<MiningToolItem> SWORDS = new ObjectOpenHashSet<>();
    public static final Set<MiningToolItem> AXES = new ObjectOpenHashSet<>();
    public static final Set<MiningToolItem> SHOVES = new ObjectOpenHashSet<>();
    public static final Set<MiningToolItem> HOES = new ObjectOpenHashSet<>();

    public MiningToolItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(material
                .applySwordProperties(
                        settings,
                        attackDamage,
                        attackSpeed
                )
                .tool(material,
                        ModTags.BlockTypeTag.MIN_TOOL,
                        attackDamage,
                        attackSpeed,0.0F
                )
        );

        MINING_TOOLS.add(this);
    }

}
