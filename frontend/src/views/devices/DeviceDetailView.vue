<script setup lang="ts">
import { Download, UploadFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { summarizeMaintenance } from '@/api/ai';
import { createConsumable, getDeviceConsumables } from '@/api/consumable';
import { getDeviceDetail } from '@/api/device';
import { downloadFile, getFiles, uploadFile } from '@/api/file';
import { createMaintenance, getMaintenancePage } from '@/api/maintenance';
import { createWarranty, getDeviceWarranties } from '@/api/warranty';
import { useAuthStore } from '@/stores/auth';
import type { ConsumableItem, FileResource, MaintenanceRecord, WarrantyRecord } from '@/types/business';
import type { DeviceDetail } from '@/types/device';
import {
  consumableStatusOptions,
  deviceStatusOptions,
  formatFileSize,
  labelOf,
  maintenanceStatusOptions,
  statusType,
  warrantyTypeOptions
} from '@/utils/dicts';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const familyId = computed(() => auth.currentFamilyId);
const deviceId = computed(() => Number(route.params.id));
const loading = ref(false);
const detail = ref<DeviceDetail>();
const warranties = ref<WarrantyRecord[]>([]);
const consumables = ref<ConsumableItem[]>([]);
const maintenance = ref<MaintenanceRecord[]>([]);
const files = ref<FileResource[]>([]);
const aiSummary = ref('');
const activeTab = ref('timeline');

const warrantyForm = reactive({
  warrantyType: 'OFFICIAL',
  startDate: '',
  endDate: '',
  remindDaysBefore: 30,
  servicePhone: '',
  serviceAddress: '',
  serviceNote: ''
});
const consumableForm = reactive({
  name: '',
  cycleDays: 180,
  lastReplacedDate: '',
  remindDaysBefore: 15,
  brand: '',
  model: '',
  remark: ''
});
const maintenanceForm = reactive({
  title: '',
  faultDescription: '',
  occurredAt: '',
  repairChannel: '',
  repairContact: ''
});

const timelineItems = computed(() => {
  const items = [
    detail.value?.purchaseDate
      ? { time: detail.value.purchaseDate, title: '购买设备', content: `${detail.value.name} 建立家庭设备档案` }
      : undefined,
    ...warranties.value.map((item) => ({
      time: item.startDate,
      title: '添加保修',
      content: `${labelOf(warrantyTypeOptions, item.warrantyType)} 至 ${item.endDate}`
    })),
    ...consumables.value.map((item) => ({
      time: item.nextRemindDate || item.lastReplacedDate || '',
      title: '耗材提醒',
      content: `${item.name} 下次提醒 ${item.nextRemindDate || '-'}`
    })),
    ...maintenance.value.map((item) => ({
      time: item.occurredAt || '',
      title: '维修记录',
      content: `${item.title}：${labelOf(maintenanceStatusOptions, item.status)}`
    }))
  ].filter(Boolean) as Array<{ time: string; title: string; content: string }>;
  return items.sort((a, b) => (b.time || '').localeCompare(a.time || ''));
});


const deviceInitial = computed(() => (detail.value?.categoryName || detail.value?.name || '家').slice(0, 1));
const deviceMeta = computed(() =>
  [detail.value?.brand, detail.value?.model].filter(Boolean).join(' / ') || '品牌型号待补充'
);
const lifecycleStats = computed(() => [
  { label: '保修记录', value: warranties.value.length, hint: warranties.value[0]?.endDate || '待补充' },
  { label: '耗材项', value: consumables.value.length, hint: nextConsumableDate.value || '暂无提醒' },
  { label: '维修记录', value: maintenance.value.length, hint: latestMaintenanceStatus.value || '暂无维修' },
  { label: '附件凭证', value: files.value.length, hint: files.value.length > 0 ? '已归档' : '可上传' }
]);
const nextConsumableDate = computed(() => {
  return consumables.value
    .map((item) => item.nextRemindDate)
    .filter(Boolean)
    .sort()[0];
});
const latestMaintenanceStatus = computed(() => {
  const latest = maintenance.value
    .filter((item) => item.occurredAt)
    .sort((a, b) => (b.occurredAt || '').localeCompare(a.occurredAt || ''))[0];
  return latest ? labelOf(maintenanceStatusOptions, latest.status) : '';
});

async function loadData() {
  if (!familyId.value) return;
  loading.value = true;
  try {
    const [device, warrantyList, consumableList, maintenancePage, fileList] = await Promise.all([
      getDeviceDetail(familyId.value, deviceId.value),
      getDeviceWarranties(familyId.value, deviceId.value),
      getDeviceConsumables(familyId.value, deviceId.value),
      getMaintenancePage(familyId.value, { pageNum: 1, pageSize: 20, deviceId: deviceId.value }),
      getFiles(familyId.value, 'DEVICE', deviceId.value).catch(() => [])
    ]);
    detail.value = device;
    warranties.value = warrantyList;
    consumables.value = consumableList;
    maintenance.value = maintenancePage.records;
    files.value = fileList;
  } finally {
    loading.value = false;
  }
}

async function addWarranty() {
  await createWarranty(familyId.value!, deviceId.value, warrantyForm);
  ElMessage.success('保修记录已添加');
  await loadData();
}

async function addConsumable() {
  await createConsumable(familyId.value!, deviceId.value, consumableForm);
  ElMessage.success('耗材已添加');
  await loadData();
}

async function addMaintenance() {
  await createMaintenance(familyId.value!, deviceId.value, maintenanceForm);
  ElMessage.success('维修记录已添加');
  await loadData();
}

async function handleUpload(options: { file: File }) {
  await uploadFile(familyId.value!, 'DEVICE', deviceId.value, options.file);
  ElMessage.success('设备附件已上传');
  await loadData();
}

async function generateSummary() {
  const result = await summarizeMaintenance(familyId.value!, deviceId.value);
  aiSummary.value = `${result.summary}\n${result.careSuggestion}`;
}

onMounted(loadData);
</script>

<template>
  <div v-loading="loading" class="page-shell">
    <section class="device-cover">
      <div class="cover-copy">
        <button class="back-link" type="button" @click="router.push('/devices')">返回设备护照</button>
        <div class="cover-kicker">设备护照 / {{ detail?.location || '未设置房间' }}</div>
        <h1>{{ detail?.name || '设备详情' }}</h1>
        <p>{{ deviceMeta }} · {{ detail?.purchaseDate || '购买日期待补充' }}</p>
        <div class="cover-actions">
          <el-tag size="large" :type="statusType(detail?.status)">
            {{ labelOf(deviceStatusOptions, detail?.status) }}
          </el-tag>
          <el-button type="primary" @click="router.push(`/devices/${deviceId}/edit`)">编辑设备</el-button>
        </div>
      </div>
      <div class="device-orb" aria-hidden="true">
        <span>{{ deviceInitial }}</span>
      </div>
    </section>

    <div class="detail-hero">
      <el-card class="glass-card device-profile-card" shadow="never">
        <div class="device-title-row">
          <div>
            <div class="device-name">基础资料</div>
            <div class="device-subtitle">购买、位置、序列号和备注集中归档。</div>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="分类">{{ detail?.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="购买日期">{{ detail?.purchaseDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="购买渠道">{{ detail?.purchaseChannel || '-' }}</el-descriptions-item>
          <el-descriptions-item label="购买价格">{{ detail?.purchasePrice || '-' }}</el-descriptions-item>
          <el-descriptions-item label="存放位置">{{ detail?.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="序列号">{{ detail?.serialNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detail?.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <div class="summary-stack">
        <div v-for="item in lifecycleStats" :key="item.label" class="mini-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </div>
      </div>
    </div>

    <el-card class="glass-card" shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="时间线" name="timeline">
          <el-timeline>
            <el-timeline-item v-for="item in timelineItems" :key="`${item.time}-${item.title}`" :timestamp="item.time">
              <strong>{{ item.title }}</strong>
              <p>{{ item.content }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-if="timelineItems.length === 0" description="暂无设备生命周期记录" />
        </el-tab-pane>

        <el-tab-pane label="保修记录" name="warranties">
          <el-table :data="warranties">
            <el-table-column label="类型" width="120">
              <template #default="{ row }">{{ labelOf(warrantyTypeOptions, row.warrantyType) }}</template>
            </el-table-column>
            <el-table-column prop="startDate" label="开始日期" width="120" />
            <el-table-column prop="endDate" label="结束日期" width="120" />
            <el-table-column prop="servicePhone" label="售后电话" />
          </el-table>
          <el-divider />
          <el-form :model="warrantyForm" class="form-grid" label-position="top">
            <el-form-item label="保修类型">
              <el-select v-model="warrantyForm.warrantyType">
                <el-option v-for="item in warrantyTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="开始日期"><el-date-picker v-model="warrantyForm.startDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
            <el-form-item label="结束日期"><el-date-picker v-model="warrantyForm.endDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
            <el-form-item label="售后电话"><el-input v-model="warrantyForm.servicePhone" /></el-form-item>
            <el-form-item class="full-row" label="说明"><el-input v-model="warrantyForm.serviceNote" /></el-form-item>
          </el-form>
          <el-button type="primary" @click="addWarranty">新增保修</el-button>
        </el-tab-pane>

        <el-tab-pane label="耗材" name="consumables">
          <el-table :data="consumables">
            <el-table-column prop="name" label="耗材" />
            <el-table-column prop="cycleDays" label="周期天数" width="100" />
            <el-table-column prop="nextRemindDate" label="下次提醒" width="130" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ labelOf(consumableStatusOptions, row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-divider />
          <el-form :model="consumableForm" class="form-grid" label-position="top">
            <el-form-item label="耗材名称"><el-input v-model="consumableForm.name" /></el-form-item>
            <el-form-item label="周期天数"><el-input-number v-model="consumableForm.cycleDays" :min="1" /></el-form-item>
            <el-form-item label="最近更换"><el-date-picker v-model="consumableForm.lastReplacedDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
            <el-form-item label="提前提醒"><el-input-number v-model="consumableForm.remindDaysBefore" :min="0" /></el-form-item>
          </el-form>
          <el-button type="primary" @click="addConsumable">新增耗材</el-button>
        </el-tab-pane>

        <el-tab-pane label="维修记录" name="maintenance">
          <el-table :data="maintenance">
            <el-table-column prop="title" label="标题">
              <template #default="{ row }">
                <el-link type="primary" @click="router.push(`/maintenance/${row.id}`)">{{ row.title }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="occurredAt" label="发生时间" width="170" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ labelOf(maintenanceStatusOptions, row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-divider />
          <el-form :model="maintenanceForm" class="form-grid" label-position="top">
            <el-form-item label="标题"><el-input v-model="maintenanceForm.title" /></el-form-item>
            <el-form-item label="发生时间"><el-date-picker v-model="maintenanceForm.occurredAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
            <el-form-item class="full-row" label="故障描述"><el-input v-model="maintenanceForm.faultDescription" type="textarea" /></el-form-item>
          </el-form>
          <el-button type="primary" @click="addMaintenance">新增维修</el-button>
          <el-button @click="generateSummary">生成 AI 维修总结</el-button>
          <el-alert v-if="aiSummary" class="ai-summary" type="success" :closable="false" :title="aiSummary" />
        </el-tab-pane>

        <el-tab-pane label="附件" name="files">
          <el-upload drag :http-request="handleUpload" :show-file-list="false">
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽发票、保修卡或说明书到这里，或点击上传</div>
          </el-upload>
          <el-table :data="files" style="margin-top: 16px">
            <el-table-column prop="originalName" label="文件名" />
            <el-table-column prop="contentType" label="类型" width="160" />
            <el-table-column label="大小" width="120">
              <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" :icon="Download" @click="downloadFile(familyId!, row)">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.device-cover {
  position: relative;
  display: grid;
  overflow: hidden;
  grid-template-columns: minmax(0, 1fr) 260px;
  gap: 24px;
  align-items: center;
  min-height: 300px;
  padding: clamp(24px, 4vw, 42px);
  border: 1px solid rgba(255, 255, 255, 0.78);
  border-radius: 38px;
  background:
    radial-gradient(circle at 16% 14%, rgba(255, 196, 122, 0.34), transparent 30%),
    radial-gradient(circle at 86% 18%, rgba(255, 255, 255, 0.72), transparent 24%),
    linear-gradient(135deg, #fffdf8 0%, #f7ecd9 52%, #edf2eb 100%);
  box-shadow: var(--fl-shadow-md);
}

.device-cover::after {
  position: absolute;
  right: -88px;
  bottom: -104px;
  width: 280px;
  height: 280px;
  border: 36px solid rgba(255, 138, 31, 0.1);
  border-radius: 999px;
  content: '';
}

.cover-copy,
.device-orb {
  position: relative;
  z-index: 1;
}

.back-link {
  padding: 0;
  border: none;
  margin-bottom: 18px;
  background: transparent;
  color: var(--fl-mi-orange-dark);
  cursor: pointer;
  font-weight: 900;
}

.cover-kicker {
  display: inline-flex;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 138, 31, 0.12);
  color: var(--fl-mi-orange-dark);
  font-size: 12px;
  font-weight: 950;
  letter-spacing: 0.16em;
}

.device-cover h1 {
  max-width: 760px;
  margin: 18px 0 12px;
  color: var(--fl-ink);
  font-size: clamp(38px, 6vw, 72px);
  font-weight: 950;
  letter-spacing: -0.08em;
  line-height: 0.98;
}

.device-cover p {
  margin: 0;
  color: var(--fl-muted);
  font-size: 16px;
}

.cover-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-top: 22px;
}

.device-orb {
  display: grid;
  width: 220px;
  height: 220px;
  place-items: center;
  justify-self: end;
  border: 1px solid rgba(255, 255, 255, 0.78);
  border-radius: 54px;
  background:
    radial-gradient(circle at 35% 24%, rgba(255, 255, 255, 0.82), transparent 30%),
    linear-gradient(145deg, #ff9b2f, #ffd18a);
  box-shadow: 0 28px 68px rgba(255, 138, 31, 0.24);
  transform: rotate(-7deg);
}

.device-orb span {
  color: #fff;
  font-size: 78px;
  font-weight: 950;
  text-shadow: 0 8px 18px rgba(132, 72, 10, 0.2);
  transform: rotate(7deg);
}

.detail-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 18px;
}

.device-profile-card {
  overflow: hidden;
}

.device-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.device-name {
  color: var(--fl-ink);
  font-size: 25px;
  font-weight: 950;
  letter-spacing: -0.04em;
}

.device-subtitle {
  margin-top: 6px;
  color: var(--fl-muted);
}

.summary-stack {
  display: grid;
  gap: 14px;
}

.mini-card {
  display: grid;
  gap: 4px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 14px 32px rgba(88, 72, 49, 0.08);
}

.mini-card span {
  color: var(--fl-muted);
  font-size: 13px;
  font-weight: 800;
}

.mini-card strong {
  color: var(--fl-ink);
  font-size: 36px;
  font-weight: 950;
  letter-spacing: -0.06em;
  line-height: 1;
}

.mini-card small {
  overflow: hidden;
  color: var(--fl-mi-orange-dark);
  font-size: 12px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ai-summary {
  margin-top: 14px;
  white-space: pre-line;
}

:deep(.el-tabs__item) {
  font-weight: 900;
}

:deep(.el-descriptions__label) {
  color: var(--fl-muted);
  font-weight: 800;
}

:deep(.el-descriptions__content) {
  color: var(--fl-ink);
  font-weight: 700;
}

@media (max-width: 1080px) {
  .device-cover,
  .detail-hero {
    grid-template-columns: 1fr;
  }

  .device-orb {
    justify-self: start;
  }
}

@media (max-width: 720px) {
  .device-cover {
    border-radius: 26px;
  }

  .device-orb {
    width: 150px;
    height: 150px;
    border-radius: 38px;
  }

  .device-orb span {
    font-size: 54px;
  }
}
</style>