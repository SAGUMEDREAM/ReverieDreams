<template><RecipeDisplay v-if="resolved" :recipe="resolved"/><p v-else-if="loaded" class="wiki-data-note">{{en?'Recipe not found in this version.':'当前版本未找到此配方。'}} <code>{{recipe}}</code></p></template>
<script setup lang="ts">
import{ref,computed,onMounted}from'vue';import{useData}from'vitepress';import{getData}from'../lib/wiki';import RecipeDisplay from'./RecipeDisplay.vue';const props=defineProps<{recipe?:string}>();const{lang}=useData();const en=computed(()=>lang.value.startsWith('en')),all=ref<any[]>([]),loaded=ref(false);const resolved=computed(()=>all.value.find(r=>r.type==='reverie_dreams:gensokyo_altar'&&(r.id===props.recipe||r.id==='reverie_dreams:'+props.recipe)));onMounted(async()=>{all.value=Object.values(await getData('recipes'));loaded.value=true});
</script>
