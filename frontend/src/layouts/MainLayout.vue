<script setup lang="ts">
import {
  Calendar,
  Cpu,
  Files,
  HomeFilled,
  House,
  MagicStick,
  Memo,
  Setting
} from '@element-plus/icons-vue';
import { computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const activeMenu = computed(() => {
  if (route.path.startsWith('/devices')) return '/devices';
  if (route.path.startsWith('/files')) return '/files';
  if (route.path.startsWith('/ai-tools')) return '/ai-tools';
  if (route.path.startsWith('/settings')) return '/settings/family';
  if (route.path.startsWith('/dashboard') && route.query.focus === 'calendar') return '/dashboard?focus=calendar';
  return '/dashboard';
});

const primaryMenus = [
  { path: '/dashboard', label: '我的家', hint: '健康分 / 本周事项', icon: HomeFilled },
  { path: '/dashboard?focus=calendar', label: '家庭日历', hint: '保修 / 耗材 / 维修提醒', icon: Calendar },
  { path: '/devices', label: '设备护照', hint: '房间设备 / 生命周期', icon: Memo },
  { path: '/files', label: '凭证盒', hint: '发票 / 说明书 / 维修单', icon: Files },
  { path: '/ai-tools', label: '智能助手', hint: '票据提取 / 故障建议', icon: MagicStick }
];

const secondaryMenus = [
  { path: '/settings/family', label: '我的家庭', icon: Setting }
];

const currentFamilyName = computed(() => {
  return auth.families.find((family) => family.id === auth.currentFamilyId)?.name ?? '我的家';
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
</script>

<template>
  <el-container class="app-layout">
    <el-aside class="app-sidebar" width="286px">
      <div class="brand-block" @click="router.push('/dashboard')">
        <div class="brand-mark">
          <el-icon><House /></el-icon>
        </div>
        <div>
          <div class="brand-title">FixLedger</div>
          <div class="brand-subtitle">家庭设备管家</div>
        </div>
      </div>

      <div class="home-card">
        <span class="home-card-label">当前家庭</span>
        <strong>{{ currentFamilyName }}</strong>
        <small>把设备、凭证和提醒按家庭场景整理。</small>
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

      <div class="side-note">
        <el-icon><Cpu /></el-icon>
        <span>AI 只做辅助建议，真正的家庭设备记录仍由你确认。</span>
      </div>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div>
          <div class="header-eyebrow">Family Device Companion</div>
          <div class="header-title">今天先看家里有哪些设备小事要处理</div>
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
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.app-sidebar {
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 22px 18px;
  border-right: 1px solid rgba(47, 125, 104, 0.12);
  background:
    radial-gradient(circle at 18% 4%, rgba(242, 166, 90, 0.24), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(230, 241, 235, 0.82));
  backdrop-filter: blur(22px);
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px 18px;
  cursor: pointer;
}

.brand-mark {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: 18px;
  background: var(--fl-green);
  color: #fff;
  font-size: 22px;
  font-weight: 900;
  box-shadow: 0 14px 30px rgba(47, 125, 104, 0.32);
}

.brand-title {
  color: var(--fl-ink);
  font-size: 20px;
  font-weight: 900;
}

.brand-subtitle,
.header-eyebrow {
  color: var(--fl-muted);
  font-size: 12px;
}

.home-card {
  display: grid;
  gap: 5px;
  margin: 0 4px 16px;
  padding: 15px;
  border: 1px solid rgba(47, 125, 104, 0.12);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 16px 32px rgba(36, 49, 47, 0.06);
}

.home-card-label {
  color: var(--fl-muted);
  font-size: 12px;
}

.home-card strong {
  color: var(--fl-green-dark);
  font-size: 18px;
}

.home-card small {
  color: var(--fl-muted);
  line-height: 1.5;
}

.app-menu {
  border-right: none;
  background: transparent;
}

:deep(.el-menu-item) {
  min-height: 52px;
  height: auto;
  margin: 6px 0;
  padding: 10px 12px !important;
  border-radius: 16px;
  color: var(--fl-ink);
  line-height: 1.2;
}

:deep(.el-menu-item.is-active) {
  background: var(--fl-green);
  color: #fff;
  box-shadow: 0 16px 28px rgba(47, 125, 104, 0.24);
}

.scene-menu-item :deep(.el-icon) {
  font-size: 20px;
}

.menu-copy {
  display: grid;
  gap: 4px;
  margin-left: 2px;
}

.menu-copy strong {
  font-size: 15px;
}

.menu-copy small {
  color: inherit;
  font-size: 11px;
  opacity: 0.72;
}

.secondary-menu {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed rgba(47, 125, 104, 0.18);
}

.side-note {
  display: flex;
  gap: 10px;
  margin-top: auto;
  padding: 14px;
  border-radius: 18px;
  background: rgba(242, 166, 90, 0.16);
  color: var(--fl-muted);
  font-size: 13px;
  line-height: 1.6;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 76px;
  padding: 0 28px;
  background: rgba(247, 243, 234, 0.66);
  backdrop-filter: blur(16px);
}

.header-title {
  margin-top: 4px;
  color: var(--fl-ink);
  font-weight: 800;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.family-select {
  width: 190px;
}

.user-avatar {
  cursor: pointer;
  background: var(--fl-orange);
  color: var(--fl-ink);
  font-weight: 900;
}

.app-main {
  padding: 26px;
}

@media (max-width: 840px) {
  .app-layout {
    display: block;
  }

  .app-sidebar {
    position: relative;
    width: 100% !important;
    height: auto;
  }

  .app-header {
    align-items: flex-start;
    flex-direction: column;
    height: auto;
    padding: 18px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>