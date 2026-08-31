package cc.thonly.reverie_dreams.fabric.compat.jade;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.cooking.*;
import cc.thonly.reverie_dreams.block.entity.*;
import cc.thonly.reverie_dreams.block.kitchen.BrewingBarrelBlock;
import cc.thonly.reverie_dreams.block.kitchen.CupboardBlock;
import cc.thonly.reverie_dreams.block.props.GensokyoAltarBlock;
import cc.thonly.reverie_dreams.block.props.MusicBlock;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import net.minecraft.resources.Identifier;
import snownee.jade.api.*;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    public static final Identifier KITCHENWARE_DATA_PROVIDER = ReverieDreams.id("kitchenware_data_provider");
    public static final Identifier MUSIC_BLOCK_DATA_PROVIDER = ReverieDreams.id("music_block_data_provider");
    public static final Identifier GENSOKYO_ALTAR_DATA_PROVIDER = ReverieDreams.id("gensokyo_altar_data_provider");
    public static final Identifier NPC_DESCRIPTION_PROVIDER = ReverieDreams.id("role_description");
    public static final Identifier BREWING_BARREL_PROVIDER = ReverieDreams.id("brewing_barrel");
    public static final Identifier CUPBOARD_PROVIDER = ReverieDreams.id("cupboard");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(KitchenwareServerDataProvider.INSTANCE, KitchenwareBlockEntity.class);
        registration.registerBlockDataProvider(MusicBlockServerDataProvider.INSTANCE, MusicBlockEntity.class);
        registration.registerBlockDataProvider(GensokyoAltarServerDataProvider.INSTANCE, GensokyoAltarBlockEntity.class);
        registration.registerBlockDataProvider(CupboardServerDataProvider.INSTANCE, CupboardBlockEntity.class);
        registration.registerBlockDataProvider(BrewingBarrelServerDataProvider.INSTANCE, BrewingBarrelBlockEntity.class);
        registration.registerEntityDataProvider(NPCServerDataProvider.INSTANCE, NPCSimpleEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(KitchenwareComponentProvider.INSTANCE, CookingPot.class);
        registration.registerBlockComponent(KitchenwareComponentProvider.INSTANCE, CuttingBoard.class);
        registration.registerBlockComponent(KitchenwareComponentProvider.INSTANCE, FryingPan.class);
        registration.registerBlockComponent(KitchenwareComponentProvider.INSTANCE, Grill.class);
        registration.registerBlockComponent(KitchenwareComponentProvider.INSTANCE, Steamer.class);
        registration.registerBlockComponent(MusicBlockComponentProvider.INSTANCE, MusicBlock.class);
        registration.registerBlockComponent(GensokyoAltarComponentProvider.INSTANCE, GensokyoAltarBlock.class);
        registration.registerBlockComponent(BrewingBarrelComponentProvider.INSTANCE, BrewingBarrelBlock.class);
        registration.registerBlockComponent(CupboardComponentProvider.INSTANCE, CupboardBlock.class);
        registration.registerEntityComponent(NPCEntityProvider.INSTANCE, NPCSimpleEntity.class);
    }
}
