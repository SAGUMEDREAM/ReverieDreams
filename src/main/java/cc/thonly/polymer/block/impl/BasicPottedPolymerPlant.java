package cc.thonly.polymer.block.impl;

import cc.thonly.reverie_dreams.util.IdentifierGetter;
import com.mojang.math.Axis;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

@Getter
public class BasicPottedPolymerPlant extends FlowerPotBlock implements FactoryBlock {
    private final ResourceLocation blockId;
    private final ItemStack MODEL;

    public BasicPottedPolymerPlant(ResourceLocation identifier, Block content, Properties settings, boolean useExtraModel) {
        super(content, settings);
        this.blockId = identifier;
        MODEL = ItemDisplayElementUtil.getModel(ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), "block/%s".formatted(useExtraModel ? identifier.getPath() : identifier.getPath().replace("potted_", ""))));
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.FLOWER_POT.defaultBlockState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new ItemDisplayPottedPlantModel(pos);
    }

    private class ItemDisplayPottedPlantModel extends BlockModel {

        public ItemDisplayPottedPlantModel(BlockPos pos) {
            ItemDisplayElement main = ItemDisplayElementUtil.createSimple(MODEL);
            main.setScale(new Vector3f(0.98f));
            main.setDisplaySize(1, 1);
            int rotation = pos.hashCode() % 4 * 90;
            main.setRightRotation(Axis.YP.rotationDegrees(rotation));
            this.addElement(main);
        }
    }
}