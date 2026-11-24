<template>
  <section class="altar-builder" :data-visible-blocks="visibleCount" :data-total-blocks="layout?.placements.length" :data-layer="layer">
    <div class="altar-toolbar">
      <label>{{en?'View layer':'查看层级'}}
        <select v-model="layer" :aria-label="en?'Altar layer':'祭坛层级'">
          <option value="all">{{en?'Complete structure':'完整结构'}}</option>
          <option v-for="y in layout?.height??3" :key="y" :value="String(y-1)">{{en?'Layer':'第'}} {{y}}{{en?'':'层'}} · Y{{y>1?' + '+(y-1):' = 0'}}</option>
        </select>
      </label>
      <label><input type="checkbox" v-model="grid">{{en?'Positioning grid':'定位网格'}}</label>
      <button type="button" @click="zoom=Math.max(.65,zoom-.15)" :aria-label="en?'Zoom out':'缩小'">−</button>
      <button type="button" @click="zoom=Math.min(2.4,zoom+.15)" :aria-label="en?'Zoom in':'放大'">+</button>
      <button type="button" @click="overhead">{{en?'Top view':'俯视'}}</button>
      <button type="button" @click="reset">{{en?'Reset view':'复位'}}</button>
    </div>
    <div ref="viewport" class="altar-viewport" :aria-busy="!scene&&!error">
      <canvas ref="canvas" tabindex="0" role="img" :aria-label="en?'Interactive 3D altar construction: drag to rotate, scroll to zoom, select blocks for coordinates':'祭坛搭建3D预览：拖动旋转，滚轮缩放，点选方块查看坐标'"
        :data-yaw="yaw" :data-pitch="pitch" @pointerdown="down" @pointermove="move" @pointerup="up" @pointercancel="cancel" @wheel.prevent="wheel" @keydown="key"/>
      <p v-if="!scene&&!error" class="altar-overlay">{{en?'Loading real block models…':'正在加载真实方块模型…'}}</p>
      <p v-if="error" class="altar-overlay" role="alert">{{en?'Unable to load the preview. Reload the page to retry.':'预览加载失败，请刷新页面重试。'}}</p>
      <span class="altar-visible-count" v-if="layout" aria-live="polite">{{en?'Showing':'显示'}} {{visibleCount}} / {{layout.placements.length}} {{en?'blocks':'个方块'}}</span>
    </div>
    <div class="altar-inspection" aria-live="polite">
      <template v-if="selected"><strong>{{blockName(selected.id)}}</strong><code>X {{selected.pos[0]}} · Y{{selected.pos[1]>0?' +':''}} {{selected.pos[1]}} · Z {{selected.pos[2]}}</code></template>
      <span v-else>{{en?'Select a block to read its position relative to the altar.':'点选方块，查看相对于中心祭坛的坐标。'}}</span>
    </div>
    <p class="altar-help">{{en?'Drag to rotate · +/− buttons or scroll to zoom · arrow keys rotate. The altar is (0, 0, 0).':'拖动旋转 · ± 按钮或滚轮缩放 · 方向键旋转。中心祭坛为 (0, 0, 0)。'}}</p>
    <p class="altar-grid-note">{{en?'The 9 × 9 grid is only a positioning guide; no floor platform is required by this structure check.':'9 × 9 网格仅用于定位，不代表必须铺设地板；柱子底层与祭坛底部同高。'}}</p>
    <div class="altar-materials" v-if="layout">
      <button v-for="(m,i) in layout.materials" :key="m.id" type="button" @click="openItem(m.id)" :title="en?'Open item and crafting details':'查看物品与制作配方'">
        <span class="altar-swatch" :class="'altar-material-'+i" aria-hidden="true"></span><span>{{blockName(m.id)}}</span><strong>× {{m.count}}</strong>
      </button>
    </div>
  </section>
</template>
<script setup lang="ts">
import {ref,computed,watch,onMounted,onBeforeUnmount} from 'vue';
import {useData,withBase} from 'vitepress';
import {getData,nameOf,openItem} from '../lib/wiki';
import {prepareAltarScene,drawAltar,visiblePlacements,pickBlock} from '../lib/altar-scene.mjs';
const {lang,isDark}=useData(),en=computed(()=>lang.value.startsWith('en'));
const layout=ref<any>(),canvas=ref<HTMLCanvasElement>(),viewport=ref<HTMLElement>(),error=ref(false),layer=ref('all'),grid=ref(true);
const yaw=ref(-35),pitch=ref(30),zoom=ref(1),selected=ref<any>();
const visibleCount=computed(()=>layout.value?visiblePlacements(layout.value,layer.value).length:0);
let scene:any=null,hits:any[]=[],drag:any=null,observer:ResizeObserver,raf=0,disposed=false;
function blockName(id:string){return nameOf(layout.value?.materials.find((m:any)=>m.id===id),lang.value)||id}
function draw(){if(!canvas.value||!viewport.value||!scene||!layout.value)return;const ratio=Math.min(window.devicePixelRatio||1,2),box=viewport.value.getBoundingClientRect();
  const width=Math.round(box.width*ratio),height=Math.round(box.height*ratio);if(canvas.value.width!==width)canvas.value.width=width;if(canvas.value.height!==height)canvas.value.height=height;
  hits=drawAltar(canvas.value,layout.value,scene,{yaw:yaw.value,pitch:pitch.value,zoom:zoom.value,layer:layer.value,grid:grid.value,selected:selected.value?.pos.join(','),dark:isDark.value,en:en.value});
}
function schedule(){cancelAnimationFrame(raf);raf=requestAnimationFrame(draw)}
function down(event:PointerEvent){if(event.button!==0)return;drag={x:event.clientX,y:event.clientY,startX:event.clientX,startY:event.clientY,moved:false};canvas.value?.setPointerCapture(event.pointerId)}
function move(event:PointerEvent){if(!drag)return;const dx=event.clientX-drag.x,dy=event.clientY-drag.y;if(Math.hypot(event.clientX-drag.startX,event.clientY-drag.startY)>4)drag.moved=true;
  if(drag.moved){yaw.value+=dx*.45;pitch.value=Math.max(10,Math.min(90,pitch.value+dy*.4))}drag.x=event.clientX;drag.y=event.clientY;
}
function up(event:PointerEvent){if(!drag)return;if(!drag.moved&&canvas.value){const box=canvas.value.getBoundingClientRect();selected.value=pickBlock(hits,[(event.clientX-box.left)*canvas.value.width/box.width,(event.clientY-box.top)*canvas.value.height/box.height])}drag=null;canvas.value?.releasePointerCapture(event.pointerId);schedule()}
function cancel(){drag=null}
function wheel(event:WheelEvent){zoom.value=Math.max(.65,Math.min(2.4,zoom.value-event.deltaY*.001))}
function reset(){yaw.value=-35;pitch.value=30;zoom.value=1}
function overhead(){yaw.value=0;pitch.value=90;zoom.value=1}
function key(event:KeyboardEvent){const actions:any={ArrowLeft:()=>yaw.value-=10,ArrowRight:()=>yaw.value+=10,ArrowUp:()=>pitch.value=Math.min(90,pitch.value+5),ArrowDown:()=>pitch.value=Math.max(10,pitch.value-5),'+':()=>zoom.value=Math.min(2.4,zoom.value+.1),'=':()=>zoom.value=Math.min(2.4,zoom.value+.1),'-':()=>zoom.value=Math.max(.65,zoom.value-.1),Home:reset};if(actions[event.key]){event.preventDefault();actions[event.key]()}}
watch([yaw,pitch,zoom,grid,selected,isDark,en],schedule);
watch(layer,()=>{if(selected.value&&layer.value!=='all'&&selected.value.pos[1]!==+layer.value)selected.value=null;schedule()});
onMounted(async()=>{observer=new ResizeObserver(schedule);if(viewport.value)observer.observe(viewport.value);try{const data=await getData('altar-structure');const prepared=await prepareAltarScene(data,withBase);if(disposed)return;layout.value=data;scene=prepared;selected.value=data.placements[0];schedule()}catch{if(!disposed)error.value=true}});
onBeforeUnmount(()=>{disposed=true;observer?.disconnect();cancelAnimationFrame(raf)});
</script>
<style scoped>
.altar-builder{border:1px solid var(--wiki-border);border-radius:10px;margin:24px 0 32px;overflow:hidden;background:var(--wiki-card)}
.altar-toolbar{display:flex;align-items:center;gap:12px;flex-wrap:wrap;padding:14px 16px;border-bottom:1px solid var(--wiki-border);font-size:12px}.altar-toolbar label{display:flex;align-items:center;gap:8px}.altar-toolbar select{padding:5px 8px;font-size:12px}.altar-toolbar input{accent-color:var(--vp-c-brand-1)}
.altar-toolbar button{padding:5px 10px;border:1px solid var(--wiki-border);border-radius:5px;background:var(--vp-c-bg-soft)}
.altar-viewport{height:clamp(320px,48vw,470px);position:relative;background:radial-gradient(ellipse at center,var(--vp-c-bg-soft),transparent 75%)}.altar-viewport canvas{width:100%;height:100%;display:block;touch-action:none;cursor:grab;image-rendering:pixelated}.altar-viewport canvas:active{cursor:grabbing}.altar-viewport canvas:focus-visible{outline:2px solid var(--vp-c-brand-1);outline-offset:-3px}
.altar-overlay{position:absolute;inset:40% 10% auto;text-align:center;color:var(--wiki-muted);font-size:13px;pointer-events:none}.altar-visible-count{position:absolute;left:14px;top:12px;color:var(--wiki-muted);font-size:11px;background:var(--wiki-card);border:1px solid var(--wiki-border);border-radius:5px;padding:3px 8px;pointer-events:none}
.altar-inspection{display:flex;justify-content:center;gap:12px;flex-wrap:wrap;align-items:center;min-height:40px;padding:5px 16px;color:var(--wiki-muted);font-size:12px}.altar-inspection strong{color:var(--vp-c-text-1)}.altar-inspection code{font-size:11px}
.altar-help,.altar-grid-note{font-size:11px!important;line-height:1.7!important;margin:5px 16px!important;color:var(--wiki-muted);text-align:center}.altar-grid-note{margin-bottom:16px!important}
.altar-materials{display:flex;flex-wrap:wrap;gap:6px;padding:12px 14px;border-top:1px solid var(--wiki-border);background:var(--vp-c-bg-soft)}.altar-materials button{display:flex;align-items:center;gap:7px;background:var(--wiki-card);border:1px solid var(--wiki-border);border-radius:5px;padding:7px 9px;font-size:11px;flex:1;white-space:nowrap}.altar-materials button:hover{border-color:var(--vp-c-brand-1)}.altar-materials strong{margin-left:auto}.altar-swatch{width:12px;height:12px;border-radius:2px;border:1px solid #0002;flex-shrink:0}.altar-material-0{background:#71476e}.altar-material-1{background:#b48557}.altar-material-2{background:#a4aa6c}
@media(max-width:600px){.altar-toolbar{gap:8px;padding:12px}.altar-viewport{height:350px}.altar-materials button{flex-basis:100%}.altar-inspection{gap:5px}.altar-help,.altar-grid-note{text-align:left}}
</style>
