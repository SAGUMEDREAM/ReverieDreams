package cc.thonly.reverie_dreams.fabric.util;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.util.PlatformContext;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.AjBlueprintLoader;
import de.tomalbrc.bil.file.loader.AjModelLoader;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

@Slf4j
public class ModelUtil {
    public static Identifier id(String path) {
        return ReverieDreams.id(path);
    }

    public static Model loadModel(Identifier id, Function<Identifier, Model> function) {
        try {
            var model = function.apply(id);
            if (PlatformContext.isDevMode()) {
                log.info("Loaded model {}", id);
            }
            return model;
        } catch (Exception e) {
            log.error("Can't load model {}", id.toString(), e);
            throw e;
        }
    }

    public static Model loadBBModel(Identifier id) {
        return loadModel(id, BbModelLoader::load);
    }

    public static Model loadAjModel(Identifier id) {
        return loadModel(id, AjModelLoader::load);
    }

    public static Model loadAjBlueprint(Identifier id) {
        return loadModel(id, AjBlueprintLoader::load);
    }

}
