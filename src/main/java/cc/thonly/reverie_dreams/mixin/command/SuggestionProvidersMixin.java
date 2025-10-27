package cc.thonly.reverie_dreams.mixin.command;

import cc.thonly.reverie_dreams.entity.npc.NPCRole;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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
    private static Map<Identifier, SuggestionProvider<CommandSource>> REGISTRY;

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static <S extends CommandSource> void preventCharacterInId(Identifier id, SuggestionProvider<CommandSource> provider, CallbackInfoReturnable<SuggestionProvider<S>> cir) {
        if (Objects.equals(id, Identifier.ofVanilla("summonable_entities"))) {
            provider = (context, builder) -> CommandSource.suggestFromIdentifier(Registries.ENTITY_TYPE.stream().filter(entityType -> {
                boolean a = entityType.isEnabled(context.getSource().getEnabledFeatures()) && entityType.isSummonable();
                boolean b = !RegistryManager.NPC_ROLE.stream().map(NPCRole::getEntityType).toList().contains(entityType);
                return a && b;
            }), builder, EntityType::getId, EntityType::getName);
            SuggestionProvider<CommandSource> suggestionProvider = REGISTRY.putIfAbsent(id, provider);

            cir.setReturnValue((SuggestionProvider<S>) new SuggestionProviders.LocalProvider(id, provider));
            cir.cancel();
        }
    }
}
