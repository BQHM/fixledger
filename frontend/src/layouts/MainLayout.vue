<script setup lang="ts">
import {
  Calendar,
  Download,
  Files,
  HomeFilled,
  House,
  MagicStick,
  Memo,
  RefreshRight,
  Setting,
  User,
  WarningFilled
} from '@element-plus/icons-vue';
import { computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import {
  applyPwaUpdate,
  installPwa,
  pwaCanInstall,
  pwaIsOnline,
  pwaUpdateAvailable
} from '@/pwa';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const activeMenu = computed(() => {
  if (route.path.startsWith('/calendar') || route.path.startsWith('/reminders')) return '/calendar';
  if (
    route.path.startsWith('/devices') ||
    route.path.startsWith('/warranties') ||
    route.path.startsWith('/consumables') ||
    route.path.startsWith('/maintenance')
  ) {
    return '/devices';
  }
  if (route.path.startsWith('/files')) return '/files';
  if (route.path.startsWith('/ai-tools')) return '/ai-tools';
  if (route.path.startsWith('/settings')) return '/settings/family';
  if (route.path.startsWith('/dashboard') && route.query.focus === 'calendar') return '/calendar';
  return '/dashboard';
});

const primaryMenus = [
  { path: '/dashboard', label: '总览', hint: '设备状态与待办', icon: HomeFilled },
  { path: '/calendar', label: '日历', hint: '提醒排期', icon: Calendar },
  { path: '/devices', label: '设备', hint: '档案与保修', icon: Memo },
  { path: '/files', label: '凭证', hint: '发票与说明书', icon: Files },
  { path: '/ai-tools', label: '辅助', hint: '提取与总结', icon: MagicStick }
];

const mobileMenus = [
  { path: '/dashboard', label: '首页', icon: HomeFilled },
  { path: '/devices', label: '设备', icon: Memo },
  { path: '/calendar', label: '日历', icon: Calendar },
  { path: '/files', label: '凭证', icon: Files },
  { path: '/settings/family', label: '我的', icon: User }
];

const secondaryMenus = [
  { path: '/settings/family', label: '家庭设置', icon: Setting }
];

const currentFamilyName = computed(() => {
  return auth.families.find((family) => family.id === auth.currentFamilyId)?.name ?? '我的家';
});

const pageTitle = computed(() => {
  if (route.path.startsWith('/dashboard') && route.query.focus === 'calendar') return '家庭日历';
  return {
    dashboard: '家庭总览',
    calendar: '家庭日历',
    devices: '设备档案',
    'device-create': '新建设备',
    'device-detail': '设备详情',
    'device-edit': '编辑设备',
    warranties: '保修记录',
    consumables: '耗材管理',
    maintenance: '维修记录',
    'maintenance-detail': '维修详情',
    reminders: '提醒中心',
    files: '凭证归档',
    'ai-tools': '辅助工具',
    'family-settings': '家庭设置'
  }[String(route.name)] ?? '家庭总览';
});

const mobilePageTitle = computed(() => {
  if (route.name === 'dashboard') return '首页';
  if (route.name === 'calendar') return '日历';
  if (route.name === 'devices') return '设备';
  if (route.name === 'family-settings') return '我的';
  return pageTitle.value;
});

onMounted(() => {
  if (auth.token && auth.families.length === 0) {
    auth.loadFamilies();
  }
});

function handleFamilyChange(value: number) {
  auth.setCurrentFamily(value);
  window.dispatchEvent(new CustomEvent('family-changed'));
}

async function handleLogout() {
  await auth.logout();
  router.push('/login');
}

async function handleInstallPwa() {
  await installPwa();
}

function handlePwaUpdate() {
  applyPwaUpdate();
}
</script>

<template>
  <el-container class="app-layout">
    <el-aside class="app-sidebar" width="260px">
      <button class="brand-block" type="button" @click="router.push('/dashboard')">
        <span class="brand-mark"><el-icon><House /></el-icon></span>
        <span>
          <strong>FixLedger</strong>
          <small>家庭设备档案</small>
        </span>
      </button>

      <div class="family-panel">
        <span>当前家庭</span>
        <strong>{{ currentFamilyName }}</strong>
      </div>

      <el-menu :default-active="activeMenu" router class="app-menu">
        <el-menu-item v-for="menu in primaryMenus" :key="menu.label" :index="menu.path" class="scene-menu-item">
          <el-icon><component :is="menu.icon" /></el-icon>
          <span class="menu-copy">
            <strong>{{ menu.label }}</strong>
            <small>{{ menu.hint }}</small>
          </span>
        </el-menu-item>
      </el-menu>

      <div class="secondary-menu">
        <el-menu :default-active="activeMenu" router class="app-menu">
          <el-menu-item v-for="menu in secondaryMenus" :key="menu.path" :index="menu.path">
            <el-icon><component :is="menu.icon" /></el-icon>
            <span>{{ menu.label }}</span>
          </el-menu-item>
        </el-menu>
      </div>
    </el-aside>

    <el-container class="app-workspace">
      <el-header class="app-header">
        <div class="header-copy">
          <div class="header-title desktop-header-title">{{ pageTitle }}</div>
          <div class="header-title mobile-header-title">{{ mobilePageTitle }}</div>
          <div class="header-subtitle">先处理待办，再补齐设备资料。</div>
        </div>
        <div class="header-actions">
          <el-select
            v-model="auth.currentFamilyId"
            class="family-select"
            placeholder="选择家庭"
            @change="handleFamilyChange"
          >
            <el-option
              v-for="family in auth.families"
              :key="family.id"
              :label="family.name"
              :value="family.id"
            />
          </el-select>
          <el-dropdown>
            <el-avatar class="user-avatar">{{ auth.user?.nickname?.[0] || auth.user?.username?.[0] || 'U' }}</el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ auth.user?.username }}</el-dropdown-item>
                <el-dropdown-item @click="router.push('/settings/family')">家庭设置</el-dropdown-item>
                <el-dropdown-item v-if="pwaCanInstall" :icon="Download" @click="handleInstallPwa">
                  安装到设备
                </el-dropdown-item>
                <el-dropdown-item v-if="pwaUpdateAvailable" :icon="RefreshRight" @click="handlePwaUpdate">
                  更新应用
                </el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <div v-if="!pwaIsOnline" class="pwa-offline-bar" role="status">
        <el-icon><WarningFilled /></el-icon>
        <span>当前网络不可用，恢复连接后再重试。</span>
      </div>
      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>

    <nav class="mobile-tabbar" aria-label="主要导航">
      <button
        v-for="menu in mobileMenus"
        :key="menu.path"
        class="mobile-tab"
        :class="{ 'is-active': activeMenu === menu.path }"
        type="button"
        :aria-current="activeMenu === menu.path ? 'page' : undefined"
        @click="router.push(menu.path)"
      >
        <el-icon><component :is="menu.icon" /></el-icon>
        <span>{{ menu.label }}</span>
      </button>
    </nav>
  </el-container>
</template>

<style scoped>
.app-layout {
  min-height: 100dvh;
  background: transparent;
}

.app-workspace {
  min-width: 0;
}

.app-sidebar {
  position: sticky;
  top: 0;
  overflow: hidden;
  isolation: isolate;
  display: flex;
  flex-direction: column;
  height: 100dvh;
  padding: 18px 14px;
  border-right: 1px solid rgba(255, 255, 255, 0.62);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.78), rgba(246, 248, 252, 0.56));
  box-shadow: inset -1px 0 0 rgba(255, 255, 255, 0.46), 18px 0 48px rgba(31, 41, 55, 0.075);
  backdrop-filter: blur(32px) saturate(190%);
  -webkit-backdrop-filter: blur(32px) saturate(190%);
}

.app-sidebar::before {
  position: absolute;
  inset: 0 0 auto;
  z-index: 0;
  height: 38%;
  background:
    linear-gradient(110deg, rgba(255, 255, 255, 0.48), rgba(255, 255, 255, 0)),
    linear-gradient(135deg, rgba(255, 105, 0, 0.08), rgba(255, 255, 255, 0));
  content: '';
  pointer-events: none;
}

.app-sidebar::after {
  position: absolute;
  inset: 0;
  z-index: 0;
  border-right: 1px solid rgba(255, 255, 255, 0.36);
  box-shadow: inset -10px 0 18px -18px rgba(255, 255, 255, 0.9), inset 1px 0 0 rgba(255, 255, 255, 0.62);
  content: '';
  pointer-events: none;
}

.app-sidebar > * {
  position: relative;
  z-index: 1;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 6px 8px 18px;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  text-align: left;
}

.brand-mark {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 12px;
  background: var(--fl-primary);
  color: #fff;
  box-shadow: 0 12px 24px rgba(255, 105, 0, 0.16);
  font-size: 18px;
  backdrop-filter: blur(16px) saturate(170%);
  -webkit-backdrop-filter: blur(16px) saturate(170%);
}

.brand-block strong,
.brand-block small {
  display: block;
}

.brand-block strong {
  color: var(--fl-ink);
  font-size: 18px;
  font-weight: 800;
}

.brand-block small,
.family-panel span,
.header-subtitle {
  color: var(--fl-muted);
  font-size: 12px;
}

.family-panel {
  position: relative;
  overflow: hidden;
  isolation: isolate;
  display: grid;
  gap: 4px;
  margin: 0 6px 14px;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.68);
  border-radius: 16px;
  background: var(--fl-glass-chip);
  box-shadow: var(--fl-liquid-edge), 0 10px 24px rgba(31, 41, 55, 0.045);
  backdrop-filter: blur(18px) saturate(170%);
  -webkit-backdrop-filter: blur(18px) saturate(170%);
}

.family-panel::after {
  position: absolute;
  inset: 0;
  z-index: 0;
  border-radius: inherit;
  background: rgba(255, 255, 255, 0.06);
  box-shadow: inset -8px -7px 0 -10px rgba(255, 255, 255, 0.92), inset 0 -8px 0 -9px rgba(255, 255, 255, 0.76);
  content: '';
  pointer-events: none;
}

.family-panel > * {
  position: relative;
  z-index: 1;
}

.family-panel strong {
  overflow: hidden;
  color: var(--fl-ink);
  font-size: 15px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-menu {
  border-right: none;
  background: transparent;
}

:deep(.el-menu-item) {
  min-height: 48px;
  height: auto;
  margin: 3px 0;
  padding: 8px 10px !important;
  border: 1px solid transparent;
  border-radius: 16px;
  color: var(--fl-text);
  line-height: 1.2;
  transition: background 0.18s var(--fl-ease), border-color 0.18s var(--fl-ease), box-shadow 0.18s var(--fl-ease), transform 0.18s var(--fl-ease);
}

:deep(.el-menu-item:hover) {
  border-color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.5);
  transform: translateY(-1px);
  backdrop-filter: blur(18px) saturate(170%);
  -webkit-backdrop-filter: blur(18px) saturate(170%);
}

:deep(.el-menu-item.is-active) {
  border-color: rgba(255, 255, 255, 0.72);
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.74), rgba(246, 248, 252, 0.42)),
    linear-gradient(135deg, rgba(255, 105, 0, 0.14), rgba(255, 255, 255, 0.14));
  color: var(--fl-primary-strong);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.76), 0 10px 26px rgba(255, 105, 0, 0.1);
}

.scene-menu-item :deep(.el-icon) {
  font-size: 18px;
}

.menu-copy {
  display: grid;
  gap: 3px;
  margin-left: 2px;
}

.menu-copy strong {
  font-size: 14px;
  font-weight: 800;
}

.menu-copy small {
  color: inherit;
  font-size: 11px;
  opacity: 0.7;
}

.secondary-menu {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid var(--fl-line);
}

.app-header {
  position: relative;
  overflow: hidden;
  isolation: isolate;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 68px;
  padding: 0 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.62);
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.72), rgba(246, 248, 252, 0.5));
  box-shadow: 0 12px 34px rgba(31, 41, 55, 0.055);
  backdrop-filter: blur(30px) saturate(190%);
  -webkit-backdrop-filter: blur(30px) saturate(190%);
}

.app-header::before {
  position: absolute;
  inset: 0 0 auto;
  z-index: 0;
  height: 55%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.34), rgba(255, 255, 255, 0));
  content: '';
  pointer-events: none;
}

.app-header::after {
  position: absolute;
  inset: 0;
  z-index: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.32);
  box-shadow: inset 0 -9px 0 -10px rgba(255, 255, 255, 0.72);
  content: '';
  pointer-events: none;
}

.app-header > * {
  position: relative;
  z-index: 1;
}

.header-title {
  color: var(--fl-ink);
  font-size: 18px;
  font-weight: 800;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.family-select {
  width: 190px;
}

.user-avatar {
  cursor: pointer;
  background: linear-gradient(135deg, #ff6900, #ff8f2f);
  color: #fff;
  font-weight: 800;
  box-shadow: 0 12px 28px rgba(255, 105, 0, 0.2);
}

.app-main {
  padding: 22px;
}

.pwa-offline-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  gap: 8px;
  padding: 8px 16px;
  border-bottom: 1px solid rgba(255, 105, 0, 0.16);
  background: rgba(255, 105, 0, 0.08);
  color: var(--fl-primary-strong);
  font-size: 13px;
  font-weight: 700;
  text-align: center;
}

.mobile-tabbar {
  display: none;
}

.mobile-header-title {
  display: none;
}

@media (max-width: 840px) {
  .app-layout {
    display: block;
    min-height: 100dvh;
  }

  .app-sidebar {
    display: none;
  }

  .app-workspace {
    display: block;
    width: 100%;
  }

  .app-header {
    position: sticky;
    top: 0;
    z-index: 90;
    align-items: center;
    flex-direction: row;
    min-height: 64px;
    height: auto;
    gap: 12px;
    padding: max(10px, env(safe-area-inset-top)) 14px 10px;
  }

  .header-copy {
    min-width: 68px;
  }

  .header-title {
    overflow: hidden;
    font-size: 17px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .desktop-header-title {
    display: none;
  }

  .mobile-header-title {
    display: block;
  }

  .header-subtitle {
    display: none;
  }

  .header-actions {
    min-width: 0;
    flex: 1;
    justify-content: flex-end;
    gap: 8px;
  }

  .family-select {
    width: min(40vw, 152px);
    min-width: 112px;
  }

  .user-avatar {
    width: 40px;
    height: 40px;
  }

  .app-main {
    overflow-x: hidden;
    padding: 16px 14px calc(98px + env(safe-area-inset-bottom));
  }

  .mobile-tabbar {
    position: fixed;
    right: 12px;
    bottom: max(8px, env(safe-area-inset-bottom));
    left: 12px;
    z-index: 100;
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    min-height: 68px;
    padding: 6px;
    border: 1px solid rgba(255, 255, 255, 0.78);
    border-radius: 22px;
    background: rgba(250, 251, 253, 0.86);
    box-shadow: var(--fl-liquid-edge), 0 16px 38px rgba(31, 41, 55, 0.14);
    backdrop-filter: blur(28px) saturate(190%);
    -webkit-backdrop-filter: blur(28px) saturate(190%);
  }

  .mobile-tab {
    display: grid;
    min-width: 0;
    min-height: 54px;
    padding: 6px 2px;
    place-items: center;
    align-content: center;
    gap: 3px;
    border: 0;
    border-radius: 16px;
    background: transparent;
    color: var(--fl-muted);
    cursor: pointer;
    font: inherit;
    touch-action: manipulation;
    transition: background 0.2s var(--fl-ease), color 0.2s var(--fl-ease), box-shadow 0.2s var(--fl-ease);
  }

  .mobile-tab .el-icon {
    font-size: 21px;
  }

  .mobile-tab span {
    overflow: hidden;
    max-width: 100%;
    font-size: 11px;
    font-weight: 700;
    line-height: 1.2;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .mobile-tab.is-active {
    background: rgba(255, 105, 0, 0.1);
    color: var(--fl-primary-strong);
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9);
  }

  .mobile-tab:focus-visible {
    outline: 2px solid var(--fl-primary);
    outline-offset: -2px;
  }

  .mobile-tab:active {
    background: rgba(255, 105, 0, 0.16);
  }
}
</style>
