<template><article class="wiki-item-card"><header><McItem :id="id" :size="56"/><div><h3>{{displayName}}</h3><code>{{fullId(id)}}</code></div></header><div class="item-desc"><slot/></div></article></template>
<script setup lang="ts">
import{computed,ref,onMounted}from'vue';import{useData}from'vitepress';import{getData,fullId,nameOf}from'../lib/wiki';import McItem from'./McItem.vue';const props=defineProps<{id:string;name?:string;stats?:Record<string,string|number>}>();const{lang}=useData();const catalog=ref<any>();const displayName=computed(()=>nameOf(catalog.value?.items[fullId(props.id)],lang.value)||props.name||props.id);onMounted(async()=>catalog.value=await getData());
</script>
