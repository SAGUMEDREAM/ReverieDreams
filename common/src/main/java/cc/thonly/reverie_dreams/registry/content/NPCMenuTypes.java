package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCMenuType;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCCompanionEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.gui.entity.NPCSkinGui;
import cc.thonly.reverie_dreams.gui.entity.NPCStateGui;
import cc.thonly.reverie_dreams.gui.entity.NPCWorkGui;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import cc.thonly.reverie_dreams.world.RDBuiltInGameRules;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"resource", "unused"})
public class NPCMenuTypes {
    public static final NPCMenuType BASE_INFO = registerMenuType(
            "base_info",
            new NPCMenuType()
                    .factory((player, npc, currentGui) -> new GuiElementBuilder()
                            .setItem(Items.NAME_TAG)
                            .setItemName(Component.translatable("gui.npc.info.base_info", npc.getName().getString()))
                            .setLore(NPCMenuTypes.createBaseInfo(player, npc))
                            .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                            .setCallback((index, type, action, basedGui) -> {
                                SoundEventPlayUtils.playUISound(player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                            })
                    )
                    .predicate(ofSimple()));

//    public static final NPCMenuType FOOD = registerMenuType(
//            "food",
//            new NPCMenuType()
//                    .factory((player, npc, currentGui) -> {
//                        NPCSimpleEntity simple = (NPCSimpleEntity) npc;
//
//                        return new GuiElementBuilder()
//                                .setItem(Items.COOKED_CHICKEN)
//                                .setItemName(Component.translatable("gui.npc.info.food"))
//                                .setLore(List.of(
//                                        Component.translatable("gui.npc.info.food.nutrition", simple.getFoodData().getNutrition() + " / 20.0"),
//                                        Component.translatable("gui.npc.info.food.saturation", simple.getFoodData().getSaturation() + " / 20.0")
//                                ))
//                                .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
//                                .setCallback((index, type, action, basedGui) -> {
//                                    SoundEventPlayUtils.playUISound(player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
//                                });
//                    })
//                    .predicate(ofSimple())
//    );

//    public static final NPCMenuType HEALTH = registerMenuType(
//            "health",
//            new NPCMenuType()
//                    .factory((player, npc, currentGui) -> {
//                        NPCSimpleEntity simple = (NPCSimpleEntity) npc;
//
//                        return new GuiElementBuilder()
//                                .setItem(Items.GOLDEN_APPLE)
//                                .setItemName(Component.translatable("gui.npc.info.health", simple.getHealth(), simple.getMaxHealth()))
//                                .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
//                                .setCallback((index, type, action, basedGui) -> {
//                                    SoundEventPlayUtils.playUISound(player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
//                                });
//                    })
//                    .predicate(ofSimple())
//    );

//    public static final NPCMenuType ARMOR = registerMenuType(
//            "armor",
//            new NPCMenuType()
//                    .factory((player, npc, currentGui) -> {
//                        NPCSimpleEntity simple = (NPCSimpleEntity) npc;
//
//                        return new GuiElementBuilder()
//                                .setItem(Items.IRON_HELMET)
//                                .setItemName(Component.translatable("gui.npc.info.armor", simple.getArmorValue()))
//                                .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
//                                .setCallback((index, type, action, basedGui) -> {
//                                    SoundEventPlayUtils.playUISound(player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
//                                });
//
//                    })
//                    .predicate(ofSimple())
//    );

    public static final NPCMenuType STATE = registerMenuType(
            "state",
            new NPCMenuType()
                    .factory((player, npc, currentGui) -> {
                        NPCSimpleEntity simple = (NPCSimpleEntity) npc;
                        var workingPos = simple.getWorkingPos();

                        return new GuiElementBuilder()
                                .setItem(npc.getNpcState().getItemDisplay().value())
                                .setItemName(Component.translatable("gui.npc.state.button", npc.getNpcState().translationKey()))
                                .setLore(List.of(
                                        simple.getNpcState() == NPCStates.WORKING
                                                ? Component.translatable("gui.npc.mode.work.originpos")
                                                .append(" : (" + workingPos.getX() + " "
                                                        + workingPos.getY() + " "
                                                        + workingPos.getZ() + ")")
                                                : Component.empty()
                                ))
                                .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                                .setCallback((index, type, action, basedGui) -> {
                                    SoundEventPlayUtils.playUISound(player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                                    new NPCStateGui(player, simple).open();
                                });
                    })
                    .predicate(ofSimple())
    );

    public static final NPCMenuType WORK_MODE = registerMenuType(
            "work_mode",
            new NPCMenuType()
                    .factory((player, npc, currentGui) -> {
                        NPCSimpleEntity simple = (NPCSimpleEntity) npc;
                        NPCWorkMode workMode = simple.getWorkMode();

                        return new GuiElementBuilder()
                                .setItem(workMode.getItemDisplay().value())
                                .setItemName(Component.translatable("gui.npc.work.mode"))
                                .setLore(List.of(workMode.translationKey()))
                                .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                                .setCallback((index, type, action, basedGui) -> {
                                    SoundEventPlayUtils.playUISound(player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                                    new NPCWorkGui(player, simple).open();
                                });
                    })
                    .predicate(ofSimple())
    );

    public static final NPCMenuType XP = registerMenuType("xp",
            new NPCMenuType()
                    .factory((player, npc, currentGui) -> {
                        NPCSimpleEntity simple = (NPCSimpleEntity) npc;
                        return new GuiElementBuilder()
                                .setItem(Items.EXPERIENCE_BOTTLE)
                                .setItemName(Component.translatable("gui.npc.info.xp", simple.getStoredExperience()))
                                .setLore(List.of(Component.translatable("gui.npc.info.xp.button")))
                                .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                                .setCallback((index, type, action, basedGui) -> {
                                    int experienceAmount = simple.getStoredExperience();
                                    if (experienceAmount > 0) {
                                        SoundEventPlayUtils.playUISound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                                    }
                                    player.giveExperiencePoints(experienceAmount);
                                    simple.setStoredExperience(0);
                                    SoundEventPlayUtils.playUISound(player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                                });
                    })
                    .predicate(ofSimple())
    );

    public static final NPCMenuType AUTO_PICK = registerMenuType(
            "auto_pick",
            new NPCMenuType()
                    .factory((player, npc, currentGui) -> {
                        NPCSimpleEntity simple = (NPCSimpleEntity) npc;
                        return new GuiElementBuilder()
                                .setItem(Items.NETHERITE_SCRAP)
                                .setName(Component.translatable("gui.npc.info.auto-pick"))
                                .setComponent(RDDataComponentTypes.SHOW_ONLY.value(), Unit.INSTANCE)
                                .setComponent(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, simple.isAutoPick())
                                .setCallback((index, type, action, basedGui) -> {
                                    simple.setAutoPick(
                                            !simple.isAutoPick()
                                    );

                                    SoundEventPlayUtils.playUISound(
                                            player,
                                            SoundEvents.UI_BUTTON_CLICK.value(),
                                            1.0f,
                                            1.0f
                                    );
                                });
                    })
                    .predicate(ofSimple())
    );

//    public static final NPCMenuType FAVORABILITY = registerMenuType(
//            "favorability",
//            new NPCMenuType()
//                    .factory((player, npc, currentGui) -> {
//                        NPCSimpleEntity simple = (NPCSimpleEntity) npc;
//
//                        return new GuiElementBuilder()
//                                .setItem(Items.APPLE)
//                                .setName(Component.translatable(
//                                        "gui.npc.info.favorability",
//                                        simple.getFavorabilityContainer().get(
//                                                player.getUUID()
//                                        )
//                                ))
//                                .setComponent(
//                                        RDDataComponentTypes.SHOW_ONLY.value(),
//                                        Unit.INSTANCE
//                                )
//                                .setCallback((index, type, action, basedGui) ->
//                                        SoundEventPlayUtils.playUISound(
//                                                player,
//                                                SoundEvents.UI_BUTTON_CLICK.value(),
//                                                1.0f,
//                                                1.0f
//                                        ));
//                    })
//                    .predicate(ofSimple())
//    );
    public static final NPCMenuType MODIFY_SKIN = registerMenuType("modify_skin",
            new NPCMenuType()
                    .factory((player, npc, currentGui) -> {
                        NPCSimpleEntity simple = (NPCSimpleEntity) npc;

                        GuiElementBuilder builder = new GuiElementBuilder();
                        builder.setItem(Items.RED_DYE);
                        builder.setItemName(Component.translatable("gui.npc.info.skin"));
                        builder.setCallback((i, clickType, containerInput, slotBasedGui) -> {
                            SoundEventPlayUtils.playUISound(
                                    player,
                                    SoundEvents.UI_BUTTON_CLICK.value(),
                                    1.0f,
                                    1.0f
                            );
                            if (currentGui != null) {
                                currentGui.close();
                            }
                            NPCSkinGui.open(player, simple);
                        });
                        return builder;
                    })
                    .predicate((player, npc) -> {
                        ServerLevel level = player.level();
                        GameRules gameRules = level.getGameRules();
                        return gameRules.get(RDBuiltInGameRules.FREE_CHOICE_OF_ROLE.value());
                    })
    );
    @Nullable
    public static NPCMenuType MODIFY_MODEL;

    public static List<Component> createBaseInfo(ServerPlayer player, BaseNPCLikeEntity entity) {
        List<Component> components = new ArrayList<>();
        if (!(entity instanceof NPCSimpleEntity npc)) {
            return components;
        }
        components.add(Component.translatable("gui.npc.info.name", npc.getName().getString()));
        components.add(Component.translatable("gui.npc.info.favorability", npc.getFavorabilityContainer().get(player.getUUID())));
        components.add(Component.translatable("gui.npc.info.health", npc.getHealth(), npc.getMaxHealth()));
        components.add(Component.translatable("gui.npc.info.armor", npc.getArmorValue()));
        components.add(Component.translatable("gui.npc.info.food"));
        components.add(Component.empty().append("- ").append(Component.translatable("gui.npc.info.food.nutrition", npc.getFoodData().getNutrition() + " / 20.0")));
        components.add(Component.empty().append("- ").append(Component.translatable("gui.npc.info.food.saturation", npc.getFoodData().getSaturation() + " / 20.0")));

        return components;
    }

    public static NPCMenuType registerMenuType(String name, NPCMenuType menuType) {
        return registerMenuType(ReverieDreams.id(name), menuType);
    }

    public static NPCMenuType registerMenuType(Identifier key, NPCMenuType menuType) {
        return BuiltInRegistryProviders.registerForBuiltin(BuiltInRegistryProviders.NPC_MENU_TYPE, key, menuType);
    }

    public static NPCMenuType.NPCPredicate ofSimple() {
        return (player, npc) -> npc instanceof NPCSimpleEntity;
    }

    public static NPCMenuType.NPCPredicate ofCompanion() {
        return (player, npc) -> npc instanceof NPCCompanionEntity;
    }

    public static List<NPCMenuType> get(ServerPlayer player, BaseNPCLikeEntity npc) {
        List<NPCMenuType> list = new ArrayList<>();
        for (NPCMenuType menuType : BuiltInRegistryProviders.NPC_MENU_TYPE) {
            if (menuType.test(player, npc)) {
                list.add(menuType);
            }
        }
        return list;
    }

    public static void bootstrap(RegistryProvider<NPCMenuType> npcMenuTypes) {

    }
}
