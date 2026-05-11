<script setup lang="ts">
import {
  Bell,
  Box,
  AlarmClock,
  Cpu,
  Files,
  HomeFilled,
  MagicStick,
  Monitor,
  Setting,
  Tools
} from '@element-plus/icons-vue';
import { computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const activeMenu = computed(() => '/' + route.path.split('/')[1]);

const menus = [
  { path: '/dashboard', label: '首页看板', icon: HomeFilled },
  { path: '/devices', label: '设备档案', icon: Monitor },
  { path: '/warranties', label: '保修管理', icon: AlarmClock },
  { path: '/consumables', label: '耗材管理', icon: Box },
  { path: '/maintenance', label: '维修记录', icon: Tools },
  { path: '/reminders', label: '提醒中心', icon: Bell },
  { path: '/files', label: '附件库', icon: Files },
  { path: '/ai-tools', label: 'AI 助手', icon: MagicStick },
  { path: '/settings/family', label: '家庭设置', icon: Setting }
];

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
    <el-aside class="app-sidebar" width="260px">
      <div class="brand-block" @click="router.push('/dashboard')">
        <div class="brand-mark">FL</div>
        <div>
          <div class="brand-title">FixLedger</div>
          <div class="brand-subtitle">家庭设备档案本</div>
        </div>
      </div>

      <el-menu :default-active="activeMenu" router class="app-menu">
        <el-menu-item v-for="menu in menus" :key="menu.path" :index="menu.path">
          <el-icon><component :is="menu.icon" /></el-icon>
          <span>{{ menu.label }}</span>
        </el-menu-item>
      </el-menu>

      <div class="side-note">
        <el-icon><Cpu /></el-icon>
        <span>AI 只做辅助建议，核心数据仍由用户确认。</span>
      </div>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div>
          <div class="header-eyebrow">Home Maintenance Ledger</div>
          <div class="header-title">把家里的设备、凭证和提醒放在一个地方</div>
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
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.88), rgba(230, 241, 235, 0.8));
  backdrop-filter: blur(22px);
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px 22px;
  cursor: pointer;
}

.brand-mark {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  border-radius: 16px;
  background: var(--fl-green);
  color: #fff;
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

.app-menu {
  border-right: none;
  background: transparent;
}

:deep(.el-menu-item) {
  height: 48px;
  margin: 4px 0;
  border-radius: 14px;
  color: var(--fl-ink);
}

:deep(.el-menu-item.is-active) {
  background: var(--fl-green);
  color: #fff;
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
