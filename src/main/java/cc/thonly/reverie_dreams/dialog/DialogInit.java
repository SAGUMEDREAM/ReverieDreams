package cc.thonly.reverie_dreams.dialog;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.entity.MIEntities;
import cc.thonly.registry_modifier.api.DynamicRegistryManagerCallback;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.OwnerBinding;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.Translatable;
import cc.thonly.reverie_dreams.util.NetUtil;
import cc.thonly.reverie_dreams.util.ConstantInfo;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.dialog.AfterAction;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.action.SimpleDialogAction;
import net.minecraft.dialog.body.ItemDialogBody;
import net.minecraft.dialog.body.PlainMessageDialogBody;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.dialog.type.MultiActionDialog;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.net.URI;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialogInit {
    public static final Map<String, Dialog> ARGS_DIALOG = new Object2ObjectLinkedOpenHashMap<>();
    private static final List<String> ALTAR_STR = List.of(
            "§b⏹§b⏹§b⏹§b⏹§c⏹§b⏹§b⏹§b⏹§b⏹",
            "§b⏹§c⏹§b⏹§b⏹§b⏹§b⏹§b⏹§c⏹§b⏹",
            "§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹",
            "§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹",
            "§c⏹§b⏹§b⏹§b⏹§e⏹§b⏹§b⏹§b⏹§c⏹",
            "§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹",
            "§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹§b⏹",
            "§b⏹§c⏹§b⏹§b⏹§b⏹§b⏹§b⏹§c⏹§b⏹",
            "§b⏹§b⏹§b⏹§b⏹§c⏹§b⏹§b⏹§b⏹§b⏹"
    );
    private static final Dialog TEMPLATE = new MultiActionDialog(
            new DialogCommonData(
                    Text.literal("标题"),
                    Optional.empty(),
                    true, false,
                    AfterAction.CLOSE,
                    new ArrayList<>(List.of(

                    )),
                    new ArrayList<>(List.of(

                    ))
            ),
            new ArrayList<>(List.of()),
            Optional.empty(),
            1
    );
    private static final URI GROUP_LINK = URI.create("https://qun.qq.com/universal-share/share?ac=1&authKey=ijHEaEfpwdV4sNlrpdjmEjSZsCWx4zO9F7UM4B8vI47y4nIg%2FtNOSptKxn%2BxSrhN&busi_data=eyJncm91cENvZGUiOiIxMDU3MTMyMTc1IiwidG9rZW4iOiJjN0xaWmtWWk9KUk9hSFpkdHRrUmE4U2NhUzQyZDRHYlZIblYvRkxrWlRoUmJ6T0VXcG9nVG9hUllqRHQxUWllIiwidWluIjoiODA3MTMxODI5In0%3D&data=LLbqAIG4qDGldPh2yXGEmiNpZEV9Ch4t82ROb7z5ozfubUArvyZNB5d8huGu6plHcZ61ugJfy5suoiA4Lr6e_A&svctype=4&tempid=h5_group_info");
    public static List<ItemDialogBody> UPGRADE_ITEM_BODIES = new ArrayList<>();
    public static final List<ItemStack> UPGRADE_ITEM = getUpgradeItemList();
    public static final RegistryKey<Registry<Dialog>> REGISTRY = RegistryKeys.DIALOG;
    public static final Identifier MAIN_HELP_ID = Touhou.id("main_help");
    public static final Identifier ALTAR_HELP_ID = Touhou.id("altar_help");
    public static final Identifier CRAFTING_DANMAKU_HELP_ID = Touhou.id("crafting_danmaku_help");
    public static final Identifier UPGRADE_DANMAKU_HELP_ID = Touhou.id("upgrade_danmaku_help");
    public static final Identifier GET_FUMO_HELP_ID = Touhou.id("get_fumo_help");
    public static final Identifier ROLE_HELP_ID = Touhou.id("role_help");
    public static final Identifier TOUHOU_MYSTIA_HELP_ID = Touhou.id("touhou_mystia_help");
    public static final Identifier REGISTRIES_ID = Touhou.id("registries");
    public static final Identifier OTHER_MOD_LIST_ID = Touhou.id("other_mod_list");
    public static final MultiActionDialog MAIN_HELP;
    public static final MultiActionDialog ALTAR_HELP;
    public static final MultiActionDialog CRAFTING_DANMAKU_HELP;
    public static final MultiActionDialog UPGRADE_DANMAKU_HELP;
    public static final MultiActionDialog GET_FUMO_HELP;
    public static final MultiActionDialog ROLE_HELP;
    public static final MultiActionDialog TOUHOU_MYSTIA_HELP;
    public static MultiActionDialog REGISTRIES;
    public static final MultiActionDialog OTHER_MOD_LIST;

    public static void initRegistriesDialog() {
        record SubInfo(String argId, Identifier registryKey, MultiActionDialog dialog) {
        }
        List<SubInfo> list = new ArrayList<>();
        for (Map.Entry<RegistryKey<? extends Registry<?>>, IntrinsicalRegister<?>> mapEntry : RegistryManager.ROOT.entrySet()) {
            Identifier key = mapEntry.getKey().getValue();
            IntrinsicalRegister<?> registry = mapEntry.getValue();
            MutableText text = Text.empty();
            var page = new MultiActionDialog(
                    new DialogCommonData(
                            Text.empty().append(Text.translatable("dialog.title.registries")).append(Text.literal(": %s".formatted(registry.getKey()))),
                            Optional.empty(),
                            true, false,
                            AfterAction.CLOSE,
                            new ArrayList<>(),
                            new ArrayList<>()
                    ),
                    new ArrayList<>(List.of()),
                    Optional.empty(),
                    1
            );
            for (Map.Entry<Identifier, ?> id2ValueMapEntry : registry.entrySet()) {
                Identifier entryKey = id2ValueMapEntry.getKey();
                Object entryValue = id2ValueMapEntry.getValue();
                if (entryValue instanceof Translatable translatable) {
                    text.append(entryKey.toString());
                    text.append(" → ");
                    text.append(Text.translatable(translatable.translateKey()));
                    text.append("\n");
                } else if (entryValue instanceof OwnerBinding<?> owner) {
                    IntrinsicalRegister<?> registryRef = owner.getOwner();
                    text.append(entryKey.toString());
                    text.append(" → ");
                    text.append(Text.translatable(registryRef.getKey().getValue().getPath() + ".null"));
                    text.append("\n");
                }
            }
            page.common().body().add(new PlainMessageDialogBody(text, 800));
            page.actions().add(new DialogActionButtonData(
                    new DialogButtonData(Text.empty().append(Text.translatable("dialog.text.back")), 200),
                    Optional.of(new SimpleDialogAction(showPage("REGISTRIES")))
            ));
            list.add(new SubInfo("registry/" + key.toString(), key, page));
        }
        REGISTRIES = new MultiActionDialog(
                new DialogCommonData(
                        Text.translatable("dialog.title.registries"),
                        Optional.empty(),
                        true, false,
                        AfterAction.CLOSE,
                        new ArrayList<>(List.of(
                        )),
                        new ArrayList<>(List.of(
                        ))
                ),
                new ArrayList<>(List.of()),
                Optional.empty(),
                1
        );
        for (SubInfo subInfo : list) {
            String argId = subInfo.argId();
            Identifier registryKey = subInfo.registryKey;
            MultiActionDialog dialog = subInfo.dialog();
            REGISTRIES.actions().add(
                    new DialogActionButtonData(
                            new DialogButtonData(
                                    Text.literal(registryKey.toString()),
                                    300
                            ),
                            Optional.of(new SimpleDialogAction(showPage(argId)))
                    )
            );
            ARGS_DIALOG.put(argId, dialog);
        }
        REGISTRIES.actions().add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.text.back")), 200),
                Optional.of(new SimpleDialogAction(showPage("MAIN")))
        ));
        ARGS_DIALOG.put("REGISTRIES", REGISTRIES);
    }

    public static List<ItemStack> getUpgradeItemList() {
        List<ItemStack> list = new ArrayList<>();
        list.add(Items.IRON_SWORD.getDefaultStack());
        list.add(Items.SLIME_BLOCK.getDefaultStack());
        list.add(ModItems.SPEED_FEATHER.getDefaultStack());
        list.add(ModItems.SPELL_CARD_TEMPLATE.getDefaultStack());
        list.forEach(value -> UPGRADE_ITEM_BODIES.add(createItemStackBody(value)));
        return list;
    }

    static {
        MAIN_HELP = new MultiActionDialog(
                new DialogCommonData(
                        Text.translatable("dialog.title.main"),
                        Optional.of(Text.empty()),
                        true, false,
                        AfterAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessageDialogBody(Text.empty(), 200),
                                new PlainMessageDialogBody(Text.empty().append(Text.translatable("dialog.main.welcome")), 200),
                                new PlainMessageDialogBody(Text.empty().append(Text.translatable("dialog.main.description.0", ConstantInfo.VERSION)), 200),
                                new PlainMessageDialogBody(Text.empty(), 200)
                        )),
                        new ArrayList<>(List.of(

                        ))
                ),
                new ArrayList<>(),
                Optional.empty(),
                1);
        ALTAR_HELP = new MultiActionDialog(
                new DialogCommonData(
                        Text.translatable("dialog.title.altar"),
                        Optional.of(Text.empty()),
                        true, false,
                        AfterAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessageDialogBody(Text.empty().append(Text.translatable(ModBlocks.GENSOKYO_ALTAR.getTranslationKey())).append(Text.literal("使用方式：空手 + Shift + 右键")), 200),
                                new PlainMessageDialogBody(Text.empty().append(Text.translatable("dialog.altar.material")), 180),
                                new ItemDialogBody(ModBlocks.SPIRITUAL.strippedLog().asItem().getDefaultStack(), Optional.of(new PlainMessageDialogBody(
                                        Text.empty()
                                                .append(Text.translatable(ModBlocks.SPIRITUAL.strippedLog().getTranslationKey()))
                                                .append(Text.literal(" - "))
                                                .append(Text.literal("§c\u999a").setStyle(Style.EMPTY.withFont(Touhou.id("reverie_dreams"))))
                                                .append(Text.literal(" * 3"))
                                        , 200)

                                ), true, true, 16, 16),
                                new ItemDialogBody(ModBlocks.GENSOKYO_ALTAR.asItem().getDefaultStack(), Optional.of(new PlainMessageDialogBody(
                                        Text.empty()
                                                .append(Text.translatable(ModBlocks.GENSOKYO_ALTAR.getTranslationKey()))
                                                .append(Text.literal(" - "))
                                                .append(Text.literal("§e\u999a").setStyle(Style.EMPTY.withFont(Touhou.id("reverie_dreams"))))
                                                .append(Text.literal(""))
                                        , 200)
                                ), true, true, 16, 16),
                                new PlainMessageDialogBody(Text.empty().append(Text.translatable("dialog.altar.ways")), 180)
                        )),
                        new ArrayList<>(List.of(

                        ))
                ),
                new ArrayList<>(List.of(
                )),
                Optional.empty(),
                9
        );
        CRAFTING_DANMAKU_HELP = new MultiActionDialog(
                new DialogCommonData(
                        Text.translatable("dialog.title.danmaku"),
                        Optional.of(Text.empty()),
                        true, false,
                        AfterAction.CLOSE,
                        new ArrayList<>(List.of(
                                new ItemDialogBody(
                                        new ItemStack(ModBlocks.DANMAKU_CRAFTING_TABLE, 1),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(ModBlocks.DANMAKU_CRAFTING_TABLE.getTranslationKey())).append(Text.literal("")), 200)),
                                        true, true, 16, 16
                                ),
                                new PlainMessageDialogBody(Text.empty().append(Text.translatable("dialog.danmaku.description.0")), 200),
                                new PlainMessageDialogBody(Text.empty().append(Text.translatable("dialog.danmaku.description.1")), 180),
                                new ItemDialogBody(
                                        new ItemStack(Items.RED_DYE, 4),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(Items.RED_DYE.getTranslationKey())).append(Text.literal("")), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemDialogBody(
                                        new ItemStack(Items.FIREWORK_STAR, 1),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(Items.FIREWORK_STAR.getTranslationKey())).append(Text.literal("")), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemDialogBody(
                                        new ItemStack(ModItems.POWER, 35),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(ModItems.POWER.getTranslationKey())).append(Text.literal("")), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemDialogBody(
                                        new ItemStack(ModItems.POINT, 35),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(ModItems.POINT.getTranslationKey())).append(Text.literal("")), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemDialogBody(
                                        new ItemStack(Items.BARRIER),
                                        Optional.of(new PlainMessageDialogBody(Text.translatable("dialog.text.empty"), 200)),
                                        true, true, 16, 16
                                )

                        )),
                        new ArrayList<>(List.of(

                        ))
                ),
                new ArrayList<>(List.of()),
                Optional.empty(),
                1
        );
        UPGRADE_DANMAKU_HELP = new MultiActionDialog(
                new DialogCommonData(
                        Text.translatable("dialog.title.upgrade_danmaku"),
                        Optional.empty(),
                        true, false,
                        AfterAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessageDialogBody(Text.translatable("dialog.upgrade_danmaku.description.0"), 200),
                                new PlainMessageDialogBody(Text.translatable("dialog.upgrade_danmaku.description.1"), 200)
                        )),
                        new ArrayList<>(List.of(

                        ))
                ),
                new ArrayList<>(List.of()),
                Optional.empty(),
                1
        );
        GET_FUMO_HELP = new MultiActionDialog(
                new DialogCommonData(
                        Text.translatable("dialog.title.fumo"),
                        Optional.of(Text.empty()),
                        true, false,
                        AfterAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessageDialogBody(Text.translatable("dialog.fumo.description.0"), 200),
                                new PlainMessageDialogBody(Text.empty().append(Text.translatable("dialog.fumo.description.1")), 200),
                                new PlainMessageDialogBody(Text.empty().append(Text.translatable("dialog.fumo.description.2", Text.translatable(ModItems.FUMO_LICENSE.getTranslationKey()))), 200),
                                new ItemDialogBody(ModItems.FUMO_LICENSE.getDefaultStack(), Optional.empty(), true, true, 16, 16),
                                new ItemDialogBody(Items.VILLAGER_SPAWN_EGG.getDefaultStack(), Optional.empty(), true, true, 16, 16),
                                new ItemDialogBody(ModEntities.SPAWN_EGG_BIND.getOrDefault(ModEntities.FUMO_SELLER_VILLAGER, ModItems.SPAWN_EGG).getDefaultStack(), Optional.empty(), true, true, 16, 16)
                        )),
                        new ArrayList<>(List.of(

                        ))
                ),
                new ArrayList<>(List.of()),
                Optional.empty(),
                1
        );
        ROLE_HELP = new MultiActionDialog(
                new DialogCommonData(
                        Text.translatable("dialog.title.role"),
                        Optional.of(Text.empty()),
                        true, false,
                        AfterAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessageDialogBody(Text.translatable("dialog.role.description.0"), 200),
                                new PlainMessageDialogBody(Text.translatable("dialog.role.description.1"), 200),
                                new PlainMessageDialogBody(Text.translatable("dialog.role.description.2", Text.translatable(ModItems.ROLE_ARCHIVE.getTranslationKey())), 200),
                                new ItemDialogBody(
                                        new ItemStack(ModItems.ROLE_CARD, 1),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(ModItems.ROLE_CARD.getTranslationKey())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemDialogBody(
                                        new ItemStack(ModItems.ROLE_ARCHIVE, 1),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(ModItems.ROLE_ARCHIVE.getTranslationKey())), 200)),
                                        true, true, 16, 16
                                )
                        )),
                        new ArrayList<>(List.of(

                        ))
                ),
                new ArrayList<>(List.of()),
                Optional.empty(),
                1
        );
        TOUHOU_MYSTIA_HELP = new MultiActionDialog(
                new DialogCommonData(
                        Text.translatable("dialog.title.mystia.main"),
                        Optional.of(Text.empty()),
                        true, false,
                        AfterAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessageDialogBody(Text.translatable("dialog.touhou_mystia.description.0"), 200),
                                new PlainMessageDialogBody(Text.translatable("dialog.touhou_mystia.description.1"), 200),
                                new PlainMessageDialogBody(Text.translatable("dialog.touhou_mystia.description.2"), 200),
                                new PlainMessageDialogBody(Text.translatable("dialog.touhou_mystia.description.3"), 200),
                                new PlainMessageDialogBody(Text.translatable("dialog.touhou_mystia.description.4"), 200),
                                new ItemDialogBody(
                                        new ItemStack(MIBlocks.COOKING_POT, 1),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(MIBlocks.COOKING_POT.getTranslationKey())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemDialogBody(
                                        new ItemStack(MIBlocks.CUTTING_BOARD, 1),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(MIBlocks.CUTTING_BOARD.getTranslationKey())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemDialogBody(
                                        new ItemStack(MIBlocks.FRYING_PAN, 1),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(MIBlocks.FRYING_PAN.getTranslationKey())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemDialogBody(
                                        new ItemStack(MIBlocks.GRILL, 1),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(MIBlocks.GRILL.getTranslationKey())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemDialogBody(
                                        new ItemStack(MIBlocks.STEAMER, 1),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(MIBlocks.STEAMER.getTranslationKey())), 200)),
                                        true, true, 16, 16
                                ),
                                new PlainMessageDialogBody(Text.translatable("dialog.touhou_mystia.description.5"), 200),
                                new PlainMessageDialogBody(Text.translatable("dialog.touhou_mystia.description.6"), 200),
                                new PlainMessageDialogBody(Text.translatable("dialog.touhou_mystia.description.7", Text.translatable(MIEntities.TAVERN_VILLAGER.getTranslationKey())), 200),
                                new PlainMessageDialogBody(Text.translatable("dialog.touhou_mystia.description.8", Text.translatable(Blocks.BARREL.getTranslationKey()), Text.translatable(MIEntities.TAVERN_VILLAGER.getTranslationKey())), 200),
                                new ItemDialogBody(
                                        new ItemStack(Blocks.BARREL, 1),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(Blocks.BARREL.getTranslationKey())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemDialogBody(
                                        new ItemStack(Items.VILLAGER_SPAWN_EGG, 1),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(Items.VILLAGER_SPAWN_EGG.getTranslationKey())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemDialogBody(
                                        new ItemStack(ModEntities.SPAWN_EGG_BIND.getOrDefault(MIEntities.TAVERN_VILLAGER, Items.VILLAGER_SPAWN_EGG), 1),
                                        Optional.of(new PlainMessageDialogBody(Text.empty().append(Text.translatable(ModEntities.SPAWN_EGG_BIND.getOrDefault(MIEntities.TAVERN_VILLAGER, Items.VILLAGER_SPAWN_EGG).getTranslationKey())), 200)),
                                        true, true, 16, 16
                                )
                        )),
                        new ArrayList<>(List.of(

                        ))
                ),
                new ArrayList<>(List.of()),
                Optional.empty(),
                1
        );
        OTHER_MOD_LIST = new MultiActionDialog(
                new DialogCommonData(
                        Text.translatable("dialog.title.other_mod_list"),
                        Optional.of(Text.empty()),
                        true, false,
                        AfterAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessageDialogBody(Text.translatable("dialog.other_mod_list.description.0"), 200)
                        )),
                        new ArrayList<>(List.of(

                        ))
                ),
                new ArrayList<>(List.of(
                        new DialogActionButtonData(
                                new DialogButtonData(Text.empty().append(Text.literal("Polymer - Quality of Life")), 200),
                                Optional.of(new SimpleDialogAction(new ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/polymer-qol"))))
                        ),
                        new DialogActionButtonData(
                                new DialogButtonData(Text.empty().append(Text.literal("Polydex")), 200),
                                Optional.of(new SimpleDialogAction(new ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/polydex"))))
                        ),
                        new DialogActionButtonData(
                                new DialogButtonData(Text.empty().append(Text.literal("ExtendedItemView")), 200),
                                Optional.of(new SimpleDialogAction(new ClickEvent.OpenUrl(URI.create("https://github.com/SAGUMEDREAM/ExtendedItemView/releases"))))
                        ),
                        new DialogActionButtonData(
                                new DialogButtonData(Text.empty().append(Text.literal("Polydex2EIV")), 200),
                                Optional.of(new SimpleDialogAction(new ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/polydex2eiv"))))
                        )
                )),
                Optional.empty(),
                1
        );
        MAIN_HELP.actions().addAll(getMainList());
        ALTAR_HELP.actions().addAll(createAltarButtonList());
        ALTAR_HELP.actions().add(getActionMain());
        CRAFTING_DANMAKU_HELP.actions().add(getActionMain());
        UPGRADE_DANMAKU_HELP.common().body().addAll(UPGRADE_ITEM_BODIES);
        UPGRADE_DANMAKU_HELP.actions().add(getActionMain());
        GET_FUMO_HELP.actions().add(getActionMain());
        ROLE_HELP.actions().add(getActionMain());
        TOUHOU_MYSTIA_HELP.actions().add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.touhou_mystia.wiki")), 200),
                Optional.of(new SimpleDialogAction(new ClickEvent.OpenUrl(URI.create("https://izakaya.cc/"))))
        ));
        TOUHOU_MYSTIA_HELP.actions().add(getActionMain());
        OTHER_MOD_LIST.actions().add(getActionMain());
    }

    public static ItemDialogBody createItemStackBody(ItemStack itemStack) {
        return new ItemDialogBody(
                itemStack,
                Optional.of(new PlainMessageDialogBody(Text.empty().append(itemStack.getName()), 200)),
                true, true, 16, 16
        );
    }

    public static List<DialogActionButtonData> getMainList() {
        List<DialogActionButtonData> list = new ArrayList<>();
        list.add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.title.altar")), 200),
                Optional.of(new SimpleDialogAction(showPage("ALTAR")))
        ));
        list.add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.title.danmaku")), 200),
                Optional.of(new SimpleDialogAction(showPage("DANMAKU")))
        ));
        list.add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.title.upgrade_danmaku")), 200),
                Optional.of(new SimpleDialogAction(showPage("UPGRADE_DANMAKU")))
        ));
        list.add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.title.fumo")), 200),
                Optional.of(new SimpleDialogAction(showPage("FUMO")))
        ));
        list.add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.title.role")), 200),
                Optional.of(new SimpleDialogAction(showPage("ROLE")))
        ));
        list.add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.title.mystia.main")), 200),
                Optional.of(new SimpleDialogAction(showPage("TOUHOU_MYSTIA")))
        ));
        list.add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.title.registries")), 200),
                Optional.of(new SimpleDialogAction(showPage("REGISTRIES")))
        ));
        list.add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.title.other_mod_list")), 200),
                Optional.of(new SimpleDialogAction(showPage("OTHER_MOD_LIST")))
        ));
        if (isChina()) {
            list.add(getIssueButtonData());
        }
        list.add(new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.title.open_recipe_manager")), 200),
                Optional.of(new SimpleDialogAction(new ClickEvent.RunCommand("touhou recipe")))
        ));
        list.add(new DialogActionButtonData(
                new DialogButtonData(Text.translatable("dialog.text.exit"), 100),
                Optional.empty()
        ));
        return list;
    }

    public static DialogActionButtonData getIssueButtonData() {
        if (isChina()) {
            return new DialogActionButtonData(
                    new DialogButtonData(Text.empty().append(Text.translatable("dialog.title.cn.issue")), 200),
                    Optional.of(new SimpleDialogAction(new ClickEvent.OpenUrl(GROUP_LINK)))
            );
        }
        return null;
    }

    private static boolean isChina() {
        ZoneId zone = ZoneId.systemDefault();
        String zoneId = zone.getId();

        if (zoneId.equals("Asia/Shanghai") || zoneId.equals("Asia/Chongqing")) {
            return true;
        }

        Locale locale = Locale.getDefault();
        String country = locale.getCountry().toUpperCase();
        if (country.equals("CN") || country.equals("HK") || country.equals("MO") || country.equals("TW")) {
            return true;
        }

        return NetUtil.isChinaNetwork();
    }

    public static List<DialogActionButtonData> createAltarButtonList() {
        List<DialogActionButtonData> list = new ArrayList<>();
        Pattern p = Pattern.compile("§.⏹");
        for (String line : ALTAR_STR) {
            Matcher m = p.matcher(line);
            while (m.find()) {
                String token = m.group();
                MutableText result = Text.empty();
//                result.append(" ");
                result.append(Text.literal(token.replace("⏹", "\u999a"))
                        .setStyle(Style.EMPTY.withFont(Touhou.id("reverie_dreams"))));
//                result.append(" ");
                list.add(new DialogActionButtonData(
                        new DialogButtonData(result, 30),
                        Optional.of(new SimpleDialogAction(showPage("ALTAR")))
                ));
            }
        }
        return list;
    }

    public static ClickEvent.RunCommand showPage(String name) {
        return new ClickEvent.RunCommand("touhou dialog \"%s\"".formatted(name));
    }

    public static DialogActionButtonData getActionMain() {
        return new DialogActionButtonData(
                new DialogButtonData(Text.empty().append(Text.translatable("dialog.text.back")), 200),
                Optional.of(new SimpleDialogAction(showPage("MAIN")))
        );
    }

    public static void bootstrap() {
        DynamicRegistryManagerCallback.Builder<Dialog> builder = DynamicRegistryManagerCallback.createBuilder(REGISTRY);
        builder.register(MAIN_HELP_ID, MAIN_HELP);
        builder.register(ALTAR_HELP_ID, ALTAR_HELP);
        builder.register(CRAFTING_DANMAKU_HELP_ID, CRAFTING_DANMAKU_HELP);
        builder.register(UPGRADE_DANMAKU_HELP_ID, UPGRADE_DANMAKU_HELP);
        builder.register(GET_FUMO_HELP_ID, GET_FUMO_HELP);
        builder.register(ROLE_HELP_ID, ROLE_HELP);
        builder.register(TOUHOU_MYSTIA_HELP_ID, TOUHOU_MYSTIA_HELP);
        builder.register(OTHER_MOD_LIST_ID, OTHER_MOD_LIST);
    }

    static {
        ARGS_DIALOG.put("MAIN", MAIN_HELP);
        ARGS_DIALOG.put("ALTAR", ALTAR_HELP);
        ARGS_DIALOG.put("DANMAKU", CRAFTING_DANMAKU_HELP);
        ARGS_DIALOG.put("UPGRADE_DANMAKU", UPGRADE_DANMAKU_HELP);
        ARGS_DIALOG.put("FUMO", GET_FUMO_HELP);
        ARGS_DIALOG.put("ROLE", ROLE_HELP);
        ARGS_DIALOG.put("TOUHOU_MYSTIA", TOUHOU_MYSTIA_HELP);
        ARGS_DIALOG.put("OTHER_MOD_LIST", OTHER_MOD_LIST);
    }
}
