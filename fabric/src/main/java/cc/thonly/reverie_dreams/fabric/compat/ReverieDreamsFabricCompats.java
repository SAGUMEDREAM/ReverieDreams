package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.compat.ReverieDreamsCompats;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReverieDreamsFabricCompats extends ReverieDreamsCompats {
    public static void initialize() {
        ReverieDreamsCompats.initialize();
        load("polydex", "cc.thonly.reverie_dreams.fabric.compat.PolydexCompatImpl");
        load("polydex2eiv", "cc.thonly.reverie_dreams.fabric.compat.Polydex2EIVCompatImpl");
//        load("borukva-food", "cc.thonly.reverie_dreams.fabric.compat.BorukvaFoodCompatImpl");
//        load("borukva-food-exotic", "cc.thonly.reverie_dreams.fabric.compat.BorukvaFoodExoticCompatImpl");
//        load("borukva-fish", "cc.thonly.reverie_dreams.fabric.compat.BorukvaFishCompatImpl");
        load("farmersdelight", "cc.thonly.reverie_dreams.fabric.compat.FarmersdelightCompatImpl");
        load("moredelight", "cc.thonly.reverie_dreams.fabric.compat.MoreDelightCompatImpl");
//        load("oceansdelight-port", "cc.thonly.reverie_dreams.fabric.compat.OceansdelightCompatImpl");
//        load("spanishdelight", "cc.thonly.reverie_dreams.fabric.compat.SpanishDelightCompatImpl");
//        load("go-fish", "cc.thonly.reverie_dreams.fabric.compat.GoFishingCompatImpl");
//        load("fishing101", "cc.thonly.reverie_dreams.fabric.compat.Fishing101CompatImpl");
        load("polyfactory", "cc.thonly.reverie_dreams.fabric.compat.PolyFactoryCompatImpl");
        load("create", "cc.thonly.reverie_dreams.fabric.compat.CreateFlyCompatImpl");
        load("appleskin", "cc.thonly.reverie_dreams.fabric.compat.AppleskinCompatImpl");
    }



}
