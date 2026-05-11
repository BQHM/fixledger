<script setup lang="ts">
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { createDevice, getDeviceCategories, getDeviceDetail, updateDevice } from '@/api/device';
import { useAuthStore } from '@/stores/auth';
import type { DeviceCategory, DeviceForm } from '@/types/device';

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);
const categories = ref<DeviceCategory[]>([]);
const deviceId = computed(() => Number(route.params.id));
const isEdit = computed(() => route.name === 'device-edit');
const familyId = computed(() => auth.currentFamilyId);
const form = reactive<DeviceForm>({
  name: '',
  categoryId: undefined,
  brand: '',
  model: '',
  serialNumber: '',
  purchaseDate: '',
  purchaseChannel: '',
  purchasePrice: undefined,
  location: '',
  remark: ''
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  purchaseDate: [{ required: true, message: '请选择购买日期', trigger: 'change' }]
};

function applyQueryDefaults() {
  if (isEdit.value) return;
  const query = route.query;
  Object.assign(form, {
    name: (query.name as string) || form.name,
    purchaseDate: (query.purchaseDate as string) || form.purchaseDate,
    purchaseChannel: (query.purchaseChannel as string) || form.purchaseChannel,
    purchasePrice: query.purchasePrice ? Number(query.purchasePrice) : form.purchasePrice
  });
}

async function loadData() {
  if (!familyId.value) return;
  categories.value = await getDeviceCategories(familyId.value);
  applyQueryDefaults();
  if (isEdit.value) {
    const detail = await getDeviceDetail(familyId.value, deviceId.value);
    Object.assign(form, detail);
  }
}

async function submit() {
  await formRef.value?.validate();
  if (!familyId.value) return;
  loading.value = true;
  try {
    if (isEdit.value) {
      await updateDevice(familyId.value, deviceId.value, form);
      ElMessage.success('设备已更新');
      router.push(`/devices/${deviceId.value}`);
    } else {
      const result = await createDevice(familyId.value, form);
      ElMessage.success('设备已创建');
      router.push(`/devices/${result.id}`);
    }
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ isEdit ? '编辑设备' : '新增设备' }}</h1>
        <p class="page-subtitle">设备是保修、耗材、维修和附件归档的核心入口，AI 填充内容也需要确认后保存。</p>
      </div>
    </div>
    <el-card class="glass-card" shadow="never">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="form-grid">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" clearable placeholder="选择分类">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="form.brand" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.model" />
        </el-form-item>
        <el-form-item label="序列号">
          <el-input v-model="form.serialNumber" />
        </el-form-item>
        <el-form-item label="购买日期" prop="purchaseDate">
          <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="购买渠道">
          <el-input v-model="form.purchaseChannel" />
        </el-form-item>
        <el-form-item label="购买价格">
          <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="存放位置">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="备注" class="full-row">
          <el-input v-model="form.remark" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button @click="router.back()">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submit">保存</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
