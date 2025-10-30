package cc.thonly.reverie_dreams.mixin.command;

import cc.thonly.reverie_dreams.entity.npc.NPCRole;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Objects;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

@SuppressWarnings("unchecked")
@Mixin(SuggestionProviders.class)
public class SuggestionProvidersMixin {
    @Shadow
    @Final
    private static Map<ResourceLocation, SuggestionProvider<SharedSuggestionProvider>> PROVIDERS_BY_NAME;

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static <S extends SharedSuggestionProvider> void preventCharacterInId(ResourceLocation id, SuggestionProvider<SharedSuggestionProvider> provider, CallbackInfoReturnable<SuggestionProvider<S>> cir) {
        if (Objects.equals(id, ResourceLocation.withDefaultNamespace("summonable_entities"))) {
            provider = (context, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.stream().filter(entityType -> {
                boolean a = entityType.isEnabled(context.getSource().enabledFeatures()) && entityType.canSummon();
                boolean b = !RegistryManager.NPC_ROLE.stream().map(NPCRole::getEntityType).toList().contains(entityType);
                return a && b;
            }), builder, EntityType::getKey, EntityType::getDescription);
            SuggestionProvider<SharedSuggestionProvider> suggestionProvider = PROVIDERS_BY_NAME.putIfAbsent(id, provider);

            cir.setReturnValue((SuggestionProvider<S>) new SuggestionProviders.RegisteredSuggestion(id, provider));
            cir.cancel();
        }
    }
}
