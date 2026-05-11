<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';

import { createFamily, getFamilyMembers, updateFamily } from '@/api/family';
import { useAuthStore } from '@/stores/auth';
import type { FamilyMemberResponse, FamilyResponse } from '@/types/family';

const auth = useAuthStore();
const familyId = computed(() => auth.currentFamilyId);
const formRef = ref<FormInstance>();
const dialogVisible = ref(false);
const isEdit = ref(false);
const loading = ref(false);
const members = ref<FamilyMemberResponse[]>([]);
const editingFamily = ref<FamilyResponse>();

const form = reactive({
  name: '',
  description: ''
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入家庭空间名称', trigger: 'blur' }]
};

async function loadData() {
  await auth.loadFamilies();
  if (familyId.value) {
    members.value = await getFamilyMembers(familyId.value);
  }
}

function openCreate() {
  isEdit.value = false;
  editingFamily.value = undefined;
  Object.assign(form, { name: '', description: '' });
  dialogVisible.value = true;
}

function openEdit(family: FamilyResponse) {
  isEdit.value = true;
  editingFamily.value = family;
  Object.assign(form, { name: family.name, description: family.description || '' });
  dialogVisible.value = true;
}

async function submit() {
  await formRef.value?.validate();
  loading.value = true;
  try {
    if (isEdit.value && editingFamily.value) {
      await updateFamily(editingFamily.value.id, form);
      ElMessage.success('家庭空间已更新');
    } else {
      const created = await createFamily(form);
      auth.setCurrentFamily(created.id);
      ElMessage.success('家庭空间已创建');
    }
    dialogVisible.value = false;
    await loadData();
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
        <h1 class="page-title">家庭设置</h1>
        <p class="page-subtitle">家庭空间是 FixLedger 的数据隔离边界，设备、提醒和附件都归属于某个家庭。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">创建家庭空间</el-button>
    </div>

    <div class="section-grid">
      <el-card class="glass-card" shadow="never">
        <template #header>我的家庭空间</template>
        <div class="family-list">
          <div
            v-for="family in auth.families"
            :key="family.id"
            class="family-card"
            :class="{ active: family.id === auth.currentFamilyId }"
          >
            <div>
              <h3>{{ family.name }}</h3>
              <p>{{ family.description || '暂无描述' }}</p>
              <el-tag effect="plain">{{ family.role }}</el-tag>
            </div>
            <div class="family-actions">
              <el-button v-if="family.id !== auth.currentFamilyId" @click="auth.setCurrentFamily(family.id)">
                切换
              </el-button>
              <el-button type="primary" plain @click="openEdit(family)">编辑</el-button>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="glass-card" shadow="never">
        <template #header>成员列表</template>
        <el-table :data="members">
          <el-table-column prop="username" label="账号" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="role" label="角色" width="110" />
          <el-table-column prop="joinedAt" label="加入时间" width="170" />
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑家庭空间' : '创建家庭空间'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.family-list {
  display: grid;
  gap: 14px;
}

.family-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px;
  border: 1px solid rgba(47, 125, 104, 0.12);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.62);
}

.family-card.active {
  border-color: var(--fl-green);
  box-shadow: inset 0 0 0 1px var(--fl-green);
}

.family-card h3 {
  margin: 0 0 8px;
  color: var(--fl-green-dark);
}

.family-card p {
  margin: 0 0 10px;
  color: var(--fl-muted);
}

.family-actions {
  display: flex;
  gap: 8px;
}
</style>
