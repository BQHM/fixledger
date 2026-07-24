import 'element-plus/dist/index.css';
import './styles/main.css';

import { createPinia } from 'pinia';
import { createApp } from 'vue';

import App from './App.vue';
import { installElementPlus } from './plugins/element-plus';
import { initializePwa } from './pwa';
import router from './router';

initializePwa();

const app = createApp(App);
installElementPlus(app);
app.use(createPinia()).use(router).mount('#app');
