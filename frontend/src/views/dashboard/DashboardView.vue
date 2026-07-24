<script setup lang="ts">
import {
  ArrowLeft,
  ArrowRight,
  Files,
  Plus,
  Refresh,
  WarningFilled
} from '@element-plus/icons-vue';
import { LineChart, PieChart } from 'echarts/charts';
import { GridComponent, TooltipComponent } from 'echarts/components';
import { init, use, type ECharts } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { getCategoryDistribution, getDashboardSummary, getMaintenanceCostTrend, getReminderCalendar } from '@/api/dashboard';
import { getDevicePage } from '@/api/device';
import { getMaintenancePage } from '@/api/maintenance';
import { getReminderPage } from '@/api/reminder';
import { useAuthStore } from '@/stores/auth';
import type { MaintenanceRecord, ReminderItem } from '@/types/business';
import type { DeviceListItem } from '@/types/device';
import type {
  CategoryDistribution,
  DashboardSummary,
  MaintenanceCostTrend,
  ReminderCalendarDay,
  ReminderCalendarItem
} from '@/types/dashboard';
import { labelOf, maintenanceStatusOptions, reminderTypeOptions, statusType } from '@/utils/dicts';

use([LineChart, PieChart, GridComponent, TooltipComponent, CanvasRenderer]);

interface CalendarCell {
  date: string;
  day: number;
  inCurrentMonth: boolean;
  isToday: boolean;
  reminders: ReminderCalendarItem[];
}

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();
const loading = ref(false);
const dataLoaded = ref(false);
const summary = ref<DashboardSummary>();
const categories = ref<CategoryDistribution[]>([]);
const costs = ref<MaintenanceCostTrend[]>([]);
const calendar = ref<ReminderCalendarDay[]>([]);
const reminders = ref<ReminderItem[]>([]);
const maintenance = ref<MaintenanceRecord[]>([]);
const devices = ref<DeviceListItem[]>([]);
const selectedMonth = ref(startOfMonth(new Date()));
const selectedDate = ref(formatDate(new Date()));
const categoryChartRef = ref<HTMLDivElement>();
const costChartRef = ref<HTMLDivElement>();
let categoryChart: ECharts | undefined;
let costChart: ECharts | undefined;

const weekdays = ['一', '二', '三', '四', '五', '六', '日'];
const familyId = computed(() => auth.currentFamilyId);
const familyName = computed(() => auth.families.find((family) => family.id === auth.currentFamilyId)?.name ?? '我的家');
const isCalendarPage = computed(() =>
  route.path === '/calendar' || (route.path === '/dashboard' && route.query.focus === 'calendar')
);

const healthScore = computed(() => {
  const overdue = summary.value?.consumableOverdueCount ?? 0;
  const dueSoon = summary.value?.consumableDueSoonCount ?? 0;
  const expiring = summary.value?.warrantyExpiringCount ?? 0;
  const repairing = summary.value?.repairingCount ?? 0;
  return Math.max(60, 100 - overdue * 10 - dueSoon * 6 - expiring * 5 - repairing * 8);
});

const healthTone = computed(() => {
  if (healthScore.value >= 88) return '状态稳定，暂无高优先级事项。';
  if (healthScore.value >= 75) return '整体稳定，有几件设备小事需要留意。';
  return '需要重点处理，建议先看逾期耗材和维修事项。';
});

const metrics = computed(() => [
  { label: '设备总数', value: summary.value?.deviceTotal ?? 0, suffix: '台' },
  { label: '即将过保', value: summary.value?.warrantyExpiringCount ?? 0, suffix: '项' },
  {
    label: '耗材待更换',
    value: (summary.value?.consumableDueSoonCount ?? 0) + (summary.value?.consumableOverdueCount ?? 0),
    suffix: '项'
  },
  { label: '维修中', value: summary.value?.repairingCount ?? 0, suffix: '台' },
  { label: '本月维修费用', value: summary.value?.monthlyMaintenanceCost ?? 0, suffix: '元' }
]);

const roomOverview = computed(() => {
  const roomMap = new Map<string, DeviceListItem[]>();
  devices.value.forEach((device) => {
    const room = device.location?.trim() || '未设置位置';
    roomMap.set(room, [...(roomMap.get(room) ?? []), device]);
  });
  return Array.from(roomMap.entries())
    .map(([room, list]) => ({ room, devices: list, attention: list.filter((item) => item.nextReminderDate).length }))
    .sort((a, b) => b.devices.length - a.devices.length)
    .slice(0, 6);
});

const weekTasks = computed(() => {
  const now = new Date();
  const end = addDays(now, 7);
  return reminders.value
    .filter((item) => {
      const remindDate = new Date(item.remindAt);
      return remindDate >= now && remindDate <= end;
    })
    .slice(0, 4);
});

const monthTitle = computed(() => {
  const month = selectedMonth.value;
  return `${month.getFullYear()}年${month.getMonth() + 1}月`;
});

const calendarByDate = computed(() => {
  const map = new Map<string, ReminderCalendarItem[]>();
  calendar.value.forEach((day) => map.set(day.date, day.reminders ?? []));
  return map;
});

const calendarCells = computed<CalendarCell[]>(() => {
  const month = selectedMonth.value;
  const first = startOfMonth(month);
  const last = endOfMonth(month);
  const firstWeekday = first.getDay() === 0 ? 7 : first.getDay();
  const start = addDays(first, -(firstWeekday - 1));
  const cells: CalendarCell[] = [];

  for (let index = 0; index < 42; index += 1) {
    const date = addDays(start, index);
    const dateKey = formatDate(date);
    cells.push({
      date: dateKey,
      day: date.getDate(),
      inCurrentMonth: date >= first && date <= last,
      isToday: dateKey === formatDate(new Date()),
      reminders: calendarByDate.value.get(dateKey) ?? []
    });
  }

  return cells;
});

const selectedDayReminders = computed(() => calendarByDate.value.get(selectedDate.value) ?? []);
const monthReminderCount = computed(() => calendar.value.reduce((sum, day) => sum + day.count, 0));
const importantReminderCount = computed(() =>
  calendar.value.reduce(
    (sum, day) => sum + day.reminders.filter((item) => reminderLevel(item) === 'danger').length,
    0
  )
);
const hasNoDevices = computed(() =>
  dataLoaded.value && !loading.value && (summary.value?.deviceTotal ?? 0) === 0
);

async function loadData() {
  if (!familyId.value) {
    dataLoaded.value = false;
    summary.value = undefined;
    return;
  }
  dataLoaded.value = false;
  loading.value = true;
  try {
    const range = getSelectedMonthRange();
    const [summaryData, categoryData, costData, calendarData, reminderPage, maintenancePage, devicePage] =
      await Promise.all([
        getDashboardSummary(familyId.value),
        getCategoryDistribution(familyId.value),
        getMaintenanceCostTrend(familyId.value, 6),
        getReminderCalendar(familyId.value, range.startDate, range.endDate),
        getReminderPage(familyId.value, { pageNum: 1, pageSize: 8 }),
        getMaintenancePage(familyId.value, { pageNum: 1, pageSize: 6 }),
        getDevicePage(familyId.value, { pageNum: 1, pageSize: 100 })
      ]);
    summary.value = summaryData;
    categories.value = categoryData;
    costs.value = costData;
    calendar.value = calendarData;
    reminders.value = reminderPage.records;
    maintenance.value = maintenancePage.records;
    devices.value = devicePage.records;
    dataLoaded.value = true;
    await nextTick();
    renderCharts();
  } finally {
    loading.value = false;
  }
}

async function changeMonth(offset: number) {
  selectedMonth.value = startOfMonth(addMonths(selectedMonth.value, offset));
  selectedDate.value = formatDate(selectedMonth.value);
  await loadData();
}

function selectDay(cell: CalendarCell) {
  selectedDate.value = cell.date;
}

function getSelectedMonthRange() {
  return {
    startDate: formatDate(startOfMonth(selectedMonth.value)),
    endDate: formatDate(endOfMonth(selectedMonth.value))
  };
}

function reminderLevel(item: ReminderCalendarItem | ReminderItem) {
  if (['CONSUMABLE_OVERDUE', 'WARRANTY_EXPIRED', 'MAINTENANCE_FOLLOW_UP'].includes(item.reminderType)) {
    return 'danger';
  }
  return 'warning';
}

function dayPinLevel(remindersForDay: ReminderCalendarItem[]) {
  if (remindersForDay.some((item) => reminderLevel(item) === 'danger')) return 'danger';
  if (remindersForDay.length > 0) return 'warning';
  return 'info';
}

function openReminderTarget(item: ReminderCalendarItem | ReminderItem) {
  if (item.bizType === 'DEVICE') {
    router.push(`/devices/${item.bizId}`);
  } else if (item.bizType === 'MAINTENANCE') {
    router.push(`/maintenance/${item.bizId}`);
  } else {
    router.push('/reminders');
  }
}

function reminderTagType(item: ReminderCalendarItem) {
  return reminderLevel(item) === 'danger' ? 'danger' : 'warning';
}

function formatDate(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function startOfMonth(date: Date) {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

function endOfMonth(date: Date) {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0);
}

function addMonths(date: Date, offset: number) {
  return new Date(date.getFullYear(), date.getMonth() + offset, 1);
}

function addDays(date: Date, offset: number) {
  const next = new Date(date);
  next.setDate(next.getDate() + offset);
  return next;
}

function renderCharts() {
  if (isChartContainerReady(categoryChartRef.value)) {
    categoryChart = categoryChart || init(categoryChartRef.value);
    categoryChart.setOption({
      tooltip: { trigger: 'item' },
      color: ['#ff6900', '#3aa6b9', '#6b7280', '#ff9f0a', '#ff3b30'],
      series: [
        {
          type: 'pie',
          radius: ['42%', '72%'],
          data: categories.value.map((item) => ({ name: item.categoryName, value: item.count })),
          label: { formatter: '{b}\n{c} 台' }
        }
      ]
    });
  }
  if (isChartContainerReady(costChartRef.value)) {
    costChart = costChart || init(costChartRef.value);
    costChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 42, right: 18, top: 34, bottom: 36 },
      xAxis: { type: 'category', data: costs.value.map((item) => item.month) },
      yAxis: { type: 'value' },
      color: ['#ff6900'],
      series: [
        {
          type: 'line',
          smooth: true,
          areaStyle: { color: 'rgba(255, 105, 0, 0.14)' },
          data: costs.value.map((item) => item.cost)
        }
      ]
    });
  }
}

function isChartContainerReady(element?: HTMLDivElement): element is HTMLDivElement {
  return Boolean(element && element.clientWidth > 0 && element.clientHeight > 0);
}

function resizeCharts() {
  renderCharts();
  categoryChart?.resize();
  costChart?.resize();
}

onMounted(() => {
  loadData();
  window.addEventListener('resize', resizeCharts);
  window.addEventListener('family-changed', loadData);
});

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts);
  window.removeEventListener('family-changed', loadData);
  categoryChart?.dispose();
  costChart?.dispose();
});
</script>

<template>
  <div
    v-loading="loading"
    class="page-shell home-shell"
    :class="isCalendarPage ? 'calendar-page' : 'dashboard-page'"
  >
    <section v-if="!isCalendarPage" class="home-summary">
      <div class="summary-copy">
        <p class="section-kicker">总览</p>
        <h1>{{ familyName }}</h1>
        <p>先看待处理事项，再进入设备、凭证或维修记录补齐信息。</p>
        <div class="summary-actions">
          <el-button type="primary" :icon="Refresh" @click="loadData">刷新家庭状态</el-button>
          <el-button :icon="Files" @click="router.push('/files')">打开凭证盒</el-button>
        </div>
      </div>
      <div class="health-card">
        <span>设备安心指数</span>
        <strong>{{ healthScore }}</strong>
        <p>{{ healthTone }}</p>
      </div>
    </section>

    <div v-if="!isCalendarPage" class="metric-grid home-metrics">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}<small>{{ item.suffix }}</small></div>
      </div>
    </div>

    <section v-if="!isCalendarPage && hasNoDevices" class="first-device-guide" aria-label="新用户设备引导">
      <div>
        <p class="section-kicker">开始使用</p>
        <h2>先添加第一台设备</h2>
        <p>
          设备档案会串起保修、耗材、维修和凭证。建好第一台设备后，
          总览、日历和凭证盒都会围绕它展开。
        </p>
      </div>
      <div class="guide-actions">
        <el-button type="primary" :icon="Plus" @click="router.push('/devices/create')">
          添加第一台设备
        </el-button>
        <el-button :icon="Files" @click="router.push('/files')">
          了解凭证盒
        </el-button>
      </div>
    </section>

    <div v-if="!isCalendarPage" class="home-grid">
      <el-card class="glass-card task-card" shadow="never">
        <template #header>
          <div class="card-title-row">
            <span>本周设备小事</span>
            <el-button link type="primary" @click="router.push('/reminders')">全部待办</el-button>
          </div>
        </template>
        <el-empty v-if="weekTasks.length === 0" description="本周暂时没有要处理的设备事项" />
        <div v-else class="task-list">
          <article v-for="item in weekTasks" :key="item.id" class="task-item">
            <div class="task-icon" :class="`task-${reminderLevel(item)}`">
              <el-icon><WarningFilled /></el-icon>
            </div>
            <div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.content || item.remindAt }}</p>
            </div>
            <el-button link type="primary" @click="openReminderTarget(item)">去处理</el-button>
          </article>
        </div>
      </el-card>

      <el-card class="glass-card room-card" shadow="never">
        <template #header>
          <div class="card-title-row">
            <span>房间设备概览</span>
            <el-button link type="primary" @click="router.push('/devices')">设备档案</el-button>
          </div>
        </template>
        <el-empty v-if="roomOverview.length === 0" description="还没有设备位置，先给设备设置房间吧" />
        <div v-else class="room-list">
          <article v-for="room in roomOverview" :key="room.room" class="room-item">
            <div>
              <strong>{{ room.room }}</strong>
              <p>{{ room.devices.slice(0, 3).map((item) => item.name).join('、') }}</p>
            </div>
            <span>{{ room.devices.length }} 台</span>
          </article>
        </div>
      </el-card>
    </div>

    <el-card class="glass-card calendar-card" shadow="never">
      <div class="calendar-head">
        <div>
          <p class="section-kicker">家庭日历</p>
          <h2>{{ monthTitle }}</h2>
          <p class="calendar-summary">
            本月共有 {{ monthReminderCount }} 条提醒，{{ importantReminderCount }} 条需要优先处理。
          </p>
        </div>
        <div class="month-switcher">
          <el-button :icon="ArrowLeft" circle @click="changeMonth(-1)" />
          <strong>{{ monthTitle }}</strong>
          <el-button :icon="ArrowRight" circle @click="changeMonth(1)" />
        </div>
      </div>

      <div class="calendar-layout">
        <div class="month-calendar">
          <div class="weekday-row">
            <span v-for="day in weekdays" :key="day">{{ day }}</span>
          </div>
          <div class="calendar-grid">
            <button
              v-for="cell in calendarCells"
              :key="cell.date"
              class="calendar-cell"
              :class="{
                'is-muted': !cell.inCurrentMonth,
                'is-today': cell.isToday,
                'is-selected': selectedDate === cell.date,
                'has-reminders': cell.reminders.length > 0
              }"
              type="button"
              @click="selectDay(cell)"
            >
              <span class="date-number">{{ cell.day }}</span>
              <span
                v-if="cell.reminders.length > 0"
                class="status-dot"
                :class="`status-dot-${dayPinLevel(cell.reminders)}`"
                aria-hidden="true"
              />
              <div class="cell-reminders">
                <span v-for="item in cell.reminders.slice(0, 2)" :key="item.id" class="cell-reminder-title">
                  {{ item.title }}
                </span>
                <span v-if="cell.reminders.length > 2" class="more-reminders">
                  +{{ cell.reminders.length - 2 }}
                </span>
              </div>
            </button>
          </div>
        </div>

        <aside class="day-panel">
          <p class="section-kicker">{{ selectedDate }}</p>
          <h3>当天提醒</h3>
          <el-empty v-if="selectedDayReminders.length === 0" description="这天没有设备事项" />
          <div v-else class="day-reminder-list">
            <article v-for="item in selectedDayReminders" :key="item.id" class="day-reminder-card">
              <div class="day-reminder-title-row">
                <span class="status-dot status-dot-inline" :class="`status-dot-${reminderLevel(item)}`" aria-hidden="true" />
                <strong>{{ item.title }}</strong>
              </div>
              <el-tag :type="reminderTagType(item)" effect="plain">
                {{ labelOf(reminderTypeOptions, item.reminderType) }}
              </el-tag>
            </article>
          </div>
        </aside>
      </div>
    </el-card>

    <div v-if="!isCalendarPage" class="section-grid">
      <el-card class="glass-card" shadow="never">
        <template #header>设备分类分布</template>
        <div ref="categoryChartRef" class="chart-box" />
      </el-card>
      <el-card class="glass-card" shadow="never">
        <template #header>近 6 个月维修费用</template>
        <div ref="costChartRef" class="chart-box" />
      </el-card>
    </div>

    <div v-if="!isCalendarPage" class="section-grid">
      <el-card class="glass-card" shadow="never">
        <template #header>最近提醒</template>
        <el-timeline>
          <el-timeline-item
            v-for="item in reminders.slice(0, 6)"
            :key="item.id"
            :timestamp="item.remindAt"
            :type="statusType(item.status)"
          >
            <strong>{{ item.title }}</strong>
            <p>{{ item.content }}</p>
          </el-timeline-item>
        </el-timeline>
      </el-card>
      <el-card class="glass-card" shadow="never">
        <template #header>最近维修</template>
        <el-table :data="maintenance" size="small">
          <el-table-column prop="title" label="维修事项" min-width="140" />
          <el-table-column prop="deviceName" label="设备" min-width="120" />
          <el-table-column label="状态" width="96">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">
                {{ labelOf(maintenanceStatusOptions, row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.home-shell {
  gap: 22px;
}

.home-summary {
  position: relative;
  overflow: hidden;
  isolation: isolate;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  padding: 20px;
  border: 1px solid var(--fl-glass-line);
  border-radius: var(--fl-radius-lg);
  background: var(--fl-glass-strong);
  box-shadow: var(--fl-shadow-md);
  backdrop-filter: blur(32px) saturate(190%);
  -webkit-backdrop-filter: blur(32px) saturate(190%);
}

.home-summary::before {
  position: absolute;
  inset: 1px 1px auto;
  height: 42%;
  border-radius: inherit;
  background:
    linear-gradient(110deg, rgba(255, 209, 179, 0.22), rgba(255, 255, 255, 0.68), rgba(255, 255, 255, 0)),
    var(--fl-glass-veil);
  content: '';
  pointer-events: none;
}

.home-summary::after {
  position: absolute;
  inset: 0;
  border: 1px solid rgba(255, 255, 255, 0.42);
  border-radius: inherit;
  content: '';
  pointer-events: none;
}

.summary-copy,
.health-card {
  position: relative;
  z-index: 1;
}

.summary-copy h1 {
  margin: 0;
  color: var(--fl-ink);
  font-size: clamp(24px, 2.4vw, 30px);
  font-weight: 800;
  letter-spacing: 0;
  line-height: 1.2;
}

.summary-copy p {
  max-width: 660px;
  color: var(--fl-muted);
  line-height: 1.85;
}

.summary-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}

.health-card {
  display: grid;
  align-content: center;
  min-height: 170px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.68);
  border-radius: 20px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.62), rgba(255, 244, 235, 0.46)),
    var(--fl-glass-tint);
  color: var(--fl-primary-strong);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.74), 0 14px 34px rgba(255, 105, 0, 0.12);
  backdrop-filter: blur(24px) saturate(185%);
  -webkit-backdrop-filter: blur(24px) saturate(185%);
}

.health-card span {
  font-size: 13px;
  font-weight: 700;
}

.health-card strong {
  margin-top: 8px;
  font-size: 38px;
  font-weight: 800;
  letter-spacing: 0;
  line-height: 1;
}

.health-card p {
  margin: 10px 0 0;
  line-height: 1.7;
  opacity: 0.9;
}

.home-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
  gap: 18px;
}

.first-device-guide {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
  padding: 18px;
  border: 1px solid var(--fl-glass-line);
  border-radius: var(--fl-radius-lg);
  background: var(--fl-glass);
  box-shadow: var(--fl-shadow-sm);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
}

.first-device-guide h2 {
  margin: 0;
  color: var(--fl-ink);
  font-size: 21px;
  font-weight: 800;
  letter-spacing: 0;
}

.first-device-guide p:last-child {
  max-width: 760px;
  margin: 8px 0 0;
  color: var(--fl-muted);
  line-height: 1.7;
}

.guide-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.task-list,
.room-list {
  display: grid;
  gap: 12px;
}

.task-item,
.room-item {
  display: grid;
  align-items: center;
  gap: 14px;
  padding: 15px;
  border: 1px solid rgba(255, 255, 255, 0.64);
  border-radius: 18px;
  background: var(--fl-glass-chip);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.66), 0 8px 22px rgba(31, 41, 55, 0.035);
}

.task-item {
  grid-template-columns: 44px minmax(0, 1fr) auto;
}

.task-icon {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border-radius: 10px;
  color: #fff;
}

.task-danger {
  background: var(--fl-danger);
}

.task-warning {
  background: var(--fl-warning);
}

.task-item strong,
.room-item strong {
  color: var(--fl-ink);
  font-weight: 800;
}

.task-item p,
.room-item p {
  overflow: hidden;
  margin: 5px 0 0;
  color: var(--fl-muted);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.room-item {
  grid-template-columns: minmax(0, 1fr) auto;
}

.room-item span {
  padding: 7px 11px;
  border-radius: 999px;
  background: var(--fl-glass-tint);
  color: var(--fl-primary-strong);
  font-weight: 800;
}

.metric-value small {
  margin-left: 4px;
  color: var(--fl-muted);
  font-size: 14px;
  letter-spacing: 0;
}

.calendar-card {
  overflow: hidden;
}

.calendar-head {
  display: flex;
  gap: 24px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
}

.section-kicker {
  margin: 0 0 7px;
  color: var(--fl-primary-strong);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
}

.calendar-head h2 {
  margin: 0;
  color: var(--fl-ink);
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 0;
}

.calendar-summary {
  margin: 8px 0 0;
  color: var(--fl-muted);
}

.month-switcher {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid rgba(255, 255, 255, 0.68);
  border-radius: 16px;
  background: var(--fl-glass-chip);
  color: var(--fl-ink);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.calendar-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 22px;
}

.weekday-row,
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
}

.weekday-row {
  margin-bottom: 8px;
  color: var(--fl-muted);
  font-size: 12px;
  font-weight: 800;
  text-align: center;
}

.calendar-grid {
  gap: 8px;
}

.calendar-cell {
  position: relative;
  min-height: 112px;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.64);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.54);
  color: var(--fl-text);
  cursor: pointer;
  text-align: left;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.62);
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.calendar-cell:hover {
  border-color: rgba(255, 105, 0, 0.32);
  box-shadow: var(--fl-shadow-sm);
}

.calendar-cell.is-muted {
  color: rgba(45, 54, 50, 0.35);
  background: rgba(255, 255, 255, 0.38);
}

.calendar-cell.is-today {
  border-color: rgba(255, 105, 0, 0.42);
}

.calendar-cell.is-selected {
  border-color: rgba(255, 105, 0, 0.58);
  box-shadow: 0 0 0 3px rgba(255, 105, 0, 0.14), 0 16px 34px rgba(255, 105, 0, 0.1);
}

.calendar-cell.has-reminders {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.74), rgba(255, 244, 235, 0.54));
}

.date-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 29px;
  height: 29px;
  border-radius: 50%;
  color: var(--fl-ink);
  font-weight: 800;
}

.is-today .date-number {
  background: var(--fl-glass-tint);
  color: var(--fl-primary-strong);
}

.status-dot {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--fl-danger);
  box-shadow: 0 0 0 4px rgba(194, 65, 59, 0.12);
}

.status-dot-warning {
  background: var(--fl-warning);
  box-shadow: 0 0 0 4px rgba(183, 121, 31, 0.14);
}

.status-dot-danger {
  background: var(--fl-danger);
  box-shadow: 0 0 0 4px rgba(194, 65, 59, 0.12);
}

.status-dot-inline {
  position: relative;
  top: auto;
  right: auto;
  flex: 0 0 10px;
  width: 10px;
  height: 10px;
}

.cell-reminders {
  display: grid;
  gap: 4px;
  margin-top: 10px;
}

.cell-reminder-title,
.more-reminders {
  overflow: hidden;
  color: var(--fl-ink);
  font-size: 12px;
  font-weight: 800;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.more-reminders {
  color: var(--fl-danger);
}

.day-panel {
  min-height: 100%;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.66);
  border-radius: 18px;
  background: var(--fl-glass-chip);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.64);
}

.day-panel h3 {
  margin: 0 0 16px;
  color: var(--fl-ink);
  font-size: 20px;
  font-weight: 800;
}

.day-reminder-list {
  display: grid;
  gap: 12px;
}

.day-reminder-card {
  display: grid;
  gap: 10px;
  padding: 14px;
  border: 1px solid rgba(255, 255, 255, 0.64);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.58);
  box-shadow: none;
}

.day-reminder-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--fl-text);
}

@media (max-width: 1180px) {
  .home-summary,
  .home-grid,
  .calendar-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .dashboard-page .summary-copy > .section-kicker,
  .dashboard-page .summary-copy > p:not(.section-kicker),
  .dashboard-page .home-grid,
  .dashboard-page .calendar-card,
  .dashboard-page .section-grid {
    display: none;
  }

  .home-summary {
    gap: 14px;
    padding: 16px;
  }

  .summary-copy h1 {
    font-size: 26px;
  }

  .summary-copy p {
    margin: 10px 0 0;
    line-height: 1.65;
  }

  .summary-actions {
    gap: 8px;
    margin-top: 14px;
  }

  .summary-actions .el-button {
    min-width: 0;
    flex: 1;
    margin-left: 0;
    padding-right: 12px;
    padding-left: 12px;
  }

  .health-card {
    grid-template-columns: auto minmax(0, 1fr);
    min-height: 0;
    padding: 14px 16px;
    align-items: center;
    gap: 4px 14px;
  }

  .health-card strong {
    margin-top: 0;
    font-size: 32px;
  }

  .health-card p {
    grid-row: 1 / 3;
    grid-column: 2;
    margin: 0;
    font-size: 14px;
    line-height: 1.55;
  }

  .home-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }

  .home-metrics .metric-card {
    min-height: 94px;
    padding: 14px;
  }

  .home-metrics .metric-card:last-child {
    grid-column: 1 / -1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 76px;
  }

  .dashboard-page .home-metrics .metric-card:last-child {
    display: none;
  }

  .home-metrics .metric-card:last-child .metric-value {
    margin-top: 0;
  }

  .calendar-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .first-device-guide {
    grid-template-columns: 1fr;
  }

  .guide-actions {
    justify-content: flex-start;
  }

  .calendar-grid {
    gap: 6px;
  }

  .calendar-cell {
    min-height: 54px;
    padding: 6px;
  }

  .cell-reminder-title {
    display: none;
  }
}
</style>
