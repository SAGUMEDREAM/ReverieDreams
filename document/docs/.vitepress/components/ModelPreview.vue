<template>
  <div class="model-preview" :class="{interactive}" ref="host">
    <canvas ref="canvas" :width="pixels" :height="pixels" :aria-label="label" role="img" @pointerdown="down" @pointermove="move" @pointerup="up" @pointercancel="up" @wheel="wheel" />
    <span v-if="unavailable" class="model-unavailable" :title="en?'Uses an in-game special renderer':'此模型使用游戏内特殊渲染器'">◌</span>
    <div v-if="interactive&&!unavailable" class="model-controls"><span>{{en?'Drag to rotate · scroll to zoom':'拖动旋转 · 滚轮缩放'}}</span><button @click="reset">{{en?'Reset':'复位'}}</button></div>
  </div>
</template>
<script setup lang="ts">
import {ref,computed,onMounted,onBeforeUnmount,watch} from 'vue';
import {withBase,useData} from 'vitepress';
import {loadModel,drawModel} from '../lib/model-renderer.mjs';
const props=withDefaults(defineProps<{url?:string;size?:number;interactive?:boolean;label?:string}>(),{size:48,interactive:false,label:''});
const {lang}=useData();const en=computed(()=>lang.value.startsWith('en'));
const host=ref<HTMLElement>(),canvas=ref<HTMLCanvasElement>(),unavailable=ref(false),pixels=computed(()=>props.interactive?600:Math.max(64,props.size*2));
let model:any,observer:IntersectionObserver,visible=false,version=0,yaw=0,pitch=0,zoom=1,drag:any=null,raf=0,animation=0;
async function load(){const token=++version;unavailable.value=false;if(!props.url){unavailable.value=true;return}try{let prepared=await loadModel(props.url,withBase);if(token!==version)return;model=prepared;unavailable.value=prepared.unavailable;draw();cancelAnimationFrame(animation);if(props.interactive&&Object.values(prepared.textureInfo).some((v:any)=>v.animation))animation=requestAnimationFrame(animate)}catch{if(token===version)unavailable.value=true}}
function animate(time:number){if(canvas.value&&model)drawModel(canvas.value,model,{yaw,pitch,zoom,ticks:time/50});animation=requestAnimationFrame(animate)}
function draw(){if(canvas.value&&model)drawModel(canvas.value,model,{yaw,pitch,zoom})}
function down(e:PointerEvent){if(!props.interactive)return;drag=[e.clientX,e.clientY];canvas.value?.setPointerCapture(e.pointerId)}
function move(e:PointerEvent){if(!drag)return;yaw+=(e.clientX-drag[0])*.6;pitch=Math.max(-85,Math.min(85,pitch+(e.clientY-drag[1])*.6));drag=[e.clientX,e.clientY];cancelAnimationFrame(raf);raf=requestAnimationFrame(draw)}
function up(){drag=null}function wheel(e:WheelEvent){if(props.interactive){e.preventDefault();zoom=Math.max(.4,Math.min(2.5,zoom-e.deltaY*.001));draw()}}
function reset(){yaw=0;pitch=0;zoom=1;draw()}
onMounted(()=>{observer=new IntersectionObserver(entries=>{if(entries.some(e=>e.isIntersecting)){visible=true;load();observer.disconnect()}},{rootMargin:'160px'});if(host.value)observer.observe(host.value)});
watch(()=>props.url,()=>{if(visible){reset();load()}});onBeforeUnmount(()=>{observer?.disconnect();cancelAnimationFrame(raf);cancelAnimationFrame(animation);version++});
</script>
