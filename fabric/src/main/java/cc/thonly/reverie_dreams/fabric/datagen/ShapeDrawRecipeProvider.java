package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractRecipeTypeProvider;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ShapeDrawRecipeProvider extends AbstractRecipeTypeProvider {
    public final Factory<DanmakuShapeDrawRecipe> factory = this.getOrCreateFactory(RecipeManager.DANMAKU_SHAPE_DRAW, DanmakuShapeDrawRecipe.class);

    public ShapeDrawRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured() {
        this.offerShapeRecipe(DanmakuTypes.AMULET, new String[]{
                "FFFFFF",
                "FFTTFF",
                "FFTTFF",
                "FFTTFF",
                "FFTTFF",
                "FFFFFF"
        });

        this.offerShapeRecipe(DanmakuTypes.ARROWHEAD, new String[]{
                "FFFFFF",
                "FFTTFF",
                "FTTTTF",
                "FTTTTF",
                "FTFFTF",
                "FFFFFF"
        });

        this.offerShapeRecipe(DanmakuTypes.BALL, new String[]{
                "FFFFFF",
                "FFFFFF",
                "FFTTFF",
                "FFTTFF",
                "FFFFFF",
                "FFFFFF"
        });

        this.offerShapeRecipe(DanmakuTypes.BUBBLE, new String[]{
                "FFFFFF",
                "FTTTTF",
                "FTTTTF",
                "FTTTTF",
                "FTTTTF",
                "FFFFFF"
        });

        this.offerShapeRecipe(DanmakuTypes.BULLET, new String[]{
                "FFFFFF",
                "FFTTFF",
                "FTTTTF",
                "FTTTTF",
                "FTTTTF",
                "FFFFFF"
        });

        this.offerShapeRecipe(DanmakuTypes.FIREBALL, new String[]{
                "FFTTFF",
                "FTFFTF",
                "TFTTFT",
                "TFTTFT",
                "FTFFTF",
                "FFTTFF"
        });

        this.offerShapeRecipe(DanmakuTypes.FIREBALL_GLOWY, new String[]{
                "FFTTFF",
                "FTTTTF",
                "TTTTTT",
                "TTTTTT",
                "FTTTTF",
                "FFTTFF"
        });

        this.offerShapeRecipe(DanmakuTypes.KUNAI, new String[]{
                "FFTTFF",
                "FTTTTF",
                "FFTTFF",
                "FFTTFF",
                "FTFFTF",
                "FFFFFF"
        });

        this.offerShapeRecipe(DanmakuTypes.RICE, new String[]{
                "FFTTFF",
                "FTTTTF",
                "FTTTTF",
                "FTTTTF",
                "FTTTTF",
                "FFTTFF"
        });

        this.offerShapeRecipe(DanmakuTypes.STAR, new String[]{
                "FFFFFF",
                "FFTTFF",
                "TTTTTT",
                "FFTTFF",
                "FTFFTF",
                "FFFFFF"
        });

        this.offerShapeRecipe(DanmakuTypes.NOTE, new String[]{
                "FFFFTF",
                "FFFFTF",
                "FFFFTF",
                "FTTFTF",
                "FTTFFF",
                "FTTFFF"
        });
    }

    public void offerShapeRecipe(DanmakuType type, List<List<Boolean>> shape) {
        this.factory.register(type.getId(), new DanmakuShapeDrawRecipe(shape, IngredientStack.of(type.toShape().getItemStackTemplate())));
    }

    public void offerShapeRecipe(DanmakuType type, String[] shape) {
        List<List<Boolean>> list = new ArrayList<>();

        for (String line : shape) {
            ArrayList<Boolean> row = new ArrayList<>();
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == 'T') {
                    row.add(true);
                } else if (c == 'F') {
                    row.add(false);
                } else {
                    throw new IllegalArgumentException("Invalid character in shape string: " + c);
                }
            }
            list.add(row);
        }

        this.offerShapeRecipe(type, list);
    }

    @Override
    public String getName() {
        return "Danmaku Shape Recipe Provider";
    }
}
