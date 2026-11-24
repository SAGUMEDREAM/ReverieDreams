package cc.thonly.reverie_dreams.paper.npc;

import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.profile.PlayerTextures;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Original role identity and resource-pack texture; this is not a custom entity renderer. */
public record NPCRole(String id, String skinId, String textureKey, String model,
                      String translationKey, String fallbackName) {
    public Component displayName() {
        return Component.translatable(translationKey).fallback(fallbackName);
    }

    public ResolvableProfile profile() {
        var profile = ResolvableProfile.resolvableProfile()
                .uuid(UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8))).name("ReverieNPC");
        // ResourceTexture adds textures/ and .png. The key deliberately contains neither.
        if (textureKey != null) {
            profile.skinPatch(patch -> patch.body(Key.key(textureKey)).model(
                    "wide".equals(model) ? PlayerTextures.SkinModel.CLASSIC : PlayerTextures.SkinModel.SLIM));
        }
        return profile.build();
    }
}
