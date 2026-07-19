<script setup lang="ts">
import { MagicStick, UploadFilled } from '@element-plus/icons-vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { suggestTroubleshooting } from '@/api/ai';
import { downloadFile, getFiles, uploadFile } from '@/api/file';
import {
  getMaintenanceDetail,
  updateMaintenance,
  updateMaintenanceStatus,
  type MaintenanceForm,
  type MaintenanceStatusForm
} from '@/api/maintenance';
import { useAuthStore } from '@/stores/auth';
import type { FileResource, MaintenanceRecord } from '@/types/business';
import { formatFileSize, labelOf, maintenanceStatusOptions, statusType } from '@/utils/dicts';

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();
const familyId = computed(() => auth.currentFamilyId);
const maintenanceId = computed(() => Number(route.params.id));
const loading = ref(false);
const saving = ref(false);
const detail = ref<MaintenanceRecord>();
const files = ref<FileResource[]>([]);
const formRef = ref<FormInstance>();
const statusFormRef = ref<FormInstance>();
const suggestion = ref('');
const suggestionSteps = ref<string[]>([]);

const form = reactive<MaintenanceForm>({
  title: '',
  faultDescription: '',
  occurredAt: '',
  repairChannel: '',
  repairContact: '',
  repairCost: undefined,
  resultDescription: '',
  completedAt: ''
});

const statusForm = reactive<MaintenanceStatusForm>({
  status: 'PENDING',
  resultDescription: '',
  repairCost: undefined,
  completedAt: '',
  syncDeviceRepaired: false
});

const rules: FormRules = {
  title: [{ required: true, message: '请输入维修标题', trigger: 'blur' }],
  faultDescription: [{ required: true, message: '请输入故障描述', trigger: 'blur' }]
};

async function loadData() {
  if (!familyId.value) return;
  loading.value = true;
  try {
    detail.value = await getMaintenanceDetail(familyId.value, maintenanceId.value);
    Object.assign(form, detail.value);
    Object.assign(statusForm, {
      status: detail.value.status,
      resultDescription: detail.value.resultDescription || '',
      repairCost: detail.value.repairCost,
      completedAt: detail.value.completedAt || '',
      syncDeviceRepaired: false
    });
    files.value = await getFiles(familyId.value, 'MAINTENANCE', maintenanceId.value).catch(() => []);
  } finally {
    loading.value = false;
  }
}

async function saveBasic() {
  await formRef.value?.validate();
  if (!familyId.value) return;
  saving.value = true;
  try {
    await updateMaintenance(familyId.value, maintenanceId.value, form);
    ElMessage.success('维修详情已更新');
    await loadData();
  } finally {
    saving.value = false;
  }
}

async function saveStatus() {
  await statusFormRef.value?.validate();
  if (!familyId.value) return;
  await updateMaintenanceStatus(familyId.value, maintenanceId.value, statusForm);
  ElMessage.success('维修状态已流转');
  await loadData();
}

async function handleUpload(options: { file: File }) {
  await uploadFile(familyId.value!, 'MAINTENANCE', maintenanceId.value, options.file);
  ElMessage.success('维修附件已上传');
  await loadData();
}

async function askAi() {
  if (!familyId.value || !detail.value) return;
  const result = await suggestTroubleshooting(familyId.value, {
    deviceId: detail.value.deviceId,
    maintenanceId: detail.value.id,
    faultDescription: form.faultDescription
  });
  suggestion.value = result.summary;
  suggestionSteps.value = result.suggestions || [];
}

onMounted(loadData);
</script>

<template>
  <div v-loading="loading" class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ detail?.title || '维修详情' }}</h1>
        <p class="page-subtitle">维修详情用于展示故障描述、状态流转、处理结果、附件和 AI 排查建议。</p>
      </div>
      <div>
        <el-button @click="router.push('/maintenance')">返回列表</el-button>
        <el-button v-if="detail" type="primary" @click="router.push(`/devices/${detail.deviceId}`)">查看设备</el-button>
      </div>
    </div>

    <div class="section-grid">
      <el-card class="glass-card" shadow="never">
        <template #header>基础信息</template>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="form-grid">
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
            <el-input v-model="form.faultDescription" type="textarea" :rows="5" />
          </el-form-item>
          <el-form-item class="full-row" label="处理结果">
            <el-input v-model="form.resultDescription" type="textarea" :rows="4" />
          </el-form-item>
        </el-form>
        <div class="actions">
          <el-button type="primary" :loading="saving" @click="saveBasic">保存基础信息</el-button>
        </div>
      </el-card>

      <el-card class="glass-card" shadow="never">
        <template #header>状态流转</template>
        <div class="status-head">
          <span>当前状态</span>
          <el-tag :type="statusType(detail?.status)">{{ labelOf(maintenanceStatusOptions, detail?.status) }}</el-tag>
        </div>
        <el-form ref="statusFormRef" :model="statusForm" label-position="top">
          <el-form-item label="目标状态">
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
            <el-switch v-model="statusForm.syncDeviceRepaired" active-text="完成后同步设备为已维修" />
          </el-form-item>
          <el-form-item label="状态说明">
            <el-input v-model="statusForm.resultDescription" type="textarea" :rows="4" />
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="saveStatus">保存状态</el-button>
      </el-card>
    </div>

    <div class="section-grid">
      <el-card class="glass-card" shadow="never">
        <template #header>
          <div class="card-header-row">
            <span>AI 排查建议</span>
            <el-button type="primary" :icon="MagicStick" @click="askAi">生成建议</el-button>
          </div>
        </template>
        <el-alert
          title="AI 仅辅助排查，不会自动覆盖维修结果。"
          type="warning"
          :closable="false"
          show-icon
        />
        <div v-if="suggestion" class="ai-box">
          <h3>{{ suggestion }}</h3>
          <ol>
            <li v-for="item in suggestionSteps" :key="item">{{ item }}</li>
          </ol>
        </div>
        <el-empty v-else description="点击生成建议后，系统会基于故障描述给出排查步骤" />
      </el-card>

      <el-card class="glass-card" shadow="never">
        <template #header>维修附件</template>
        <el-upload drag :http-request="handleUpload" :show-file-list="false">
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">拖拽维修单或售后截图到这里，或点击上传</div>
          <template #tip>
            <div class="el-upload__tip">支持图片和 PDF，后端会校验大小与类型。</div>
          </template>
        </el-upload>
        <el-table :data="files" style="margin-top: 16px">
          <el-table-column prop="originalName" label="文件名" />
          <el-table-column label="大小" width="100">
            <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button link type="primary" @click="downloadFile(familyId!, row)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.actions {
  display: flex;
  justify-content: flex-end;
}

.status-head,
.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.status-head {
  margin-bottom: 16px;
  color: var(--fl-muted);
}

.ai-box {
  margin-top: 18px;
  padding: 16px;
  border: 1px solid var(--fl-line);
  border-radius: var(--fl-radius-md);
  background: var(--fl-bg-soft);
}

.ai-box h3 {
  margin: 0 0 12px;
  color: var(--fl-primary-strong);
}

.ai-box li {
  margin: 8px 0;
  line-height: 1.7;
}
</style>
