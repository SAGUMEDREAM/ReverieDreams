package cc.thonly.reverie_dreams.smoke;

import cc.thonly.reverie_dreams.paper.danmaku.DanmakuModule;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuRules;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuService;
import cc.thonly.reverie_dreams.paper.specialweapons.LunarWeaponType;
import cc.thonly.reverie_dreams.paper.specialweapons.MagazineService;
import cc.thonly.reverie_dreams.paper.specialweapons.SpecialWeaponsModule;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiConsumer;

final class WeaponsSmokeChecks {
    private static UUID caster;
    private WeaponsSmokeChecks() { }
    static void verify(BiConsumer<Boolean,String> check, World world, boolean restart) {
        var module=SpecialWeaponsModule.service();var magazines=module.magazines();var guns=module.guns();
        var inventory=Bukkit.createInventory(null,9);var weapon=module.create("weapon_of_the_moon");
        magazines.setRounds(weapon,7);var magazine=module.create("magazine");
        check.accept(magazines.rounds(magazine)==30,"legacy magazine is created at full type-one capacity");
        magazines.setRounds(magazine,11);
        inventory.setItem(0,weapon);inventory.setItem(1,magazine);
        var load=magazines.reload(inventory,0,1,false);
        check.accept(load.status()==MagazineService.Status.RELOADED && load.previousRounds()==7 && load.loadedRounds()==11
                        && load.returnedRounds()==7 && load.consumed()==1
                        && magazines.rounds(inventory.getItem(0))==11
                        && inventory.getItem(1)!=null && magazines.rounds(inventory.getItem(1))==7,
                "real inventory magazine reload loads new rounds and returns the old rounds as one fresh magazine");
        var roundTrip=ItemStack.deserializeBytes(inventory.getItem(0).serializeAsBytes());
        check.accept(magazines.rounds(roundTrip)==11,"weapon rounds survive native item serialization");
        inventory.setItem(1,module.create("magazine"));magazines.setRounds(roundTrip,30);inventory.setItem(0,roundTrip);
        check.accept(magazines.reload(inventory,0,1,false).status()==MagazineService.Status.FULL && inventory.getItem(1)!=null,
                "full weapon cannot consume a spare magazine");
        var pistol=module.create("lunar_type_2_pistol");magazines.setRounds(pistol,0);inventory.setItem(0,pistol);
        inventory.setItem(1,module.create("lunar_type_1_magazine"));
        check.accept(magazines.reload(inventory,0,1,false).status()==MagazineService.Status.NO_MAGAZINE,
                "pistol refuses a rifle magazine");
        inventory.setItem(1,module.create("lunar_type_2_magazine"));
        var creative=magazines.reload(inventory,0,1,true);
        check.accept(creative.status()==MagazineService.Status.RELOADED && creative.consumed()==0 && creative.returnedRounds()==0
                        && magazines.rounds(inventory.getItem(0))==12 && inventory.getItem(1)!=null
                        && magazines.rounds(inventory.getItem(1))==12,
                "creative reload keeps the source magazine and returns no old-round magazine");
        for(var type:LunarWeaponType.values()) {
            var preparedType=guns.prepare(module.create(type.itemPath()));
            var typeGroup=preparedType.getData(DataComponentTypes.USE_COOLDOWN);
            check.accept(magazines.rounds(module.create(type.itemPath()))==0
                            && magazines.rounds(module.create(type.magazinePath()))==type.capacity()
                            && typeGroup!=null&&typeGroup.cooldownGroup()!=null
                            && Math.abs(typeGroup.seconds()-type.shotInterval()/20f)<.0001f,
                    "type-aware fresh gun/magazine rounds and source shot interval: "+type.itemPath());
        }
        var rifle=module.create("lunar_type_1_rifle");
        check.accept(loreHas(rifle,"item.tooltip.magazine.loaded","0")
                        && loreHas(rifle,"item.tooltip.lunar.damage","4.00")
                        && loreHas(rifle,"item.tooltip.lunar.experience","0.0%"),
                "source three-line gun tooltip renders rounds, level-zero damage and experience bonus");
        var prepared=guns.prepare(rifle);
        var group=prepared.getData(DataComponentTypes.USE_COOLDOWN);
        check.accept(group!=null&&group.cooldownGroup()!=null&&group.cooldownGroup().value().startsWith("lunar_weapon/")
                        && Math.abs(group.seconds()-.1f)<.0001f,
                "first preparation replaces the static CE group with a per-stack source cooldown group");
        var damaged=module.create("sunflower");damaged.setData(DataComponentTypes.DAMAGE,64);
        var repair=module.repairs().preview(damaged,CraftEngineItems.byId("reverie_dreams:power_block").buildBukkitItem().asQuantity(3));
        check.accept(repair!=null && repair.materials()==1 && repair.restored()==64,
                "sunflower exact-quarter repair requires one power block instead of consuming an extra material");
        var actor=world.spawn(new Location(world,8,310,8),Sheep.class,entity->{entity.setAI(false);entity.setGravity(false);});
        caster=actor.getUniqueId();var bullets=DanmakuModule.service();
        UUID rifleShot=guns.fireShot(LunarWeaponType.RIFLE,actor,null,false,new Random(21))?guns.recentShots().getLast().bullet():null;
        var shot=bullets.snapshots().stream().filter(value->value.id().equals(rifleShot)).findFirst().orElseThrow();
        var display=(ItemDisplay)Bukkit.getEntity(rifleShot);
        check.accept(CraftEngineItems.getCustomItemId(display.getItemStack()).asString().equals("reverie_dreams:lunar_bullet_standard")
                        && shot.properties().damage()==4.0f && shot.properties().speed()==3.2f && shot.properties().scale()==1.0f
                        && shot.properties().collisionScale()==.7f && shot.duration()==30
                        && Math.abs(shot.velocity().length()-3.2)<.35 && shot.properties().infinite()==false,
                "rifle launches the registered bullet with source damage, hitbox, lifetime and three-point-two speed");
        check.accept(shot.velocity().lengthSquared()<20,"rifle spread remains a small deviation from the aim direction");
        bullets.cancel(rifleShot);
        long before=guns.recentShots().size();
        check.accept(guns.fireShot(LunarWeaponType.SHOTGUN,actor,null,false,new Random(22))
                        && guns.recentShots().size()-before==8,
                "shotgun fires eight independent pellet entities");
        for(var recent:guns.recentShots().subList((int)before,guns.recentShots().size()))
            check.accept(bullets.snapshots().stream().anyMatch(value->value.id().equals(recent.bullet())&&value.duration()==7)
                            && CraftEngineItems.getCustomItemId(((ItemDisplay)Objects.requireNonNull(Bukkit.getEntity(recent.bullet()))).getItemStack()).asString().equals("reverie_dreams:lunar_bullet_pellet"),
                    "shotgun pellets use the pellet visual and seven-tick source lifetime");
        for(var recent:guns.recentShots().subList((int)before,guns.recentShots().size()))bullets.cancel(recent.bullet());
        for(var type:List.of(LunarWeaponType.PISTOL,LunarWeaponType.SMG,LunarWeaponType.SNIPER)) {
            long start=guns.recentShots().size();
            check.accept(guns.fireShot(type,actor,null,false,new Random(30+type.number())),
                    "type fires a real projectile: "+type.itemPath());
            var fired=guns.recentShots().subList((int)start,guns.recentShots().size());
            check.accept(fired.size()==1,"type emits its source pellet count: "+type.itemPath());
            var recent=fired.getLast();
            var snapshot=bullets.snapshots().stream().filter(value->value.id().equals(recent.bullet())).findFirst().orElseThrow();
            var visual=(ItemDisplay)Objects.requireNonNull(Bukkit.getEntity(recent.bullet()));
            check.accept(snapshot.properties().damage()==type.damage() && snapshot.properties().speed()==type.speed()
                            && snapshot.properties().collisionScale()==type.bulletHitboxScale() && snapshot.duration()==type.lifetime()
                            && CraftEngineItems.getCustomItemId(visual.getItemStack()).asString().equals(type.bulletId()),
                    "type uses source damage, speed, hitbox, lifetime and bullet visual: "+type.itemPath());
            for(var extra:fired)bullets.cancel(extra.bullet());
        }
        guns.trumpet(actor,new Random(23));guns.sunflower(actor,null,new Random(24));
        if(restart) {
            var saved=YamlConfiguration.loadConfiguration(Path.of("probe-fixture.yml").toFile());
            UUID id=UUID.fromString(saved.getString("explicit_resume"));
            var restored=bullets.snapshots().stream().filter(value->value.id().equals(id)).findFirst().orElseThrow();
            var entity=(ItemDisplay)Bukkit.getEntity(id);
            var data=com.google.gson.JsonParser.parseString(entity.getPersistentDataContainer().get(
                    new NamespacedKey("reverie_dreams","danmaku_projectile"),PersistentDataType.STRING)).getAsJsonObject();
            check.accept(entity.getItemStack().getType()==Material.COPPER_NUGGET && restored.properties().speed()==6
                            && restored.age()>0 && restored.age()<200
                            && data.getAsJsonObject("launch_spec").get("initial_speed").getAsFloat()==.01f,
                    "explicit vanilla visual and independent launch properties survive unload and server restart");
            bullets.cancel(id);
            UUID lunarId=UUID.fromString(saved.getString("lunar_resume"));
            var lunar=bullets.snapshots().stream().filter(value->value.id().equals(lunarId)).findFirst().orElseThrow();
            var lunarEntity=(ItemDisplay)Bukkit.getEntity(lunarId);
            var lunarData=com.google.gson.JsonParser.parseString(lunarEntity.getPersistentDataContainer().get(
                    new NamespacedKey("reverie_dreams","danmaku_projectile"),PersistentDataType.STRING)).getAsJsonObject();
            check.accept(lunar.duration()==30 && lunar.age()<30
                            && CraftEngineItems.getCustomItemId(lunarEntity.getItemStack()).asString().equals("reverie_dreams:lunar_bullet_standard")
                            && lunarData.getAsJsonObject("launch_spec").get("lifetime").getAsInt()==30
                            && lunarData.getAsJsonObject("launch_spec").get("velocity_facing").getAsBoolean(),
                    "lunar projectile lifetime, velocity facing and registered visual survive restart");
            bullets.cancel(lunarId);
        }
    }
    private static boolean loreHas(ItemStack item,String key,String firstArgument){
        if(item.lore()==null)return false;
        for(Component line:item.lore())if(line instanceof TranslatableComponent translatable&&translatable.key().equals(key)
                &&!translatable.args().isEmpty()&&translatable.args().getFirst() instanceof TextComponent text
                &&text.content().equals(firstArgument))return true;
        return false;
    }
    static void complete(BiConsumer<Boolean,String> check) {
        var guns=SpecialWeaponsModule.service().guns();
        var shots=guns.recentShots().stream().filter(shot->shot.caster().equals(caster)).toList();
        var trumpet=shots.stream().filter(shot->shot.weapon().equals("trumpet_gun")).toList();
        var flower=shots.stream().filter(shot->shot.weapon().equals("sunflower")).toList();
        check.accept(trumpet.size()==3 && flower.size()==15 && guns.bursts().stream().noneMatch(burst->burst.caster().equals(caster)),
                "trumpet completes three delayed shots and sunflower completes three five-shot fans");
        check.accept(trumpet.stream().allMatch(shot->Math.abs(shot.spec().initialSpeed()-1.1f)<.0001
                        && shot.spec().properties().speed()==2.3f && shot.spec().forwardOffset()==1.5f)
                        && flower.stream().allMatch(shot->shot.spec().initialSpeed()==1 && shot.spec().properties().speed()==1.8f
                        && shot.spec().properties().damage()==3.5f && shot.spec().properties().collisionScale()==2.5f),
                "source post-spawn speed metadata does not replace actual trumpet/sunflower initial velocity");
        var entity=Bukkit.getEntity(caster);if(entity!=null)entity.remove();
    }
    static void prepareResume(World world,YamlConfiguration saved) {
        var properties=new DanmakuRules.Properties("reverie_dreams:single",1,0,"reverie_dreams:danmaku_generic",.4f,6,.1f,true,true,1,.4f);
        var spec=new DanmakuService.LaunchSpec(properties,.01f,0,0,false);
        var id=DanmakuModule.service().launchOne(null,new ItemStack(Material.COPPER_NUGGET),new Location(world,514.5,220,12.5),0,0,spec);
        saved.set("explicit_resume",Objects.requireNonNull(id).toString());
        var lunarProperties=new DanmakuRules.Properties("reverie_dreams:single",1,4f,"reverie_dreams:danmaku_generic",1f,3.2f,0,false,false,1,.7f);
        var lunarSpec=new DanmakuService.LaunchSpec(lunarProperties,3.2f,.6f,0,true,30,true);
        var visual=CraftEngineItems.byId("reverie_dreams:lunar_bullet_standard").buildBukkitItem();
        var lunarId=DanmakuModule.service().launchOne(null,visual,new Location(world,514.5,220,14.5),0,0,lunarSpec);
        saved.set("lunar_resume",Objects.requireNonNull(lunarId).toString());
    }
}
