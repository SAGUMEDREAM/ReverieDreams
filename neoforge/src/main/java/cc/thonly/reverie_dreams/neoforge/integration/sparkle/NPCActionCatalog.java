package cc.thonly.reverie_dreams.neoforge.integration.sparkle;

import cc.thonly.reverie_dreams.client.gui.NPCActionWheelScreen.Entry;
import cc.thonly.reverie_dreams.compat.ysm.NPCAnimationActions;
import com.micaftic.morpher.client.gui.ModelMetadataPresenter;
import com.micaftic.morpher.client.model.ModelAssembly;
import com.micaftic.morpher.geckolib3.core.builder.Animation;
import com.micaftic.morpher.resource.models.ModelProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class NPCActionCatalog {
    // Fox and Sparkle use different return types for ModelAssembly#getModelData.
    private static final Method MODEL_DATA = method(ModelAssembly.class, "getModelData");
    private static final Method MODEL_PROPERTIES = method(MODEL_DATA.getReturnType(), "getModelProperties");

    private NPCActionCatalog() {
    }

    public static List<Entry> entries(ModelAssembly assembly) {
        ModelProperties properties = properties(assembly);
        var groups = new LinkedHashMap<String, Entry>();
        properties.getExtraAnimationClassify().forEach((id, actions) -> {
            var children = new ArrayList<Entry>();
            actions.forEach((key, label) -> {
                if (clip(assembly, key) != null) {
                    children.add(action(assembly, key, label));
                }
            });
            if (!children.isEmpty()) {
                groups.put(id, Entry.group(Component.literal(id), children));
            }
        });

        var root = new ArrayList<Entry>();
        properties.getExtraAnimation().forEach((key, label) -> {
            if (key.startsWith("#")) {
                Entry group = groups.remove(key.substring(1));
                if (group != null) {
                    root.add(Entry.group(Component.literal(ChatFormatting.stripFormatting(label)), group.children()));
                }
            } else if (clip(assembly, key) != null) {
                root.add(action(assembly, key, label));
            }
        });
        root.addAll(groups.values());
        return root;
    }

    public static @Nullable Animation declaredClip(ModelAssembly assembly, String key) {
        if (key.isEmpty()) {
            return null;
        }
        Animation clip = clip(assembly, key);
        if (clip == null) {
            return null;
        }
        ModelProperties properties = properties(assembly);
        return properties.getExtraAnimation().containsKey(key)
                || properties.getExtraAnimationClassify().values().stream().anyMatch(actions -> actions.containsKey(key))
                ? clip : null;
    }

    private static @Nullable Animation clip(ModelAssembly assembly, String key) {
        if (assembly.getAnimationBundle() == null || key.isEmpty() || !NPCAnimationActions.isActionName(key)) {
            return null;
        }
        return assembly.getAnimationBundle().getMainAnimations().get(key);
    }

    private static Entry action(ModelAssembly assembly, String key, String label) {
        String translated = ModelMetadataPresenter.getLocalizedModelString(assembly,
                "properties.extra_animation." + key, label.isBlank() ? key : label);
        return Entry.action(Component.literal(ChatFormatting.stripFormatting(translated)), key);
    }

    private static ModelProperties properties(ModelAssembly assembly) {
        try {
            return (ModelProperties) MODEL_PROPERTIES.invoke(MODEL_DATA.invoke(assembly));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read the model's wheel actions", e);
        }
    }

    private static Method method(Class<?> type, String name) {
        try {
            return type.getMethod(name);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Incompatible model metadata API", e);
        }
    }
}
