<template>
<Teleport to="body"><div v-if="selection&&item" class="wiki-modal-backdrop" @click.self="closeItem">
<section ref="panel" class="wiki-modal" role="dialog" aria-modal="true" :aria-label="displayName" tabindex="-1">
<button class="modal-close" @click="closeItem" :aria-label="en?'Close':'关闭'">×</button>
<header class="detail-heading"><span class="eyebrow">{{categoryNames[lc][item.category]}}</span><h2>{{displayName}}</h2><code>{{item.id}}</code></header>
<div class="detail-overview"><div><ModelPreview :key="previewUrl" :url="previewUrl" :size="280" interactive :label="displayName"/><label v-if="item.placedBlock"><input type="checkbox" v-model="placed">{{en?'Placed dish model':'查看摆盘模型'}}</label></div>
<div class="detail-facts"><label v-if="item.variants.length">{{en?'Variant':'款式'}}<select v-model="variant"><option value="">{{en?'Default':'默认'}}</option><option v-for="v in item.variants" :key="v.key" :value="v.key">{{nameOf(v,lang)}}</option></select></label>
<dl><template v-for="fact in facts" :key="fact[0]"><dt>{{fact[0]}}</dt><dd>{{fact[1]}}</dd></template></dl>
<p v-for="(line,i) in item.descriptions[lc]" :key="i">{{line}}</p>
<p v-if="item.nameFromWiki" class="muted">{{en?'The mod has no localized item name for this entry. This descriptive name follows its block implementation; use the ID to identify it in game.':'原模组未提供此项译名，本站依据方块实现标为“水田”；请结合 ID 在游戏内确认。'}}</p><p v-if="item.previewIssues?.length" class="muted">{{en?'The original mod model has missing resource references; the preview retains its available geometry and missing-texture markers.':'模组原模型存在缺失引用，预览保留已有几何及缺失纹理标记。'}}</p><p v-if="item.internal" class="muted">{{en?'Internal display or debug item; registry presence does not imply a survival item.':'界面展示或调试用物品，注册在游戏中不代表属于生存玩法。'}}</p>
<p v-if="properties.length" class="tag-list"><span v-for="p in properties" :key="p">{{propertyName(p)}}</span></p>
<a :href="withBase('/'+lc+'/'+guide)">{{en?'Read the gameplay guide →':'查看使用与玩法说明 →'}}</a>
</div></div>
<div class="detail-tabs" role="tablist"><button v-for="t in ['recipes','uses','drops','data']" :key="t" :aria-selected="section===t" role="tab" :class="{active:section===t}" @click="section=t">{{labels[t]}}</button></div>
<div v-if="section==='recipes'||section==='uses'" class="detail-content">
<p v-if="!activeRecipes.length">{{en?'No matching recipe in this snapshot. Check obtaining records and the guide; an empty recipe list does not mean creative-only.':'此快照没有匹配配方，请结合获取记录与玩法说明；没有配方不代表仅限创造模式。'}}</p>
<RecipeDisplay v-for="r in activeRecipes.slice(0,limit)" :key="r.key" :recipe="r"/><button v-if="activeRecipes.length>limit" @click="limit+=12">{{en?'Show more':'继续显示'}}</button></div>
<div v-else-if="section==='drops'" class="detail-content">
<article v-for="t in item.trades" :key="t.id" class="source-record"><strong>{{en?'Villager trade':'村民交易'}} · {{t.id.split('_result_')[0]}}</strong><div class="recipe-flow"><IngredientSlot :ingredient="t.data.wants"/><IngredientSlot v-if="t.data.additional_wants" :ingredient="t.data.additional_wants"/><span>→</span><McItem :id="item.id" :count="tradeCount(t)" :show-name="true"/></div><details><summary>{{en?'Trade rules':'交易规则'}}</summary><pre>{{JSON.stringify(t.data,null,2)}}</pre><code>{{t.source}}</code></details></article>
<p>{{en?'Loot entries may depend on maturity, tools, random rolls and other conditions. They are not guaranteed drops.':'战利品记录可能依赖成熟度、工具、随机数及其他条件，不表示必定掉落。'}}</p>
<article v-for="d in item.drops" :key="d.table" class="source-record"><strong>{{dropName(d.table)}}</strong><code>{{d.table}}</code><details><summary>{{en?'Conditions and count rules':'条件与数量规则'}}</summary><pre>{{JSON.stringify(lootTables[d.table]?.data,null,2)}}</pre><code>{{d.source}}</code></details></article>
<p v-if="!item.drops.length&&!item.trades.length">{{en?'No direct loot-table or villager-trade reference. See its recipes and guide for other ways to obtain it.':'没有直接引用此物品的战利品表或村民交易，请查看制作配方和玩法说明中的其他获取方式。'}}</p></div>
<div v-else class="detail-content"><h3>{{en?'Ingredient tags':'材料标签'}}</h3><p class="tag-list"><code v-for="tag in item.tags" :key="tag">#{{tag}}</code></p><details><summary>{{en?'Components of the selected stack':'当前所选物品的数据组件'}}</summary><pre>{{JSON.stringify(components,null,2)}}</pre></details></div>
</section></div></Teleport>
</template>
<script setup lang="ts">
import {sameValue,matchesVariant} from '../../../shared/value.mjs';
import {ref,computed,watch,onMounted,onBeforeUnmount,nextTick} from 'vue';
import {useData,withBase} from 'vitepress';
import {selection,closeItem,getData,nameOf,locale,categoryNames} from '../lib/wiki';
import ModelPreview from './ModelPreview.vue';import RecipeDisplay from './RecipeDisplay.vue';import IngredientSlot from './IngredientSlot.vue';import McItem from './McItem.vue';
const {lang}=useData();const lc=computed(()=>locale(lang.value)),en=computed(()=>lc.value==='en');
const catalog=ref<any>(),recipes=ref<any>({}),lootTables=ref<any>({}),language=ref<any>({}),variant=ref(''),placed=ref(false),section=ref('recipes'),limit=ref(12),panel=ref<HTMLElement>();
const item=computed(()=>catalog.value?.items[selection.value?.id]);
const chosen=computed(()=>item.value?.variants.find((v:any)=>v.key===variant.value));
const patch=computed(()=>chosen.value?.components??selection.value?.stack?.components??{});
const previewUrl=computed(()=>placed.value?item.value?.placedBlock?.model:chosen.value?.model??item.value?.model);
const displayName=computed(()=>nameOf(chosen.value??item.value,lang.value));
const components=computed(()=>({...item.value?.components,...patch.value}));
const properties=computed(()=>['foodProperties','beverageProperties'].flatMap((field:string)=>components.value['reverie_dreams:'+field.replace(/[A-Z]/g,s=>'_'+s.toLowerCase())]?.length?components.value['reverie_dreams:'+field.replace(/[A-Z]/g,s=>'_'+s.toLowerCase())]:item.value?.[field]??[]));
const guide=computed(()=>({weapon:'items/weapons',equipment:'items/equipment',prop:'items/props',food:'blocks/cooking',beverage:'blocks/brewing',ingredient:'blocks/crops',material:'items/materials',block:'blocks/interactive',danmaku:'items/danmaku',role:'entities/npc-companion',spawn_egg:'entities/monsters'} as any)[item.value?.category]??'catalog');
const labels=computed<any>(()=>en.value?{recipes:'Crafting',uses:'Uses',drops:'Obtaining',data:'Details'}:{recipes:'如何制作',uses:'用在哪里',drops:'获取记录',data:'详细数据'});
const facts=computed(()=>{const c=components.value,f:any[]=[];if(c['minecraft:max_stack_size'])f.push([en.value?'Stack limit':'堆叠上限',c['minecraft:max_stack_size']]);if(c['minecraft:max_damage'])f.push([en.value?'Durability':'耐久度',c['minecraft:max_damage']]);const food=c['minecraft:food'];if(food){f.push([en.value?'Nutrition':'饥饿值恢复',food.nutrition]);f.push([en.value?'Saturation':'饱和度',food.saturation])}if(c['minecraft:blocks_attacks'])f.push([en.value?'Blocking delay':'举盾延迟',(c['minecraft:blocks_attacks'].block_delay_seconds??0)+' s']);const b=catalog.value?.blocks[item.value?.block];if(b){f.push([en.value?'Hardness':'硬度',b.hardness]);f.push([en.value?'Blast resistance':'爆炸抗性',b.blastResistance])}return f});
function matches(ingredient:any){if(!ingredient)return false;if(ingredient.alternatives)return ingredient.alternatives.some(matches);if(ingredient.tag)return catalog.value?.tags[ingredient.tag]?.includes(item.value?.id);if(ingredient.id!==item.value?.id)return false;return Object.entries(ingredient.components??{}).every(([k,v])=>sameValue(components.value[k],v))}
function matchesOutput(output:any){return output?.id===item.value?.id&&matchesVariant(item.value.components,output.components,patch.value)}
const activeRecipes=computed(()=>{const keys=section.value==='uses'?item.value?.uses:item.value?.recipes;return(keys??[]).map((k:string)=>recipes.value[k]).filter((r:any)=>r&&(section.value==='uses'?r.inputs.some(matches):!variant.value&&!selection.value?.stack?.components||matchesOutput(r.output)))});
function propertyName(id:string){const [ns,name]=id.split(':');return language.value['food_property.'+ns+'.'+name]??language.value['beverage_property.'+ns+'.'+name]??id}
function tradeCount(t:any){const change=t.data.given_item_modifiers?.find((v:any)=>v.function==='minecraft:set_count'&&!v.add);return typeof change?.count==='number'?change.count:t.data.gives.count??1}
function dropName(id:string){const e=catalog.value?.entities.find((e:any)=>e.lootTable===id);const b=catalog.value?.blocks[id.replace(':blocks/',':')];return e?nameOf(e,lang.value):b?nameOf(b,lang.value):id}
function key(event:KeyboardEvent){if(!selection.value)return;if(event.key==='Escape'){event.preventDefault();closeItem()}if(event.key==='Tab'&&panel.value){const focus=[...panel.value.querySelectorAll<HTMLElement>('button,select,a,input,[tabindex="0"]')].filter(e=>!e.hasAttribute('disabled'));if(!focus.length)return;const first=focus[0],last=focus.at(-1);if(event.shiftKey&&(document.activeElement===first||document.activeElement===panel.value)){event.preventDefault();last?.focus()}else if(!event.shiftKey&&document.activeElement===last){event.preventDefault();first.focus()}}}
let previousFocus:Element|null=null;
watch(selection,async value=>{if(typeof document==='undefined')return;if(!value){document.body.style.overflow='';(previousFocus as HTMLElement)?.focus?.();return}if(document.body.style.overflow!=='hidden')previousFocus=document.activeElement;const [data,allRecipes,strings,loot]=await Promise.all([getData(),getData('recipes'),getData('lang-'+lc.value),getData('loot-tables')]);if(selection.value!==value)return;catalog.value=data;recipes.value=allRecipes;language.value=strings;lootTables.value=loot;if(!data.items[value.id]){closeItem();return}variant.value='';placed.value=false;section.value='recipes';limit.value=12;const selected=value.stack?.components;if(selected)variant.value=item.value?.variants.find((v:any)=>Object.entries(selected).every(([k,n])=>sameValue(({...item.value.components,...v.components})[k],n)))?.key??'';document.body.style.overflow='hidden';await nextTick();panel.value?.focus()});
watch([section,variant],()=>limit.value=12);
function readQuery(){const id=new URLSearchParams(location.search).get('item');selection.value=id?{id}:null}
onMounted(()=>{document.addEventListener('keydown',key);window.addEventListener('popstate',readQuery);readQuery()});
onBeforeUnmount(()=>{document.removeEventListener('keydown',key);window.removeEventListener('popstate',readQuery);document.body.style.overflow=''});
</script>
