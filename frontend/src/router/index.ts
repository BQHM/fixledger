import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

import { useAuthStore } from '@/stores/auth';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'dashboard', component: () => import('@/views/dashboard/DashboardView.vue') },
      { path: 'devices', name: 'devices', component: () => import('@/views/devices/DeviceListView.vue') },
      { path: 'devices/create', name: 'device-create', component: () => import('@/views/devices/DeviceFormView.vue') },
      { path: 'devices/:id', name: 'device-detail', component: () => import('@/views/devices/DeviceDetailView.vue') },
      { path: 'devices/:id/edit', name: 'device-edit', component: () => import('@/views/devices/DeviceFormView.vue') },
      { path: 'warranties', name: 'warranties', component: () => import('@/views/warranties/WarrantyView.vue') },
      { path: 'consumables', name: 'consumables', component: () => import('@/views/consumables/ConsumableView.vue') },
      { path: 'maintenance', name: 'maintenance', component: () => import('@/views/maintenance/MaintenanceView.vue') },
      { path: 'maintenance/:id', name: 'maintenance-detail', component: () => import('@/views/maintenance/MaintenanceDetailView.vue') },
      { path: 'reminders', name: 'reminders', component: () => import('@/views/reminders/ReminderView.vue') },
      { path: 'files', name: 'files', component: () => import('@/views/files/FileLibraryView.vue') },
      { path: 'ai-tools', name: 'ai-tools', component: () => import('@/views/ai/AiToolsView.vue') },
      { path: 'settings/family', name: 'family-settings', component: () => import('@/views/settings/FamilySettingsView.vue') }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
});

router.beforeEach(async (to) => {
  const auth = useAuthStore();
  if (to.meta.public) {
    return auth.isAuthenticated && to.path !== '/login' ? '/dashboard' : true;
  }
  if (!auth.isAuthenticated) {
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`;
  }
  if (!auth.user) {
    await auth.bootstrap();
  }
  return true;
});

export default router;