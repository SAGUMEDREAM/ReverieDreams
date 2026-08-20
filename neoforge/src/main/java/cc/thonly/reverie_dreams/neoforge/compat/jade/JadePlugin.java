package cc.thonly.reverie_dreams.neoforge.compat.jade;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.GensokyoAltarBlock;
import cc.thonly.reverie_dreams.block.MusicBlock;
import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import cc.thonly.reverie_dreams.block.entity.KitchenwareBlockEntity;
import cc.thonly.reverie_dreams.block.entity.MusicBlockEntity;
import cc.thonly.reverie_dreams.block.kitchen.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import net.minecraft.resources.Identifier;
import snownee.jade.api.*;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    public static final Identifier KITCHENWARE_DATA_PROVIDER = ReverieDreams.id("kitchenware_data_provider");
    public static final Identifier MUSIC_BLOCK_DATA_PROVIDER = ReverieDreams.id("music_block_data_provider");
    public static final Identifier GENSOKYO_ALTAR_DATA_PROVIDER = ReverieDreams.id("gensokyo_altar_data_provider");
    public static final Identifier ROLE_DESCRIPTION_PROVIDER = ReverieDreams.id("role_description");
    public static final Identifier NPC_DESCRIPTION_PROVIDER = ReverieDreams.id("role_description");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(KitchenwareServerDataProvider.INSTANCE, KitchenwareBlockEntity.class);
        registration.registerBlockDataProvider(MusicBlockServerDataProvider.INSTANCE, MusicBlockEntity.class);
        registration.registerBlockDataProvider(GensokyoAltarServerDataProvider.INSTANCE, GensokyoAltarBlockEntity.class);
        registration.registerEntityDataProvider(NPCServerDataProvider.INSTANCE, NPCSimpleEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(KitchenwareComponentProvider.INSTANCE, AbstractKitchenwareBlock.class);
        registration.registerBlockComponent(MusicBlockComponentProvider.INSTANCE, MusicBlock.class);
        registration.registerBlockComponent(GensokyoAltarComponentProvider.INSTANCE, GensokyoAltarBlock.class);
        registration.registerEntityComponent(NPCEntityProvider.INSTANCE, NPCSimpleEntity.class);
    }
}
