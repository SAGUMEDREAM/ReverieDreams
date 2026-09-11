package cc.thonly.reverie_dreams.neoforge.integration.sparkle;

import cc.thonly.reverie_dreams.neoforge.integration.sparkle.predicate.NPCMovementAnimationPredicate;
import cc.thonly.reverie_dreams.neoforge.integration.sparkle.predicate.NPCRouletteAnimationPredicate;
import com.micaftic.morpher.client.animation.IAnimationPredicate;
import com.micaftic.morpher.client.animation.condition.ConditionArmor;
import com.micaftic.morpher.client.animation.predicate.*;
import com.micaftic.morpher.client.model.AnimationDataProvider;
import com.micaftic.morpher.client.model.ModelResourceBundle;
import com.micaftic.morpher.client.model.PlayerModelBundle;
import com.micaftic.morpher.client.model.processor.*;
import com.micaftic.morpher.geckolib3.core.builder.Animation;
import com.micaftic.morpher.geckolib3.core.builder.AnimationController;
import com.micaftic.morpher.geckolib3.core.controller.CompositeAnimationController;
import com.micaftic.morpher.geckolib3.core.controller.IAnimationController;
import com.micaftic.morpher.geckolib3.core.controller.PredicateBasedController;
import com.micaftic.morpher.geckolib3.core.enums.PlayState;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import net.minecraft.world.entity.EquipmentSlot;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class NPCPlayerAnimationControllers {
    private static final String PLAYER_PREFIX = "player";
    private static final String MAID_PREFIX = "maid";
    private static final ProcessorPipeline<NPCCapability, PlayerModelBundle> REGISTRY = new ProcessorPipeline<>();

    @SuppressWarnings("ALL")
    private static void registerControllers() {
//        System.out.println("registered controllers");
        IAnimationPredicate<NPCCapability> stop = (event, evaluator) -> PlayState.STOP;
        registerParallelController("pre_parallel", (name, cap, animation) -> new CompositeAnimationController(cap, name, 0.0F, (animation == null ? stop : new NamedAnimationPredicate<>(animation))));
        registerController("vehicle", (name, cap) -> new CompositeAnimationController(cap, name, 0.1F, new LivingMovementAnimationPredicate()));
        registerSlotController("pre_main", (name, cap) -> new CompositeAnimationController(cap, name, 0.0F, stop));
        registerController("main", (name, cap) -> new CompositeAnimationController(cap, name, 0.1F, new NPCMovementAnimationPredicate()));
        registerSlotController("post_main", (name, cap) -> new CompositeAnimationController(cap, name, 0.0F, stop));
        registerSlotController("pre_hold", (name, cap) -> new CompositeAnimationController(cap, name, 0.0F, stop));
        registerController("hold_offhand", (name, cap) -> new CompositeAnimationController(cap, name, 0.1F, new OffHandHoldPredicate()));
        registerController("hold_mainhand", (name, cap) -> new CompositeAnimationController(cap, name, 0.1F, new MainHandHoldPredicate()));
        registerSlotController("post_hold", (name, cap) -> new CompositeAnimationController(cap, name, 0.0F, stop));

        registerSlotController("pre_swing", (name, cap) -> new CompositeAnimationController(cap, name, 0.0F, stop));
        registerController("swing", (name, cap) -> new CompositeAnimationController(cap, name, 0.0F, new ItemHoldAnimationPredicate()));
        registerSlotController("post_swing", (name, cap) -> new CompositeAnimationController<>(cap, name, 0.0F, stop));
        registerSlotController("pre_use", (name, cap) -> new CompositeAnimationController<>(cap, name, 0.0F, stop));
        registerController("use", (name, cap) -> new CompositeAnimationController(cap, name, 0.1F, new InteractionHandAnimationPredicate()));
        registerSlotController("post_use", (name, cap) -> new CompositeAnimationController<>(cap, name, 0.0F, stop));
//        registerNamedController("misc", new String[]{"game_win", "game_lost", "beg"}, true, (name, cap) -> new CompositeAnimationController<>(cap, name, 0.1F, new NPCStateAnimationPredicate(false)));
        registerController("passenger", (name, cap) -> new CompositeAnimationController(cap, name, 0.1F, new OffhandAttackAnimationPredicate()));
        registerController("cap", (name, cap) -> new PredicateBasedController<>(cap, name, 0.0F, new NPCRouletteAnimationPredicate()));
        registerParallelController("parallel", (name, cap, animation) -> new CompositeAnimationController(cap, name, 0.0F, (animation == null ? stop : new NamedAnimationPredicate<>(animation)), true));
        registerArmorController("armor", (name, cap, slot) -> new CompositeAnimationController(cap, name, 0.0F, new ArmorPredicate(slot)));
//        registerNamedController("statue", new String[]{"statue", "garage_kit"}, true, (name, cap) -> new CompositeAnimationController<>(cap, name, 0.0F, new NPCStateAnimationPredicate(true)));
    }

    public static synchronized Consumer<NPCCapability> buildControllers(PlayerModelBundle model, ModelResourceBundle resources) {
        REGISTRY.initializeOnce(NPCPlayerAnimationControllers::registerControllers);
        return REGISTRY.buildAll(model, resources);
    }

    private static ModelProcessor<NPCCapability, PlayerModelBundle> registerController(String name, BiFunction<String, NPCCapability, IAnimationController<NPCCapability>> factory) {
        return REGISTRY.register((model, resources) -> (capability, consumer) -> {
            consumer.accept(factory.apply("player." + name, capability));
        });
    }

    private static ModelProcessor<NPCCapability, PlayerModelBundle> registerSlotController(String name, BiFunction<String, NPCCapability, IAnimationController<NPCCapability>> factory) {
        return REGISTRY.register(new ControllerSlotBinder<>(PLAYER_PREFIX, name, DataProvider.INSTANCE, factory));
    }

    private static ModelProcessor<NPCCapability, PlayerModelBundle> registerNamedController(String name, String[] animations, boolean checkEntries, BiFunction<String, NPCCapability, IAnimationController<NPCCapability>> factory) {
        return REGISTRY.register(new NamedModelProcessor<>(MAID_PREFIX, name, animations, checkEntries, DataProvider.INSTANCE, factory));
    }

    private static ModelProcessor<NPCCapability, PlayerModelBundle> registerParallelController(String name, TriFunction<String, NPCCapability, String, IAnimationController<NPCCapability>> factory) {
        return REGISTRY.register(new ParallelProcessor<>(PLAYER_PREFIX, name, true, DataProvider.INSTANCE, factory));
    }

    private static ModelProcessor<NPCCapability, PlayerModelBundle> registerArmorController(String name, TriFunction<String, NPCCapability, EquipmentSlot, IAnimationController<NPCCapability>> factory) {
        return REGISTRY.register(new ArmorSlotProcessor<>(PLAYER_PREFIX, name, DataProvider.INSTANCE, factory));
    }

    public enum DataProvider implements AnimationDataProvider<PlayerModelBundle> {
        INSTANCE;

        public Object2ReferenceMap<String, AnimationController> getAnimationEntries(PlayerModelBundle model, ModelResourceBundle resources) {
            return model.getAnimationEntries();
        }

        public Object2ReferenceMap<String, Animation> getAnimations(PlayerModelBundle model, ModelResourceBundle resources) {
            return model.getMainAnimations();
        }

        public ConditionArmor getConditionArmor(PlayerModelBundle model, ModelResourceBundle resources) {
            return model.getConditionManager().getArmor();
        }
    }

}
