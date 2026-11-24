package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractRecipeTypeProvider;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ShapeDrawRecipeProvider extends AbstractRecipeTypeProvider {
    public final Factory<DanmakuShapeDrawRecipe> factory = this.getOrCreateFactory(RecipeManager.DANMAKU_SHAPE_DRAW, DanmakuShapeDrawRecipe.class);

    public ShapeDrawRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured(HolderLookup.Provider provider) {
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

        this.offerShapeRecipe(DanmakuTypes.LEAF, new String[]{
                "FFFFTF",
                "FFTTTF",
                "FTTTTF",
                "FTTTFF",
                "FTTFFF",
                "TFFFFF"
        });

        this.offerShapeRecipe(DanmakuTypes.BUTTERFLY, new String[]{
                "TFFFFT",
                "TTFFTT",
                "FTTTTF",
                "FTTTTF",
                "TTFFTT",
                "FFFFFF"
        });

        this.offerShapeRecipe(DanmakuTypes.ICICLE, new String[]{
                "FTTTTF",
                "FTTTTF",
                "FFTTTF",
                "FFTTFF",
                "FFTTFF",
                "FFTFFF"
        });

        this.offerShapeRecipe(DanmakuTypes.SAKURA_PETAL, new String[]{
                "FTFFTF",
                "FTTTTF",
                "FTTTTF",
                "FFTTTF",
                "FFTTFF",
                "FFFFFF"
        });

        this.offerShapeRecipe(DanmakuTypes.THROWING_KNIFE, new String[]{
                "FFFTFF",
                "FFTTFF",
                "FFTTFF",
                "FTTTTF",
                "FFTFFF",
                "FFTTFF"
        });
        this.offerShapeRecipe(DanmakuTypes.PELLET, new String[]{
                "FFFFFF", "FFFFFF", "FFTFFF", "FFFFFF", "FFFFFF", "FFFFFF"
        });
        this.offerShapeRecipe(DanmakuTypes.NEEDLE, new String[]{
                "FFTFFF", "FFTFFF", "FFTFFF", "FFTFFF", "FFTFFF", "FFTFFF"
        });
        this.offerShapeRecipe(DanmakuTypes.WATER_DROP, new String[]{
                "FFTFFF", "FFTTFF", "FTTTTF", "TTTTTT", "FTTTTF", "FFTTFF"
        });
        this.offerShapeRecipe(DanmakuTypes.YIN_YANG, new String[]{
                "FFTTFF", "FTFTTF", "TFFTTT", "TTTFFT", "FTTFTF", "FFTTFF"
        });
        this.offerShapeRecipe(DanmakuTypes.BACILLUS, new String[]{
                "TTFFFF", "TTTFFF", "FTTTFF", "FFTTTF", "FFFTTT", "FFFFTT"
        });
        this.offerShapeRecipe(DanmakuTypes.HEART, new String[]{
                "FTFFTF", "TTTTTT", "TTTTTT", "FTTTTF", "FFTTFF", "FFFFFF"
        });
        this.offerShapeRecipe(DanmakuTypes.ARROW, new String[]{
                "FFTFFF", "FTTTFF", "FFTFFF", "FFTFFF", "FFTFFF", "FTTTFF"
        });
        this.offerShapeRecipe(DanmakuTypes.OVAL, new String[]{
                "FFFFFF", "FFTTFF", "FTTTTF", "FTTTTF", "FFTTFF", "FFFFFF"
        });
        this.offerShapeRecipe(DanmakuTypes.MEDIUM_ORB, new String[]{
                "FFFFFF", "FFTTFF", "FTFFTF", "FTFFTF", "FFTTFF", "FFFFFF"
        });
        this.offerShapeRecipe(DanmakuTypes.HIGHLIGHT_ORB, new String[]{
                "FFFFFF", "FFTTFF", "FTTFTF", "FTFFTF", "FFTTFF", "FFFFFF"
        });
        this.offerShapeRecipe(DanmakuTypes.LIGHT_ORB, new String[]{
                "FFTTFF", "FTFFTF", "TFTFFT", "TFFTFT", "FTFFTF", "FFTTFF"
        });

        List<Identifier> missing = BuiltInRegistryProviders.DANMAKU_TYPE.stream()
                .filter(type -> !type.isDeleteFromList() && type.getId().getNamespace().equals(ReverieDreams.MOD_ID))
                .map(DanmakuType::getId)
                .map(id -> Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace('/', '-')))
                .filter(id -> !this.factory.getRegistries().containsKey(id))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing shape drawing recipes for craftable danmaku types: " + missing);
        }
    }

    public void offerShapeRecipe(DanmakuType type, List<List<Boolean>> shape) {
        // The editor returns the first exact match, so equal drawings would hide one of the results.
        for (var entry : this.factory.getRegistries().entrySet()) {
            if (entry.getValue().getShape().equals(shape)) {
                throw new IllegalStateException("Duplicate shape drawing for " + type.getId() + " and " + entry.getKey());
            }
        }
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
