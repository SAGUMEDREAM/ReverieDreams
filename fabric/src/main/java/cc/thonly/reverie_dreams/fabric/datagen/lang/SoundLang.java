package cc.thonly.reverie_dreams.fabric.datagen.lang;

import cc.thonly.reverie_dreams.registry.content.sound.RDJukeboxSongs;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;

public interface SoundLang {

    static void add(TranslationBuilder builder) {
        for (var sound : RDSoundEvents.FUMO_SOUNDS) {
            addSubtitle(builder, sound.value(), "fumo");
        }
        addSubtitle(builder, RDSoundEvents.BIU.value(), "满身疮痍");
        addSubtitle(builder, RDSoundEvents.POINT.value(), "收点");
        addSubtitle(builder, RDSoundEvents.SPELL_CARD.value(), "符卡释放");
        addSubtitle(builder, RDSoundEvents.UP.value(), "升级");
        addSubtitle(builder, RDSoundEvents.FIRE.value(), "弹幕发射");
        addSubtitle(builder, RDSoundEvents.BAGUA.value(), "魔炮");
        addSubtitle(builder, RDSoundEvents.PHOTO.value(), "拍照");
        addSubtitle(builder, RDSoundEvents.TICK_WAVE.value(), "波");
        addSubtitle(builder, RDSoundEvents.GRAZE.value(), "擦弹");
        addDisc(builder, RDJukeboxSongs.HR01_01.getJukeboxSongKey(), "蓬莱人形　～ Dolls in Pseudo Paradise. - 蓬莱伝説");
        addDisc(builder, RDJukeboxSongs.HR02_08.getJukeboxSongKey(), "莲台野夜行 - 过去的花 ～ Fairy of Flower");
        addDisc(builder, RDJukeboxSongs.HR03_01.getJukeboxSongKey(), "ZUN - 童祭　～ Innocent Treasures");
        addDisc(builder, RDJukeboxSongs.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS.getJukeboxSongKey(), "Melodic-Taste-Nightmare-before-Crossroads");
        addDisc(builder, RDJukeboxSongs.YV_FLOWER_CLOCK_AND_DREAMS.getJukeboxSongKey(), "Yonder-Voice - 花時計と夢");
        addDisc(builder, RDJukeboxSongs.GLOWING_NEEDLES_LITTLE_PEOPLE.getJukeboxSongKey(), "Inchlings of the Shining Needle ~ Little Princess : 「Miracle Remix」");
        addDisc(builder, RDJukeboxSongs.COOKIE.getJukeboxSongKey(), "温馨的神社（迫真）");
        addDisc(builder, RDJukeboxSongs.BAD_APPLE.getJukeboxSongKey(), "Bad Apple");
        addDisc(builder, RDJukeboxSongs.A_MELODY_BLOOMING_ON_THE_EARTH.getJukeboxSongKey(), "幽閉サテライト - 大地に咲く旋律 / 星空下的旋律 - AcAciaZizzo");
        addDisc(builder, RDJukeboxSongs.SPRAWLING_EARTH.getJukeboxSongKey(), "ごんばこんなか - 欢迎来到月面购物中心　～ Sprawling Earth");
    }

    private static void addSubtitle(TranslationBuilder builder, SoundEvent sound, String text) {
        // sounds.json consumes the project's existing sound.* keys.
        builder.add(sound.location().toLanguageKey("sound"), text);
    }

    private static void addDisc(TranslationBuilder builder, ResourceKey<JukeboxSong> key, String title) {
        builder.add(key.identifier().toLanguageKey("sound"), title);
        builder.add(key.identifier().toLanguageKey("jukebox_song").replace('/', '.'), title);
    }
}
