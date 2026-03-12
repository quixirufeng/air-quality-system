<template>
  <div class="app-container">
    <header class="dashboard-header">
      <div class="header-content">
        <div class="logo">
          <span class="icon">🌏</span>
          <div class="title-box">
            <h1>全国空气质量监测平台</h1>
            <span class="subtitle">Big Data Analysis & Monitoring System</span>
          </div>
        </div>
        <div class="nav-links">
          <div class="nav-item" :class="{ active: currentView === 'map' }" @click="currentView = 'map'">
            <span class="nav-icon">🗺️</span> 监控大屏
          </div>

          <div class="nav-item" :class="{ active: currentView === 'list' }" @click="currentView = 'list'">
            <span class="nav-icon">📊</span> 数据明细
          </div>

          <div class="nav-item" @click="showAbout = true">
            <span class="nav-icon">ℹ️</span> 关于平台
          </div>
        </div>

        <Teleport to="body">
          <AboutModal v-model:visible="showAbout" />
        </Teleport>
      </div>
    </header>

    <main class="main-content">
      <transition name="fade" mode="out-in">

        <div v-if="currentView === 'map'" key="map" class="view-container map-mode">

          <div class="map-wrapper">
            <ChinaMap ref="mapRef" />
          </div>

          <div class="left-panel">
            <DistributionChart :dataList="dataList" />

            <PollutantRadar />
          </div>

          <div class="right-panel">
            <RankChart ref="rankChartRef" key="rank" />
          </div>

          <div class="stats-overlay">
            <div class="stat-card glass">
              <div class="stat-icon blue">📡</div>
              <div class="stat-info">
                <h3>在线监测站点</h3>
                <p>{{ stats.total }} <span class="unit">个</span></p>
              </div>
            </div>
            <div class="stat-card glass">
              <div class="stat-icon green">🍃</div>
              <div class="stat-info">
                <h3>全国平均 AQI</h3>
                <p>{{ stats.avg }} <span class="unit">{{ getLevel(stats.avg) }}</span></p>
              </div>
            </div>
            <div class="stat-card glass">
              <div class="stat-icon red">⚠️</div>
              <div class="stat-info">
                <h3>重污染区域</h3>
                <p>{{ stats.polluted }} <span class="unit">个</span></p>
              </div>
            </div>
          </div>
        </div>

        <div v-else key="list" class="view-container list-mode">
          <div class="data-table-card">
            <div class="card-header">
              <h2>实时监测数据明细 ({{ stats.lastUpdateTime }})</h2>

              <div class="action-box">
                <input v-model="searchCity" type="text" placeholder="搜索列表..." class="search-input" />

                <button class="export-btn" @click="showExportModal = true">
                  📥 高级导出
                </button>
              </div>

              <ExportModal v-model:visible="showExportModal" :dataList="dataList" />
            </div>
            <div class="table-scroll">
              <table class="styled-table">
                <thead>
                  <tr>
                    <th>城市/省份</th>

                    <th @click="handleSort('aqi')" class="sortable-th">
                      <div class="th-content">
                        AQI 指数
                        <span class="caret-wrapper">
                          <i class="sort-caret ascending"
                            :class="{ active: sortState.key === 'aqi' && sortState.order === 'asc' }"></i>
                          <i class="sort-caret descending"
                            :class="{ active: sortState.key === 'aqi' && sortState.order === 'desc' }"></i>
                        </span>
                      </div>
                    </th>

                    <th @click="handleSort('pm25')" class="sortable-th">
                      <div class="th-content">
                        PM2.5
                        <span class="caret-wrapper">
                          <i class="sort-caret ascending"
                            :class="{ active: sortState.key === 'pm25' && sortState.order === 'asc' }"></i>
                          <i class="sort-caret descending"
                            :class="{ active: sortState.key === 'pm25' && sortState.order === 'desc' }"></i>
                        </span>
                      </div>
                    </th>

                    <th>污染等级</th>
                    <th>监测时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in sortedDataList" :key="item.id" class="table-row">
                    <td style="font-weight: 600; color: #2c3e50;">{{ item.cityName }}</td>

                    <td class="num-cell">
                      <span class="aqi-tag"
                        :style="{ color: getAqiColor(item.aqi), background: getAqiColor(item.aqi) + '15' }">
                        {{ item.aqi }}
                      </span>
                    </td>

                    <td class="num-cell" style="font-family: 'DIN Alternate', sans-serif;">{{ item.pm25 }}</td>

                    <td>
                      <span class="status-badge" :class="getLevelClass(item.level)">
                        {{ item.level }}
                      </span>
                    </td>

                    <td style="color: #909399; font-size: 12px;">{{ formatTime(item.recordTime) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </transition>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import axios from 'axios';
import ChinaMap from './components/ChinaMap.vue';
import RankChart from './components/RankChart.vue';
import DistributionChart from './components/DistributionChart.vue';
import PollutantRadar from './components/PollutantRadar.vue';
import ExportModal from './components/ExportModal.vue';
import AboutModal from './components/AboutModal.vue';

// --- 状态定义 ---
const currentView = ref('map'); // 当前视图
const dataList = ref([]);       // 所有数据
const mapRef = ref(null);       // 地图组件引用
const rankChartRef = ref(null); // 【新增】排行榜组件引用
let timer = null;               // 定时器
const searchCity = ref(''); // 用于绑定输入框
const showExportModal = ref(false); // 增加一个控制弹窗显示的变量
const showAbout = ref(false);

// 统计数据 (不再写死，而是根据 dataList 算出来)
const stats = reactive({
  total: 0,
  avg: 0,
  polluted: 0,
  lastUpdateTime: '-'
});

// --- 生命周期 ---
onMounted(() => {
  loadData(); // 进页面先查一次

  // 启动轮询：每 10 秒刷新一次数据 (配合后端的自动生成)
  timer = setInterval(() => {
    console.log("⏰ 触发自动刷新...");
    loadData();
    // 如果排行榜组件也需要刷新，这里其实重新 fetch 就行，因为 rank 接口也是实时的
    // 【新增】这里！每次定时刷新时，也让排行榜刷新一下
    if (rankChartRef.value) {
      rankChartRef.value.refresh();
    }
    // 【新增这一段】刷新地图数据
    if (mapRef.value) {
      mapRef.value.refresh();
    }
  }, 10000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer); // 离开页面销毁定时器
});

// --- 核心业务逻辑 ---

// 1. 加载数据
const loadData = async () => {
  try {
    // 查列表
    //const res = await axios.get('http://localhost:8080/api/air/list');
    const res = await axios.get('/api/air/list');

    // 简单的去重逻辑：如果后端返回了历史数据，我们只取每个城市最新的一条
    // (这里假设后端返回的是混杂数据，我们在前端做一个简单的“分组取最大值”)
    // 如果你的后端 /list 已经只返回最新数据，那直接 dataList.value = res.data 即可
    // 这里为了演示前端处理能力，写一个简单的去重：
    const uniqueMap = new Map();
    res.data.forEach(item => {
      // 假设 id 越大越新，或者根据 recordTime 比较
      const existing = uniqueMap.get(item.cityName);
      if (!existing || item.id > existing.id) {
        uniqueMap.set(item.cityName, item);
      }
    });
    const cleanData = Array.from(uniqueMap.values());

    dataList.value = cleanData;

    // 更新统计指标
    calcStats(cleanData);

    // 如果在地图模式，尝试通知地图刷新（虽然地图组件自己也在请求，但这样可以保持同步）
    // 这里的最佳实践其实是把 cleanData 传给 ChinaMap，但为了不改动太多组件代码，我们维持现状

  } catch (error) {
    console.error("获取数据失败", error);
  }
};

// 2. 计算统计指标
const calcStats = (list) => {
  if (!list.length) return;

  stats.total = list.length;

  // 计算平均 AQI
  const sumAqi = list.reduce((acc, cur) => acc + cur.aqi, 0);
  stats.avg = Math.round(sumAqi / list.length);

  // 计算污染城市数量 (AQI > 100 或者 等级包含污染)
  stats.polluted = list.filter(item => item.aqi > 100).length;

  // 更新时间
  const now = new Date();
  stats.lastUpdateTime = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;
};

// 3. 导出 Excel
const handleExport = () => {
  //let url = 'http://localhost:8080/api/air/export';
  let url = '/api/air/export';

  // 如果输入框里有字，就拼接到 URL 后面
  if (searchCity.value) {
    url += `?cityName=${searchCity.value}`;
  }

  // 触发下载
  window.open(url);
};

// --- 辅助显示函数 ---

const getLevel = (aqi) => {
  if (aqi <= 50) return '优';
  if (aqi <= 100) return '良';
  if (aqi <= 150) return '轻度';
  if (aqi <= 200) return '中度';
  return '重度';
};

// const getLevelClass = (level) => {
//   if (!level) return '';
//   if (level.includes('优') || level.includes('良')) return 'status-badge good';
//   if (level.includes('轻度')) return 'status-badge warn';
//   return 'status-badge danger';
// };

// const getAqiColor = (aqi) => {
//   if (aqi <= 50) return '#67c23a';
//   if (aqi <= 100) return '#E6A23C';
//   return '#F56C6C';
// };

const formatTime = (timeStr) => {
  if (!timeStr) return '';
  return timeStr.replace('T', ' '); // 把 Java 的 LocalDateTime 'T' 去掉
};

// === 【新增】排序状态管理 ===
const sortState = reactive({
  key: '',    // 当前按哪一列排序：'aqi' 或 'pm25'
  order: ''   // 排序方向：'desc' (降序) 或 'asc' (升序)
});

// === 【新增】点击表头触发排序 ===
const handleSort = (key) => {
  // 如果点击的是当前已排序的列，就反转顺序
  if (sortState.key === key) {
    sortState.order = sortState.order === 'desc' ? 'asc' : 'desc';
  } else {
    // 如果点击的是新列，默认先按降序排列（污染高的数据先看到）
    sortState.key = key;
    sortState.order = 'desc';
  }
};

// === 【新增】计算最终展示的数据（排序 + 搜索） ===
const sortedDataList = computed(() => {
  // 1. 复制一份数据，避免直接修改原数组
  let list = [...dataList.value];

  // (可选) 这里顺便把搜索框的筛选功能也加到表格显示里，体验更好
  if (searchCity.value) {
    list = list.filter(item => item.cityName.includes(searchCity.value));
  }

  // 2. 执行排序逻辑
  if (sortState.key) {
    list.sort((a, b) => {
      const valA = a[sortState.key];
      const valB = b[sortState.key];

      if (sortState.order === 'asc') {
        return valA - valB; // 升序
      } else {
        return valB - valA; // 降序
      }
    });
  }

  return list;
});

// 获取颜色（用于 AQI 数字）
const getAqiColor = (val) => {
  if (val <= 50) return '#67C23A'; // 绿
  if (val <= 100) return '#E6A23C'; // 黄
  if (val <= 150) return '#F56C6C'; // 红
  return '#909399'; // 紫/灰
};

// 获取等级样式类（用于胶囊背景）
const getLevelClass = (level) => {
  if (level === '优' || level === '良') return 'good';
  if (level.includes('轻度') || level.includes('中度')) return 'warn';
  return 'danger';
};
</script>

<style>
/* --- 全局样式重置 --- */
:root {
  --primary-color: #409EFF;
  --glass-bg: rgba(255, 255, 255, 0.75);
  --glass-border: 1px solid rgba(255, 255, 255, 0.8);
  --glass-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
  --header-height: 70px;
}

body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.app-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
}

/* --- Header 顶栏美化 --- */
.dashboard-header {
  height: var(--header-height);
  /* 背景升级：深邃的蓝黑渐变 + 高强度磨砂 */
  background: rgba(12, 22, 36, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 0 40px;
  /* 增加左右内边距，更透气 */
  /* 底部增加一条发光的蓝线，提升科技感 */
  border-bottom: 1px solid rgba(64, 158, 255, 0.15);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  z-index: 100;
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 左侧 LOGO 区域 */
.logo {
  display: flex;
  align-items: center;
  gap: 15px;
}

/* 图标增加缓慢旋转动画 */
.logo .icon {
  font-size: 32px;
  filter: drop-shadow(0 0 8px rgba(0, 229, 255, 0.6));
  /* 霓虹光晕 */
  animation: floatIcon 3s ease-in-out infinite alternate;
}

@keyframes floatIcon {
  from {
    transform: translateY(0);
  }

  to {
    transform: translateY(-3px);
  }
}

.title-box {
  display: flex;
  flex-direction: column;
}

.title-box h1 {
  font-size: 24px;
  margin: 0;
  font-weight: 800;
  letter-spacing: 2px;
  /* 【核心升级】文字流光渐变效果 */
  background: linear-gradient(120deg, #ffffff 20%, #409EFF 100%);
  -webkit-background-clip: text;
  background-clip: text;
  /* ✅ 补上这一行标准属性，警告就会消失 */
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 20px rgba(64, 158, 255, 0.1);
}

.title-box .subtitle {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.4);
  letter-spacing: 4px;
  /* 拉宽字母间距，显得更有电影质感 */
  text-transform: uppercase;
  margin-top: 4px;
  font-family: sans-serif;
}

/* 右侧导航胶囊 */
.nav-links {
  display: flex;
  gap: 8px;
  background: rgba(0, 0, 0, 0.3);
  /* 深色底槽 */
  padding: 5px;
  border-radius: 50px;
  /* 胶囊圆角 */
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.nav-item {
  padding: 8px 24px;
  border-radius: 40px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  color: rgba(255, 255, 255, 0.5);
  /* 默认灰色 */
  position: relative;
  overflow: hidden;
}

.nav-item:hover {
  color: white;
  background: rgba(255, 255, 255, 0.05);
}

/* 选中状态：高亮发光胶囊 */
.nav-item.active {
  background: linear-gradient(135deg, #409EFF 0%, #3a8ee6 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
  /* 蓝色辉光 */
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.nav-icon {
  font-size: 16px;
}

/* --- 主内容区布局 --- */
.main-content {
  flex: 1;
  position: relative;
  width: 100%;
}

.view-container {
  width: 100%;
  height: 100%;
}

/* 🔴 关键修复：地图容器必须有宽高 */
.map-mode {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.map-wrapper {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

/* 面板定位 */
.left-panel {
  position: absolute;
  top: 90px;
  left: 30px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.right-panel {
  position: absolute;
  top: 90px;
  right: 30px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 底部统计卡片 */
.stats-overlay {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 25px;
  z-index: 50;
  width: 60%;
  min-width: 600px;
}

.stat-card.glass {
  flex: 1;
  background: var(--glass-bg);
  backdrop-filter: blur(12px);
  border: var(--glass-border);
  box-shadow: var(--glass-shadow);
  padding: 20px 25px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
}

.stat-card.glass:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 26px;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.05);
}

.stat-icon.blue {
  background: #eef7ff;
  color: #409EFF;
}

.stat-icon.green {
  background: #f0f9eb;
  color: #67C23A;
}

.stat-icon.red {
  background: #fef0f0;
  color: #F56C6C;
}

.stat-info h3 {
  margin: 0 0 5px 0;
  font-size: 13px;
  color: #606266;
  font-weight: 600;
}

.stat-info p {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #303133;
  letter-spacing: -1px;
  font-family: 'DIN Alternate', sans-serif;
}

.stat-info .unit {
  font-size: 12px;
  font-weight: normal;
  color: #909399;
  margin-left: 4px;
}

/* --- 列表模式 (数据明细页) --- */
.list-mode {
  padding: 40px;
  box-sizing: border-box;
  overflow: auto;
  display: flex;
  justify-content: center;
  /* 背景已经由 body 统一控制，这里不用重复写 */
}

.data-table-card {
  width: 100%;
  max-width: 1200px;
  /* 升级为毛玻璃大卡片 */
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  padding: 35px;
  border-radius: 24px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.08);
  height: fit-content;
  transition: transform 0.3s;
}

/* 标题栏布局 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #2c3e50;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 给标题加个蓝色小竖条装饰 */
.card-header h2::before {
  content: '';
  display: block;
  width: 6px;
  height: 24px;
  background: linear-gradient(180deg, #409EFF 0%, #a0cfff 100%);
  border-radius: 4px;
}

/* 搜索操作区 */
.action-box {
  display: flex;
  gap: 12px;
}

.search-input {
  padding: 10px 16px;
  border: 1px solid #e0e6ed;
  border-radius: 50px;
  /* 圆润风格 */
  outline: none;
  font-size: 14px;
  width: 220px;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.02);
}

.search-input:focus {
  border-color: #409EFF;
  width: 260px;
  /* 聚焦时伸长，增加交互感 */
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15);
  background: #fff;
}

.export-btn {
  background: linear-gradient(135deg, #67c23a 0%, #529b2e 100%);
  color: white;
  border: none;
  padding: 0 24px;
  height: 40px;
  /* 与输入框等高 */
  border-radius: 50px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
  display: flex;
  align-items: center;
  gap: 6px;
}

.export-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.4);
  filter: brightness(1.1);
}

/* 表格滚动区 */
.table-scroll {
  max-height: 65vh;
  overflow-y: auto;
  border-radius: 12px;
  /* 表格本身也给个圆角 */
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.03);
  /* 细微边框 */
}

/* 表格本体 */
.styled-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

/* 表头美化：品牌蓝渐变 */
.styled-table th {
  position: sticky;
  top: 0;
  background: linear-gradient(90deg, #f8faff 0%, #eef5fe 100%);
  /* 极淡的蓝底 */
  padding: 18px 20px;
  text-align: left;
  color: #5a6d82;
  font-weight: 700;
  font-size: 14px;
  border-bottom: 2px solid #dcdfe6;
  z-index: 10;
  backdrop-filter: blur(10px);
}

.styled-table td {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f2f5;
  color: #606266;
  font-size: 14px;
  transition: background 0.2s;
}

/* 斑马纹：隔行变色 */
.styled-table tr:nth-child(even) {
  background-color: #fafbfc;
}

/* 鼠标悬停高亮 */
.styled-table tr:hover td {
  background: #ecf5ff;
  /* 浅蓝色高亮 */
  color: #409EFF;
}

/* 状态徽章优化：胶囊样式 */
.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
  min-width: 40px;
  text-align: center;
}

.status-badge.good {
  background: #f0f9eb;
  color: #67c23a;
}

.status-badge.warn {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-badge.danger {
  background: #fef0f0;
  color: #f56c6c;
}

/* 滚动条美化 */
.table-scroll::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.table-scroll::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.table-scroll::-webkit-scrollbar-track {
  background: transparent;
}

/* --- 表格整体容器 --- */
.styled-table {
  width: 100%;
  border-collapse: separate;
  /* 关键：为了圆角效果，不能用 collapse */
  border-spacing: 0 8px;
  /* 关键：行与行之间留出 8px 的缝隙，更有层次感 */
}

/* --- 表头样式 --- */
.styled-table th {
  padding: 15px 20px;
  color: #909399;
  font-weight: 600;
  font-size: 13px;
  text-align: left;
  border: none;
  /* 去掉表头的线 */
  position: sticky;
  top: 0;
  background: rgba(255, 255, 255, 0.95);
  /* 防止滚动时透底 */
  z-index: 10;
}

/* --- 排序表头的交互 --- */
.sortable-th {
  cursor: pointer;
  transition: color 0.3s;
}

.sortable-th:hover {
  color: #409EFF;
  background: #f2f6fc;
  /* 鼠标放表头上，表头微亮 */
  border-radius: 8px;
}

.sort-icon {
  display: inline-block;
  margin-left: 5px;
  transition: transform 0.3s;
  font-size: 12px;
  opacity: 0.3;
}

.sort-icon.active {
  color: #409EFF;
  opacity: 1;
  font-weight: bold;
}

/* --- 表格行样式 (卡片式列表) --- */
.table-row {
  background: #fff;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
  /* 给每一行加一点微弱的阴影 */
}

.table-row td {
  padding: 18px 20px;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
}

/* 第一列和最后一列要有圆角 */
.table-row td:first-child {
  border-left: 1px solid #ebeef5;
  border-top-left-radius: 10px;
  border-bottom-left-radius: 10px;
}

.table-row td:last-child {
  border-right: 1px solid #ebeef5;
  border-top-right-radius: 10px;
  border-bottom-right-radius: 10px;
}

/* --- 悬停高亮光棒效果 --- */
.table-row:hover {
  transform: scale(1.005);
  /* 微微放大 */
  background: #f0f9eb;
  /* 极淡的绿色背景 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  /* 阴影加深 */
  position: relative;
  z-index: 5;
}

/* --- 胶囊徽章 (Status Badge) --- */
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* 优/良：清新绿 */
.status-badge.good {
  background: rgba(103, 194, 58, 0.15);
  /* 半透明背景 */
  color: #67C23A;
  border: 1px solid rgba(103, 194, 58, 0.2);
}

/* 轻度/中度：警示黄 */
.status-badge.warn {
  background: rgba(230, 162, 60, 0.15);
  color: #E6A23C;
  border: 1px solid rgba(230, 162, 60, 0.2);
}

/* 重度/严重：危险红 */
.status-badge.danger {
  background: rgba(245, 108, 108, 0.15);
  color: #F56C6C;
  border: 1px solid rgba(245, 108, 108, 0.2);
}

/* --- AQI 数字标签 --- */
.aqi-tag {
  font-weight: 800;
  font-family: 'Arial', sans-serif;
  padding: 2px 8px;
  border-radius: 4px;
}

/* --- 排序箭头优化 (双三角风格) --- */

/* 表头内容容器：让文字和箭头横向排列 */
.th-content {
  display: flex;
  align-items: center;
  gap: 6px;
  /* 文字和箭头的间距 */
}

/* 箭头包裹器：垂直排列两个小三角 */
.caret-wrapper {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  height: 14px;
  width: 12px;
  vertical-align: middle;
  cursor: pointer;
  position: relative;
}

/* 三角形基础样式 */
.sort-caret {
  width: 0;
  height: 0;
  border: 4px solid transparent;
  /* 利用边框画三角形 */
  position: absolute;
  transition: border-color 0.3s;
  /* 颜色变化动画 */
}

/* 上三角 (Ascending) */
.sort-caret.ascending {
  border-bottom-color: #c0c4cc;
  /* 默认灰色 */
  top: -1px;
  /* 微调位置 */
}

/* 下三角 (Descending) */
.sort-caret.descending {
  border-top-color: #c0c4cc;
  /* 默认灰色 */
  bottom: -1px;
  /* 微调位置 */
}

/* --- 激活状态 --- */

/* 升序激活：上三角变蓝 */
.sort-caret.ascending.active {
  border-bottom-color: #409EFF;
}

/* 降序激活：下三角变蓝 */
.sort-caret.descending.active {
  border-top-color: #409EFF;
}

/* 鼠标悬停时的微交互：两个都稍微变深一点，提示可点击 */
.sortable-th:hover .sort-caret.ascending {
  border-bottom-color: #909399;
}

.sortable-th:hover .sort-caret.descending {
  border-top-color: #909399;
}

/* 保持激活色的优先级最高 */
.sortable-th:hover .sort-caret.ascending.active {
  border-bottom-color: #409EFF;
}

.sortable-th:hover .sort-caret.descending.active {
  border-top-color: #409EFF;
}
</style>