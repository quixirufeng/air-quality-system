<template>
    <div v-if="visible" class="modal-overlay" @click.self="close">
        <div class="modal-content glass">
            <div class="modal-header">
                <h2>{{ cityName }} - 空气质量趋势分析</h2>
                <button class="close-btn" @click="close">×</button>
            </div>
            <div id="trendChart" class="chart-box"></div>
            <div class="analysis-text">
                大数据分析：根据最近7天走势，预测明日 AQI 为
                <span class="highlight">{{ predictValue }}</span>，
                建议 <span class="suggestion">{{ suggestion }}</span>。
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import * as echarts from 'echarts';
import axios from 'axios';

const visible = ref(false);
const cityName = ref('');
const predictValue = ref(0);
const suggestion = ref('');
let myChart = null;

// 暴露给父组件调用的方法
const open = async (name) => {
    cityName.value = name;
    visible.value = true;

    // 等待 DOM 渲染完成后再画图
    await nextTick();
    fetchDataAndDraw(name);
};

const close = () => {
    visible.value = false;
    if (myChart) {
        myChart.dispose();
        myChart = null;
    }
};

const fetchDataAndDraw = async (city) => {
    //const res = await axios.get(`http://localhost:8080/api/air/history?cityName=${city}`);
    const res = await axios.get(`/api/air/history?cityName=${city}`);
    const data = res.data.data;

    if (!data || data.length === 0) {
        alert("该城市暂无历史数据（请先在数据库插入测试数据）");
        return;
    }

    // 1. 准备历史数据
    const xData = data.map(item => item.recordTime.substring(5, 10)); // 取 "MM-dd"
    const yData = data.map(item => item.aqi);

    // 2. 简单的“线性回归”预测（模拟）：取最后三天的平均值作为预测值
    const last3 = yData.slice(-3);
    const prediction = Math.round(last3.reduce((a, b) => a + b, 0) / last3.length);
    predictValue.value = prediction;

    // 给点建议
    if (prediction < 50) suggestion.value = "适宜户外运动";
    else if (prediction < 100) suggestion.value = "正常出行";
    else suggestion.value = "佩戴口罩";

    // 3. 构建图表数据（包含预测点）
    // 预测点的 X 轴是 "明日"
    xData.push('预测');
    // 历史数据的序列（最后一位补空，为了连线）
    const historySeries = [...yData, '-'];
    // 预测数据的序列（前面的补空，只显示最后一段虚线）
    const predictSeries = new Array(yData.length - 1).fill('-');
    predictSeries.push(yData[yData.length - 1]); // 连接点
    predictSeries.push(prediction);

    // 4. ECharts 绘图
    const chartDom = document.getElementById('trendChart');
    myChart = echarts.init(chartDom);

    const option = {
        tooltip: { trigger: 'axis' },
        grid: { top: '15%', bottom: '10%', left: '10%', right: '10%' },
        xAxis: {
            type: 'category',
            data: xData,
            axisLine: { lineStyle: { color: '#fff' } }
        },
        yAxis: {
            type: 'value',
            splitLine: { show: false },
            axisLine: { show: false },
            axisLabel: { color: '#fff' }
        },
        series: [
            {
                name: '历史趋势',
                data: historySeries,
                type: 'line',
                smooth: true,
                itemStyle: { color: '#409EFF' },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(64,158,255,0.5)' },
                        { offset: 1, color: 'rgba(64,158,255,0.0)' }
                    ])
                }
            },
            {
                name: 'AI预测',
                data: predictSeries,
                type: 'line',
                smooth: false,
                lineStyle: { type: 'dashed', color: '#E6A23C' }, // 虚线表示预测
                itemStyle: { color: '#E6A23C' }
            }
        ]
    };
    myChart.setOption(option);
};

// 暴露 open 方法
defineExpose({ open });
</script>

<style scoped>
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 999;
}

.modal-content {
    width: 600px;
    padding: 20px;
    border-radius: 16px;
    color: white;
    background: rgba(20, 30, 48, 0.9);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.close-btn {
    background: none;
    border: none;
    color: white;
    font-size: 24px;
    cursor: pointer;
}

.chart-box {
    width: 100%;
    height: 300px;
}

.analysis-text {
    margin-top: 15px;
    font-size: 14px;
    background: rgba(255, 255, 255, 0.1);
    padding: 10px;
    border-radius: 8px;
}

.highlight {
    color: #E6A23C;
    font-weight: bold;
    font-size: 18px;
}

.suggestion {
    font-weight: bold;
    color: #67C23A;
}
</style>