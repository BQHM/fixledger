<script setup lang="ts">
import { MagicStick, Tickets, Tools } from '@element-plus/icons-vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { parseInvoice, suggestTroubleshooting, summarizeMaintenance } from '@/api/ai';
import { getDevicePage } from '@/api/device';
import { useAuthStore } from '@/stores/auth';
import type {
  InvoiceParseResponse,
  MaintenanceSummaryResponse,
  TroubleshootingResponse
} from '@/types/ai';
import type { DeviceListItem } from '@/types/device';

const auth = useAuthStore();
const router = useRouter();
const familyId = computed(() => auth.currentFamilyId);
const devices = ref<DeviceListItem[]>([]);
const invoiceResult = ref<InvoiceParseResponse>();
const troubleshootingResult = ref<TroubleshootingResponse>();
const summaryResult = ref<MaintenanceSummaryResponse>();
const invoiceLoading = ref(false);
const troubleLoading = ref(false);
const summaryLoading = ref(false);
const invoiceFormRef = ref<FormInstance>();
const troubleFormRef = ref<FormInstance>();
const summaryFormRef = ref<FormInstance>();

const invoiceForm = reactive({
  text: '发票抬头：净水器订单\n商品名称：AquaPro 600G 净水器\n购买日期：2025-12-10\n金额：2399\n商家：京东自营'
});

const troubleForm = reactive({
  deviceId: undefined as number | undefined,
  maintenanceId: undefined as number | undefined,
  faultDescription: ''
});

const summaryForm = reactive({
  deviceId: undefined as number | undefined
});

const invoiceRules: FormRules = {
  text: [{ required: true, message: '请粘贴票据文本', trigger: 'blur' }]
};
const troubleRules: FormRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  faultDescription: [{ required: true, message: '请输入故障描述', trigger: 'blur' }]
};
const summaryRules: FormRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }]
};

async function loadDevices() {
  if (!familyId.value) return;
  const page = await getDevicePage(familyId.value, { pageNum: 1, pageSize: 100 });
  devices.value = page.records;
}

async function handleParseInvoice() {
  await invoiceFormRef.value?.validate();
  if (!familyId.value) return;
  invoiceLoading.value = true;
  try {
    invoiceResult.value = await parseInvoice(familyId.value, invoiceForm.text);
  } finally {
    invoiceLoading.value = false;
  }
}

function fillDeviceForm() {
  if (!invoiceResult.value) return;
  const params = new URLSearchParams();
  if (invoiceResult.value.deviceName) params.set('name', invoiceResult.value.deviceName);
  if (invoiceResult.value.purchaseDate) params.set('purchaseDate', invoiceResult.value.purchaseDate);
  if (invoiceResult.value.price) params.set('purchasePrice', String(invoiceResult.value.price));
  if (invoiceResult.value.seller) params.set('purchaseChannel', invoiceResult.value.seller);
  ElMessage.warning('AI 结果仅供参考，跳转后请确认再保存');
  router.push(`/devices/create?${params.toString()}`);
}

async function handleTroubleshooting() {
  await troubleFormRef.value?.validate();
  if (!familyId.value || !troubleForm.deviceId) return;
  troubleLoading.value = true;
  try {
    troubleshootingResult.value = await suggestTroubleshooting(familyId.value, {
      deviceId: troubleForm.deviceId,
      maintenanceId: troubleForm.maintenanceId,
      faultDescription: troubleForm.faultDescription
    });
  } finally {
    troubleLoading.value = false;
  }
}

async function handleSummary() {
  await summaryFormRef.value?.validate();
  if (!familyId.value || !summaryForm.deviceId) return;
  summaryLoading.value = true;
  try {
    summaryResult.value = await summarizeMaintenance(familyId.value, summaryForm.deviceId);
  } finally {
    summaryLoading.value = false;
  }
}

onMounted(loadDevices);
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">AI 助手</h1>
        <p class="page-subtitle">AI 是辅助录入和分析工具，结果必须由用户确认后才进入核心业务数据。</p>
      </div>
    </div>

    <el-alert
      class="ai-rule"
      title="项目定位：AI 失败不能影响设备创建、维修记录保存和提醒生成。"
      type="warning"
      :closable="false"
      show-icon
    />

    <div class="ai-tool-grid">
      <el-card class="glass-card" shadow="never">
        <template #header>
          <div class="tool-title"><el-icon><Tickets /></el-icon>票据文本提取</div>
        </template>
        <el-form ref="invoiceFormRef" :model="invoiceForm" :rules="invoiceRules" label-position="top">
          <el-form-item label="发票 / 订单文本" prop="text">
            <el-input v-model="invoiceForm.text" type="textarea" :rows="7" />
          </el-form-item>
          <el-button type="primary" :loading="invoiceLoading" :icon="MagicStick" @click="handleParseInvoice">
            提取票据信息
          </el-button>
        </el-form>
        <div v-if="invoiceResult" class="result-box">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="设备名称">{{ invoiceResult.deviceName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="购买日期">{{ invoiceResult.purchaseDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="金额">{{ invoiceResult.price || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商家">{{ invoiceResult.seller || '-' }}</el-descriptions-item>
            <el-descriptions-item label="建议分类">{{ invoiceResult.suggestedCategory || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-button class="result-action" type="primary" plain @click="fillDeviceForm">
            填入新增设备表单
          </el-button>
        </div>
      </el-card>

      <el-card class="glass-card" shadow="never">
        <template #header>
          <div class="tool-title"><el-icon><Tools /></el-icon>故障排查建议</div>
        </template>
        <el-form ref="troubleFormRef" :model="troubleForm" :rules="troubleRules" label-position="top">
          <el-form-item label="设备" prop="deviceId">
            <el-select v-model="troubleForm.deviceId" filterable placeholder="选择设备">
              <el-option v-for="item in devices" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="维修记录 ID（可选）">
            <el-input-number v-model="troubleForm.maintenanceId" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="故障描述" prop="faultDescription">
            <el-input v-model="troubleForm.faultDescription" type="textarea" :rows="5" />
          </el-form-item>
          <el-button type="primary" :loading="troubleLoading" :icon="MagicStick" @click="handleTroubleshooting">
            生成排查建议
          </el-button>
        </el-form>
        <div v-if="troubleshootingResult" class="result-box">
          <h3>{{ troubleshootingResult.summary }}</h3>
          <ol>
            <li v-for="item in troubleshootingResult.suggestions" :key="item">{{ item }}</li>
          </ol>
        </div>
      </el-card>
    </div>

    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="tool-title"><el-icon><MagicStick /></el-icon>维修记录总结</div>
      </template>
      <el-form ref="summaryFormRef" :model="summaryForm" :rules="summaryRules" :inline="true">
        <el-form-item label="设备" prop="deviceId">
          <el-select v-model="summaryForm.deviceId" filterable placeholder="选择设备" style="width: 260px">
            <el-option v-for="item in devices" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="summaryLoading" @click="handleSummary">生成维护总结</el-button>
        </el-form-item>
      </el-form>
      <div v-if="summaryResult" class="summary-box">
        <h3>{{ summaryResult.summary }}</h3>
        <p>{{ summaryResult.careSuggestion }}</p>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.ai-rule {
  border-radius: 16px;
}

.ai-tool-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.tool-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--fl-green-dark);
  font-weight: 900;
}

.result-box,
.summary-box {
  margin-top: 18px;
  padding: 16px;
  border-radius: 18px;
  background: rgba(47, 125, 104, 0.08);
}

.result-action {
  margin-top: 14px;
}

.result-box h3,
.summary-box h3 {
  margin: 0 0 12px;
  color: var(--fl-green-dark);
}

.result-box li,
.summary-box p {
  line-height: 1.8;
}

@media (max-width: 1080px) {
  .ai-tool-grid {
    grid-template-columns: 1fr;
  }
}
</style>
