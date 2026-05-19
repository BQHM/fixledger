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
  padding: 18px;
  border-right: 1px solid rgba(39, 46, 42, 0.07);
  background:
    radial-gradient(circle at 20% 0%, rgba(255, 196, 122, 0.32), transparent 30%),
    linear-gradient(180deg, rgba(255, 253, 248, 0.92), rgba(246, 241, 232, 0.84));
  backdrop-filter: blur(26px);
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 8px 16px;
  border-radius: 22px;
  cursor: pointer;
}

.brand-mark {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: 18px;
  background: linear-gradient(145deg, var(--fl-mi-orange), #ffbd63);
  color: #fff;
  font-size: 22px;
  box-shadow: 0 16px 34px rgba(255, 138, 31, 0.3);
}

.brand-title {
  color: var(--fl-ink);
  font-size: 20px;
  font-weight: 950;
  letter-spacing: -0.04em;
}

.brand-subtitle,
.header-eyebrow {
  color: var(--fl-muted);
  font-size: 12px;
}

.home-card {
  display: grid;
  gap: 5px;
  margin: 0 2px 16px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.74);
  border-radius: 24px;
  background:
    radial-gradient(circle at 85% 16%, rgba(255, 211, 144, 0.42), transparent 36%),
    rgba(255, 255, 255, 0.66);
  box-shadow: 0 14px 32px rgba(88, 72, 49, 0.07);
}

.home-card-label {
  color: var(--fl-muted);
  font-size: 12px;
  font-weight: 800;
}

.home-card strong {
  color: var(--fl-ink);
  font-size: 19px;
  letter-spacing: -0.03em;
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
  min-height: 56px;
  height: auto;
  margin: 6px 0;
  padding: 11px 12px !important;
  border-radius: 18px;
  color: var(--fl-text);
  line-height: 1.2;
}

:deep(.el-menu-item:hover) {
  background: rgba(255, 138, 31, 0.09);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #fff2df, #fff9f0);
  color: var(--fl-mi-orange-dark);
  box-shadow: inset 0 0 0 1px rgba(255, 138, 31, 0.16), 0 12px 24px rgba(255, 138, 31, 0.1);
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
  font-weight: 900;
}

.menu-copy small {
  color: inherit;
  font-size: 11px;
  opacity: 0.68;
}

.secondary-menu {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed rgba(39, 46, 42, 0.12);
}

.side-note {
  display: flex;
  gap: 10px;
  margin-top: auto;
  padding: 14px;
  border-radius: 20px;
  background: rgba(255, 245, 230, 0.86);
  color: var(--fl-muted);
  font-size: 13px;
  line-height: 1.6;
}

.side-note :deep(.el-icon) {
  color: var(--fl-mi-orange);
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 76px;
  padding: 0 28px;
  border-bottom: 1px solid rgba(39, 46, 42, 0.05);
  background: rgba(250, 247, 239, 0.72);
  backdrop-filter: blur(18px);
}

.header-title {
  margin-top: 4px;
  color: var(--fl-ink);
  font-weight: 900;
  letter-spacing: -0.02em;
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
  background: linear-gradient(145deg, var(--fl-mi-orange), #ffbd63);
  color: #fff;
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

  .app-main {
    padding: 18px;
  }
}
</style>