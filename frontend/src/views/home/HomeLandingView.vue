<script setup lang="ts">
import { ArrowRight, Calendar, Files, House, Memo, Monitor, Setting } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const landingStats = [
  { label: '设备档案', value: '24' },
  { label: '今日事项', value: '5' },
  { label: '凭证完整度', value: '82%' }
];

const previewItems = [
  { title: '净水器滤芯', meta: '还剩 12 天更换', tone: 'warning' },
  { title: '路由器保修', meta: '2026-09-18 到期', tone: 'info' },
  { title: '吸尘器维修单', meta: '已归档 3 份凭证', tone: 'success' }
];

const featureTiles = [
  { icon: Memo, title: '设备档案', text: '购买日期、位置、序列号、保修状态集中记录。' },
  { icon: Calendar, title: '提醒日历', text: '保修、耗材、维修跟进按时间自动聚合。' },
  { icon: Files, title: '凭证盒', text: '发票、说明书、维修单和保修卡按设备归档。' }
];

function goLogin() {
  router.push('/login');
}

function goRegister() {
  router.push('/register');
}
</script>

<template>
  <main class="home-page">
    <header class="site-header" aria-label="公开首页导航">
      <button class="brand" type="button" aria-label="FixLedger 首页" @click="router.push('/')">
        <span class="brand-mark"><el-icon><House /></el-icon></span>
        <span class="brand-name">FixLedger</span>
      </button>

      <nav class="site-nav" aria-label="产品能力">
        <a href="#features">设备档案</a>
        <a href="#features">提醒日历</a>
        <a href="#features">凭证盒</a>
      </nav>

      <div class="header-actions">
        <el-button text @click="goLogin">登录</el-button>
        <el-button type="primary" @click="goLogin">进入演示</el-button>
      </div>
    </header>

    <section class="intro-section" aria-labelledby="landing-title">
      <div class="intro-copy">
        <p class="eyebrow">家庭设备生命周期管理</p>
        <h1 id="landing-title">设备记录，一目了然。</h1>
        <p class="intro-text">
          FixLedger 帮你整理家电、数码和网络设备的保修、耗材、维修与凭证，让日常设备有完整生命周期。
        </p>
        <div class="intro-actions">
          <el-button type="primary" size="large" @click="goLogin">
            进入演示账号
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
          <el-button size="large" @click="goRegister">创建家庭档案</el-button>
        </div>
        <p class="demo-note">演示账号：demo / fixledger123</p>
      </div>

      <div class="product-scene" aria-label="FixLedger 产品界面预览">
        <div class="device-board">
          <div class="board-topbar">
            <div>
              <span>我的家</span>
              <strong>今天要处理 5 件事</strong>
            </div>
            <span class="status-dot" aria-hidden="true"></span>
          </div>

          <div class="stats-row">
            <div v-for="item in landingStats" :key="item.label" class="stat-pill">
              <strong>{{ item.value }}</strong>
              <span>{{ item.label }}</span>
            </div>
          </div>

          <div class="device-preview">
            <div class="device-icon"><el-icon><Monitor /></el-icon></div>
            <div>
              <strong>客厅设备</strong>
              <p>路由器、电视、音箱和空气净化器</p>
            </div>
            <span>8 台</span>
          </div>

          <div class="preview-list">
            <article v-for="item in previewItems" :key="item.title" :class="`is-${item.tone}`">
              <span class="item-line" aria-hidden="true"></span>
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.meta }}</p>
              </div>
            </article>
          </div>
        </div>

        <div class="floating-card warranty-card">
          <el-icon><Setting /></el-icon>
          <div>
            <strong>维修状态</strong>
            <span>2 台设备跟进中</span>
          </div>
        </div>
        <div class="floating-card file-card">
          <el-icon><Files /></el-icon>
          <div>
            <strong>凭证盒</strong>
            <span>56 份资料已归档</span>
          </div>
        </div>
      </div>
    </section>

    <section id="features" class="feature-section" aria-label="核心能力">
      <article v-for="item in featureTiles" :key="item.title" class="feature-tile">
        <el-icon><component :is="item.icon" /></el-icon>
        <h2>{{ item.title }}</h2>
        <p>{{ item.text }}</p>
      </article>
    </section>
  </main>
</template>

<style scoped>
.home-page {
  position: relative;
  min-height: 100dvh;
  overflow: hidden;
  --home-accent: #ff6900;
  --home-accent-soft: rgba(255, 105, 0, 0.12);
  --home-info: #3aa6b9;
  --home-text: #1d1d1f;
  --home-muted: rgba(29, 29, 31, 0.62);
  --home-line: rgba(17, 24, 39, 0.08);
  --home-surface: rgba(255, 255, 255, 0.78);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(246, 248, 252, 0.86)),
    linear-gradient(180deg, #fbfbfb 0%, #f3f5f9 100%);
  color: var(--home-text);
}

.home-page::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.74), rgba(255, 255, 255, 0.16) 42%, rgba(255, 255, 255, 0.58)),
    linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.62), transparent);
  content: '';
}

.site-header,
.intro-section,
.feature-section {
  position: relative;
  z-index: 1;
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 76px;
  gap: 24px;
  border-bottom: 1px solid rgba(17, 24, 39, 0.06);
  background: rgba(251, 251, 252, 0.78);
  backdrop-filter: blur(22px) saturate(180%);
  -webkit-backdrop-filter: blur(22px) saturate(180%);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  transition: opacity 180ms ease;
}

.brand-mark {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: 10px;
  background: var(--home-accent);
  color: #fff;
  font-size: 18px;
}

.brand-name {
  font-size: 17px;
  font-weight: 800;
}

.site-nav {
  display: flex;
  align-items: center;
  gap: 30px;
}

.site-nav a {
  color: rgba(29, 29, 31, 0.68);
  font-size: 14px;
  font-weight: 650;
  text-decoration: none;
  transition: color 180ms ease;
}

.brand:hover,
.site-nav a:hover {
  color: var(--home-text);
  opacity: 0.86;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.intro-section {
  display: grid;
  grid-template-columns: minmax(0, 0.92fr) minmax(460px, 1.08fr);
  align-items: center;
  gap: clamp(32px, 5vw, 76px);
  min-height: clamp(520px, calc(100dvh - 176px), 660px);
  padding: 34px 0 44px;
}

.intro-copy {
  max-width: 520px;
}

.eyebrow {
  margin: 0 0 18px;
  color: var(--home-accent);
  font-size: 13px;
  font-weight: 780;
  letter-spacing: 0.08em;
}

.intro-copy h1 {
  margin: 0;
  color: #151517;
  font-size: clamp(34px, 3.5vw, 48px);
  font-weight: 740;
  letter-spacing: 0;
  line-height: 1.08;
}

.intro-text {
  margin: 24px 0 0;
  color: var(--home-muted);
  font-size: 18px;
  line-height: 1.75;
}

.intro-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 34px;
}

.intro-actions :deep(.el-button) {
  min-width: 148px;
  padding-inline: 24px;
}

.intro-actions :deep(.el-button--primary),
.header-actions :deep(.el-button--primary) {
  border-color: var(--home-accent);
  background: var(--home-accent);
  box-shadow: 0 12px 26px rgba(255, 105, 0, 0.18), inset 0 1px 0 rgba(255, 255, 255, 0.36);
}

.demo-note {
  margin: 18px 0 0;
  color: rgba(29, 29, 31, 0.5);
  font-size: 14px;
  font-weight: 650;
}

.product-scene {
  position: relative;
  min-height: 520px;
}

.device-board {
  position: relative;
  z-index: 1;
  width: min(100%, 540px);
  margin: 28px 0 0 auto;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 30px;
  background: var(--home-surface);
  box-shadow: 0 26px 70px rgba(31, 41, 55, 0.11), inset 0 1px 0 rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(28px) saturate(180%);
  -webkit-backdrop-filter: blur(28px) saturate(180%);
}

.board-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.board-topbar span,
.stat-pill span,
.device-preview p,
.preview-list p,
.floating-card span {
  color: rgba(29, 29, 31, 0.56);
}

.board-topbar strong {
  display: block;
  margin-top: 4px;
  color: var(--home-text);
  font-size: 21px;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  background: var(--home-accent);
  box-shadow: 0 0 0 8px var(--home-accent-soft);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 22px;
}

.stat-pill {
  padding: 16px 14px;
  border: 1px solid var(--home-line);
  border-radius: 18px;
  background: rgba(247, 248, 251, 0.82);
}

.stat-pill strong,
.stat-pill span {
  display: block;
}

.stat-pill strong {
  color: #111827;
  font-size: 26px;
  line-height: 1;
}

.stat-pill span {
  margin-top: 8px;
  font-size: 12px;
  font-weight: 700;
}

.device-preview {
  display: grid;
  grid-template-columns: 54px minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  margin-top: 16px;
  padding: 18px;
  border: 1px solid rgba(17, 24, 39, 0.08);
  border-radius: 22px;
  background: #fff;
  color: var(--home-text);
}

.device-preview p {
  margin: 4px 0 0;
  color: rgba(29, 29, 31, 0.55);
  font-size: 13px;
}

.device-preview > span {
  font-size: 20px;
  font-weight: 800;
}

.device-icon {
  display: grid;
  width: 54px;
  height: 54px;
  place-items: center;
  border-radius: 16px;
  background: linear-gradient(135deg, #ff7a1a, #ffb066);
  color: #fff;
  font-size: 24px;
}

.preview-list {
  display: grid;
  gap: 10px;
  margin-top: 16px;
}

.preview-list article {
  display: grid;
  grid-template-columns: 5px minmax(0, 1fr);
  gap: 12px;
  padding: 15px 16px;
  border: 1px solid rgba(17, 24, 39, 0.06);
  border-radius: 18px;
  background: rgba(247, 248, 251, 0.84);
}

.item-line {
  width: 5px;
  border-radius: 999px;
  background: var(--home-info);
}

.preview-list article.is-warning .item-line {
  background: #ff9500;
}

.preview-list article.is-success .item-line {
  background: #34c759;
}

.preview-list strong {
  color: #1d1d1f;
}

.preview-list p {
  margin: 4px 0 0;
  font-size: 13px;
}

.floating-card {
  position: absolute;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 206px;
  padding: 14px 16px;
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 18px 46px rgba(31, 41, 55, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(20px) saturate(170%);
  -webkit-backdrop-filter: blur(20px) saturate(170%);
}

.floating-card .el-icon {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 12px;
  background: var(--home-accent-soft);
  color: var(--home-accent);
}

.floating-card strong,
.floating-card span {
  display: block;
}

.floating-card span {
  margin-top: 2px;
  font-size: 12px;
  font-weight: 700;
}

.warranty-card {
  top: 0;
  left: 4px;
}

.file-card {
  right: 0;
  bottom: 44px;
}

.feature-section {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-top: -34px;
  padding-bottom: 58px;
}

.feature-tile {
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.84);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 14px 42px rgba(31, 41, 55, 0.07), inset 0 1px 0 rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
}

.feature-tile .el-icon {
  color: var(--home-accent);
  font-size: 26px;
}

.feature-tile h2 {
  margin: 16px 0 0;
  color: #1d1d1f;
  font-size: 20px;
}

.feature-tile p {
  margin: 10px 0 0;
  color: rgba(29, 29, 31, 0.58);
  line-height: 1.7;
}

@media (max-width: 980px) {
  .site-nav {
    display: none;
  }

  .intro-section {
    grid-template-columns: 1fr;
    min-height: auto;
    padding-top: 28px;
  }

  .intro-copy {
    max-width: 680px;
  }

  .product-scene {
    min-height: 500px;
  }

  .device-board {
    margin-inline: auto;
  }

  .feature-section {
    margin-top: 0;
  }
}

@media (max-width: 720px) {
  .site-header,
  .intro-section,
  .feature-section {
    width: min(100% - 28px, 1180px);
  }

  .site-header {
    min-height: 68px;
  }

  .header-actions :deep(.el-button:first-child) {
    display: none;
  }

  .intro-copy h1 {
    font-size: clamp(30px, 8.5vw, 38px);
  }

  .intro-text {
    font-size: 16px;
  }

  .product-scene {
    min-height: auto;
    padding-bottom: 24px;
  }

  .device-board {
    padding: 18px;
    border-radius: 28px;
  }

  .feature-section {
    grid-template-columns: 1fr;
  }

  .stats-row {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 8px;
  }

  .stat-pill {
    padding: 12px 10px;
    border-radius: 14px;
  }

  .stat-pill strong {
    font-size: 22px;
  }

  .stat-pill span {
    font-size: 11px;
  }

  .preview-list {
    display: none;
  }

  .floating-card {
    display: none;
  }

  .feature-section {
    gap: 12px;
    padding-bottom: 28px;
  }
}
</style>
