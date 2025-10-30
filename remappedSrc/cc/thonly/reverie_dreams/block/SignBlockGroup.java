package cc.thonly.reverie_dreams.block;

import lombok.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SignBlockGroup {
    public Block standingBlock;
    public Block wallBlock;
    public Item sign;
}
