<template>
  <div class="chart-panel">
    <div class="title">
      <span class="dot"></span>
      🧪 污染物实时成分
    </div>
    <div id="radarChart" class="chart-box"></div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';

let myChart = null;
let timer = null;

onMounted(() => {
  initChart();
  // 启动“呼吸”定时器
  startBreathing();
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
  if (myChart) myChart.dispose();
});

// 模拟数据生成器：在基准值附近微调
const getRandomData = () => {
  return [
    Math.round(40 + Math.random() * 15),  // PM2.5: 40-55
    Math.round(60 + Math.random() * 20),  // PM10: 60-80
    Math.round(15 + Math.random() * 5),   // SO2
    Math.round(25 + Math.random() * 10),  // NO2
    (0.6 + Math.random() * 0.4).toFixed(1), // CO
    Math.round(45 + Math.random() * 15)   // O3
  ];
};

const initChart = () => {
  const chartDom = document.getElementById('radarChart');
  myChart = echarts.init(chartDom);

  const option = {
    // 雷达图坐标系配置
    radar: {
      indicator: [
        { name: 'PM2.5', max: 100 },
        { name: 'PM10', max: 150 },
        { name: 'SO2', max: 50 },
        { name: 'NO2', max: 80 },
        { name: 'CO', max: 2 },
        { name: 'O3', max: 100 }
      ],
      radius: '65%',
      center: ['50%', '55%'],
      splitNumber: 4,
      axisName: {
        color: '#666',
        fontWeight: 'bold',
        fontSize: 10
      },
      splitLine: {
        lineStyle: {
          color: ['rgba(64, 158, 255, 0.1)', 'rgba(64, 158, 255, 0.2)']
        }
      },
      splitArea: { show: false }
    },
    series: [
      {
        name: '污染物',
        type: 'radar',
        // 关键配置：开启平滑动画
        animationDuration: 2000, 
        animationEasing: 'cubicInOut',
        data: [
          {
            value: getRandomData(), // 初始数据
            name: '实时监测值',
            symbol: 'circle',
            symbolSize: 6,
            areaStyle: {
              color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
                { offset: 0, color: 'rgba(64, 158, 255, 0.1)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.6)' }
              ])
            },
            itemStyle: { color: '#409EFF', borderColor: '#fff', borderWidth: 2 },
            lineStyle: { width: 2, color: '#409EFF' }
          }
        ]
      }
    ]
  };

  myChart.setOption(option);
  window.addEventListener('resize', () => myChart.resize());
};

const startBreathing = () => {
  // 每 3 秒钟更新一次数据，产生“形变”效果
  timer = setInterval(() => {
    if (!myChart) return;
    
    // 生成新的随机数据
    const newData = getRandomData();

    myChart.setOption({
      series: [{
        data: [
          {
            value: newData, // 只更新 value，其他样式保持不变
            name: '实时监测值'
          }
        ]
      }]
    });
  }, 3000);
};
</script>

<style scoped>
/* 保持和其他组件一致的玻璃拟态 */
.chart-panel {
  width: 280px;
  background: rgba(255, 255, 255, 0.75); 
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  padding: 20px;
  transition: transform 0.3s;
}

.chart-panel:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
}

.title { 
  font-size: 15px; 
  font-weight: 700; 
  color: #2c3e50; 
  margin-bottom: 5px; 
  display: flex; 
  align-items: center; 
  gap: 8px;
  padding-left: 8px;
  border-left: 4px solid #409EFF;
}

.chart-box { width: 100%; height: 200px; }

/* 绿色的呼吸灯小圆点 */
.dot {
  width: 8px;
  height: 8px;
  background-color: #67c23a;
  border-radius: 50%;
  display: inline-block;
  box-shadow: 0 0 5px #67c23a;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.95); opacity: 0.7; }
  50% { transform: scale(1.2); opacity: 1; box-shadow: 0 0 10px #67c23a; }
  100% { transform: scale(0.95); opacity: 0.7; }
}
</style>