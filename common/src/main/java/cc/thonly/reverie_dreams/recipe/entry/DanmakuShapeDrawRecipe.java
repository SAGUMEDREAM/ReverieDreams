package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuShape;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.ItemStackTemplateWrapper;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@Builder(toBuilder = true)
public class DanmakuShapeDrawRecipe extends BaseRecipe {
    public static final Codec<DanmakuShapeDrawRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.listOf().listOf().fieldOf("shape").forGetter(DanmakuShapeDrawRecipe::getShape),
            ItemStackTemplateWrapper.CODEC.fieldOf("result").forGetter(DanmakuShapeDrawRecipe::getOutput)
    ).apply(instance, DanmakuShapeDrawRecipe::new));

    private final List<List<Boolean>> shape;
    private final ItemStackTemplateWrapper output;

    public DanmakuShapeDrawRecipe(List<List<Boolean>> shape, ItemStackTemplateWrapper output) {
        this.shape = shape;
        this.output = output;
        if (this.shape.size() > 6) {
            throw new IllegalArgumentException(
                    "The Y-axis size of the shape cannot be greater than 6, actual: %s".formatted(this.shape.size())
            );
        }
        for (int i = 0; i < this.shape.size(); i++) {
            List<Boolean> shapeX = this.shape.get(i);
            if (shapeX.size() > 6) {
                throw new IllegalArgumentException(
                        "The X-axis size of the shape cannot be greater than 6 at row %s, actual: %s"
                                .formatted(i, shapeX.size())
                );
            }
        }
    }

    public DanmakuShapeDrawRecipe(List<List<Boolean>> shape, DanmakuShape danmakuShape) {
        this.shape = shape;
        this.output = ItemStackTemplateWrapper.of(danmakuShape.getItemStackTemplate());
        if (this.shape.size() > 6) {
            throw new IllegalArgumentException(
                    "The Y-axis size of the shape cannot be greater than 6, actual: %s".formatted(this.shape.size())
            );
        }
        for (int i = 0; i < this.shape.size(); i++) {
            List<Boolean> shapeX = this.shape.get(i);
            if (shapeX.size() > 6) {
                throw new IllegalArgumentException(
                        "The X-axis size of the shape cannot be greater than 6 at row %s, actual: %s"
                                .formatted(i, shapeX.size())
                );
            }
        }
    }
}