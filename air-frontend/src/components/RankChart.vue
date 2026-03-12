<template>
    <div class="rank-panel">
        <div class="rank-box">
            <div class="title error">⚠️ 污染严重 Top 5</div>
            <div id="worstChart" class="chart-box"></div>
        </div>

        <div class="rank-box">
            <div class="title success">🍃 空气清新 Top 5</div>
            <div id="bestChart" class="chart-box"></div>
        </div>
    </div>
</template>

<script setup>
import { onMounted } from 'vue';
import * as echarts from 'echarts';
import axios from 'axios';

// 定义图表实例变量，方便后续更新数据
let worstChartInstance = null;
let bestChartInstance = null;

onMounted(async () => {
    // 初始化图表空壳
    worstChartInstance = echarts.init(document.getElementById('worstChart'));
    bestChartInstance = echarts.init(document.getElementById('bestChart'));

    // 首次加载数据
    await refresh();
});

// 【核心修改】将数据请求逻辑封装成 refresh 方法
const refresh = async () => {
    try {
        //const res = await axios.get('http://localhost:8080/api/air/rank');
        const res = await axios.get('/api/air/rank');
        const worstData = res.data.data.worst;
        const bestData = res.data.data.best;

        // 更新两个图表
        updateChart(worstChartInstance, worstData, '#f56c6c');
        updateChart(bestChartInstance, bestData, '#67c23a');

        console.log("排行榜数据已更新");
    } catch (e) {
        console.error("排行榜更新失败", e);
    }
};

// 抽取通用的渲染逻辑
const updateChart = (chartInstance, data, color) => {
    if (!chartInstance) return;

    const yData = data.map(item => item.cityName).reverse();
    const xData = data.map(item => item.aqi).reverse();

    const option = {
        grid: { top: '5%', bottom: '5%', left: '30%', right: '15%' },
        xAxis: { show: false },
        yAxis: {
            type: 'category',
            data: yData,
            axisLine: { show: false },
            axisTick: { show: false },
            axisLabel: { color: '#333', fontWeight: 'bold', fontSize: 12 }
        },
        series: [
            {
                type: 'bar',
                data: xData,
                itemStyle: {
                    borderRadius: [0, 10, 10, 0],
                    color: color
                },
                label: { show: true, position: 'right', fontWeight: 'bold' },
                barWidth: 12,
                // 开启这个动画配置，更新时会有丝滑的过渡效果
                realtimeSort: true,
                animationDuration: 1000,
                animationDurationUpdate: 1000
            }
        ]
    };
    chartInstance.setOption(option);
};

// 【核心修改】暴露 refresh 方法给父组件调用
defineExpose({ refresh });
</script>

<style scoped>
/* 1. 外层容器：变成透明的布局容器，负责把两个卡片隔开 */
.rank-panel {
  width: 280px;
  display: flex;       /* 开启 Flex 布局 */
  flex-direction: column; /* 垂直排列 */
  gap: 20px;           /* 【关键】控制红榜和绿榜之间的间距 */
  background: transparent; /* 不要背景 */
  box-shadow: none;    /* 不要阴影 */
  border: none;        /* 不要边框 */
  padding: 0;
}

/* 2. 内层盒子：这才是真正的“毛玻璃卡片” */
.rank-box {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  padding: 20px;
  transition: transform 0.3s;
}

/* 悬浮效果加在具体的卡片上 */
.rank-box:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
}

.title {
  font-size: 15px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 8px;
  border-left: 4px solid #409EFF;
}

.title.error { border-left-color: #F56C6C; color: #303133; }
.title.success { border-left-color: #67C23A; color: #303133; }

.chart-box { width: 100%; height: 180px; }
</style>