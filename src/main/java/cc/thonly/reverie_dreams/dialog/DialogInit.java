package cc.thonly.reverie_dreams.dialog;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.entity.MIEntities;
import cc.thonly.registry_modifier.api.DynamicRegistryManagerCallback;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.util.network.NetUtil;
import cc.thonly.reverie_dreams.util.ConstantInfo;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.net.URI;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

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
            new CommonDialogData(
                    Component.literal("标题"),
                    Optional.empty(),
                    true, false,
                    DialogAction.CLOSE,
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
    public static List<ItemBody> UPGRADE_ITEM_BODIES = new ArrayList<>();
    public static final List<ItemStack> UPGRADE_ITEM = getUpgradeItemList();
    public static final ResourceKey<Registry<Dialog>> REGISTRY = Registries.DIALOG;
    public static final ResourceLocation MAIN_HELP_ID = ReverieDreams.id("main_help");
    public static final ResourceLocation ALTAR_HELP_ID = ReverieDreams.id("altar_help");
    public static final ResourceLocation CRAFTING_DANMAKU_HELP_ID = ReverieDreams.id("crafting_danmaku_help");
    public static final ResourceLocation UPGRADE_DANMAKU_HELP_ID = ReverieDreams.id("upgrade_danmaku_help");
    public static final ResourceLocation GET_FUMO_HELP_ID = ReverieDreams.id("get_fumo_help");
    public static final ResourceLocation ROLE_HELP_ID = ReverieDreams.id("role_help");
    public static final ResourceLocation TOUHOU_MYSTIA_HELP_ID = ReverieDreams.id("touhou_mystia_help");
    public static final ResourceLocation REGISTRIES_ID = ReverieDreams.id("registries");
    public static final ResourceLocation OTHER_MOD_LIST_ID = ReverieDreams.id("other_mod_list");
    public static final MultiActionDialog MAIN_HELP;
    public static final MultiActionDialog ALTAR_HELP;
    public static final MultiActionDialog CRAFTING_DANMAKU_HELP;
    public static final MultiActionDialog UPGRADE_DANMAKU_HELP;
    public static final MultiActionDialog GET_FUMO_HELP;
    public static final MultiActionDialog ROLE_HELP;
    public static final MultiActionDialog TOUHOU_MYSTIA_HELP;
    public static final MultiActionDialog OTHER_MOD_LIST;


    public static List<ItemStack> getUpgradeItemList() {
        List<ItemStack> list = new ArrayList<>();
        list.add(Items.IRON_SWORD.getDefaultInstance());
        list.add(Items.SLIME_BLOCK.getDefaultInstance());
        list.add(ModItems.SPEED_FEATHER.getDefaultInstance());
        list.add(ModItems.SPELL_CARD_TEMPLATE.getDefaultInstance());
        list.forEach(value -> UPGRADE_ITEM_BODIES.add(createItemStackBody(value)));
        return list;
    }

    static {
        MAIN_HELP = new MultiActionDialog(
                new CommonDialogData(
                        Component.translatable("dialog.title.main"),
                        Optional.of(Component.empty()),
                        true, false,
                        DialogAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessage(Component.empty(), 200),
                                new PlainMessage(Component.empty().append(Component.translatable("dialog.main.welcome")), 200),
                                new PlainMessage(Component.empty().append(Component.translatable("dialog.main.description.0", ConstantInfo.VERSION)), 200),
                                new PlainMessage(Component.empty(), 200)
                        )),
                        new ArrayList<>(List.of(

                        ))
                ),
                new ArrayList<>(),
                Optional.empty(),
                1);
        ALTAR_HELP = new MultiActionDialog(
                new CommonDialogData(
                        Component.translatable("dialog.title.altar"),
                        Optional.of(Component.empty()),
                        true, false,
                        DialogAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessage(Component.empty().append(Component.translatable(ModBlocks.GENSOKYO_ALTAR.getDescriptionId())).append(Component.literal("使用方式：空手 + Shift + 右键")), 200),
                                new PlainMessage(Component.empty().append(Component.translatable("dialog.altar.material")), 180),
                                new ItemBody(ModBlocks.SPIRITUAL.strippedLog().asItem().getDefaultInstance(), Optional.of(new PlainMessage(
                                        Component.empty()
                                                .append(Component.translatable(ModBlocks.SPIRITUAL.strippedLog().getDescriptionId()))
                                                .append(Component.literal(" - "))
                                                .append(Component.literal("§c\u999a").setStyle(Style.EMPTY.withFont(ReverieDreams.id("reverie_dreams"))))
                                                .append(Component.literal(" * 3"))
                                        , 200)

                                ), true, true, 16, 16),
                                new ItemBody(ModBlocks.GENSOKYO_ALTAR.asItem().getDefaultInstance(), Optional.of(new PlainMessage(
                                        Component.empty()
                                                .append(Component.translatable(ModBlocks.GENSOKYO_ALTAR.getDescriptionId()))
                                                .append(Component.literal(" - "))
                                                .append(Component.literal("§e\u999a").setStyle(Style.EMPTY.withFont(ReverieDreams.id("reverie_dreams"))))
                                                .append(Component.literal(""))
                                        , 200)
                                ), true, true, 16, 16),
                                new PlainMessage(Component.empty().append(Component.translatable("dialog.altar.ways")), 180)
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
                new CommonDialogData(
                        Component.translatable("dialog.title.danmaku"),
                        Optional.of(Component.empty()),
                        true, false,
                        DialogAction.CLOSE,
                        new ArrayList<>(List.of(
                                new ItemBody(
                                        new ItemStack(ModBlocks.DANMAKU_CRAFTING_TABLE, 1),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(ModBlocks.DANMAKU_CRAFTING_TABLE.getDescriptionId())).append(Component.literal("")), 200)),
                                        true, true, 16, 16
                                ),
                                new PlainMessage(Component.empty().append(Component.translatable("dialog.danmaku.description.0")), 200),
                                new PlainMessage(Component.empty().append(Component.translatable("dialog.danmaku.description.1")), 180),
                                new ItemBody(
                                        new ItemStack(Items.RED_DYE, 4),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(Items.RED_DYE.getDescriptionId())).append(Component.literal("")), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemBody(
                                        new ItemStack(ModItems.DANMAKU_CORE, 4),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(ModItems.DANMAKU_CORE.getDescriptionId())).append(Component.literal("")), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemBody(
                                        new ItemStack(ModItems.POWER, 35),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(ModItems.POWER.getDescriptionId())).append(Component.literal("")), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemBody(
                                        new ItemStack(ModItems.POINT, 35),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(ModItems.POINT.getDescriptionId())).append(Component.literal("")), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemBody(
                                        new ItemStack(ModItems.DANMAKU_SHAPE_CREATOR),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(ModItems.DANMAKU_SHAPE_CREATOR.getDescriptionId())).append(Component.literal("")), 200)),
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
                new CommonDialogData(
                        Component.translatable("dialog.title.upgrade_danmaku"),
                        Optional.empty(),
                        true, false,
                        DialogAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessage(Component.translatable("dialog.upgrade_danmaku.description.0"), 200),
                                new PlainMessage(Component.translatable("dialog.upgrade_danmaku.description.1"), 200)
                        )),
                        new ArrayList<>(List.of(

                        ))
                ),
                new ArrayList<>(List.of()),
                Optional.empty(),
                1
        );
        GET_FUMO_HELP = new MultiActionDialog(
                new CommonDialogData(
                        Component.translatable("dialog.title.fumo"),
                        Optional.of(Component.empty()),
                        true, false,
                        DialogAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessage(Component.translatable("dialog.fumo.description.0"), 200),
                                new PlainMessage(Component.empty().append(Component.translatable("dialog.fumo.description.1")), 200),
                                new PlainMessage(Component.empty().append(Component.translatable("dialog.fumo.description.2", Component.translatable(ModItems.FUMO_LICENSE.getDescriptionId()))), 200),
                                new ItemBody(ModItems.FUMO_LICENSE.getDefaultInstance(), Optional.empty(), true, true, 16, 16),
                                new ItemBody(Items.VILLAGER_SPAWN_EGG.getDefaultInstance(), Optional.empty(), true, true, 16, 16),
                                new ItemBody(ModEntities.SPAWN_EGG_BIND.getOrDefault(ModEntities.FUMO_SELLER_VILLAGER, ModItems.SPAWN_EGG).getDefaultInstance(), Optional.empty(), true, true, 16, 16)
                        )),
                        new ArrayList<>(List.of(

                        ))
                ),
                new ArrayList<>(List.of()),
                Optional.empty(),
                1
        );
        ROLE_HELP = new MultiActionDialog(
                new CommonDialogData(
                        Component.translatable("dialog.title.role"),
                        Optional.of(Component.empty()),
                        true, false,
                        DialogAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessage(Component.translatable("dialog.role.description.0"), 200),
                                new PlainMessage(Component.translatable("dialog.role.description.1"), 200),
                                new PlainMessage(Component.translatable("dialog.role.description.2", Component.translatable(ModItems.ROLE_ARCHIVE.getDescriptionId())), 200),
                                new PlainMessage(Component.translatable("dialog.role.description.3"), 200),
                                new PlainMessage(Component.translatable("dialog.role.description.4"), 200),
                                new PlainMessage(Component.translatable("dialog.role.description.5"), 200),
                                new ItemBody(
                                        new ItemStack(ModItems.ROLE_CARD, 1),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(ModItems.ROLE_CARD.getDescriptionId())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemBody(
                                        new ItemStack(ModItems.ROLE_ARCHIVE, 1),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(ModItems.ROLE_ARCHIVE.getDescriptionId())), 200)),
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
                new CommonDialogData(
                        Component.translatable("dialog.title.mystia.main"),
                        Optional.of(Component.empty()),
                        true, false,
                        DialogAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessage(Component.translatable("dialog.touhou_mystia.description.0"), 200),
                                new PlainMessage(Component.translatable("dialog.touhou_mystia.description.1"), 200),
                                new PlainMessage(Component.translatable("dialog.touhou_mystia.description.2"), 200),
                                new PlainMessage(Component.translatable("dialog.touhou_mystia.description.3"), 200),
                                new PlainMessage(Component.translatable("dialog.touhou_mystia.description.4"), 200),
                                new ItemBody(
                                        new ItemStack(MIBlocks.COOKING_POT, 1),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(MIBlocks.COOKING_POT.getDescriptionId())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemBody(
                                        new ItemStack(MIBlocks.CUTTING_BOARD, 1),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(MIBlocks.CUTTING_BOARD.getDescriptionId())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemBody(
                                        new ItemStack(MIBlocks.FRYING_PAN, 1),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(MIBlocks.FRYING_PAN.getDescriptionId())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemBody(
                                        new ItemStack(MIBlocks.GRILL, 1),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(MIBlocks.GRILL.getDescriptionId())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemBody(
                                        new ItemStack(MIBlocks.STEAMER, 1),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(MIBlocks.STEAMER.getDescriptionId())), 200)),
                                        true, true, 16, 16
                                ),
                                new PlainMessage(Component.translatable("dialog.touhou_mystia.description.5"), 200),
                                new PlainMessage(Component.translatable("dialog.touhou_mystia.description.6"), 200),
                                new PlainMessage(Component.translatable("dialog.touhou_mystia.description.7", Component.translatable(MIEntities.TAVERN_VILLAGER.getDescriptionId())), 200),
                                new PlainMessage(Component.translatable("dialog.touhou_mystia.description.8", Component.translatable(Blocks.BARREL.getDescriptionId()), Component.translatable(MIEntities.TAVERN_VILLAGER.getDescriptionId())), 200),
                                new ItemBody(
                                        new ItemStack(Blocks.BARREL, 1),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(Blocks.BARREL.getDescriptionId())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemBody(
                                        new ItemStack(Items.VILLAGER_SPAWN_EGG, 1),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(Items.VILLAGER_SPAWN_EGG.getDescriptionId())), 200)),
                                        true, true, 16, 16
                                ),
                                new ItemBody(
                                        new ItemStack(ModEntities.SPAWN_EGG_BIND.getOrDefault(MIEntities.TAVERN_VILLAGER, Items.VILLAGER_SPAWN_EGG), 1),
                                        Optional.of(new PlainMessage(Component.empty().append(Component.translatable(ModEntities.SPAWN_EGG_BIND.getOrDefault(MIEntities.TAVERN_VILLAGER, Items.VILLAGER_SPAWN_EGG).getDescriptionId())), 200)),
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
                new CommonDialogData(
                        Component.translatable("dialog.title.other_mod_list"),
                        Optional.of(Component.empty()),
                        true, false,
                        DialogAction.CLOSE,
                        new ArrayList<>(List.of(
                                new PlainMessage(Component.translatable("dialog.other_mod_list.description.0"), 200)
                        )),
                        new ArrayList<>(List.of(

                        ))
                ),
                new ArrayList<>(List.of(
                        new ActionButton(
                                new CommonButtonData(Component.empty().append(Component.literal("Polymer - Quality of Life")), 200),
                                Optional.of(new StaticAction(new ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/polymer-qol"))))
                        ),
                        new ActionButton(
                                new CommonButtonData(Component.empty().append(Component.literal("Polydex")), 200),
                                Optional.of(new StaticAction(new ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/polydex"))))
                        ),
                        new ActionButton(
                                new CommonButtonData(Component.empty().append(Component.literal("ExtendedItemView")), 200),
                                Optional.of(new StaticAction(new ClickEvent.OpenUrl(URI.create("https://github.com/SAGUMEDREAM/ExtendedItemView/releases"))))
                        ),
                        new ActionButton(
                                new CommonButtonData(Component.empty().append(Component.literal("Polydex2EIV")), 200),
                                Optional.of(new StaticAction(new ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/polydex2eiv"))))
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
        TOUHOU_MYSTIA_HELP.actions().add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.touhou_mystia.wiki")), 200),
                Optional.of(new StaticAction(new ClickEvent.OpenUrl(URI.create("https://izakaya.cc/"))))
        ));
        TOUHOU_MYSTIA_HELP.actions().add(getActionMain());
        OTHER_MOD_LIST.actions().add(getActionMain());
    }

    public static ItemBody createItemStackBody(ItemStack itemStack) {
        return new ItemBody(
                itemStack,
                Optional.of(new PlainMessage(Component.empty().append(itemStack.getHoverName()), 200)),
                true, true, 16, 16
        );
    }

    public static List<ActionButton> getMainList() {
        List<ActionButton> list = new ArrayList<>();
        list.add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.title.altar")), 200),
                Optional.of(new StaticAction(showPage("ALTAR")))
        ));
        list.add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.title.danmaku")), 200),
                Optional.of(new StaticAction(showPage("DANMAKU")))
        ));
        list.add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.title.upgrade_danmaku")), 200),
                Optional.of(new StaticAction(showPage("UPGRADE_DANMAKU")))
        ));
        list.add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.title.fumo")), 200),
                Optional.of(new StaticAction(showPage("FUMO")))
        ));
        list.add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.title.role")), 200),
                Optional.of(new StaticAction(showPage("ROLE")))
        ));
        list.add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.title.mystia.main")), 200),
                Optional.of(new StaticAction(showPage("TOUHOU_MYSTIA")))
        ));
        list.add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.title.registries")), 200),
                Optional.of(new StaticAction(showPage("REGISTRIES")))
        ));
        list.add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.title.other_mod_list")), 200),
                Optional.of(new StaticAction(showPage("OTHER_MOD_LIST")))
        ));
        if (isChina()) {
            list.add(getIssueButtonData());
        }
        list.add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.title.open_recipe_manager")), 200),
                Optional.of(new StaticAction(new ClickEvent.RunCommand("touhou recipe")))
        ));
        list.add(new ActionButton(
                new CommonButtonData(Component.translatable("dialog.text.exit"), 100),
                Optional.empty()
        ));
        return list;
    }

    public static ActionButton getIssueButtonData() {
        if (isChina()) {
            return new ActionButton(
                    new CommonButtonData(Component.empty().append(Component.translatable("dialog.title.cn.issue")), 200),
                    Optional.of(new StaticAction(new ClickEvent.OpenUrl(GROUP_LINK)))
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

    public static List<ActionButton> createAltarButtonList() {
        List<ActionButton> list = new ArrayList<>();
        Pattern p = Pattern.compile("§.⏹");
        for (String line : ALTAR_STR) {
            Matcher m = p.matcher(line);
            while (m.find()) {
                String token = m.group();
                MutableComponent result = Component.empty();
//                result.append(" ");
                result.append(Component.literal(token.replace("⏹", "\u999a"))
                        .setStyle(Style.EMPTY.withFont(ReverieDreams.id("reverie_dreams"))));
//                result.append(" ");
                list.add(new ActionButton(
                        new CommonButtonData(result, 30),
                        Optional.of(new StaticAction(showPage("ALTAR")))
                ));
            }
        }
        return list;
    }

    public static ClickEvent.RunCommand showPage(String name) {
        return new ClickEvent.RunCommand("touhou dialog \"%s\"".formatted(name));
    }

    public static ActionButton getActionMain() {
        return new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.text.back")), 200),
                Optional.of(new StaticAction(showPage("MAIN")))
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
