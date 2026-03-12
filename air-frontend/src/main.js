import { createApp } from 'vue'
import App from './App.vue'
// 1. 引入 axios
import axios from 'axios'

const app = createApp(App)

// ==========================================
// 👇【核心配置】重点就是这几行！
// ==========================================

// 1. 设置基础路径：所有请求自动加上 '/api' 前缀
// 这样 Nginx 看到 /api 开头，就会自动转发给后端 8080 端口
axios.defaults.baseURL = '/api';

// 2. (可选) 设置超时时间：5秒没反应就报错，防止死等
// axios.defaults.timeout = 5000;

// 3. 挂载到全局 (可选)，这样你以后可以用 this.$axios 访问
app.config.globalProperties.$axios = axios;

// ==========================================

app.mount('#app')