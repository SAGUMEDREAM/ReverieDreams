package cc.thonly.reverie_dreams.util.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import net.minecraft.resources.ResourceLocation;

public class ModelUtil {
    public static ResourceLocation id(String path) {
        return ReverieDreams.id(path);
    }

    public static Model loadModel(ResourceLocation id) {
        return BbModelLoader.load(id);
    }
}
