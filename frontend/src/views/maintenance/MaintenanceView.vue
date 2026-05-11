<script setup lang="ts">
import { Money, Plus, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import {
  createMaintenance,
  deleteMaintenance,
  getMaintenanceCostSummary,
  getMaintenancePage,
  updateMaintenanceStatus,
  type MaintenanceForm,
  type MaintenanceStatusForm
} from '@/api/maintenance';
import { getDevicePage } from '@/api/device';
import { useAuthStore } from '@/stores/auth';
import type { MaintenanceRecord } from '@/types/business';
import type { DeviceListItem } from '@/types/device';
import { labelOf, maintenanceStatusOptions, statusType } from '@/utils/dicts';

const auth = useAuthStore();
const router = useRouter();
const familyId = computed(() => auth.currentFamilyId);
const loading = ref(false);
const dialogVisible = ref(false);
const statusDialogVisible = ref(false);
const formRef = ref<FormInstance>();
const statusFormRef = ref<FormInstance>();
const records = ref<MaintenanceRecord[]>([]);
const devices = ref<DeviceListItem[]>([]);
const total = ref(0);
const costSummary = ref({ totalCost: 0, recordCount: 0 });
const selectedRecord = ref<MaintenanceRecord>();

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  deviceId: undefined as number | undefined,
  status: ''
});

const form = reactive<MaintenanceForm & { deviceId?: number }>({
  deviceId: undefined,
  title: '',
  faultDescription: '',
  occurredAt: '',
  repairChannel: '',
  repairContact: ''
});

const statusForm = reactive<MaintenanceStatusForm>({
  status: 'REPORTED',
  resultDescription: '',
  repairCost: undefined,
  completedAt: '',
  syncDeviceRepaired: false
});

const rules: FormRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  title: [{ required: true, message: '请输入维修标题', trigger: 'blur' }],
  faultDescription: [{ required: true, message: '请输入故障描述', trigger: 'blur' }]
};

const statusRules: FormRules = {
  status: [{ required: true, message: '请选择目标状态', trigger: 'change' }]
};

async function loadData() {
  if (!familyId.value) return;
  loading.value = true;
  try {
    const [page, devicePage, summary] = await Promise.all([
      getMaintenancePage(familyId.value, query),
      getDevicePage(familyId.value, { pageNum: 1, pageSize: 100 }),
      getMaintenanceCostSummary(familyId.value)
    ]);
    records.value = page.records;
    total.value = page.total;
    devices.value = devicePage.records;
    costSummary.value = summary;
  } finally {
    loading.value = false;
  }
}

function resetQuery() {
  query.deviceId = undefined;
  query.status = '';
  query.pageNum = 1;
  loadData();
}

function openCreate() {
  Object.assign(form, {
    deviceId: undefined,
    title: '',
    faultDescription: '',
    occurredAt: '',
    repairChannel: '',
    repairContact: ''
  });
  dialogVisible.value = true;
}

async function submit() {
  await formRef.value?.validate();
  if (!familyId.value || !form.deviceId) return;
  await createMaintenance(familyId.value, form.deviceId, form);
  ElMessage.success('维修记录已创建');
  dialogVisible.value = false;
  await loadData();
}

function openStatus(row: MaintenanceRecord) {
  selectedRecord.value = row;
  Object.assign(statusForm, {
    status: row.status,
    resultDescription: row.resultDescription || '',
    repairCost: row.repairCost,
    completedAt: row.completedAt || '',
    syncDeviceRepaired: false
  });
  statusDialogVisible.value = true;
}

async function submitStatus() {
  await statusFormRef.value?.validate();
  if (!familyId.value || !selectedRecord.value) return;
  await updateMaintenanceStatus(familyId.value, selectedRecord.value.id, statusForm);
  ElMessage.success('维修状态已更新');
  statusDialogVisible.value = false;
  await loadData();
}

async function handleDelete(row: MaintenanceRecord) {
  await ElMessageBox.confirm(`确认删除维修记录「${row.title}」吗？`, '删除维修记录', { type: 'warning' });
  await deleteMaintenance(familyId.value!, row.id);
  await loadData();
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
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">维修记录</h1>
        <p class="page-subtitle">跟踪故障、报修、维修中到完成的状态流转，并沉淀维修费用。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增维修</el-button>
    </div>

    <div class="metric-grid compact-grid">
      <div class="metric-card">
        <div class="metric-label">维修记录数</div>
        <div class="metric-value">{{ costSummary.recordCount }}<small>条</small></div>
      </div>
      <div class="metric-card">
        <div class="metric-label">累计维修费用</div>
        <div class="metric-value"><el-icon><Money /></el-icon>{{ costSummary.totalCost }}<small>元</small></div>
      </div>
    </div>

    <el-card class="glass-card toolbar-card" shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="设备">
          <el-select v-model="query.deviceId" clearable filterable placeholder="全部设备" style="width: 180px">
            <el-option v-for="item in devices" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 150px">
            <el-option v-for="item in maintenanceStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="glass-card" shadow="never">
      <el-table v-loading="loading" :data="records">
        <el-table-column prop="title" label="维修事项" min-width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/maintenance/${row.id}`)">{{ row.title }}</el-link>
            <div class="muted">{{ row.faultDescription }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="deviceName" label="设备" min-width="130">
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/devices/${row.deviceId}`)">
              {{ row.deviceName || `设备 ${row.deviceId}` }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="occurredAt" label="故障时间" width="170" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ labelOf(maintenanceStatusOptions, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="repairChannel" label="维修渠道" width="120" />
        <el-table-column prop="repairCost" label="费用" width="100" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openStatus(row)">状态流转</el-button>
            <el-button link type="primary" @click="router.push(`/maintenance/${row.id}`)">详情</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        class="pager"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增维修记录" width="680px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="form-grid">
        <el-form-item label="设备" prop="deviceId">
          <el-select v-model="form.deviceId" filterable placeholder="选择设备">
            <el-option v-for="item in devices" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="故障时间">
          <el-date-picker v-model="form.occurredAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="维修渠道">
          <el-input v-model="form.repairChannel" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.repairContact" />
        </el-form-item>
        <el-form-item class="full-row" label="故障描述" prop="faultDescription">
          <el-input v-model="form.faultDescription" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusDialogVisible" title="维修状态流转" width="620px">
      <el-form ref="statusFormRef" :model="statusForm" :rules="statusRules" label-position="top" class="form-grid">
        <el-form-item label="目标状态" prop="status">
          <el-select v-model="statusForm.status">
            <el-option v-for="item in maintenanceStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修费用">
          <el-input-number v-model="statusForm.repairCost" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="完成时间">
          <el-date-picker v-model="statusForm.completedAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="同步设备状态">
          <el-switch v-model="statusForm.syncDeviceRepaired" active-text="同步为已维修" />
        </el-form-item>
        <el-form-item class="full-row" label="处理结果">
          <el-input v-model="statusForm.resultDescription" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStatus">保存状态</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.compact-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.metric-value {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.metric-value small,
.muted {
  color: var(--fl-muted);
}

.muted {
  overflow: hidden;
  max-width: 360px;
  margin-top: 4px;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

