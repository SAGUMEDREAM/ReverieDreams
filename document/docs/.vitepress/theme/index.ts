import DefaultTheme from 'vitepress/theme';
import { h } from 'vue';
import './custom.css';
import McItem from '../components/McItem.vue';
import ModelPreview from '../components/ModelPreview.vue';
import ItemCard from '../components/ItemCard.vue';
import AltarRecipe from '../components/AltarRecipe.vue';
import BarrelRecipe from '../components/BarrelRecipe.vue';
import ItemCatalog from '../components/ItemCatalog.vue';
import RecipeCatalog from '../components/RecipeCatalog.vue';
import EntityCatalog from '../components/EntityCatalog.vue';
import WikiDetails from '../components/WikiDetails.vue';
import WikiHome from '../components/WikiHome.vue';
import BlockCatalog from '../components/BlockCatalog.vue';
import SourceNote from '../components/SourceNote.vue';
import AltarStructure from '../components/AltarStructure.vue';
export default {
  extends: DefaultTheme,
  Layout: () => h(DefaultTheme.Layout, null, { 'layout-bottom': () => h(WikiDetails) }),
  enhanceApp({ app }) {
    for (const [name, component] of Object.entries({ McItem, ModelPreview, ItemCard, AltarRecipe, BarrelRecipe, ItemCatalog, RecipeCatalog, EntityCatalog, WikiHome, BlockCatalog, SourceNote, AltarStructure })) app.component(name, component);
  }
};
