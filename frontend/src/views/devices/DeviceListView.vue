<script setup lang="ts">
import { Download, Plus, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import {
  deleteDevice,
  exportDeviceCsv,
  getDeviceCategories,
  getDevicePage,
  updateDeviceStatus
} from '@/api/device';
import { useAuthStore } from '@/stores/auth';
import type { DeviceCategory, DeviceListItem } from '@/types/device';
import { deviceStatusOptions, labelOf, statusType } from '@/utils/dicts';

interface DeviceRoomGroup {
  location: string;
  devices: DeviceListItem[];
  attentionCount: number;
}

const auth = useAuthStore();
const router = useRouter();
const loading = ref(false);
const exportLoading = ref(false);
const devices = ref<DeviceListItem[]>([]);
const categories = ref<DeviceCategory[]>([]);
const total = ref(0);
const advancedPanels = ref<string[]>([]);
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  categoryId: undefined as number | undefined,
  status: '',
  brand: ''
});
const familyId = computed(() => auth.currentFamilyId);
const roomGroups = computed<DeviceRoomGroup[]>(() => {
  const grouped = new Map<string, DeviceListItem[]>();
  devices.value.forEach((device) => {
    const location = device.location?.trim() || '未设置房间';
    grouped.set(location, [...(grouped.get(location) ?? []), device]);
  });

  return Array.from(grouped.entries()).map(([location, roomDevices]) => ({
    location,
    devices: roomDevices,
    attentionCount: roomDevices.filter(isAttentionDevice).length
  }));
});
const currentPageActiveCount = computed(
  () => devices.value.filter((device) => !['SCRAPPED', 'IDLE'].includes(device.status)).length
);
const currentPageReminderCount = computed(() => devices.value.filter((device) => device.nextReminderDate).length);
const currentPageRoomCount = computed(() => roomGroups.value.length);

async function loadData() {
  if (!familyId.value) return;
  loading.value = true;
  try {
    const [page, categoryList] = await Promise.all([
      getDevicePage(familyId.value, query),
      getDeviceCategories(familyId.value)
    ]);
    devices.value = page.records;
    total.value = page.total;
    categories.value = categoryList;
  } finally {
    loading.value = false;
  }
}

function resetQuery() {
  query.keyword = '';
  query.categoryId = undefined;
  query.status = '';
  query.brand = '';
  query.pageNum = 1;
  loadData();
}

function openDevicePassport(deviceId: number) {
  router.push(`/devices/${deviceId}`);
}

function getBrandModel(device: DeviceListItem) {
  const text = [device.brand, device.model].filter(Boolean).join(' / ');
  return text || '品牌型号待补充';
}

function getCategoryInitial(device: DeviceListItem) {
  return (device.categoryName || device.name || '家').slice(0, 1);
}

function getWarrantyText(value?: string) {
  const statusMap: Record<string, string> = {
    ACTIVE: '保修中',
    EXPIRED: '已过保',
    EXPIRING_SOON: '即将过保',
    NONE: '未登记保修'
  };
  return value ? statusMap[value] ?? value : '未登记保修';
}

function isAttentionDevice(device: DeviceListItem) {
  return ['PENDING_REPAIR', 'REPAIRING', 'SCRAPPED'].includes(device.status) || Boolean(device.nextReminderDate);
}

async function handleDelete(row: DeviceListItem) {
  await ElMessageBox.confirm(`确认删除设备「${row.name}」吗？`, '删除设备', { type: 'warning' });
  await deleteDevice(familyId.value!, row.id);
  loadData();
}

async function handleStatus(row: DeviceListItem, status: string) {
  await updateDeviceStatus(familyId.value!, row.id, status);
  loadData();
}

async function handleExportDevices() {
  if (!familyId.value) return;
  exportLoading.value = true;
  try {
    await exportDeviceCsv(familyId.value);
    ElMessage.success('设备清单已开始下载');
  } finally {
    exportLoading.value = false;
  }
}

onMounted(() => {
  loadData();
  window.addEventListener('family-changed', loadData);
});

onUnmounted(() => {
  window.removeEventListener('family-changed', loadData);
});
</script>

<template>
  <div class="page-shell device-list-page">
    <section class="device-list-summary">
      <div class="summary-copy">
        <div class="summary-kicker">设备</div>
        <h1>设备档案</h1>
        <p>
          按房间查看设备状态、保修和下一次提醒，表格视图保留给精确筛选。
        </p>
      </div>
      <div class="summary-side">
        <div class="summary-stats" aria-label="设备档案摘要">
          <div>
            <strong>{{ total }}</strong>
            <span>全部设备</span>
          </div>
          <div>
            <strong>{{ currentPageRoomCount }}</strong>
            <span>本页房间</span>
          </div>
          <div>
            <strong>{{ currentPageReminderCount }}</strong>
            <span>本页提醒</span>
          </div>
          <div>
            <strong>{{ currentPageActiveCount }}</strong>
            <span>本页在用</span>
          </div>
        </div>
        <div class="summary-actions">
          <el-button
            plain
            size="large"
            :icon="Download"
            :disabled="!familyId"
            :loading="exportLoading"
            @click="handleExportDevices"
          >
            导出清单
          </el-button>
          <el-button type="primary" size="large" :icon="Plus" @click="router.push('/devices/create')">
            新增设备
          </el-button>
        </div>
      </div>
    </section>

    <el-card class="glass-card toolbar-card" shadow="never">
      <div class="filter-title">
        <span>查找家里的设备</span>
        <small>支持名称、分类、状态和品牌筛选</small>
      </div>
      <el-form :inline="true" :model="query" class="device-filter-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="名称 / 品牌 / 型号" :prefix-icon="Search" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" clearable placeholder="全部分类" style="width: 150px">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 150px">
            <el-option v-for="item in deviceStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="query.brand" clearable placeholder="例如 小米 / 海尔" style="width: 170px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <section v-loading="loading" class="room-wall" aria-label="按房间分组的设备档案">
      <el-empty v-if="!loading && devices.length === 0" description="还没有设备档案。先添加一台家里的净水器、路由器或耳机吧。">
        <el-button type="primary" :icon="Plus" @click="router.push('/devices/create')">新增第一台设备</el-button>
      </el-empty>

      <article v-for="group in roomGroups" v-else :key="group.location" class="room-section">
        <header class="room-header">
          <div>
            <p class="room-label">家庭场景</p>
            <h2>{{ group.location }}</h2>
          </div>
          <div class="room-meta">
            <span>{{ group.devices.length }} 台设备</span>
            <span v-if="group.attentionCount > 0">{{ group.attentionCount }} 件待关注</span>
            <span v-else>状态平稳</span>
          </div>
        </header>

        <div class="device-card-grid">
          <article
            v-for="device in group.devices"
            :key="device.id"
            class="device-passport-card"
            :class="{ 'is-attention': isAttentionDevice(device) }"
          >
            <div class="device-card-top">
              <div class="passport-stamp" aria-hidden="true">{{ getCategoryInitial(device) }}</div>
              <el-tag :type="statusType(device.status)" effect="light">
                {{ labelOf(deviceStatusOptions, device.status) }}
              </el-tag>
            </div>

            <button class="device-card-main" type="button" @click="openDevicePassport(device.id)">
              <span>{{ device.name }}</span>
              <small>{{ getBrandModel(device) }}</small>
            </button>

            <div class="device-card-facts">
              <div>
                <span>分类</span>
                <strong>{{ device.categoryName || '未分类' }}</strong>
              </div>
              <div>
                <span>购买日期</span>
                <strong>{{ device.purchaseDate || '待补充' }}</strong>
              </div>
              <div>
                <span>保修状态</span>
                <strong>{{ getWarrantyText(device.warrantyStatus) }}</strong>
              </div>
              <div>
                <span>下次提醒</span>
                <strong>{{ device.nextReminderDate || '暂无提醒' }}</strong>
              </div>
            </div>

            <div class="device-card-actions">
              <el-button type="primary" plain @click="openDevicePassport(device.id)">打开档案</el-button>
              <el-button plain @click="router.push(`/devices/${device.id}/edit`)">编辑</el-button>
              <el-dropdown @command="(status: string) => handleStatus(device, status)">
                <el-button plain>改状态</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="item in deviceStatusOptions" :key="item.value" :command="item.value">
                      {{ item.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button text type="danger" @click="handleDelete(device)">删除</el-button>
            </div>
          </article>
        </div>
      </article>
    </section>

    <el-card class="glass-card" shadow="never">
      <el-collapse v-model="advancedPanels" class="advanced-collapse">
        <el-collapse-item name="table">
          <template #title>
            <div class="advanced-title">
              <strong>高级清单视图</strong>
              <span>用于精确筛选、批量浏览和快速定位设备</span>
            </div>
          </template>
          <el-table v-loading="loading" :data="devices">
            <el-table-column prop="name" label="设备" min-width="170">
              <template #default="{ row }">
                <el-link type="primary" @click="openDevicePassport(row.id)">{{ row.name }}</el-link>
                <div class="muted">{{ getBrandModel(row) }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column prop="location" label="位置" width="120" />
            <el-table-column prop="purchaseDate" label="购买日期" width="130" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ labelOf(deviceStatusOptions, row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" @click="router.push(`/devices/${row.id}/edit`)">编辑</el-button>
                  <el-dropdown class="table-action-dropdown" @command="(status: string) => handleStatus(row, status)">
                    <el-button link type="primary">改状态</el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item v-for="item in deviceStatusOptions" :key="item.value" :command="item.value">
                          {{ item.label }}
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                  <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
      </el-collapse>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        class="pager"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @change="loadData"
      />
    </el-card>
  </div>
</template>

<style scoped>
.device-list-page {
  gap: 20px;
}

.device-list-summary {
  position: relative;
  overflow: hidden;
  isolation: isolate;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(300px, 420px);
  gap: 18px;
  align-items: stretch;
  padding: 20px;
  border: 1px solid var(--fl-glass-line);
  border-radius: var(--fl-radius-lg);
  background: var(--fl-glass-strong);
  box-shadow: var(--fl-shadow-md);
  backdrop-filter: blur(32px) saturate(190%);
  -webkit-backdrop-filter: blur(32px) saturate(190%);
}

.device-list-summary::before {
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

.device-list-summary::after {
  position: absolute;
  inset: 0;
  border: 1px solid rgba(255, 255, 255, 0.42);
  border-radius: inherit;
  content: '';
  pointer-events: none;
}

.summary-copy,
.summary-side {
  position: relative;
  z-index: 1;
}

.summary-kicker {
  display: inline-flex;
  color: var(--fl-primary-strong);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
}

.device-list-summary h1 {
  max-width: 720px;
  margin: 10px 0 10px;
  color: var(--fl-ink);
  font-size: clamp(24px, 2.4vw, 30px);
  font-weight: 800;
  letter-spacing: 0;
  line-height: 1.2;
}

.device-list-summary p {
  max-width: 650px;
  margin: 0;
  color: var(--fl-muted);
  line-height: 1.85;
}

.summary-side {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 18px;
}

.summary-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.summary-actions :deep(.el-button) {
  margin-left: 0;
}

.summary-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.summary-stats div {
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.66);
  border-radius: 18px;
  background: var(--fl-glass-chip);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.64), 0 8px 22px rgba(31, 41, 55, 0.035);
}

.summary-stats strong {
  display: block;
  color: var(--fl-ink);
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
}

.summary-stats span {
  display: block;
  margin-top: 8px;
  color: var(--fl-muted);
  font-size: 13px;
  font-weight: 700;
}

.filter-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.filter-title span {
  color: var(--fl-ink);
  font-size: 16px;
  font-weight: 800;
}

.filter-title small {
  color: var(--fl-muted);
}

.device-filter-form :deep(.el-form-item) {
  margin-bottom: 10px;
}

.room-wall {
  display: flex;
  min-height: 220px;
  flex-direction: column;
  gap: 18px;
}

.room-section {
  padding: 20px;
  border: 1px solid var(--fl-glass-line);
  border-radius: var(--fl-radius-lg);
  background: var(--fl-glass);
  box-shadow: var(--fl-shadow-sm);
  backdrop-filter: blur(28px) saturate(185%);
  -webkit-backdrop-filter: blur(28px) saturate(185%);
}

.room-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.room-label {
  margin: 0 0 4px;
  color: var(--fl-muted);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
}

.room-header h2 {
  margin: 0;
  color: var(--fl-ink);
  font-size: 21px;
  font-weight: 800;
  letter-spacing: 0;
}

.room-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.room-meta span {
  padding: 7px 11px;
  border-radius: 999px;
  background: var(--fl-glass-tint);
  color: var(--fl-primary-strong);
  font-size: 12px;
  font-weight: 800;
}

.device-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(276px, 1fr));
  gap: 14px;
}

.device-passport-card {
  display: flex;
  min-height: 268px;
  flex-direction: column;
  gap: 16px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.64);
  border-radius: 20px;
  background: var(--fl-glass-chip);
  box-shadow: none;
  backdrop-filter: blur(20px) saturate(175%);
  -webkit-backdrop-filter: blur(20px) saturate(175%);
  transition:
    box-shadow 0.18s ease,
    border-color 0.18s ease;
}

.device-passport-card:hover {
  border-color: rgba(255, 105, 0, 0.32);
  box-shadow: var(--fl-shadow-sm);
}

.device-passport-card.is-attention {
  border-left: 4px solid var(--fl-warning);
}

.device-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.passport-stamp {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.7), rgba(255, 244, 235, 0.5));
  color: var(--fl-primary-strong);
  font-size: 21px;
  font-weight: 800;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
}

.device-card-main {
  display: flex;
  width: 100%;
  padding: 0;
  border: none;
  background: transparent;
  color: inherit;
  cursor: pointer;
  flex-direction: column;
  gap: 6px;
  text-align: left;
}

.device-card-main span {
  color: var(--fl-ink);
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0;
}

.device-card-main small {
  color: var(--fl-muted);
  font-size: 13px;
}

.device-card-main:focus-visible {
  outline: 3px solid rgba(255, 105, 0, 0.24);
  outline-offset: 4px;
}

.device-card-facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.device-card-facts div {
  min-width: 0;
  padding: 10px;
  border: 1px solid rgba(255, 255, 255, 0.62);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.56);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.58);
}

.device-card-facts span,
.advanced-title span,
.muted {
  color: var(--fl-muted);
  font-size: 12px;
}

.device-card-facts strong {
  display: block;
  overflow: hidden;
  margin-top: 5px;
  color: var(--fl-ink);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: auto;
}

.device-card-actions :deep(.el-button) {
  margin-left: 0;
}

.advanced-collapse {
  --el-collapse-header-bg-color: transparent;
  --el-collapse-content-bg-color: transparent;
  border-top: none;
  border-bottom: none;
}

.advanced-collapse :deep(.el-collapse-item__header) {
  border-bottom: none;
}

.advanced-title {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.advanced-title strong {
  color: var(--fl-ink);
  font-size: 16px;
}

.muted {
  margin-top: 4px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  line-height: 1;
  white-space: nowrap;
}

.table-actions :deep(.el-button) {
  height: auto;
  margin-left: 0;
  padding: 0;
}

.table-action-dropdown,
.table-action-dropdown :deep(.el-tooltip__trigger) {
  display: flex;
  align-items: center;
}

@media (max-width: 1080px) {
  .device-list-summary {
    grid-template-columns: 1fr;
  }

  .summary-side {
    align-items: stretch;
  }
}

@media (max-width: 720px) {
  .device-list-summary,
  .room-section {
    border-radius: var(--fl-radius-lg);
  }

  .summary-stats,
  .device-card-facts {
    grid-template-columns: 1fr;
  }

  .room-header,
  .filter-title,
  .advanced-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .room-meta {
    justify-content: flex-start;
  }

  .device-card-grid {
    grid-template-columns: 1fr;
  }

  .pager {
    justify-content: flex-start;
  }
}
</style>
