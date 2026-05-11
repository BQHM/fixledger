<script setup lang="ts">
import * as echarts from 'echarts';
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue';

import {
  getCategoryDistribution,
  getDashboardSummary,
  getMaintenanceCostTrend,
  getReminderCalendar
} from '@/api/dashboard';
import { getMaintenancePage } from '@/api/maintenance';
import { getReminderPage } from '@/api/reminder';
import { useAuthStore } from '@/stores/auth';
import type { MaintenanceRecord, ReminderItem } from '@/types/business';
import type { CategoryDistribution, DashboardSummary, MaintenanceCostTrend, ReminderCalendarDay } from '@/types/dashboard';
import { labelOf, maintenanceStatusOptions, statusType } from '@/utils/dicts';

const auth = useAuthStore();
const loading = ref(false);
const summary = ref<DashboardSummary>();
const categories = ref<CategoryDistribution[]>([]);
const costs = ref<MaintenanceCostTrend[]>([]);
const calendar = ref<ReminderCalendarDay[]>([]);
const reminders = ref<ReminderItem[]>([]);
const maintenance = ref<MaintenanceRecord[]>([]);
const categoryChartRef = ref<HTMLDivElement>();
const costChartRef = ref<HTMLDivElement>();
let categoryChart: echarts.ECharts | undefined;
let costChart: echarts.ECharts | undefined;

const familyId = computed(() => auth.currentFamilyId);

const metrics = computed(() => [
  { label: '设备总数', value: summary.value?.deviceTotal ?? 0, suffix: '台' },
  { label: '即将过保', value: summary.value?.warrantyExpiringCount ?? 0, suffix: '项' },
  { label: '耗材待更换', value: (summary.value?.consumableDueSoonCount ?? 0) + (summary.value?.consumableOverdueCount ?? 0), suffix: '项' },
  { label: '维修中', value: summary.value?.repairingCount ?? 0, suffix: '台' },
  { label: '本月维修费用', value: summary.value?.monthlyMaintenanceCost ?? 0, suffix: '元' }
]);

async function loadData() {
  if (!familyId.value) return;
  loading.value = true;
  try {
    const [summaryData, categoryData, costData, calendarData, reminderPage, maintenancePage] =
      await Promise.all([
        getDashboardSummary(familyId.value),
        getCategoryDistribution(familyId.value),
        getMaintenanceCostTrend(familyId.value, 6),
        getReminderCalendar(familyId.value),
        getReminderPage(familyId.value, { pageNum: 1, pageSize: 6 }),
        getMaintenancePage(familyId.value, { pageNum: 1, pageSize: 6 })
      ]);
    summary.value = summaryData;
    categories.value = categoryData;
    costs.value = costData;
    calendar.value = calendarData;
    reminders.value = reminderPage.records;
    maintenance.value = maintenancePage.records;
    await nextTick();
    renderCharts();
  } finally {
    loading.value = false;
  }
}

function renderCharts() {
  if (categoryChartRef.value) {
    categoryChart = categoryChart || echarts.init(categoryChartRef.value);
    categoryChart.setOption({
      tooltip: { trigger: 'item' },
      color: ['#2f7d68', '#f2a65a', '#5b8def', '#d9534f', '#91c0b1'],
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
  if (costChartRef.value) {
    costChart = costChart || echarts.init(costChartRef.value);
    costChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 42, right: 18, top: 34, bottom: 36 },
      xAxis: { type: 'category', data: costs.value.map((item) => item.month) },
      yAxis: { type: 'value' },
      color: ['#2f7d68'],
      series: [
        {
          type: 'line',
          smooth: true,
          areaStyle: { color: 'rgba(47, 125, 104, 0.12)' },
          data: costs.value.map((item) => item.cost)
        }
      ]
    });
  }
}

function resizeCharts() {
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
  <div v-loading="loading" class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">首页看板</h1>
        <p class="page-subtitle">用一屏讲清楚设备、保修、耗材、维修和提醒的核心闭环。</p>
      </div>
      <el-button type="primary" @click="loadData">刷新数据</el-button>
    </div>

    <div class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}<small>{{ item.suffix }}</small></div>
      </div>
    </div>

    <div class="section-grid">
      <el-card class="glass-card" shadow="never">
        <template #header>设备分类分布</template>
        <div ref="categoryChartRef" class="chart-box" />
      </el-card>
      <el-card class="glass-card" shadow="never">
        <template #header>近 6 个月维修费用</template>
        <div ref="costChartRef" class="chart-box" />
      </el-card>
    </div>

    <div class="section-grid">
      <el-card class="glass-card" shadow="never">
        <template #header>最近提醒</template>
        <el-timeline>
          <el-timeline-item
            v-for="item in reminders"
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

    <el-card class="glass-card" shadow="never">
      <template #header>未来 30 天提醒日历</template>
      <el-empty v-if="calendar.length === 0" description="暂无提醒" />
      <div v-else class="calendar-list">
        <div v-for="day in calendar" :key="day.date" class="calendar-day">
          <div class="calendar-date">{{ day.date }}</div>
          <div class="calendar-items">
            <el-tag v-for="item in day.reminders" :key="item.id" type="warning" effect="plain">
              {{ item.title }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.metric-value small {
  margin-left: 4px;
  color: var(--fl-muted);
  font-size: 14px;
}

.calendar-list {
  display: grid;
  gap: 12px;
}

.calendar-day {
  display: flex;
  gap: 18px;
  align-items: center;
  padding: 14px;
  border-radius: 16px;
  background: rgba(47, 125, 104, 0.06);
}

.calendar-date {
  min-width: 120px;
  color: var(--fl-green-dark);
  font-weight: 900;
}

.calendar-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>