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

function assertCondition(condition, label) {
  if (!condition) {
    throw new Error(`Invalid ${label}`);
  }
}

const requiredViews = [
  'src/layouts/MainLayout.vue',
  'src/views/home/HomeLandingView.vue',
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

const requiredPwaFiles = [
  'public/manifest.webmanifest',
  'public/offline.html',
  'public/pwa-192x192.png',
  'public/pwa-512x512.png',
  'public/service-worker.js',
  'src/pwa.ts'
];

const requiredRouteNames = [
  'login',
  'register',
  'dashboard',
  'calendar',
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
  [
    'src/api/family.ts',
    ['getFamilies', 'createFamily', 'getFamilyMembers', 'inviteFamilyMember', 'updateFamilyMemberRole'],
    ['/api/families', '/members/${memberId}/role']
  ],
  [
    'src/api/device.ts',
    ['getDevicePage', 'createDevice', 'getDeviceDetail', 'exportDeviceCsv'],
    ['/api/families/${familyId}/devices', '/exports/devices.csv']
  ],
  ['src/api/warranty.ts', ['getDeviceWarranties', 'createWarranty', 'getExpiringWarranties'], ['/warranties/expiring']],
  ['src/api/consumable.ts', ['getDeviceConsumables', 'createConsumable', 'createReplaceRecord'], ['/consumables/due-soon']],
  [
    'src/api/maintenance.ts',
    ['getMaintenancePage', 'createMaintenance', 'updateMaintenanceStatus', 'exportMaintenanceCostCsv'],
    ['/maintenance-records', '/exports/maintenance-costs.csv']
  ],
  ['src/api/reminder.ts', ['getReminderPage', 'scanReminders', 'getUnreadCount'], ['/reminders']],
  ['src/api/dashboard.ts', ['getDashboardSummary', 'getReminderCalendar'], ['/dashboard/summary']],
  ['src/api/file.ts', ['uploadFile', 'getFiles', 'downloadFile'], ['/files']],
  ['src/api/ai.ts', ['parseInvoice', 'suggestTroubleshooting', 'summarizeMaintenance'], ['/ai/']],
  ['src/api/system.ts', ['getOperationLogs'], ['/api/system/operation-logs']]
];

for (const path of requiredViews) {
  assertFile(path);
}
for (const path of requiredPwaFiles) {
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
assertContains(authStore, 'resolveCurrentFamilyId', 'current family recovery helper');
assertContains(authStore, 'clearCurrentFamily', 'invalid family context cleanup');

const deviceForm = readProjectFile('src/views/devices/DeviceFormView.vue');
assertContains(deviceForm, 'applyDefaultCategory', 'device form default category helper');
assertContains(deviceForm, 'categoryAutoSelected', 'device form default category hint');
assertContains(deviceForm, 'categoryName', 'device form AI category draft');

const deviceListView = readProjectFile('src/views/devices/DeviceListView.vue');
assertContains(deviceListView, 'handleExportDevices', 'device export action');
assertContains(deviceListView, 'exportDeviceCsv', 'device export API usage');

const dashboardView = readProjectFile('src/views/dashboard/DashboardView.vue');
assertContains(dashboardView, 'first-device-guide', 'dashboard first device guide');
assertContains(dashboardView, 'dataLoaded', 'dashboard empty guide load guard');
assertContains(dashboardView, '/devices/create', 'dashboard create device action');
assertContains(dashboardView, 'isCalendarPage', 'calendar page content separation');
assertContains(dashboardView, 'dashboard-page', 'mobile home content scope');
assertContains(dashboardView, 'isChartContainerReady', 'hidden chart render guard');
assertContains(dashboardView, "from 'echarts/core'", 'modular ECharts core import');
assertCondition(!dashboardView.includes("from 'echarts';"), 'dashboard avoids full ECharts import');

const frontendEntry = readProjectFile('src/main.ts');
assertContains(frontendEntry, "from './plugins/element-plus'", 'selective Element Plus installer');
assertCondition(!frontendEntry.includes('import ElementPlus'), 'entry avoids full Element Plus plugin');

const elementPlusInstaller = readProjectFile('src/plugins/element-plus.ts');
for (const path of ['src/App.vue', ...requiredViews]) {
  const source = readProjectFile(path);
  for (const match of source.matchAll(/<el-([a-z-]+)/g)) {
    const componentName = `El${match[1]
      .split('-')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join('')}`;
    assertContains(elementPlusInstaller, componentName, `${path} component ${componentName}`);
  }
}

const mainLayout = readProjectFile('src/layouts/MainLayout.vue');
assertContains(mainLayout, 'mobile-tabbar', 'mobile primary navigation');
assertContains(mainLayout, 'aria-current', 'mobile navigation active state');
assertContains(mainLayout, 'safe-area-inset-bottom', 'mobile safe area spacing');
assertContains(mainLayout, 'display: none', 'mobile desktop sidebar removal');
assertContains(mainLayout, 'const mobileMenus', 'mobile app navigation model');
assertContains(mainLayout, "{ path: '/dashboard', label: '首页'", 'mobile home tab');
assertContains(mainLayout, "{ path: '/settings/family', label: '我的'", 'mobile profile tab');
assertContains(mainLayout, "route.path.startsWith('/reminders')", 'reminders calendar tab mapping');
assertContains(mainLayout, "maintenance: '维修记录'", 'maintenance page title');
assertContains(mainLayout, '安装到设备', 'PWA install command');
assertContains(mainLayout, '更新应用', 'PWA update command');
assertContains(mainLayout, '当前网络不可用', 'PWA offline state');

const indexHtml = readProjectFile('index.html');
assertContains(indexHtml, '<meta name="color-scheme" content="only light" />', 'light color scheme metadata');
assertContains(indexHtml, 'rel="manifest" href="/manifest.webmanifest"', 'PWA manifest link');
assertContains(indexHtml, 'name="theme-color"', 'PWA theme color metadata');

const manifest = JSON.parse(readProjectFile('public/manifest.webmanifest'));
assertCondition(manifest.name === 'FixLedger 家庭设备档案本', 'PWA manifest name');
assertCondition(manifest.start_url === '/dashboard', 'PWA start URL');
assertCondition(manifest.scope === '/', 'PWA scope');
assertCondition(manifest.display === 'standalone', 'PWA standalone display');
assertCondition(Array.isArray(manifest.icons) && manifest.icons.length >= 2, 'PWA icons');

const serviceWorker = readProjectFile('public/service-worker.js');
assertContains(serviceWorker, "'/api'", 'PWA API network-only boundary');
assertContains(serviceWorker, "'/actuator'", 'PWA Actuator network-only boundary');
assertContains(serviceWorker, "'/swagger-ui'", 'PWA Swagger network-only boundary');
assertContains(serviceWorker, "'/v3/api-docs'", 'PWA API docs network-only boundary');
assertContains(serviceWorker, "request.headers.has('Authorization')", 'PWA authorization cache exclusion');
assertContains(serviceWorker, "request.mode === 'navigate'", 'PWA offline navigation fallback');
assertContains(serviceWorker, "event.data?.type === 'SKIP_WAITING'", 'PWA controlled update activation');

const offlinePage = readProjectFile('public/offline.html');
assertContains(offlinePage, '当前网络不可用', 'PWA offline heading');
assertContains(offlinePage, '重新连接', 'PWA offline retry command');

const pwaModule = readProjectFile('src/pwa.ts');
assertContains(pwaModule, "window.addEventListener('beforeinstallprompt'", 'PWA install event');
assertContains(pwaModule, 'import.meta.env.PROD', 'production-only service worker registration');
assertContains(pwaModule, "navigator.serviceWorker.register('/service-worker.js')", 'service worker registration');
assertContains(pwaModule, 'navigator.onLine', 'PWA network status');

const globalStyles = readProjectFile('src/styles/main.css');
assertContains(globalStyles, 'color-scheme: only light', 'light color scheme style');
assertContains(globalStyles, '.mobile-data-list', 'mobile data list styles');
assertContains(globalStyles, '.desktop-data-table', 'desktop data table breakpoint');
assertContains(globalStyles, 'flex-direction: column', 'mobile inline form stacking');
assertContains(globalStyles, '.el-form--inline .el-select__selection', 'mobile select shrinking');

const responsiveListViews = [
  'src/views/settings/FamilySettingsView.vue',
  'src/views/maintenance/MaintenanceView.vue',
  'src/views/consumables/ConsumableView.vue',
  'src/views/warranties/WarrantyView.vue',
  'src/views/reminders/ReminderView.vue'
];
for (const path of responsiveListViews) {
  const source = readProjectFile(path);
  assertContains(source, 'desktop-data-table', `${path} desktop table`);
  assertContains(source, 'mobile-data-list', `${path} mobile card list`);
}

const fileLibraryView = readProjectFile('src/views/files/FileLibraryView.vue');
assertContains(fileLibraryView, 'credential-empty-guide', 'credential box empty device guide');
assertContains(fileLibraryView, 'devicesLoaded', 'credential box device load guard');
assertContains(fileLibraryView, 'hasNoDeviceMatches', 'credential box search empty state');
assertContains(fileLibraryView, 'clearDeviceSearch', 'credential box search reset action');
assertContains(fileLibraryView, 'hasDevices', 'credential box workflow guard');

const aiToolsView = readProjectFile('src/views/ai/AiToolsView.vue');
assertContains(aiToolsView, "params.set('categoryName'", 'AI invoice category draft handoff');

const maintenanceView = readProjectFile('src/views/maintenance/MaintenanceView.vue');
assertContains(maintenanceView, 'handleExportCosts', 'maintenance cost export action');
assertContains(maintenanceView, 'exportMaintenanceCostCsv', 'maintenance export API usage');

const familySettingsView = readProjectFile('src/views/settings/FamilySettingsView.vue');
assertContains(familySettingsView, 'inviteDialogVisible', 'family member invite dialog');
assertContains(familySettingsView, 'updateFamilyMemberRole', 'family member role update action');
assertContains(familySettingsView, 'removeFamilyMember', 'family member remove action');
assertContains(familySettingsView, 'getOperationLogs', 'family operation log list');

console.log('Frontend smoke check passed.');
