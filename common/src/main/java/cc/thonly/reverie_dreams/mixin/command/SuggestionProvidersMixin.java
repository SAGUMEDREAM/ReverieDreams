package cc.thonly.reverie_dreams.mixin.command;

import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
@Mixin(SuggestionProviders.class)
public class SuggestionProvidersMixin {
    @Shadow
    @Final
    private static Map<Identifier, SuggestionProvider<SharedSuggestionProvider>> PROVIDERS_BY_NAME;

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static <S extends SharedSuggestionProvider> void preventCharacterInId(Identifier id, SuggestionProvider<SharedSuggestionProvider> provider, CallbackInfoReturnable<SuggestionProvider<S>> cir) {
        if (Objects.equals(id, Identifier.withDefaultNamespace("summonable_entities"))) {
            provider = (context, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.stream().filter(entityType -> {
                boolean a = entityType.isEnabled(context.getSource().enabledFeatures()) && entityType.canSummon();
                boolean b = !BuiltInRegistryProviders.NPC_ROLE_TYPE.stream().map(NPCRoleType::getEntityType).toList().contains(entityType);
                return a && b;
            }), builder, EntityType::getKey, EntityType::getDescription);
            SuggestionProvider<SharedSuggestionProvider> suggestionProvider = PROVIDERS_BY_NAME.putIfAbsent(id, provider);

            cir.setReturnValue((SuggestionProvider<S>) new SuggestionProviders.RegisteredSuggestion(id, provider));
            cir.cancel();
        }
    }
}
