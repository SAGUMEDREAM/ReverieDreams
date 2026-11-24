import { defineConfig } from 'vitepress';
const groups = {
  zh: [
    {text:'开始游玩',items:[{text:'首页',link:'/zh/'},{text:'新手入门',link:'/zh/getting-started'},{text:'全量物品图鉴',link:'/zh/catalog'},{text:'配方查询',link:'/zh/recipes'}]},
    {text:'战斗与装备',items:[{text:'武器操作',link:'/zh/items/weapons'},{text:'弹幕与符卡',link:'/zh/items/danmaku'},{text:'装备与防具',link:'/zh/items/equipment'},{text:'道具使用',link:'/zh/items/props'},{text:'材料与通货',link:'/zh/items/materials'}]},
    {text:'生产与经营',items:[{text:'幻想乡祭坛',link:'/zh/blocks/altar'},{text:'料理与厨具',link:'/zh/blocks/cooking'},{text:'酿造饮料',link:'/zh/blocks/brewing'},{text:'餐厅营业',link:'/zh/blocks/restaurant'},{text:'种植与采集',link:'/zh/blocks/crops'},{text:'功能方块与装饰',link:'/zh/blocks/interactive'}]},
    {text:'伙伴与世界',items:[{text:'NPC 伙伴与工作',link:'/zh/entities/npc-companion'},{text:'生物图鉴',link:'/zh/entities/monsters'},{text:'维度探索',link:'/zh/world/gensokyo'},{text:'月球更新与旧存档',link:'/zh/world/moon-upgrade'},{text:'设置与常见问题',link:'/zh/settings'}]}
  ],
  en: [
    {text:'Start here',items:[{text:'Home',link:'/en/'},{text:'Getting started',link:'/en/getting-started'},{text:'Item catalog',link:'/en/catalog'},{text:'Recipe browser',link:'/en/recipes'}]},
    {text:'Combat & equipment',items:[{text:'Weapons',link:'/en/items/weapons'},{text:'Danmaku & spellcards',link:'/en/items/danmaku'},{text:'Armor & equipment',link:'/en/items/equipment'},{text:'Tools & artifacts',link:'/en/items/props'},{text:'Materials',link:'/en/items/materials'}]},
    {text:'Crafting & management',items:[{text:'Gensokyo Altar',link:'/en/blocks/altar'},{text:'Cooking',link:'/en/blocks/cooking'},{text:'Brewing',link:'/en/blocks/brewing'},{text:'Restaurants',link:'/en/blocks/restaurant'},{text:'Farming',link:'/en/blocks/crops'},{text:'Workstations & decorations',link:'/en/blocks/interactive'}]},
    {text:'Companions & exploration',items:[{text:'NPC companions',link:'/en/entities/npc-companion'},{text:'Creature catalog',link:'/en/entities/monsters'},{text:'Dimensions',link:'/en/world/gensokyo'},{text:'Moon save migration',link:'/en/world/moon-upgrade'},{text:'Settings & troubleshooting',link:'/en/settings'}]}
  ]
};
export default defineConfig({
  title:'ReverieDreams Wiki',
  description:'按模组实际内容编写的玩家指南、物品图鉴与配方查询。',
  base:process.env.WIKI_BASE || '/',
  cleanUrls:true,
  outDir:process.env.WIKI_OUT_DIR || undefined,
  lastUpdated:false,
  head:[['link',{rel:'icon',href:(process.env.WIKI_BASE||'/')+'mc-assets/assets/reverie_dreams/icon.png'}]],
  locales:{
    root:{label:'简体中文',lang:'zh-CN',link:'/zh/'},
    zh:{label:'简体中文',lang:'zh-CN',link:'/zh/',themeConfig:{nav:[{text:'开始游玩',link:'/zh/getting-started'},{text:'查图鉴',link:'/zh/catalog'},{text:'查配方',link:'/zh/recipes'}],sidebar:{'/zh/':groups.zh},outline:{label:'本页内容',level:[2,3]},docFooter:{prev:'上一页',next:'下一页'}}},
    en:{label:'English',lang:'en-US',link:'/en/',themeConfig:{nav:[{text:'Start here',link:'/en/getting-started'},{text:'Items',link:'/en/catalog'},{text:'Recipes',link:'/en/recipes'}],sidebar:{'/en/':groups.en},outline:{label:'On this page',level:[2,3]}}}
  },
  themeConfig:{logo:'/mc-assets/assets/reverie_dreams/icon.png',siteTitle:'ReverieDreams Wiki',search:{provider:'local',options:{locales:{zh:{translations:{button:{buttonText:'搜索攻略',buttonAriaLabel:'搜索攻略'},modal:{noResultsText:'没有找到相关攻略',resetButtonTitle:'清空',footer:{selectText:'选择',navigateText:'切换',closeText:'关闭'}}}},root:{translations:{button:{buttonText:'搜索攻略',buttonAriaLabel:'搜索攻略'}}}}}},socialLinks:[{icon:'github',link:'https://github.com/SAGUMEDREAM/ReverieDreams'}],footer:{message:'Gensokyo: Reverie of Lost Dreams · Minecraft 26.1.2'}},
  markdown:{image:{lazyLoading:true}},
  vite:{build:{chunkSizeWarningLimit:1200}}
});
