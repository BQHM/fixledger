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
const categoryAutoSelected = ref(false);
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

const selectedCategoryName = computed(() => {
  return categories.value.find((item) => item.id === form.categoryId)?.name;
});

function toQueryString(value: unknown) {
  const raw = Array.isArray(value) ? value[0] : value;
  return typeof raw === 'string' ? raw : undefined;
}

function toQueryNumber(value: unknown) {
  const raw = toQueryString(value);
  if (!raw) {
    return undefined;
  }
  const parsed = Number(raw);
  return Number.isFinite(parsed) ? parsed : undefined;
}

function applyQueryDefaults() {
  if (isEdit.value) return;
  const query = route.query;
  const categoryName = toQueryString(query.categoryName);
  const categoryId = toQueryNumber(query.categoryId)
    ?? categories.value.find((item) => item.name === categoryName)?.id;
  Object.assign(form, {
    name: toQueryString(query.name) || form.name,
    categoryId: categoryId ?? form.categoryId,
    purchaseDate: toQueryString(query.purchaseDate) || form.purchaseDate,
    purchaseChannel: toQueryString(query.purchaseChannel) || form.purchaseChannel,
    purchasePrice: toQueryNumber(query.purchasePrice) ?? form.purchasePrice
  });
}

function applyDefaultCategory() {
  if (isEdit.value || form.categoryId || categories.value.length === 0) {
    return;
  }
  const defaultCategory =
    categories.value.find((item) => item.name === '其他') ?? categories.value[0];
  form.categoryId = defaultCategory.id;
  categoryAutoSelected.value = true;
}

function handleCategoryChange() {
  categoryAutoSelected.value = false;
}

async function loadData() {
  if (!familyId.value) return;
  categories.value = await getDeviceCategories(familyId.value);
  applyQueryDefaults();
  applyDefaultCategory();
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
          <el-select
            v-model="form.categoryId"
            clearable
            placeholder="选择分类"
            @change="handleCategoryChange"
          >
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <p v-if="categoryAutoSelected" class="field-hint">
            已为新设备预选“{{ selectedCategoryName }}”，可按实际类型调整。
          </p>
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
          <el-input-number
            v-model="form.purchasePrice"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
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

.field-hint {
  margin: 8px 0 0;
  color: var(--fl-text-muted);
  font-size: 12px;
  line-height: 1.5;
}
</style>
