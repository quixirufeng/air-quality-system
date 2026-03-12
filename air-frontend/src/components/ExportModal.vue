<template>
  <div v-if="visible" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <h3>📥 高级导出选项</h3>
        <span class="close-btn" @click="close">×</span>
      </div>

      <div class="modal-body">
        <div class="form-item">
          <label>📅 时间范围</label>
          <div class="date-range-box">
            <input type="date" v-model="filters.startDate" class="date-input" />
            <span class="separator">至</span>
            <input type="date" v-model="filters.endDate" class="date-input" />
          </div>
        </div>

        <div class="form-item">
          <label>
            🏙️ 选择城市 
            <span class="select-all" @click="toggleSelectAll">
              {{ isAllSelected ? '取消全选' : '全选' }}
            </span>
          </label>
          <div class="city-grid">
            <label 
              v-for="city in uniqueCities" 
              :key="city" 
              class="city-checkbox"
              :class="{ active: filters.selectedCities.includes(city) }"
            >
              <input type="checkbox" :value="city" v-model="filters.selectedCities" hidden>
              <span class="check-mark"></span>
              {{ city }}
            </label>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="cancel-btn" @click="close">取消</button>
        <button class="confirm-btn" @click="handleExport">
          确认导出 ({{ filters.selectedCities.length }})
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, watch } from 'vue';

const props = defineProps(['visible', 'dataList']);
const emit = defineEmits(['update:visible']);

const filters = reactive({
  startDate: '',
  endDate: '',
  selectedCities: []
});

// 从父组件的数据中提取所有不重复的城市名
const uniqueCities = computed(() => {
  if (!props.dataList) return [];
  const cities = props.dataList.map(item => item.cityName);
  return [...new Set(cities)]; // 去重
});

// 全选/反选逻辑
const isAllSelected = computed(() => {
  return uniqueCities.value.length > 0 && filters.selectedCities.length === uniqueCities.value.length;
});

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    filters.selectedCities = [];
  } else {
    filters.selectedCities = [...uniqueCities.value];
  }
};

// 监听弹窗打开，初始化默认选中所有城市
watch(() => props.visible, (newVal) => {
  if (newVal && filters.selectedCities.length === 0) {
    filters.selectedCities = [...uniqueCities.value];
  }
});

const close = () => {
  emit('update:visible', false);
};

const handleExport = () => {
  // 构建 URL 参数
  //let url = 'http://localhost:8080/api/air/export?';
  let url = '/api/air/export?';
  
  // 1. 拼接城市 (cityNames=北京&cityNames=上海...)
  filters.selectedCities.forEach(city => {
    url += `cityNames=${encodeURIComponent(city)}&`;
  });

  // 2. 拼接时间
  if (filters.startDate) url += `startDate=${filters.startDate}&`;
  if (filters.endDate) url += `endDate=${filters.endDate}&`;

  // 3. 触发下载
  window.open(url);
  close();
};
</script>

<style scoped>
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5); backdrop-filter: blur(5px);
  z-index: 1000; display: flex; justify-content: center; align-items: center;
}
.modal-content {
  background: white; width: 500px; border-radius: 16px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.2); overflow: hidden;
  animation: popIn 0.3s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}
@keyframes popIn { from { transform: scale(0.9); opacity: 0; } to { transform: scale(1); opacity: 1; } }

.modal-header {
  padding: 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(135deg, #f8faff 0%, #eef5fe 100%);
}
.modal-header h3 { margin: 0; color: #2c3e50; font-size: 18px; }
.close-btn { font-size: 24px; cursor: pointer; color: #999; }
.close-btn:hover { color: #333; }

.modal-body { padding: 25px; }
.form-item { margin-bottom: 25px; }
.form-item label { display: block; margin-bottom: 10px; font-weight: 600; color: #606266; display: flex; justify-content: space-between; }

/* 日期选择器 */
.date-range-box { display: flex; align-items: center; gap: 10px; }
.date-input { flex: 1; padding: 10px; border: 1px solid #dcdfe6; border-radius: 8px; font-family: inherit; }
.separator { color: #909399; }

/* 城市选择网格 */
.select-all { color: #409EFF; cursor: pointer; font-size: 12px; }
.city-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; max-height: 150px; overflow-y: auto; padding-right: 5px; }
.city-checkbox {
  display: flex; align-items: center; gap: 6px; padding: 8px;
  border: 1px solid #ebeef5; border-radius: 6px; cursor: pointer; font-size: 13px; color: #606266;
  transition: all 0.2s;
}
.city-checkbox:hover { border-color: #409EFF; color: #409EFF; }
.city-checkbox.active { background: #ecf5ff; border-color: #409EFF; color: #409EFF; font-weight: bold; }
/* 自定义复选框样式 */
.check-mark { width: 14px; height: 14px; border: 2px solid #dcdfe6; border-radius: 4px; position: relative; }
.city-checkbox.active .check-mark { background: #409EFF; border-color: #409EFF; }
.city-checkbox.active .check-mark::after {
  content: ''; position: absolute; left: 4px; top: 1px; width: 4px; height: 8px;
  border: solid white; border-width: 0 2px 2px 0; transform: rotate(45deg);
}

.modal-footer { padding: 20px; background: #fafafa; display: flex; justify-content: flex-end; gap: 12px; }
.cancel-btn { padding: 10px 20px; border: none; background: transparent; color: #606266; cursor: pointer; }
.confirm-btn { 
  padding: 10px 25px; background: #409EFF; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: 600; 
  box-shadow: 0 4px 12px rgba(64,158,255,0.3);
}
.confirm-btn:hover { background: #66b1ff; transform: translateY(-1px); }
</style>