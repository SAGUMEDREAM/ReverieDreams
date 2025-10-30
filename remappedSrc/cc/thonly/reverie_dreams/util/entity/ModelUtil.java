package cc.thonly.reverie_dreams.util.entity;

import cc.thonly.reverie_dreams.Touhou;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import net.minecraft.resources.ResourceLocation;

public class ModelUtil {
    public static ResourceLocation id(String path) {
        return Touhou.id(path);
    }

    public static Model loadModel(ResourceLocation id) {
        return BbModelLoader.load(id);
    }
}
