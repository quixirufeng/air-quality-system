<template>
    <div class="chart-panel">
        <div class="title">📊 全国空气质量分布</div>
        <div id="pieChart" class="chart-box"></div>
    </div>
</template>

<script setup>
import { onMounted, watch } from 'vue';
import * as echarts from 'echarts';

// 接收父组件传来的 dataList
const props = defineProps(['dataList']);
let myChart = null;

onMounted(() => {
    myChart = echarts.init(document.getElementById('pieChart'));
});

// 监听数据变化，一旦父组件数据更新，这里就重新画图
watch(() => props.dataList, (newList) => {
    if (newList && newList.length > 0) {
        drawChart(newList);
    }
}, { deep: true });

const drawChart = (data) => {
    // 1. 前端聚合：统计每个等级有多少个城市
    // 结果类似：{ "优": 10, "良": 5, "轻度污染": 2 ... }
    const countMap = {};
    data.forEach(item => {
        // 如果没有 level 字段，根据 AQI 算一下 (容错)
        const level = item.level || getLevel(item.aqi);
        countMap[level] = (countMap[level] || 0) + 1;
    });

    // 2. 转换成 ECharts 需要的格式 [{name: '优', value: 10}, ...]
    const seriesData = Object.keys(countMap).map(key => ({
        name: key,
        value: countMap[key]
    }));

    const option = {
        // 颜色对应：优、良、轻度、中度、重度、严重
        color: ['#67c23a', '#e6a23c', '#f56c6c', '#909399', '#409EFF', '#8a2be2'],
        tooltip: {
            trigger: 'item',
            formatter: '{b}: {c}个 ({d}%)' // 显示百分比
        },
        legend: {
            orient: 'vertical',
            left: 'left',
            textStyle: { color: '#333', fontSize: 10 },
            top: '10%'
        },
        series: [
            {
                name: '污染分布',
                type: 'pie',
                radius: ['40%', '70%'], // 环形图更显高级
                center: ['60%', '50%'], // 稍微往右偏一点，给 Legend 留位置
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 5,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: { show: false }, // 标签不一直显示
                labelLine: { show: false },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: '14',
                        fontWeight: 'bold'
                    }
                },
                data: seriesData
            }
        ]
    };

    myChart.setOption(option);
};

// 辅助函数
const getLevel = (aqi) => {
    if (aqi <= 50) return '优';
    if (aqi <= 100) return '良';
    if (aqi <= 150) return '轻度污染';
    if (aqi <= 200) return '中度污染';
    if (aqi <= 300) return '重度污染';
    return '严重污染';
};
</script>

/* 适用于 RankChart.vue, DistributionChart.vue, PollutantRadar.vue */
<style scoped>
.chart-panel,
.rank-panel {
    /* 兼容两个名字 */
    width: 280px;
    /* 稍微加宽一点点，更大气 */
    /* 核心：极简通透的毛玻璃 */
    background: rgba(255, 255, 255, 0.75);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border: 1px solid rgba(255, 255, 255, 0.9);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
    border-radius: 16px;
    padding: 20px;
    transition: transform 0.3s;
}

.chart-panel:hover,
.rank-panel:hover {
    transform: translateY(-2px);
    /* 悬浮时微微上浮 */
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
    /* 左侧加个小蓝条装饰 */
}

/* 针对 RankChart 特有的样式 */
.rank-box {
    margin-bottom: 20px;
}

.rank-box:last-child {
    margin-bottom: 0;
}

.title.error {
    border-left-color: #F56C6C;
    color: #303133;
}

.title.success {
    border-left-color: #67C23A;
    color: #303133;
}

.chart-box {
    width: 100%;
    height: 180px;
}
</style>