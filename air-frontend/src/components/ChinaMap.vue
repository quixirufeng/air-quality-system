<template>
    <div class="map-container">
        <div id="chinaMap" class="chart-box"></div>
        <TrendModal ref="trendModalRef" />
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import axios from 'axios';
import * as echarts from 'echarts';
import TrendModal from './TrendModal.vue';

const trendModalRef = ref(null);
let myChart = null;

onMounted(() => {
    initMap();
    window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
    if (myChart) myChart.dispose();
});

const handleResize = () => {
    if (myChart) myChart.resize();
};

// 1. 初始化地图（核心修复：使用 async/await 确保顺序）
const initMap = async () => {
    const chartDom = document.getElementById('chinaMap');
    if (!chartDom) return;
    
    myChart = echarts.init(chartDom);
    myChart.showLoading({ text: '地图资源加载中...', color: '#fff' });

    try {
        // 【修复1】这里必须用 await 等待地图文件下载完成
        // baseURL: '' 确保不走 /api，直接去 public 目录找文件
        const mapRes = await axios.get('/map/china.json', { baseURL: '' });
        
        // 【修复2】地图下载完了，立即注册
        echarts.registerMap('china', mapRes.data);
        
        myChart.hideLoading();

        // 【修复3】删除 initChart() 调用，直接在这里配置基础 Option
        const baseOption = {
            backgroundColor: 'transparent',
            tooltip: {
                trigger: 'item',
                backgroundColor: 'rgba(255,255,255,0.95)',
                borderColor: '#409EFF',
                borderWidth: 1,
                padding: 10,
                textStyle: { color: '#333' },
                formatter: (params) => {
                    if (!params.data || isNaN(params.value)) {
                        return `${params.name}<br/><span style="color:#999;font-size:12px">暂无监测数据</span>`;
                    }
                    // 增加一个小圆点装饰
                    const dotColor = params.color || '#ccc';
                    return `
                        <div style="font-weight:bold; font-size:14px; margin-bottom:5px;">${params.name}</div>
                        <div style="display:flex; align-items:center; gap:8px;">
                            <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${dotColor}"></span>
                            AQI指数: <span style="font-weight:bold;font-size:16px">${params.value}</span>
                        </div>
                    `;
                }
            },
            visualMap: {
                type: 'piecewise', // 分段式视觉映射，更清晰
                left: '30',
                bottom: '30',
                showLabel: true,
                pieces: [
                    { min: 0, max: 50, label: '优 (0-50)', color: '#67C23A' },
                    { min: 51, max: 100, label: '良 (51-100)', color: '#E6A23C' },
                    { min: 101, max: 150, label: '轻度 (101-150)', color: '#FF9900' },
                    { min: 151, max: 200, label: '中度 (151-200)', color: '#FF4D4F' },
                    { min: 201, max: 300, label: '重度 (201-300)', color: '#CC0033' },
                    { min: 301, label: '严重 (>300)', color: '#7E0023' }
                ],
                textStyle: { color: '#666', fontSize: 10 }
            },
            geo: {
                map: 'china',
                roam: true,
                zoom: 1.2,
                top: '12%',
                label: {
                    show: true,
                    color: 'rgba(0,0,0,0.6)', 
                    fontSize: 10
                },
                itemStyle: {
                    areaColor: '#ebf1f6',
                    borderColor: '#ffffff',
                    borderWidth: 1.5,
                    shadowColor: 'rgba(64, 158, 255, 0.2)',
                    shadowBlur: 10
                },
                emphasis: {
                    label: { show: true, color: '#fff' },
                    itemStyle: {
                        areaColor: '#409EFF',
                        shadowBlur: 20,
                        shadowColor: 'rgba(0, 0, 0, 0.2)'
                    }
                }
            },
            series: [{
                name: 'AQI',
                type: 'map',
                geoIndex: 0,
                data: [] 
            }]
        };

        myChart.setOption(baseOption);

        // 点击事件：打开趋势图
        myChart.on('click', (params) => {
            if (trendModalRef.value) trendModalRef.value.open(params.name);
        });

        // 初始化完成后，立即拉取一次后端数据
        await refresh();

    } catch (err) {
        console.error("地图初始化失败", err);
        myChart.hideLoading();
        // 可以在界面上显示一个错误提示
    }
};

// 2. 刷新数据方法（暴露给父组件调用）
const refresh = async () => {
    if (!myChart) return;

    try {
        // 请求后端最新数据
        const dbRes = await axios.get('/api/air/list');
        
        if (dbRes.data && Array.isArray(dbRes.data)) {
            // 数据去重逻辑（只取每个城市最新的一条）
            const uniqueMap = new Map();
            dbRes.data.forEach(item => {
                const existing = uniqueMap.get(item.cityName);
                // 简单的去重策略：只要后面出现的就覆盖前面的（假设后端按时间倒序）
                if (!existing || item.id > existing.id) {
                    uniqueMap.set(item.cityName, item);
                }
            });

            const mapData = Array.from(uniqueMap.values()).map(item => ({
                name: item.cityName, // 确保这里是 "北京市"、"山东省" 这种全称
                value: item.aqi
            }));

            // 增量更新 series 数据
            myChart.setOption({
                series: [{
                    data: mapData
                }]
            });
            console.log("🌏 地图数据已更新，共", mapData.length, "条");
        }
    } catch (e) {
        console.error("地图数据更新失败", e);
    }
};

// 3. 暴露 refresh 方法给父组件 (App.vue)
defineExpose({ refresh });
</script>

<style scoped>
.map-container {
    width: 100%;
    height: 100%;
    position: relative; /* 确保子元素定位准确 */
    overflow: hidden;
}

.chart-box {
    width: 100%;
    height: 100%;
}
</style>