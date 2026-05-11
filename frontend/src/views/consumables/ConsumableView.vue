<script setup lang="ts">
import { Plus, Refresh, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import {
  createConsumable,
  createReplaceRecord,
  deleteConsumable,
  getDueSoonConsumables,
  updateConsumable,
  type ConsumableForm,
  type ReplaceRecordForm
} from '@/api/consumable';
import { getDevicePage } from '@/api/device';
import { useAuthStore } from '@/stores/auth';
import type { ConsumableItem } from '@/types/business';
import type { DeviceListItem } from '@/types/device';
import { consumableStatusOptions, labelOf, statusType } from '@/utils/dicts';

const auth = useAuthStore();
const router = useRouter();
const familyId = computed(() => auth.currentFamilyId);
const loading = ref(false);
const formRef = ref<FormInstance>();
const replaceFormRef = ref<FormInstance>();
const dialogVisible = ref(false);
const replaceDialogVisible = ref(false);
const isEdit = ref(false);
const consumables = ref<ConsumableItem[]>([]);
const devices = ref<DeviceListItem[]>([]);
const total = ref(0);
const editingConsumableId = ref<number>();
const replacingConsumable = ref<ConsumableItem>();

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  days: 7
});

const form = reactive<ConsumableForm & { deviceId?: number }>({
  deviceId: undefined,
  name: '',
  brand: '',
  model: '',
  cycleDays: 180,
  lastReplacedDate: '',
  remindDaysBefore: 15,
  enabled: true,
  remark: ''
});

const replaceForm = reactive<ReplaceRecordForm>({
  replacedDate: '',
  cost: undefined,
  note: ''
});

const rules: FormRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  name: [{ required: true, message: '请输入耗材名称', trigger: 'blur' }],
  cycleDays: [{ required: true, message: '请输入更换周期', trigger: 'blur' }]
};

const replaceRules: FormRules = {
  replacedDate: [{ required: true, message: '请选择更换日期', trigger: 'change' }]
};

async function loadData() {
  if (!familyId.value) return;
  loading.value = true;
  try {
    const [consumablePage, devicePage] = await Promise.all([
      getDueSoonConsumables(familyId.value, query),
      getDevicePage(familyId.value, { pageNum: 1, pageSize: 100 })
    ]);
    consumables.value = consumablePage.records;
    total.value = consumablePage.total;
    devices.value = devicePage.records;
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  editingConsumableId.value = undefined;
  Object.assign(form, {
    deviceId: undefined,
    name: '',
    brand: '',
    model: '',
    cycleDays: 180,
    lastReplacedDate: '',
    remindDaysBefore: 15,
    enabled: true,
    remark: ''
  });
}

function openCreate() {
  resetForm();
  isEdit.value = false;
  dialogVisible.value = true;
}

function openEdit(row: ConsumableItem) {
  isEdit.value = true;
  editingConsumableId.value = row.id;
  Object.assign(form, row);
  dialogVisible.value = true;
}

async function submit() {
  await formRef.value?.validate();
  if (!familyId.value || !form.deviceId) return;
  if (isEdit.value && editingConsumableId.value) {
    await updateConsumable(familyId.value, editingConsumableId.value, form);
    ElMessage.success('耗材已更新');
  } else {
    await createConsumable(familyId.value, form.deviceId, form);
    ElMessage.success('耗材已创建');
  }
  dialogVisible.value = false;
  await loadData();
}

function openReplace(row: ConsumableItem) {
  replacingConsumable.value = row;
  Object.assign(replaceForm, {
    replacedDate: new Date().toISOString().slice(0, 10),
    cost: undefined,
    note: ''
  });
  replaceDialogVisible.value = true;
}

async function submitReplace() {
  await replaceFormRef.value?.validate();
  if (!familyId.value || !replacingConsumable.value) return;
  await createReplaceRecord(familyId.value, replacingConsumable.value.id, replaceForm);
  ElMessage.success('更换记录已保存，下次提醒日期已重新计算');
  replaceDialogVisible.value = false;
  await loadData();
}

async function handleDelete(row: ConsumableItem) {
  await ElMessageBox.confirm(`确认删除耗材「${row.name}」吗？`, '删除耗材', { type: 'warning' });
  await deleteConsumable(familyId.value!, row.id);
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
        <h1 class="page-title">耗材管理</h1>
        <p class="page-subtitle">管理滤芯、滤网、电池等周期性耗材，记录更换后自动刷新提醒日期。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增耗材</el-button>
    </div>

    <el-card class="glass-card toolbar-card" shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="提醒窗口">
          <el-input-number v-model="query.days" :min="0" :max="3650" />
          <span class="field-suffix">天内</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询临期耗材</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="glass-card" shadow="never">
      <el-table v-loading="loading" :data="consumables">
        <el-table-column prop="name" label="耗材" min-width="160">
          <template #default="{ row }">
            <strong>{{ row.name }}</strong>
            <div class="muted">{{ row.brand }} {{ row.model }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="deviceName" label="所属设备" min-width="140">
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/devices/${row.deviceId}`)">
              {{ row.deviceName || `设备 ${row.deviceId}` }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="cycleDays" label="周期" width="100">
          <template #default="{ row }">{{ row.cycleDays }} 天</template>
        </el-table-column>
        <el-table-column prop="lastReplacedDate" label="上次更换" width="130" />
        <el-table-column prop="nextRemindDate" label="下次提醒" width="130" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ labelOf(consumableStatusOptions, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Refresh" @click="openReplace(row)">记录更换</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑耗材' : '新增耗材'" width="680px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="form-grid">
        <el-form-item label="所属设备" prop="deviceId">
          <el-select v-model="form.deviceId" :disabled="isEdit" filterable placeholder="选择设备">
            <el-option v-for="item in devices" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="耗材名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：净水器 PP 棉滤芯" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="form.brand" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.model" />
        </el-form-item>
        <el-form-item label="更换周期" prop="cycleDays">
          <el-input-number v-model="form.cycleDays" :min="1" />
        </el-form-item>
        <el-form-item label="上次更换">
          <el-date-picker v-model="form.lastReplacedDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="提前提醒天数">
          <el-input-number v-model="form.remindDaysBefore" :min="0" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="启用状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item class="full-row" label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="replaceDialogVisible" title="记录耗材更换" width="520px">
      <el-alert
        v-if="replacingConsumable"
        :closable="false"
        type="info"
        :title="`更换后系统会按 ${replacingConsumable.cycleDays} 天周期重新计算下次提醒日期。`"
      />
      <el-form ref="replaceFormRef" :model="replaceForm" :rules="replaceRules" label-position="top" class="replace-form">
        <el-form-item label="更换日期" prop="replacedDate">
          <el-date-picker v-model="replaceForm.replacedDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="更换费用">
          <el-input-number v-model="replaceForm.cost" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="replaceForm.note" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replaceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReplace">保存更换记录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.field-suffix,
.muted {
  color: var(--fl-muted);
}

.muted {
  margin-top: 4px;
  font-size: 12px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}

.replace-form {
  margin-top: 18px;
}
</style>

