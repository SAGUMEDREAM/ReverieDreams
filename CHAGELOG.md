# 🌙 Gensokyo: Reverie of Lost Dreams - Changelog

## 0.3.3+1.21.7
* Fixed doors and slabs not LootTable json 
* Fixed command `/touhou video play` throws error
* Fixed Door Block and Slab Block not drop
* Optimize `Crop Block` size
* Cancel `Food Display Block` collision
* `Magic Broom` will no longer delays destroying its model when it is destroyed
* Added `Moonstone Block` and its walls, slabs, and stairs
* Added `Moonstone Brick Block` and its walls, slabs, and stairs
* Added `Moonstone Block` and its walls, slabs, and stairs
* Added `Moonstone Brick Block` and its walls, slabs, and stairs
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
* The Role Card item no longer randomly generates characters, but now uses the Dialog creation selection menu to create characters. Of course, it retains the random button
* Update translation files zh_tw and en_us
* Each Danmaku item now has its own cooldown

## 0.3.0+1.21.7
* Fixed Nue Trident cannot enchant Mending and Durability
* Now Danmaku Item can enchant Mending and Durability
* Now Rokanken can instantly kill Ghost
* Now Rokanken + right click can see the undead within 16 blocks
* Wearing silver equipment now prevents ghosts from spawning around you and preventing you from being attacked by ghosts.
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
* Now synchronized with Touhou Mystia's Izakaya Wiki ingredient and recipe conflicts, conflicts will result in the production of dark dishes
* The crafting of Ice Tools and Ice Armor now requires Ice Scales instead of Magic Ice Blocks
* Magic Ice Blocks can be smelted into Ice Scales
* Added Ice Scales building block
* Added Maid Armor
* Added more kitchenware upgrades
* Added various Trapdoor
* Added wild pig skin
* Added Gamerule **doGhost** can turn off the penalty for not sleeping
* Added Dream Pillow. You can shift+right-click and place it on the bed. When you wake up, you will be transported to the dream world
* Added Ice Element spawning
* Added Ghost spawning
* Fixed spawn egg color being incorrect
* Fixed being able to craft Laser Danmaku
* Fixed Nue Trident not being able to enchant
* Fixed an issue where the food item translation files in Simplified Chinese were not consistent with the official game content in Touhou Mystia's Izakaya
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
* All recipe types are now fully implemented**, including Danmaku, cooking, and role cards
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