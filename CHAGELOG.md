# 🌙 Gensokyo: Reverie of Lost Dreams - Changelog

# 0.5.1+26.1.2
* Fixed villager not open gui for two time.
* Fixed Bad Apple Item is missing sound
* Fixed the SpawnPlacement failure
* Fixed the dialog video shows offsets
* Added Fast Recipe Book to make fast to craft kitchen recipe
* Added showcase in kitchen block
* Added Rail Controller Block to control minecart
* Added Signal Rails to test minecart's name to emit redstone
* Added Signal Delayer to delay redstone signal in tick
* Added Remote Server Block to send specify signal
* Added Remote Client Block to receive specify signal
* Now trumpet sound is trumpet
* Updated Nota

# 0.5.0+26.1.2
* Update for 26.1.2
* Added sound to TenguCamera
* This merges bug fixes from versions 0.4.19+1.21.11
* Removed all collaborations that were not updated to module 26.1

# 0.4.18+1.21.11
* Rewrite chest loot
* Rewrite Food Property and Drink Property Data-driven
* Moving Mushroom Monster drops to loot
* Added UFO Loot drops
* Added Hairball Loot drops
* Added Item Tags parse in ItemStackWrapper(be like Recipe)
* Added the ability to read armor, effects, and attack targets for SatoriEye items.
* Increase Coin drop number
* Fixed the missing recipes for fried tofu and cola potato pancakes.
* Fixed word "油豆腐" was corrected to "炸豆腐"
* Fixed mistake pixel in Cooking Pot
* Fixed mistake pixel in Cooking Tofu
* Fixed mistake material item in crafting spears
* Fixed ItemStackWrapper can't match greater than other item
* Fixed a spelling error in the id field of KitchenBlockType.
* Fixed the misalignment text in SatoriEye Item.
* Added support for NeoForge
* Added a notice in update camera item fov
* Added native compatibility for JEI and REI to this mod.
* Separated the Polymer component from the Fabric-only side; the server-side is now disabled by default unless a patch
  is installed.
* Removed the Yuka Umbrella code from version 1.21.10, which caused weapon damage to duplicate.
* The structure generation density has been modified, making structures easier to find.
* Compat for Appleskin

# 0.4.17+1.21.11

* Fixed Bagua Furnace lost attacking state when chunk unloading
* Fixed Danmaku Entity offset low
* Fixed missing loot items in sakurazuka structure
* Using Booklet API rewrote the GuideBook.
* Added lunar soldiers (test)
* Added lunar soldier weapons (test)
* Added some story in GuideBook
* The usage of Gungnir has been modified. Now, holding down the right mouse button will initiate a dash by default, and
  releasing the button during the dash + jump will fire the gun.
* Updated GuideBook textures
* The next update will reset the reward chests and add buildings on The Moon.

# 0.4.16+1.21.11

* Fixed ItemStack decode error
* Fixed a crash that occurred when operating with PolyFactory under certain conditions.

# 0.4.15+1.21.11

* Added YinYang-Orb recipe and feature
* Added some building on moon dim
* Added Spear feature for Gungnir
* Added loot chest dev command
* Yousei will not attack players in Spectator Mode or Creative Mode.
* Fixed can't open villager gui
* Fixed resulting cracking in use spawn egg on spawner
* Renamed loot chest key

# 0.4.14+1.21.11

* Fixed AI not attacking target

# 0.4.13+1.21.11

* Fixed the moon dim light error
* Fixed lack danmaku repair recipe

# 0.4.12+1.21.11

* Update for 1.21.11
* Fixed the missing Scarecrow crafting recipe
* Added a working mode selection interface
* Added compatibility and linkage with Create Fly
* Added compatibility and linkage with PolyFactory
* Added Spear tools
* Added AI to the Role to avoid Creeper explosions
* Now Yuka Flower Umbrella bas on Spear

# 0.4.11+1.21.10

* Fixed registry not sync
* Fixed disc translation missing
* Fixed BaseNPCLikeEntity NPE
* Added Sub Command For Adding item's `Food Property` and `Drink Property`
* Rewrote the danmaku function engine.
* Replace Hisou Sword recipe item with Copper Sword
* Added Damage for Hisou Sword
* Added Danmaku Speed
* Added a secure delivery policy for Crossing Chisel

# 0.4.10+1.21.10

* Upgrade to version 1.21.9/10
* Fixed an issue that caused entity animation to freeze.

# 0.4.9+1.21.7

* Optimize peach item texture
* Removed attack range bonuses for the umbrella and scythe (unstable)
* Updated with new en_us content for the previous update
* Updated with new zh_tw content for the previous update
* Updated Mod Icon
* Added Japanese ja_jp translation (experimental)
* Added Chinese (Hong Kong) zh_hk translation (experimental)
* Added English en_gb translation (experimental)
* Added a series of achievements
* Added restrictions on the generation of some creatures
* Added Texture for Scarecrow Entity
* Added item Satori Eye
* Remove DynamicRegistryModifier Lib dependencies
* Muted polymerify warn logger in client
* Setting up a collision box for Gensokyo Altar Block
* Fixed Missing CD Music
* Remove some useless code
* Change the serialization of kitchenware output items and villager conversion data
* Development of version 1.21.8 has concluded. This mod will now be migrated to version 1.21.10, which will serve as the
  final stable version for maintenance.

# 0.4.8+1.21.7

* Added the Note Danmaku (Block face reflecting when hit block )
* Added `Himekaidou Hatate's Phone` item
* Added `Yuka's Flower Umbrella` recipe
* Added empty photo item
* Added UFO Entity
* Added Scarecrow Entity
* Added speed mob effect in hold speed feather
* Added Structure Nether Hot Spring
* Added block Blessed Spiritual Log
* Added an option to enable Yousei spawning.
* Added an option to enable Ghost spawning.
* Now the top layer of the altar was changed to a Blessed Spiritual Log
* Now carrying silver items will prevent ghosts from spawning.
* Fixed not drop sapling when breaking leaves
* Fixed missing enchantable tag in enchantment
* Fixed crashing in opening settings
* Fixed the bad apple item is missing sound play
* Fixed Marisa hat can't place in HEAD slot
* Fixed Sound offset in playing Fumo
* Fixed Placing fake overlay in client
* Fixed the handheld bone meal will continue to consume the ripening fruit leaves.
* Fixed some blocks is missing particle
* Fixed Gensokyo Altar Block Entity Item not displaying
* Fixed Youmu model
* Optimized dropping item location in picking fruit leaves
* Repainted type of 3 coins textures
* Limited Upgraded Health for 46
* Increase change for shoot Wind Ball in using Wind Blessing Cane
* Allowed shooting a lot of note danmakus in place note danmaku item in OffHand

# 0.4.7+1.21.7

* Fixed async RandomSource access

# 0.4.6+1.21.7

* Added Weapon Item `Yuka's Flower Umbrella`
* Added Config to switch `Danmaku Entity` glowing
* Added Enchantment Type `Extermination`
* Added Enchantment Type `Moon Damage`
* Added Enchantment Type `Danmaku Protection`
* Added Role Type from TH19 to TH20
* Extended `Death Scythe`'s attack range
* Increased `Magic Broom`'s movement speed
* Increased `Magic Broom`'s max health
* Fixed `Knife Entity`'s model
* Fixed the firing position on the vehicle shifted.
* Updated translational files zh_tw and en_us

# 0.4.5+1.21.7

* Add tags to the vanilla food
* Fixed skin cache not hitting correctly
* Fixed the display of the negative keyword tooltip
* Fixed the issue where DanmakuEntity's speed changed with sprinting
* Fixed the danmaku bullet can't damage the EnderDragon
* The single component for bullet screen attributes has been removed, and a comprehensive component for bullet screen
  attributes has been added.
* Porting to mojmap
* Improved crop models and mechanisms
* Fixed wall tag is empty
* Added custom spellcard item component

## 0.4.4+1.21.7

* Fixed the issue where some items would not be damaged when their durability reached 0
* Fixed Time Stop Clock multiplayer threading error
* Added a cooldown to Time Stop Clock
* Added linkage with `borukva-fish`
* Added overworld structures `abandoned_altar`, `abandoned_torii`, `mini_bar`, `bamboo_forest_bbq_stall`,
  `bamboo_forest_hut`, `sakurazuka`, `outer_shrine`
* Reduced Wild Pig spawns
* Fixed the issue of negative health deduction when the player dies
* Fixed an error where the modified value would become negative when using a Remaining Health, causing negative HP loss.
* Fixed an error in the Bomb and Remaining Health recipes.
* Merge Role Entity Types
* Modified the texture of `danmaku_core`
* Increases the satiety of food items
* Provide custom character skin interface (experimental)
* Modified the data storage method of Dream Pillow

## 0.4.3+1.21.7

* Fixed an issue where the Barrage Workbench couldn't place Barrage Cores;
* Fixed an issue where the Silver Chest couldn't place items;

## 0.4.2+1.21.7

* Defined the simplified Chinese name of the module for the first time(首次定义了模组的简体中文的名字)
* Added health recovery effect to dream armor

## 0.4.1+1.21.7

* Fixed missing okio dependency crash
* Fixed food tray not loading at a distance
* Fixed Bagua Furnace abnormal consumption
* Allows udumbara to be driven by bone meal

## 0.4.0+1.21.7

* Fixed PlayerPolymerEntity causing memory leak
* Fixed outdated Dialog

## 0.3.9+1.21.7

* Updated zh_tw and en_us translation files

## 0.3.8+1.21.7

* Enhanced BaguaFurnace functionality
* Enhanced model shape Silver Chest
* Enhanced texture for danmaku shape recipe gui slot
* Enhanced texture for silver coin
* Changed Tavern Villager's currency to copper coins
* Added the `Auto Pick Item` for Role
* Added Nuke Kitchenware
* Added Wooden Box Block
* Added Villager Professions: `hawkers` and `priest`
* Fixed Array Index out of bounds
* Fixed Big Cirno Fumo texture offset error
* Fixed Dream Armor material error
* Fixed even with Ghost spawning disabled, the debuff is still applied.

## 0.3.7+1.21.7

* Added Danmaku Core to replace Firework star in recipe
* Added Silver Chest Block
* Added Cash Box Block
* Added Anti Collision Barrel Block
* Added the ability to allow Roles to open silver boxes and take out items
* Added the ability to allow Roles to open door
* Added `Chest Classification` Skill for Working Mode
* Added `Smelt` Skill for Working Mode
* Added `Sheep Shears` Skill for Working Mode
* Added `Playing Music` Skill for Working Mode
* Added new content to `zh_tw` translation file
* Added Textures for Owner Debug Stick and Battle Debug Stick
* Added Random speed in picking fruits
* Added random message in empty stack interact role
* Add 3 currency placeholders
* Now Gungnir can be thrown
* Fixed an issue where the Minecraft server network could not be detected normally.
* Fixed Music Block not playing music after re-entering the chunk or world
* Fixed Missing translationKey in NPC GUI work mode button
* Fixed incorrect orientation of kitchen utensils and hoppers
* Fixed Food Dash drop model too large
* Fixed Youmu using the wrong model
* Fixed Mistake set value in `ON_UPGRADED_HEALTH`
* Modify Bagua Furnace laser to cylindrical model
* Optimized the logic of feeding food and potions to Role
* The Role backpack page now requires empty hand + right click to open

## 0.3.6+1.21.7

* Fixed Goblin display name
* Fixed network errors when opening the GUI of other Polymer mods
* Added feedback for Danmaku Shape Edit apply
* Added BREED Skill for Touhou Role Working Mode

## 0.3.5+1.21.7

* Fixed Moon biome bug

## 0.3.4+1.21.7

* Fixed use `Cursed Decoy Doll` item offset
* Fixed `Kosuzu Fumo` rendering too large in inventory
* Fixed `Big Cirno` Size is correct
* Fixed Earphone pairing working when player is in spectator mode
* Fixed `Moonstone` and `Dreamstone` missing their slab and stair recipes
* Fixed Danmaku Item ignoring cooldown on entities
* Added compatibility with `moredelight`
* Added compatibility with `oceansdelight-port`
* Added compatibility with `spanishdelight`
* Added compatibility with `gofish`
* Added some content to the dream world (unfinished)
* Shortened the duration of the negative effect of food attribute `BIZARRE`
* Better compatibility with Polymer and visual effects that match the original
* Now Mod and Polymer code are semi-separated
* The Mod code project architecture rewrite

### Hotfix.1

* Fixed missing Polymer-Patch for Danmaku Item and GUI Slot
* Fixed Magic Broom not rendering
* Fixed Item Display missing rendering
* Fixed the abnormal attack power and speed of SwordOfHisou

### Public

* Fixed There is fall damage after dying in a dream
* Fixed the abnormal attack power and speed of Maple Leaf Fan
* Fixed the abnormal add Max Health in using Upgraded Health Item
* Fixed the issue of Role displaying empty name
* Fixed the mistake in Armor Item
* Fixed the wrong icon for `/touhou about`
* Reworked Danmaku Crafting Table GUI
* Added Danmaku Creator Template Item
* Added dream spawn room
* Added Dream Armor and Tool, its purpose is to have a chance to recover blood when taking damage
* ORDINARY SMALL CAKE and SCARLET DEVILS CAKE can now be used to tame roles
* Reduced crafting cost of BOMB and UPGRADED HEALTH

## 0.3.3+1.21.7

* Fixed doors and slabs not LootTable json
* Fixed command `/touhou video play` throws error
* Fixed Door Block and Slab Block not drop
* Optimize `Crop Block` size
* Cancel `Food Display Block` collision
* `Magic Broom` will no longer delays destroying its model when it is destroyed
* Added `Moonstone` block and its walls, slabs, and stairs
* Added `Moonstone Brick` block and its walls, slabs, and stairs
* Added `Dreamstone` block and its walls, slabs, and stairs
* Added `Dream Brick` block and its walls, slabs, and stairs
* Added Moon Rabbit

## 0.3.2+1.21.7

* Fixed missing dependencies and unable to start

## 0.3.1+1.21.7

* Fixed the issue where items could not be placed on the Food Display Block.
* Fixed the issue where some items could not accept enchantments
* Added item Bad Apple
* Added all drinks from Touhou Mystia's Izakaya
* Added Tengu Camera item
* Added Tengu Hand Shield item
* Adjusted the order and icons of the Creative Tab of this mod
* Touhou Assistant now uses Dialog and Translation keys
* The Role Card item no longer randomly generates characters, but now uses the Dialog creation selection menu to create
  characters. Of course, it retains the random button
* Update translation files zh_tw and en_us
* Each Danmaku item now has its own cooldown

## 0.3.0+1.21.7

* Fixed Nue Trident cannot enchant Mending and Durability
* Now Danmaku Item can enchant Mending and Durability
* Now Rokanken can instantly kill Ghost
* Now Rokanken + right click can see the undead within 16 blocks
* Wearing silver equipment now prevents ghosts from spawning around you and preventing you from being attacked by
  ghosts.
* Added some Touhou Mystia's Izakaya drinks (unfinished)
* Added Tavern Villager (Using **BARREL** right villager to update it)
* Initially add beverage tags
* Remake food tag code and json structure

## 0.2.9+1.21.7

* Fixed an issue with PolyMc crashing due to abnormal resource pack files
* Fixed an issue with Dream Manager save files being incorrectly saved

## 0.2.8+1.21.7

* Updated **en_us** and **zh_tw**
* Updated Lib FactoryTools version
* Now not sleeping for a long time will cause the insanity buff and spawn ghosts on the player
* Now White radish can be fed to pigs
* Now Kitchen tools can now use hoppers to output items
* Now The Role can now use Danmaku items
* Now The generated fruit trees have fruit by default
* Now the kitchen appliances do not need fuel to operate
* Now Manpozuchi has a 3d model
* Now Hakurei Cane can fire barrages
* Now Sword of Hisou will have additional effects in battle during rain and lightning
* Modified the box loot probability to solve the problem of not being able to find the modified items
* Modified wild boar cooking time
* Now synchronized with Touhou Mystia's Izakaya Wiki ingredient and recipe conflicts, conflicts will result in the
  production of dark dishes
* The crafting of Ice Tools and Ice Armor now requires Ice Scales instead of Magic Ice Blocks
* Magic Ice Blocks can be smelted into Ice Scales
* Added Ice Scales building block
* Added Maid Armor
* Added more kitchenware upgrades
* Added various Trapdoor
* Added wild pig skin
* Added Gamerule **doGhost** can turn off the penalty for not sleeping
* Added Dream Pillow. You can shift+right-click and place it on the bed. When you wake up, you will be transported to
  the dream world
* Added Ice Element spawning
* Added Ghost spawning
* Fixed spawn egg color being incorrect
* Fixed being able to craft Laser Danmaku
* Fixed Nue Trident not being able to enchant
* Fixed an issue where the food item translation files in Simplified Chinese were not consistent with the official game
  content in Touhou Mystia's Izakaya
* Removing Cooktop

## 0.2.7+1.21.7

* Fixed a crash when planting

## 0.2.6+1.21.7

* Fixed an issue where crops could float in the air
* Fixed an issue where composters couldn't use seeds
* Work mode is now usable
* Added monster hunting to work mode
* Added planting to work mode
* Added Owner Stick to the Creative Table

## 0.2.5+1.21.7

* Fixed an issue with the cooking table not working
* Fixed GUI grid rendering
* Nue Trident can now be thrown

## 0.2.3/4+1.21.7

* Fixed 1.21.8 not launching
* Fixed Pickaxe not mining Cooktop
* Increased Yousei spawn limit

## 0.2.2+1.21.7

* fixed MagicBroom entity writeCustomData Crash

## 0.2.1+1.21.7

* Fixed fruit tree leaves containing water after mining
* Added Laser Danmaku (temporarily built-in and not available for crafting)
* Added Cursed Decoy Doll
* Added Vaisravanas Pagoda
* Added Kanju Kusuri
* Reduced Truffle drop rate
* Bagua Furnace now fires Laser Danmaku
* Modified fishing drop rate
* Updated en_us translation file
* Added zh_tw translation file

## 0.2.0+1.21.7

* Fixed Silver and Magic armor durability being incorrect
* Cooktop will try to automatically use fuel when cooking
* Fixed Cooktop shadow being incorrect

## 0.1.9+1.21.7

* Added item Death Scythe
* Added crafting recipe to copy Spell Card Template
* Added characters from TH14-TH18
* Fixed sometimes not being able to trade Fumo
* Fixed missing crafting recipe for Cooktop
* Cooktop needs shift+right click to use

## 0.1.8+1.21.7

* Removed some console debug information
* Fixed NPCState translation error in zh_cn

## 0.1.6/7+1.21.7

* Shortened NPCImpl movement sync tick packet
* Fixed crash when saving Role
* Rewrote Role backpack
* Fixed Gensokyo Altar mining not dropping
* Fixed incorrect text of Gensokyo Altar structure in TOUHOU manual
* Fixed Bagua-Furnace out of sync perspective
* Fixed various wood not mining
* Fixed typo of "银稿" in Chinese
* Fixed unable to smelt silver ore block in furnace
* Added Role follow mode
* Added borukva-food to Mystias Izakaya module ingredient tag
* Added borukva-food-exotic to Mystias Izakaya module ingredient tag
* Added FarmersDelight to Mystias Izakaya module ingredient tag
* Added Fishing-101 to Mystias Izakaya module ingredient tag
* Added go-fish to Mystias Izakaya module ingredient tag
* Skipped unstable 0.1.6
* Support 1.21.8

## 0.1.5+1.21.7

* Fixed not being able to craft firework stars
* Fixed follower and yousei positions being out of sync
* Added shared tags with Farmer'Delights and Borukva Food

## 0.1.4+1.21.7

* Seed support planted on Borukva Food's `Better Farmland`

## 0.1.3+1.21.7

* Upgraded to **Minecraft 1.21.7**
* Temporarily remove the trapdoor block

## 0.1.2+1.21.5

* This update marks the final version for Minecraft **1.21.5**. Future development will shift to **1.21.7**.
* The mod is now **playable in survival**
* All recipe typeInstance are now fully implemented**, including Danmaku, cooking, and role cards
* All `Danmaku` and `Danmaku Templates` are now unified as a **single item instance**, with **color changeable via dye**
* All color variants of fairies (except the main type) are now implemented as **singleton entities**
* **Fairies** and **mushrooms** now **naturally spawn** in the world
* **BLACK SALT BLOCK** now generates **naturally in the Nether**
* Added **Fumo Seller** NPC
* Added new entities: `Wild Pig`, `Fumo Seller`
* Added **Role Archive Card** and **Role Card** items
* Added Lemon Tree
* Added Ginkgo Tree
* Added Peach Tree
* Chili Pepper
* Added Cucumber
* Added Grape
* Added Onion
* Added Red Bean
* Added Soybean
* Added Tomato
* Added Toona (Chinese Mahogany)
* Added White Radish
* Added Sweet Potato
* Added Broccoli
* **Refactored several parts of the codebase**
* Simplified recipe and entity registration systems
* Fixed incorrect block placing sounds
* Fixed network packet errors in some cases
* ✅ Work will now begin on migrating to **1.21.7**

## 0.1.1+1.21.5

* Optimized OST textures and prepared for the 1.21.5 update

## 0.1.0+1.21.5

* Earlier releases.
* Many recipes are not yet implemented