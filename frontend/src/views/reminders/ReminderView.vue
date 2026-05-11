<script setup lang="ts">
import { Check, Refresh, Search } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import {
  getReminderPage,
  ignoreReminder,
  markReminderRead,
  scanReminders,
  type ReminderScanResult
} from '@/api/reminder';
import { useAuthStore } from '@/stores/auth';
import type { ReminderItem } from '@/types/business';
import {
  labelOf,
  reminderStatusOptions,
  reminderTypeOptions,
  statusType
} from '@/utils/dicts';

const auth = useAuthStore();
const router = useRouter();
const familyId = computed(() => auth.currentFamilyId);
const loading = ref(false);
const scanLoading = ref(false);
const reminders = ref<ReminderItem[]>([]);
const total = ref(0);
const scanResult = ref<ReminderScanResult>();

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  type: ''
});

async function loadData() {
  if (!familyId.value) return;
  loading.value = true;
  try {
    const page = await getReminderPage(familyId.value, query);
    reminders.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
}

function resetQuery() {
  query.status = '';
  query.type = '';
  query.pageNum = 1;
  loadData();
}

async function handleScan() {
  if (!familyId.value) return;
  scanLoading.value = true;
  try {
    scanResult.value = await scanReminders(familyId.value);
    ElMessage.success('提醒扫描已完成');
    await loadData();
  } finally {
    scanLoading.value = false;
  }
}

async function handleRead(row: ReminderItem) {
  await markReminderRead(familyId.value!, row.id);
  await loadData();
}

async function handleIgnore(row: ReminderItem) {
  await ignoreReminder(familyId.value!, row.id);
  await loadData();
}

function goBiz(row: ReminderItem) {
  if (row.bizType === 'DEVICE') router.push(`/devices/${row.bizId}`);
  if (row.bizType === 'WARRANTY') router.push('/warranties');
  if (row.bizType === 'CONSUMABLE') router.push('/consumables');
  if (row.bizType === 'MAINTENANCE') router.push(`/maintenance/${row.bizId}`);
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
        <h1 class="page-title">提醒中心</h1>
        <p class="page-subtitle">提醒任务由后端定时扫描或手动扫描生成，Redis 负责避免同一天重复提醒。</p>
      </div>
      <el-button type="primary" :icon="Refresh" :loading="scanLoading" @click="handleScan">手动扫描提醒</el-button>
    </div>

    <el-alert
      v-if="scanResult"
      class="scan-alert"
      type="success"
      :closable="false"
      show-icon
      :title="`扫描完成：保修 ${scanResult.warrantyCreated} 条，耗材 ${scanResult.consumableCreated} 条，通知 ${scanResult.notificationCreated} 条，跳过重复 ${scanResult.skippedDuplicate} 条，失败 ${scanResult.failedCount} 条。`"
    />

    <el-card class="glass-card toolbar-card" shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="提醒类型">
          <el-select v-model="query.type" clearable placeholder="全部类型" style="width: 190px">
            <el-option v-for="item in reminderTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 150px">
            <el-option v-for="item in reminderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="glass-card" shadow="never">
      <el-table v-loading="loading" :data="reminders">
        <el-table-column prop="title" label="提醒标题" min-width="220">
          <template #default="{ row }">
            <strong>{{ row.title }}</strong>
            <div class="muted">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="160">
          <template #default="{ row }">
            <el-tag :type="statusType(row.reminderType)" effect="plain">
              {{ labelOf(reminderTypeOptions, row.reminderType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remindAt" label="提醒时间" width="170" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ labelOf(reminderStatusOptions, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goBiz(row)">查看关联</el-button>
            <el-button link type="success" :icon="Check" @click="handleRead(row)">已读</el-button>
            <el-button link type="warning" @click="handleIgnore(row)">忽略</el-button>
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
  </div>
</template>

<style scoped>
.scan-alert {
  border-radius: 16px;
}

.muted {
  margin-top: 4px;
  color: var(--fl-muted);
  font-size: 12px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

