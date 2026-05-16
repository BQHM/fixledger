<script setup lang="ts">
import { Plus, Search } from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { deleteDevice, getDeviceCategories, getDevicePage, updateDeviceStatus } from '@/api/device';
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

onMounted(() => {
  loadData();
  window.addEventListener('family-changed', loadData);
});

onUnmounted(() => {
  window.removeEventListener('family-changed', loadData);
});
</script>

<template>
  <div class="page-shell device-passport-page">
    <section class="passport-hero">
      <div class="hero-copy">
        <div class="hero-kicker">设备护照</div>
        <h1>按房间收好每一台家庭设备</h1>
        <p>
          用卡片墙记录设备购买、保修、耗材、维修和凭证，让面试演示先看到家庭场景，表格作为高级清单保留。
        </p>
      </div>
      <div class="hero-side">
        <div class="hero-stats" aria-label="设备护照摘要">
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
        <el-button type="primary" size="large" :icon="Plus" @click="router.push('/devices/create')">
          新增设备护照
        </el-button>
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

    <section v-loading="loading" class="room-wall" aria-label="按房间分组的设备护照">
      <el-empty v-if="!loading && devices.length === 0" description="还没有设备护照。先添加一台家里的净水器、路由器或耳机吧。">
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
              <el-button type="primary" plain @click="openDevicePassport(device.id)">打开护照</el-button>
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
              <span>给精确筛选、批量浏览和面试时说明后台能力使用</span>
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
.device-passport-page {
  gap: 20px;
}

.passport-hero {
  position: relative;
  display: grid;
  overflow: hidden;
  grid-template-columns: minmax(0, 1fr) minmax(300px, 420px);
  gap: 28px;
  align-items: stretch;
  padding: clamp(24px, 4vw, 42px);
  border: 1px solid rgba(47, 125, 104, 0.14);
  border-radius: 32px;
  background:
    radial-gradient(circle at 8% 12%, rgba(242, 166, 90, 0.28), transparent 28%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(222, 239, 231, 0.86));
  box-shadow: 0 24px 70px rgba(36, 49, 47, 0.12);
}

.passport-hero::after {
  position: absolute;
  right: -72px;
  bottom: -92px;
  width: 240px;
  height: 240px;
  border: 32px solid rgba(47, 125, 104, 0.1);
  border-radius: 999px;
  content: '';
}

.hero-copy,
.hero-side {
  position: relative;
  z-index: 1;
}

.hero-kicker {
  display: inline-flex;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(47, 125, 104, 0.12);
  color: var(--fl-green-dark);
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 0.18em;
}

.passport-hero h1 {
  max-width: 680px;
  margin: 18px 0 14px;
  color: var(--fl-ink);
  font-size: clamp(32px, 5vw, 56px);
  font-weight: 900;
  letter-spacing: -0.07em;
  line-height: 1;
}

.passport-hero p {
  max-width: 650px;
  margin: 0;
  color: var(--fl-muted);
  font-size: 16px;
  line-height: 1.8;
}

.hero-side {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 18px;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.hero-stats div {
  padding: 16px;
  border: 1px solid rgba(47, 125, 104, 0.12);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
}

.hero-stats strong {
  display: block;
  color: var(--fl-green-dark);
  font-size: 30px;
  font-weight: 900;
  line-height: 1;
}

.hero-stats span {
  display: block;
  margin-top: 8px;
  color: var(--fl-muted);
  font-size: 13px;
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
  font-weight: 900;
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
  border: 1px solid rgba(47, 125, 104, 0.12);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 18px 45px rgba(36, 49, 47, 0.08);
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
  color: var(--fl-orange);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.14em;
}

.room-header h2 {
  margin: 0;
  color: var(--fl-ink);
  font-size: 24px;
  letter-spacing: -0.04em;
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
  background: rgba(47, 125, 104, 0.1);
  color: var(--fl-green-dark);
  font-size: 12px;
  font-weight: 800;
}

.device-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(270px, 1fr));
  gap: 14px;
}

.device-passport-card {
  position: relative;
  display: flex;
  overflow: hidden;
  min-height: 260px;
  flex-direction: column;
  gap: 16px;
  padding: 18px;
  border: 1px solid rgba(47, 125, 104, 0.1);
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 242, 231, 0.9)),
    repeating-linear-gradient(90deg, rgba(47, 125, 104, 0.04) 0 1px, transparent 1px 18px);
  box-shadow: 0 14px 34px rgba(36, 49, 47, 0.1);
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease;
}

.device-passport-card::before {
  position: absolute;
  top: 0;
  left: 22px;
  width: 72px;
  height: 5px;
  border-radius: 0 0 12px 12px;
  background: var(--fl-orange);
  content: '';
}

.device-passport-card:hover {
  border-color: rgba(47, 125, 104, 0.28);
  box-shadow: 0 22px 45px rgba(36, 49, 47, 0.14);
  transform: translateY(-2px);
}

.device-passport-card.is-attention::before {
  background: var(--fl-danger);
}

.device-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.passport-stamp {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  border: 2px solid rgba(47, 125, 104, 0.24);
  border-radius: 16px;
  background: rgba(220, 238, 230, 0.7);
  color: var(--fl-green-dark);
  font-size: 20px;
  font-weight: 900;
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
  font-size: 22px;
  font-weight: 900;
  letter-spacing: -0.04em;
}

.device-card-main small {
  color: var(--fl-muted);
  font-size: 13px;
}

.device-card-main:focus-visible {
  outline: 3px solid rgba(47, 125, 104, 0.28);
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
  border-radius: 16px;
  background: rgba(47, 125, 104, 0.06);
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
  .passport-hero {
    grid-template-columns: 1fr;
  }

  .hero-side {
    align-items: stretch;
  }
}

@media (max-width: 720px) {
  .passport-hero,
  .room-section {
    border-radius: 22px;
  }

  .hero-stats,
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
