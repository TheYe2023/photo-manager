import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import Antd from "ant-design-vue";
import '@/access/'
import '@/router/index'
import "ant-design-vue/dist/reset.css";
import VueCropper from 'vue-cropper';
import 'vue-cropper/dist/index.css'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'


const app = createApp(App)
const pinia = createPinia()

app.use(Antd);
app.use(createPinia())
app.use(router)
app.use(VueCropper)
app.mount('#app')
pinia.use(piniaPluginPersistedstate)

