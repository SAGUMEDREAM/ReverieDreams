package cc.thonly.reverie_dreams.util.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.util.ConstantInfo;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.AjModelLoader;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

@Slf4j
public class ModelUtil {
    public static ResourceLocation id(String path) {
        return ReverieDreams.id(path);
    }

    public static Model loadModel(ResourceLocation id, Function<ResourceLocation, Model> function) {
        try {
            var model = function.apply(id);
            if (ConstantInfo.isDevMode()) {
                log.info("Loaded model {}", id);
            }
            return model;
        } catch (Exception e) {
            log.error("Can't load model {}", id.toString(), e);
            throw e;
        }
    }

    public static Model loadBBModel(ResourceLocation id) {
        return loadModel(id, BbModelLoader::load);
    }

    public static Model loadAjModel(ResourceLocation id) {
        return loadModel(id, AjModelLoader::load);
    }

    public static Model loadAjBlueprint(ResourceLocation id) {
        return loadModel(id, AjModelLoader::load);
    }

}
