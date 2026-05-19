<script setup lang="ts">
import { ArrowRight, Calendar, Files, House, MagicStick, Memo, Monitor, SwitchButton } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const featureCards = [
  {
    icon: Memo,
    title: '设备护照',
    text: '按房间整理净水器、路由器、吸尘器和数码设备，购买信息、保修和维修历史都在一张卡里。'
  },
  {
    icon: Calendar,
    title: '家庭日历',
    text: '保修即将到期、滤芯需要更换、维修需要跟进，都用日历和提醒统一呈现。'
  },
  {
    icon: Files,
    title: '凭证盒',
    text: '发票、保修卡、说明书、维修单集中归档，文件内容进入对象存储，访问仍由后端鉴权。'
  },
  {
    icon: MagicStick,
    title: 'AI 辅助',
    text: 'AI 帮你提取票据文本、生成故障排查建议和维修总结，但不会自动覆盖核心数据。'
  }
];

const sceneCards = [
  { label: '保修', value: '30 天', text: '可配置提前提醒' },
  { label: '耗材', value: '180 天', text: '周期重新计算' },
  { label: '维修', value: '4 步', text: '状态流转追踪' },
  { label: '安全', value: 'family_id', text: '家庭空间隔离' }
];

const stackItems = ['Spring Boot 3', 'Vue 3', 'MySQL 8', 'Redis 7', 'RustFS', 'Mock AI'];

function goLogin() {
  router.push('/login');
}

function goRegister() {
  router.push('/register');
}
</script>

<template>
  <main class="landing-page">
    <nav class="landing-nav">
      <button class="brand" type="button" @click="router.push('/')">
        <span class="brand-mark"><el-icon><House /></el-icon></span>
        <span>
          <strong>FixLedger</strong>
          <small>家庭设备管家</small>
        </span>
      </button>
      <div class="nav-links" aria-label="首页导航">
        <a href="#features">核心能力</a>
        <a href="#scenes">家庭场景</a>
        <a href="#engineering">工程能力</a>
      </div>
      <div class="nav-actions">
        <el-button text @click="goLogin">登录</el-button>
        <el-button type="primary" round @click="goLogin">体验演示</el-button>
      </div>
    </nav>

    <section class="landing-hero">
      <div class="hero-copy">
        <p class="eyebrow">Household Device Companion</p>
        <h1>把家里的每台设备，都整理成清晰的一本账。</h1>
        <p class="hero-text">
          FixLedger 像一个轻量的家庭设备中枢，帮你记录保修、耗材、维修、凭证和提醒，
          让净水器、路由器、吸尘器这些日常设备都有完整生命周期。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="goLogin">
            进入演示账号
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
          <el-button size="large" round @click="goRegister">创建家庭档案</el-button>
        </div>
        <div class="hero-proof">
          <span>Docker 一键启动</span>
          <span>Mock AI 可离线演示</span>
          <span>家庭空间隔离</span>
        </div>
      </div>

      <div class="hero-device-stage" aria-label="家庭设备状态预览">
        <div class="stage-orbit" />
        <article class="device-panel main-panel">
          <div class="panel-top">
            <span class="device-dot" />
            <strong>小米净水器 S1</strong>
            <el-tag type="success" effect="light">正常</el-tag>
          </div>
          <div class="panel-score">
            <span>设备安心指数</span>
            <strong>92</strong>
          </div>
          <div class="panel-progress">
            <span style="width: 76%" />
          </div>
          <p>PP 棉滤芯下次提醒：2026-07-19</p>
        </article>
        <article class="float-card warranty-card">
          <el-icon><Calendar /></el-icon>
          <strong>保修提醒</strong>
          <span>路由器 45 天内到期</span>
        </article>
        <article class="float-card file-card">
          <el-icon><Files /></el-icon>
          <strong>凭证已归档</strong>
          <span>发票 / 保修卡 / 维修单</span>
        </article>
      </div>
    </section>

    <section id="features" class="feature-section">
      <div class="section-heading">
        <p class="eyebrow">Core Features</p>
        <h2>不是普通后台，是按家庭设备生命周期组织。</h2>
      </div>
      <div class="feature-grid">
        <article v-for="item in featureCards" :key="item.title" class="feature-card">
          <div class="feature-icon"><el-icon><component :is="item.icon" /></el-icon></div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.text }}</p>
        </article>
      </div>
    </section>

    <section id="scenes" class="scene-section">
      <div class="scene-copy">
        <p class="eyebrow">Family Scenes</p>
        <h2>从购买到维修，每件设备小事都有去处。</h2>
        <p>
          设备档案记录基础信息，保修和耗材生成提醒，维修记录保留过程和费用，凭证盒收纳发票说明书。
          AI 只负责辅助总结，不替用户做核心决定。
        </p>
      </div>
      <div class="scene-grid">
        <article v-for="item in sceneCards" :key="item.label" class="scene-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.text }}</small>
        </article>
      </div>
    </section>

    <section id="engineering" class="engineering-section">
      <div>
        <p class="eyebrow">Engineering Ready</p>
        <h2>演示能跑，架构也能被追问。</h2>
        <p>
          后端使用 Spring Boot + MyBatis Plus，Redis 做提醒去重和 JWT 黑名单，RustFS 保存附件内容，
          Docker Compose 一条命令启动完整环境。
        </p>
      </div>
      <div class="stack-list">
        <span v-for="item in stackItems" :key="item">{{ item }}</span>
      </div>
    </section>

    <section class="final-cta">
      <el-icon><Monitor /></el-icon>
      <h2>现在进入演示家庭，看看设备护照和家庭日历。</h2>
      <p>演示账号：demo / fixledger123。登录页提供一键填入按钮，不会把演示密码默认暴露在表单里。</p>
      <el-button type="primary" size="large" round @click="goLogin">
        开始体验
        <el-icon class="el-icon--right"><SwitchButton /></el-icon>
      </el-button>
    </section>
  </main>
</template>

<style scoped>
.landing-page {
  min-height: 100vh;
  padding: 24px clamp(18px, 4vw, 56px) 56px;
  background:
    radial-gradient(circle at 12% 6%, rgba(255, 196, 122, 0.42), transparent 28%),
    radial-gradient(circle at 84% 10%, rgba(255, 255, 255, 0.88), transparent 24%),
    linear-gradient(145deg, #f6f2ea 0%, #fbfaf5 52%, #edf1ea 100%);
  color: var(--fl-ink);
}

.landing-nav {
  position: sticky;
  top: 18px;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  max-width: 1220px;
  margin: 0 auto;
  padding: 12px 14px;
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 999px;
  background: rgba(255, 253, 248, 0.78);
  box-shadow: 0 16px 40px rgba(88, 72, 49, 0.08);
  backdrop-filter: blur(18px);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 0;
  border: none;
  background: transparent;
  color: inherit;
  cursor: pointer;
  text-align: left;
}

.brand-mark {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 16px;
  background: linear-gradient(145deg, var(--fl-mi-orange), #ffbd63);
  color: #fff;
  box-shadow: 0 12px 28px rgba(255, 138, 31, 0.28);
}

.brand strong,
.brand small {
  display: block;
}

.brand strong {
  font-weight: 950;
  letter-spacing: -0.04em;
}

.brand small,
.nav-links a,
.hero-proof span,
.scene-card small,
.final-cta p,
.engineering-section p,
.scene-copy p,
.feature-card p,
.hero-text {
  color: var(--fl-muted);
}

.nav-links {
  display: flex;
  gap: 28px;
}

.nav-links a {
  font-size: 14px;
  font-weight: 800;
  text-decoration: none;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.landing-hero {
  display: grid;
  max-width: 1220px;
  min-height: 690px;
  grid-template-columns: minmax(0, 1fr) minmax(420px, 0.82fr);
  gap: clamp(32px, 6vw, 72px);
  align-items: center;
  margin: 38px auto 0;
}

.eyebrow {
  margin: 0 0 12px;
  color: var(--fl-mi-orange-dark);
  font-size: 12px;
  font-weight: 950;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.hero-copy h1 {
  max-width: 780px;
  margin: 0;
  color: var(--fl-ink);
  font-size: clamp(48px, 7vw, 96px);
  font-weight: 950;
  letter-spacing: -0.09em;
  line-height: 0.96;
}

.hero-text {
  max-width: 660px;
  margin: 24px 0 0;
  font-size: 18px;
  line-height: 1.9;
}

.hero-actions,
.hero-proof {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 28px;
}

.hero-proof span {
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.62);
  font-size: 13px;
  font-weight: 800;
}

.hero-device-stage {
  position: relative;
  min-height: 520px;
}

.stage-orbit {
  position: absolute;
  inset: 44px 16px 28px 28px;
  border-radius: 54px;
  background:
    radial-gradient(circle at 28% 20%, rgba(255, 255, 255, 0.88), transparent 30%),
    linear-gradient(145deg, #fffaf2, #f2e7d6);
  box-shadow: 0 34px 88px rgba(88, 72, 49, 0.14);
  transform: rotate(-4deg);
}

.device-panel,
.float-card {
  position: absolute;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.78);
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 24px 58px rgba(88, 72, 49, 0.12);
  backdrop-filter: blur(18px);
}

.main-panel {
  top: 92px;
  right: 42px;
  left: 28px;
  padding: 24px;
  border-radius: 34px;
}

.panel-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.device-dot {
  width: 14px;
  height: 14px;
  border-radius: 999px;
  background: var(--fl-green);
  box-shadow: 0 0 0 8px rgba(77, 143, 115, 0.12);
}

.panel-top strong {
  margin-right: auto;
  font-size: 18px;
  font-weight: 950;
}

.panel-score {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: 46px;
}

.panel-score span {
  color: var(--fl-muted);
  font-weight: 800;
}

.panel-score strong {
  color: var(--fl-ink);
  font-size: 92px;
  font-weight: 950;
  letter-spacing: -0.1em;
  line-height: 0.8;
}

.panel-progress {
  overflow: hidden;
  height: 12px;
  margin-top: 22px;
  border-radius: 999px;
  background: rgba(39, 46, 42, 0.08);
}

.panel-progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--fl-mi-orange), #ffd18a);
}

.main-panel p {
  margin: 18px 0 0;
  color: var(--fl-muted);
  font-weight: 800;
}

.float-card {
  display: grid;
  gap: 6px;
  width: 210px;
  padding: 18px;
  border-radius: 26px;
}

.float-card .el-icon {
  color: var(--fl-mi-orange);
  font-size: 28px;
}

.float-card strong {
  font-weight: 950;
}

.float-card span {
  color: var(--fl-muted);
  font-size: 13px;
  line-height: 1.5;
}

.warranty-card {
  top: 24px;
  right: 0;
}

.file-card {
  bottom: 34px;
  left: 0;
}

.feature-section,
.scene-section,
.engineering-section,
.final-cta {
  max-width: 1220px;
  margin: 0 auto;
}

.section-heading {
  max-width: 760px;
  margin-bottom: 26px;
}

.section-heading h2,
.scene-copy h2,
.engineering-section h2,
.final-cta h2 {
  margin: 0;
  color: var(--fl-ink);
  font-size: clamp(32px, 4vw, 56px);
  font-weight: 950;
  letter-spacing: -0.07em;
  line-height: 1.05;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.feature-card,
.scene-card,
.engineering-section,
.final-cta {
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 34px;
  background: rgba(255, 255, 255, 0.66);
  box-shadow: 0 18px 42px rgba(88, 72, 49, 0.08);
}

.feature-card {
  min-height: 270px;
  padding: 22px;
}

.feature-icon {
  display: grid;
  width: 54px;
  height: 54px;
  place-items: center;
  border-radius: 20px;
  background: rgba(255, 138, 31, 0.11);
  color: var(--fl-mi-orange-dark);
  font-size: 26px;
}

.feature-card h3 {
  margin: 24px 0 10px;
  font-size: 22px;
  font-weight: 950;
  letter-spacing: -0.04em;
}

.feature-card p,
.scene-copy p,
.engineering-section p {
  line-height: 1.8;
}

.scene-section {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(420px, 1fr);
  gap: 28px;
  align-items: center;
  padding: 92px 0 48px;
}

.scene-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.scene-card {
  display: grid;
  gap: 10px;
  min-height: 170px;
  padding: 22px;
}

.scene-card span {
  color: var(--fl-mi-orange-dark);
  font-weight: 950;
}

.scene-card strong {
  font-size: 42px;
  font-weight: 950;
  letter-spacing: -0.08em;
}

.engineering-section {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(360px, 0.72fr);
  gap: 26px;
  align-items: center;
  padding: 34px;
}

.stack-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.stack-list span {
  padding: 10px 13px;
  border-radius: 999px;
  background: rgba(255, 244, 229, 0.9);
  color: var(--fl-mi-orange-dark);
  font-weight: 900;
}

.final-cta {
  display: grid;
  justify-items: center;
  margin-top: 48px;
  padding: 54px 24px;
  text-align: center;
}

.final-cta .el-icon {
  color: var(--fl-mi-orange);
  font-size: 42px;
}

.final-cta p {
  max-width: 680px;
  line-height: 1.8;
}

@media (max-width: 1040px) {
  .landing-hero,
  .scene-section,
  .engineering-section {
    grid-template-columns: 1fr;
  }

  .feature-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hero-device-stage {
    min-height: 460px;
  }
}

@media (max-width: 720px) {
  .landing-page {
    padding: 14px 14px 36px;
  }

  .landing-nav {
    position: relative;
    top: auto;
    align-items: flex-start;
    border-radius: 26px;
    flex-direction: column;
  }

  .nav-links {
    flex-wrap: wrap;
    gap: 14px;
  }

  .landing-hero {
    min-height: auto;
    margin-top: 28px;
  }

  .hero-copy h1 {
    font-size: 46px;
  }

  .feature-grid,
  .scene-grid {
    grid-template-columns: 1fr;
  }

  .hero-device-stage {
    min-height: 440px;
  }

  .main-panel {
    right: 0;
    left: 0;
  }

  .float-card {
    width: 180px;
  }
}
</style>
