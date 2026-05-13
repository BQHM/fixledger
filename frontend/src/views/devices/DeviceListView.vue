<script setup lang="ts">
import { Plus, Search } from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { deleteDevice, getDeviceCategories, getDevicePage, updateDeviceStatus } from '@/api/device';
import { useAuthStore } from '@/stores/auth';
import type { DeviceCategory, DeviceListItem } from '@/types/device';
import { deviceStatusOptions, labelOf, statusType } from '@/utils/dicts';

const auth = useAuthStore();
const router = useRouter();
const loading = ref(false);
const devices = ref<DeviceListItem[]>([]);
const categories = ref<DeviceCategory[]>([]);
const total = ref(0);
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  categoryId: undefined as number | undefined,
  status: '',
  brand: ''
});
const familyId = computed(() => auth.currentFamilyId);

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
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">设备档案</h1>
        <p class="page-subtitle">管理家庭设备基础信息，后续保修、耗材、维修都围绕设备展开。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="router.push('/devices/create')">新增设备</el-button>
    </div>

    <el-card class="glass-card toolbar-card" shadow="never">
      <el-form :inline="true" :model="query">
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
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="glass-card" shadow="never">
      <el-table v-loading="loading" :data="devices">
        <el-table-column prop="name" label="设备" min-width="170">
          <template #default="{ row }">
            <el-link type="primary" @click="router.push(`/devices/${row.id}`)">{{ row.name }}</el-link>
            <div class="muted">{{ row.brand }} {{ row.model }}</div>
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
.muted {
  margin-top: 4px;
  color: var(--fl-muted);
  font-size: 12px;
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
</style>
