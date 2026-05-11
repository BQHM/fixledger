<script setup lang="ts">
import { Plus, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import {
  createWarranty,
  deleteWarranty,
  getExpiringWarranties,
  updateWarranty,
  type WarrantyForm
} from '@/api/warranty';
import { getDevicePage } from '@/api/device';
import { useAuthStore } from '@/stores/auth';
import type { WarrantyRecord } from '@/types/business';
import type { DeviceListItem } from '@/types/device';
import { labelOf, statusType, warrantyTypeOptions } from '@/utils/dicts';

const auth = useAuthStore();
const router = useRouter();
const familyId = computed(() => auth.currentFamilyId);
const formRef = ref<FormInstance>();
const loading = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const warranties = ref<WarrantyRecord[]>([]);
const devices = ref<DeviceListItem[]>([]);
const total = ref(0);
const editingWarrantyId = ref<number>();

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  days: 30
});

const form = reactive<WarrantyForm & { deviceId?: number }>({
  deviceId: undefined,
  warrantyType: 'OFFICIAL',
  startDate: '',
  endDate: '',
  remindDaysBefore: 30,
  servicePhone: '',
  serviceAddress: '',
  serviceNote: ''
});

const rules: FormRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择保修开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择保修结束日期', trigger: 'change' }]
};

async function loadData() {
  if (!familyId.value) return;
  loading.value = true;
  try {
    const [warrantyPage, devicePage] = await Promise.all([
      getExpiringWarranties(familyId.value, query),
      getDevicePage(familyId.value, { pageNum: 1, pageSize: 100 })
    ]);
    warranties.value = warrantyPage.records;
    total.value = warrantyPage.total;
    devices.value = devicePage.records;
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  editingWarrantyId.value = undefined;
  Object.assign(form, {
    deviceId: undefined,
    warrantyType: 'OFFICIAL',
    startDate: '',
    endDate: '',
    remindDaysBefore: 30,
    servicePhone: '',
    serviceAddress: '',
    serviceNote: ''
  });
}

function openCreate() {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: WarrantyRecord) {
  isEdit.value = true;
  editingWarrantyId.value = row.id;
  Object.assign(form, { ...row });
  dialogVisible.value = true;
}

async function submit() {
  await formRef.value?.validate();
  if (!familyId.value || !form.deviceId) return;
  const payload: WarrantyForm = {
    warrantyType: form.warrantyType,
    startDate: form.startDate,
    endDate: form.endDate,
    remindDaysBefore: form.remindDaysBefore,
    servicePhone: form.servicePhone,
    serviceAddress: form.serviceAddress,
    serviceNote: form.serviceNote
  };
  if (isEdit.value && editingWarrantyId.value) {
    await updateWarranty(familyId.value, editingWarrantyId.value, payload);
    ElMessage.success('保修记录已更新');
  } else {
    await createWarranty(familyId.value, form.deviceId, payload);
    ElMessage.success('保修记录已创建');
  }
  dialogVisible.value = false;
  await loadData();
}

async function handleDelete(row: WarrantyRecord) {
  await ElMessageBox.confirm(`确认删除「${row.deviceName || row.deviceId}」的保修记录吗？`, '删除保修', {
    type: 'warning'
  });
  await deleteWarranty(familyId.value!, row.id);
  await loadData();
}

function remainingDays(endDate: string) {
  const end = new Date(`${endDate}T00:00:00`).getTime();
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  return Math.ceil((end - now.getTime()) / 86400000);
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
        <h1 class="page-title">保修管理</h1>
        <p class="page-subtitle">集中查看即将过保的设备，并维护保修类型、售后电话和提醒提前量。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增保修</el-button>
    </div>

    <el-card class="glass-card toolbar-card" shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="过保窗口">
          <el-input-number v-model="query.days" :min="0" :max="3650" />
          <span class="field-suffix">天内</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="glass-card" shadow="never">
      <el-table v-loading="loading" :data="warranties">
        <el-table-column prop="deviceName" label="设备" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/devices/${row.deviceId}`)">
              {{ row.deviceName || `设备 ${row.deviceId}` }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="保修类型" width="120">
          <template #default="{ row }">{{ labelOf(warrantyTypeOptions, row.warrantyType) }}</template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="remainingDays(row.endDate) < 0 ? 'danger' : statusType('DUE_SOON')">
              {{ remainingDays(row.endDate) < 0 ? '已过保' : `剩余 ${remainingDays(row.endDate)} 天` }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="servicePhone" label="售后电话" min-width="130" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="router.push(`/devices/${row.deviceId}`)">设备详情</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑保修记录' : '新增保修记录'" width="680px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="form-grid">
        <el-form-item label="关联设备" prop="deviceId">
          <el-select v-model="form.deviceId" :disabled="isEdit" filterable placeholder="选择设备">
            <el-option v-for="item in devices" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="保修类型">
          <el-select v-model="form.warrantyType">
            <el-option v-for="item in warrantyTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="提前提醒天数">
          <el-input-number v-model="form.remindDaysBefore" :min="0" />
        </el-form-item>
        <el-form-item label="售后电话">
          <el-input v-model="form.servicePhone" />
        </el-form-item>
        <el-form-item class="full-row" label="售后地址">
          <el-input v-model="form.serviceAddress" />
        </el-form-item>
        <el-form-item class="full-row" label="服务备注">
          <el-input v-model="form.serviceNote" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.field-suffix {
  margin-left: 8px;
  color: var(--fl-muted);
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

