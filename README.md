<img width="1274" height="701" alt="3989d036e2c4722b597df381770b2c2a" src="https://github.com/user-attachments/assets/5d5c311e-124d-44c1-ae50-a55d1ab252de" />
## 🌍 全国空气质量监测平台 (Air Quality Monitoring System)

![Vue.js](https://img.shields.io/badge/Vue-3.0-4FC08D?style=for-the-badge&logo=vue.js)
![Vite](https://img.shields.io/badge/Vite-Ready-646CFF?style=for-the-badge&logo=vite)
![ECharts](https://img.shields.io/badge/ECharts-5.0-E43961?style=for-the-badge&logo=apacheecharts)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-Backend-6DB33F?style=for-the-badge&logo=springboot)

> 让呼吸的数据看得见 · 让环境的未来更清晰

## 📖 项目简介

本项目基于大数据分析技术，致力于构建一个**实时、精准、可视化**的空气质量监测系统。通过对海量监测数据的深度挖掘，以直观的地图热力、动态图表和多维分析模型，帮助用户快速掌握全国环境状况，为环境保护决策提供数据支撑。

*⚠️ 声明：本项目为学习交流项目，数据来源为模拟生成，请勿用于商业用途。*

---

## ✨ 核心特性

- 🗺️ **全国交互式地图**：基于 GeoJSON 渲染的中国地图，直观展示各省份 AQI 污染热力分布，支持点击下钻。
- 📊 **多维数据可视化**：
  - **污染物成分雷达**：动态“呼吸”动画，实时监控 PM2.5, PM10, SO2, NO2, CO, O3 六项指标。
  - **空气等级分布**：高清晰度环形图，展示全国空气优良率。
  - **动态红黑榜**：实时排序的“污染严重 Top 5”与“空气清新 Top 5”柱状图。
- 📈 **历史趋势与 AI 预测**：结合历史数据生成连线趋势，并基于简易线性回归提供“明日 AQI 预测”及出行建议。
- 📥 **高级报表导出**：支持按“自定义时间范围”和“城市多选”精准筛选数据，并一键导出 Excel 报表。
- 🎨 **极致拟态 UI 设计**：全局采用现代 **Glassmorphism (毛玻璃)** 风格，提供深沉而通透的沉浸式视觉体验。

---

## 🛠️ 技术架构

### 前端 (Frontend)
- **核心框架**: Vue 3.0 + Vite
- **网络请求**: Axios
- **数据可视化**: Apache ECharts 5 (配合 GeoJSON)
- **UI 风格**: 纯手工 CSS3 打造毛玻璃态 (Glassmorphism)，无过度依赖 UI 组件库

### 后端 (Backend) - *参考*
- **核心框架**: Java Spring Boot
- **持久层框架**: MyBatis Plus
- **数据库**: MySQL 8.0

---

## 📂 前端核心组件结构

```text
src/
 ├── components/
 │   ├── ChinaMap.vue          # 全国核心热力地图组件
 │   ├── DistributionChart.vue # 污染分布环形图组件
 │   ├── PollutantRadar.vue    # 污染物雷达图 (带呼吸动画)
 │   ├── RankChart.vue         # 动态红黑榜柱状图
 │   ├── TrendModal.vue        # 历史趋势与预测分析弹窗
 │   ├── ExportModal.vue       # 高级条件筛选与 Excel 导出弹窗
 │   └── AboutModal.vue        # 关于平台 (技术栈介绍页)
 ├── App.vue                   # 主页面布局与入口
 └── main.js                   # 全局配置 (Axios 反向代理设置等)

```

---

## 🚀 快速启动

### 1. 环境准备

请确保你的电脑上已经安装了 [Node.js](https://nodejs.org/) (推荐 v16+)。

### 2. 克隆项目

```bash
git clone [https://github.com/你的用户名/你的仓库名.git](https://github.com/你的用户名/你的仓库名.git)
cd 你的仓库名

```

### 3. 安装依赖

```bash
npm install

```

### 4. 本地开发运行

```bash
npm run dev

```

### 5. 生产环境打包

```bash
npm run build

```

打包后生成的 `dist` 文件夹即可部署到 Nginx 等静态服务器上。

---

## ⚠️ 部署注意事项 (Nginx 代理)

本项目前端 Axios 配置了全局统一前缀 `baseURL: '/api'`。在生产环境部署时，**必须在 Nginx 中配置反向代理**，以解决跨域问题并将请求转发给后端 Spring Boot 接口：

```nginx
location / {
    root   /www/wwwroot/你的前端项目路径/dist;
    index  index.html index.htm;
    try_files $uri $uri/ /index.html; # 防止 Vue 刷新 404
}

location /api/ {
    proxy_pass [http://127.0.0.1:8080/](http://127.0.0.1:8080/); # 转发给后端服务 (注意最后的斜杠 /)
    proxy_set_header Host $host;
}

```

---

## 📬 联系作者

* **Author**: 阿湫不看月亮
* **Email**: 1947503577@qq.com
* **WeChat**: qiu2464

*如果这个项目对你有帮助，欢迎给我点个 ⭐ Star 呀！*
