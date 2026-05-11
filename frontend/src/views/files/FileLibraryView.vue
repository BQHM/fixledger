<script setup lang="ts">
import { Download, Search, UploadFilled } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { computed, reactive, ref } from 'vue';

import { deleteFile, downloadFile, getFiles, uploadFile } from '@/api/file';
import { useAuthStore } from '@/stores/auth';
import type { FileResource } from '@/types/business';
import { fileBizTypeOptions, formatFileSize, labelOf } from '@/utils/dicts';

const auth = useAuthStore();
const familyId = computed(() => auth.currentFamilyId);
const formRef = ref<FormInstance>();
const loading = ref(false);
const files = ref<FileResource[]>([]);

const query = reactive({
  bizType: 'DEVICE',
  bizId: undefined as number | undefined
});

const rules: FormRules = {
  bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  bizId: [{ required: true, message: '请输入关联业务 ID', trigger: 'blur' }]
};

async function loadData() {
  await formRef.value?.validate();
  if (!familyId.value || !query.bizId) return;
  loading.value = true;
  try {
    files.value = await getFiles(familyId.value, query.bizType, query.bizId);
  } finally {
    loading.value = false;
  }
}

async function handleUpload(options: { file: File }) {
  await formRef.value?.validate();
  if (!familyId.value || !query.bizId) return;
  await uploadFile(familyId.value, query.bizType, query.bizId, options.file);
  ElMessage.success('附件已上传');
  await loadData();
}

async function handleDelete(row: FileResource) {
  await ElMessageBox.confirm(`确认删除附件「${row.originalName}」吗？`, '删除附件', { type: 'warning' });
  await deleteFile(familyId.value!, row.id);
  await loadData();
}
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">附件库</h1>
        <p class="page-subtitle">统一管理发票、保修卡、说明书、维修单和售后截图；文件访问仍由后端鉴权。</p>
      </div>
    </div>

    <el-card class="glass-card toolbar-card" shadow="never">
      <el-form ref="formRef" :inline="true" :model="query" :rules="rules">
        <el-form-item label="业务类型" prop="bizType">
          <el-select v-model="query.bizType" style="width: 160px">
            <el-option v-for="item in fileBizTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联业务 ID" prop="bizId">
          <el-input-number v-model="query.bizId" :min="1" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询附件</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="card-header-row">
          <span>附件列表</span>
          <el-upload :http-request="handleUpload" :show-file-list="false">
            <el-button type="primary" :icon="UploadFilled">上传附件</el-button>
          </el-upload>
        </div>
      </template>
      <el-alert
        class="tip-alert"
        title="当前后端附件接口按业务类型 + 业务 ID 查询，设备详情页也会展示设备关联附件。"
        type="info"
        :closable="false"
        show-icon
      />
      <el-table v-loading="loading" :data="files">
        <el-table-column prop="originalName" label="文件名" min-width="220" />
        <el-table-column label="业务类型" width="120">
          <template #default="{ row }">{{ labelOf(fileBizTypeOptions, row.bizType) }}</template>
        </el-table-column>
        <el-table-column prop="bizId" label="业务 ID" width="100" />
        <el-table-column prop="contentType" label="MIME 类型" width="170" />
        <el-table-column label="大小" width="110">
          <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Download" @click="downloadFile(familyId!, row)">下载</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tip-alert {
  margin-bottom: 16px;
  border-radius: 14px;
}
</style>
