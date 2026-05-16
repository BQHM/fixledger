import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');

function readProjectFile(path) {
  return readFileSync(resolve(root, path), 'utf8');
}

function assertFile(path) {
  if (!existsSync(resolve(root, path))) {
    throw new Error(`Missing required file: ${path}`);
  }
}

function assertContains(source, expected, label) {
  if (!source.includes(expected)) {
    throw new Error(`Missing ${label}: ${expected}`);
  }
}

const requiredViews = [
  'src/layouts/MainLayout.vue',
  'src/views/auth/LoginView.vue',
  'src/views/auth/RegisterView.vue',
  'src/views/dashboard/DashboardView.vue',
  'src/views/devices/DeviceListView.vue',
  'src/views/devices/DeviceFormView.vue',
  'src/views/devices/DeviceDetailView.vue',
  'src/views/warranties/WarrantyView.vue',
  'src/views/consumables/ConsumableView.vue',
  'src/views/maintenance/MaintenanceView.vue',
  'src/views/maintenance/MaintenanceDetailView.vue',
  'src/views/reminders/ReminderView.vue',
  'src/views/files/FileLibraryView.vue',
  'src/views/ai/AiToolsView.vue',
  'src/views/settings/FamilySettingsView.vue'
];

const requiredRouteNames = [
  'login',
  'register',
  'dashboard',
  'devices',
  'device-create',
  'device-detail',
  'device-edit',
  'warranties',
  'consumables',
  'maintenance',
  'maintenance-detail',
  'reminders',
  'files',
  'ai-tools',
  'family-settings'
];

const apiContracts = [
  ['src/api/auth.ts', ['login', 'register', 'logout', 'getCurrentUser'], ['/api/auth/login', '/api/auth/me']],
  ['src/api/family.ts', ['getFamilies', 'createFamily', 'getFamilyMembers'], ['/api/families']],
  ['src/api/device.ts', ['getDevicePage', 'createDevice', 'getDeviceDetail'], ['/api/families/${familyId}/devices']],
  ['src/api/warranty.ts', ['getDeviceWarranties', 'createWarranty', 'getExpiringWarranties'], ['/warranties/expiring']],
  ['src/api/consumable.ts', ['getDeviceConsumables', 'createConsumable', 'createReplaceRecord'], ['/consumables/due-soon']],
  ['src/api/maintenance.ts', ['getMaintenancePage', 'createMaintenance', 'updateMaintenanceStatus'], ['/maintenance-records']],
  ['src/api/reminder.ts', ['getReminderPage', 'scanReminders', 'getUnreadCount'], ['/reminders']],
  ['src/api/dashboard.ts', ['getDashboardSummary', 'getReminderCalendar'], ['/dashboard/summary']],
  ['src/api/file.ts', ['uploadFile', 'getFiles', 'downloadFile'], ['/files']],
  ['src/api/ai.ts', ['parseInvoice', 'suggestTroubleshooting', 'summarizeMaintenance'], ['/ai/']]
];

for (const path of requiredViews) {
  assertFile(path);
}

const router = readProjectFile('src/router/index.ts');
for (const routeName of requiredRouteNames) {
  assertContains(router, `name: '${routeName}'`, `route ${routeName}`);
}
assertContains(router, 'router.beforeEach', 'auth route guard');
assertContains(router, 'redirect=${encodeURIComponent(to.fullPath)}', 'login redirect preservation');

for (const [path, functions, urls] of apiContracts) {
  assertFile(path);
  const source = readProjectFile(path);
  for (const functionName of functions) {
    assertContains(source, `function ${functionName}`, `${path} export ${functionName}`);
  }
  for (const url of urls) {
    assertContains(source, url, `${path} URL ${url}`);
  }
}

const request = readProjectFile('src/api/request.ts');
assertContains(request, 'Authorization', 'request token header');
assertContains(request, 'pageSize', 'request page size normalization');

const authStore = readProjectFile('src/stores/auth.ts');
assertContains(authStore, 'defineStore', 'Pinia auth store');
assertContains(authStore, 'bootstrap', 'auth bootstrap');

console.log('Frontend smoke check passed.');
