<template>
    <transition name="fade">
        <div v-if="visible" class="about-overlay">
            <div class="close-btn" @click="close">
                <span>×</span>
            </div>

            <div class="about-content">
                <div class="header-section">
                    <div class="logo-icon">🌏</div>
                    <h1 class="gradient-text">全国空气质量监测平台</h1>
                    <p class="subtitle">让呼吸的数据看得见 · 让环境的未来更清晰</p>
                </div>

                <div class="intro-card glass-card">
                    <h3>🚀 项目愿景</h3>
                    <p>
                        本项目基于大数据分析技术，致力于构建一个<strong>实时、精准、可视化</strong>的空气质量监测系统。
                        通过对海量监测数据的深度挖掘，我们以直观的地图热力、动态图表和多维分析模型，
                        帮助用户快速掌握全国环境状况，为环境保护决策提供数据支撑。
                        由于本站没有资金，数据来源均是<strong>假数据</strong>，仅供学习交流使用，请勿用于商业用途。
                    </p>
                </div>

                <div class="tech-stack-section">
                    <h3>🛠️ 技术架构</h3>
                    <div class="stack-grid">
                        <div class="stack-item glass-card">
                            <div class="icon">☕</div>
                            <div class="info">
                                <h4>后端技术</h4>
                                <span>Java Spring Boot</span>
                                <span>MyBatis Plus</span>
                            </div>
                        </div>
                        <div class="stack-item glass-card">
                            <div class="icon">⚡</div>
                            <div class="info">
                                <h4>前端技术</h4>
                                <span>Vue 3.0 + Vite</span>
                                <span>Axios + CSS3</span>
                            </div>
                        </div>
                        <div class="stack-item glass-card">
                            <div class="icon">📊</div>
                            <div class="info">
                                <h4>可视化</h4>
                                <span>Apache ECharts 5</span>
                                <span>GeoJSON Maps</span>
                            </div>
                        </div>
                        <div class="stack-item glass-card">
                            <div class="icon">🎨</div>
                            <div class="info">
                                <h4>用户界面设计</h4>
                                <span>Glassmorphism</span>
                                <span>Responsive Layout</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="contact-section">
                    <h3>📬 联系方式</h3>
                    <div class="contact-grid">
                        <div class="contact-item glass-card">
                            <div class="icon">📧</div>
                            <div class="info">
                                <h4>Email</h4>
                                <span>1947503577@qq.com</span>
                            </div>
                        </div>

                        <div class="contact-item glass-card">
                            <div class="icon">🐙</div>
                            <div class="info">
                                <h4>WeChat</h4>
                                <span>qiu9426248946</span>
                            </div>
                        </div>
                    </div>
                    
                </div>
                <div class="footer-section">
                    <p>Designed & Developed by <strong>阿湫不看月亮</strong></p>
                    <p class="copyright">© 2026 Big Data Analysis System. All Rights Reserved.</p>
                </div>
            </div>
        </div>
    </transition>
</template>

<script setup>
defineProps(['visible']);
const emit = defineEmits(['update:visible']);

const close = () => {
    emit('update:visible', false);
};
</script>

<style scoped>
/* 全屏遮罩：深色模糊背景 */
.about-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(12, 22, 36, 0.85);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    z-index: 9999;
    /* 最高层级 */
    display: flex;
    justify-content: center;
    align-items: center;
    overflow-y: auto;
    /* 防止内容过长无法滚动 */
}

/* 内容容器 */
.about-content {
  width: 800px;
  max-width: 90%;
  text-align: center;
  color: white;
  padding-top: 100px; /* 【新增】顶部增加 40px 的呼吸空间 */
  animation: slideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

/* 关闭按钮 */
.close-btn {
    position: absolute;
    top: 40px;
    right: 40px;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid rgba(255, 255, 255, 0.1);
}

.close-btn span {
    font-size: 30px;
    color: rgba(255, 255, 255, 0.8);
    line-height: 1;
}

.close-btn:hover {
    background: rgba(255, 64, 64, 0.8);
    transform: rotate(90deg);
    border-color: transparent;
}

.close-btn:hover span {
    color: white;
}

/* 头部区域 */
.logo-icon { 
  font-size: 60px; 
  margin-bottom: 15px; 
  margin-top: 20px; /* 【新增】头部多给点空隙，让它沉下来 */
  filter: drop-shadow(0 0 20px rgba(64,158,255,0.6)); 
  display: inline-block; /* 确保 margin 生效 */
}

.gradient-text {
    font-size: 36px;
    font-weight: 800;
    margin: 0;
    background: linear-gradient(135deg, #fff 0%, #409EFF 100%);
    -webkit-background-clip: text;
    background-clip: text;
    /* ✅ 补上这一行标准属性，警告就会消失 */
    -webkit-text-fill-color: transparent;
    letter-spacing: 2px;
}

.subtitle {
    color: rgba(255, 255, 255, 0.6);
    letter-spacing: 4px;
    margin-top: 10px;
    font-size: 14px;
    text-transform: uppercase;
}

/* 通用毛玻璃卡片 */
.glass-card {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 16px;
    padding: 30px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

/* 项目简介 */
.intro-card {
    margin: 40px auto;
    text-align: left;
    max-width: 700px;
}

.intro-card h3 {
    color: #409EFF;
    margin-top: 0;
    display: flex;
    align-items: center;
    gap: 10px;
}

.intro-card p {
    line-height: 1.8;
    color: rgba(255, 255, 255, 0.8);
    margin: 0;
    text-align: justify;
}

/* 技术栈网格 */
.tech-stack-section {
    margin-bottom: 40px;
}

.tech-stack-section h3 {
    color: rgba(255, 255, 255, 0.5);
    margin-bottom: 20px;
    font-size: 16px;
    letter-spacing: 1px;
}

.stack-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    max-width: 700px;
    margin: 0 auto;
}

.stack-item {
    display: flex;
    align-items: center;
    gap: 20px;
    text-align: left;
    transition: transform 0.3s;
}

.stack-item:hover {
    transform: translateY(-5px);
    background: rgba(255, 255, 255, 0.1);
    border-color: #409EFF;
}

.stack-item .icon {
    font-size: 32px;
    width: 50px;
    height: 50px;
    background: rgba(0, 0, 0, 0.2);
    border-radius: 12px;
    display: flex;
    justify-content: center;
    align-items: center;
}

.stack-item h4 {
    margin: 0 0 5px 0;
    color: #fff;
    font-size: 16px;
}

.stack-item span {
    display: block;
    font-size: 12px;
    color: rgba(255, 255, 255, 0.5);
}

/* 底部版权 */
.footer-section {
    color: rgba(255, 255, 255, 0.4);
    font-size: 12px;
    margin-top: 50px;
    border-top: 1px solid rgba(255, 255, 255, 0.05);
    padding-top: 20px;
}

.copyright {
    font-family: monospace;
    opacity: 0.6;
}

/* 动画 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.4s;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

@keyframes slideUp {
    from {
        transform: translateY(50px);
        opacity: 0;
    }

    to {
        transform: translateY(0);
        opacity: 1;
    }
}
/* --- 新增：联系方式样式 --- */
.contact-section { margin-bottom: 30px; }
.contact-section h3 { color: rgba(255,255,255,0.5); margin-bottom: 20px; font-size: 16px; letter-spacing: 1px; }

.contact-grid { 
  display: flex; 
  gap: 20px; 
  justify-content: center; /* 居中显示 */
  max-width: 600px; 
  margin: 0 auto; 
}

.contact-item { 
  flex: 1; 
  display: flex; 
  align-items: center; 
  gap: 15px; 
  padding: 20px; 
  transition: transform 0.3s, border-color 0.3s;
  cursor: pointer;
}

.contact-item:hover { 
  transform: translateY(-3px); 
  background: rgba(255,255,255,0.15); 
  border-color: #67C23A; /* 悬停变绿色，表示连通 */
}

.contact-item .icon { font-size: 24px; }

.contact-item .info { text-align: left; }
.contact-item h4 { margin: 0 0 2px 0; color: #fff; font-size: 14px; }
.contact-item span { font-size: 12px; color: rgba(255,255,255,0.6); font-family: monospace; }
</style>