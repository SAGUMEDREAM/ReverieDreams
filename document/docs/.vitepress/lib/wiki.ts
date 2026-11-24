import { shallowRef } from 'vue';
import { withBase } from 'vitepress';

export const selection=shallowRef<any>(null);
const cache=new Map<string,Promise<any>>();
export function getData(name='catalog') {
  if(!cache.has(name)) cache.set(name,fetch(withBase(`/mc-assets/data/${name}.json`)).then(r=>{if(!r.ok)throw new Error(`Wiki data: ${r.status}`);return r.json()}));
  return cache.get(name)!;
}
export const fullId=(id:string)=>id?.includes(':')?id:`reverie_dreams:${id}`;
export const locale=(lang:string)=>lang.startsWith('en')?'en':'zh';
export function nameOf(item:any,lang:string){return item?.[lang.startsWith('en')?'nameEn':'nameZh']||item?.id||''}
export function openItem(id:string,stack?:any){selection.value={id:fullId(id),stack};if(typeof window!=='undefined'){const u=new URL(location.href);u.searchParams.set('item',fullId(id));history.replaceState(null,'',u)}}
export function closeItem(){selection.value=null;if(typeof window!=='undefined'){const u=new URL(location.href);u.searchParams.delete('item');history.replaceState(null,'',u)}}
export const categoryNames:any={zh:{all:'全部内容',weapon:'武器与盾牌',equipment:'装备',prop:'道具',block:'方块',food:'料理',beverage:'饮料',ingredient:'食材',material:'材料',danmaku:'弹幕',role:'角色卡',spawn_egg:'生成蛋'},en:{all:'All content',weapon:'Weapons & shields',equipment:'Equipment',prop:'Tools',block:'Blocks',food:'Dishes',beverage:'Drinks',ingredient:'Ingredients',material:'Materials',danmaku:'Danmaku',role:'Role cards',spawn_egg:'Spawn eggs'}};
export const recipeNames:any={zh:{'minecraft:crafting_shaped':'工作台 · 有序合成','minecraft:crafting_shapeless':'工作台 · 无序合成','minecraft:smelting':'熔炉','minecraft:blasting':'高炉','minecraft:smoking':'烟熏炉','minecraft:campfire_cooking':'营火','minecraft:stonecutting':'切石机','minecraft:smithing_transform':'锻造台','reverie_dreams:gensokyo_altar':'幻想乡祭坛','reverie_dreams:kitchen':'厨具','reverie_dreams:barrel':'酿酒桶','reverie_dreams:danmaku':'弹幕制作台','reverie_dreams:strength_table':'强化台','reverie_dreams:danmaku_shape_draw':'弹幕形状绘制'},en:{'minecraft:crafting_shaped':'Crafting · shaped','minecraft:crafting_shapeless':'Crafting · shapeless','minecraft:smelting':'Furnace','minecraft:blasting':'Blast furnace','minecraft:smoking':'Smoker','minecraft:campfire_cooking':'Campfire','minecraft:stonecutting':'Stonecutter','minecraft:smithing_transform':'Smithing','reverie_dreams:gensokyo_altar':'Gensokyo Altar','reverie_dreams:kitchen':'Kitchenware','reverie_dreams:barrel':'Brewing Barrel','reverie_dreams:danmaku':'Danmaku Crafting Table','reverie_dreams:strength_table':'Strengthening Table','reverie_dreams:danmaku_shape_draw':'Danmaku Shape Drawing'}};
