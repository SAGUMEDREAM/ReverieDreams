package cc.thonly.reverie_dreams.fumo;

import cc.thonly.reverie_dreams.block.BasicFumoBlock;
import cc.thonly.reverie_dreams.item.BasicBlockItem;
import cc.thonly.reverie_dreams.registry.RegistrableObject;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import com.mojang.serialization.Codec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Setter
@Getter
public class Fumo implements RegistrableObject<Fumo> {
    public static final Codec<Fumo> CODEC = Codec.unit(Fumo::new);
    private Identifier id;
    private Identifier registryKey;

    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Block block;
    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Item item;
    private Vec3d offset;

    private Fumo() {
    }

    public Fumo(Identifier id, Vec3d offset) {
        this.id = id;
        this.registryKey = Identifier.of(id.getNamespace(), "fumo/" + id.getPath());
        this.offset = offset;
    }

    public Block block() {
        return this.block;
    }

    public Item item() {
        return this.item;
    }

    public Fumo build() {
        Pair<Block, Item> pair = registerBlock(new BasicFumoBlock(this.registryKey, this.offset, AbstractBlock.Settings.copy(Blocks.WHITE_WOOL)));
        this.block = pair.getLeft();
        this.item = pair.getRight();
        return this;
    }

    protected static Pair<Block, Item> registerBlock(IdentifierGetter block) {
        Pair<Block, Item> pair = new Pair<>(null, null);
        Block left = Registry.register(Registries.BLOCK, block.getIdentifier(), (Block) block);
        Item right = Registry.register(Registries.ITEM, block.getIdentifier(), new BasicBlockItem(block.getIdentifier(), (Block) block, Items.FIREWORK_ROCKET, new Item.Settings()) {
            @Override
            public ActionResult useOnBlock(ItemUsageContext context) {
                super.useOnBlock(context);
                if (!context.getWorld().isClient) {
                    return ActionResult.SUCCESS_SERVER;
                }
                return ActionResult.SUCCESS;
            }

            @Override
            public ActionResult use(World world, PlayerEntity user, Hand hand) {
                super.use(world, user, hand);
                if (!world.isClient) {
                    world.playSound(null, user.getBlockPos(), SoundEventInit.randomFumo(), SoundCategory.BLOCKS, 1f, 1);
                    return ActionResult.SUCCESS_SERVER;
                }
                return ActionResult.SUCCESS;
            }
        });
        pair.setLeft(left);
        pair.setRight(right);
        return pair;
    }

    @Override
    public StandaloneRegistry<Fumo> getRegistryRef() {
        return RegistryManager.FUMO;
    }

    @Override
    public Codec<Fumo> getCodec() {
        return CODEC;
    }
}
