<script setup lang="ts">
import { Delete, Plus, Refresh, UserFilled } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';

import {
  createFamily,
  getFamilyMembers,
  inviteFamilyMember,
  removeFamilyMember,
  updateFamily,
  updateFamilyMemberRole
} from '@/api/family';
import { getOperationLogs } from '@/api/system';
import { useAuthStore } from '@/stores/auth';
import type { FamilyMemberResponse, FamilyResponse } from '@/types/family';
import type { OperationLogResponse } from '@/types/system';

const auth = useAuthStore();
const familyId = computed(() => auth.currentFamilyId);
const currentFamily = computed(() => auth.currentFamily);
const canManageMembers = computed(() => currentFamily.value?.role === 'OWNER');
const formRef = ref<FormInstance>();
const inviteFormRef = ref<FormInstance>();
const roleFormRef = ref<FormInstance>();
const dialogVisible = ref(false);
const inviteDialogVisible = ref(false);
const roleDialogVisible = ref(false);
const isEdit = ref(false);
const loading = ref(false);
const memberLoading = ref(false);
const logLoading = ref(false);
const members = ref<FamilyMemberResponse[]>([]);
const operationLogs = ref<OperationLogResponse[]>([]);
const editingFamily = ref<FamilyResponse>();
const editingMember = ref<FamilyMemberResponse>();

const roleOptions = [
  { label: '所有者', value: 'OWNER' },
  { label: '成员', value: 'MEMBER' }
];

const form = reactive({
  name: '',
  description: ''
});

const inviteForm = reactive({
  account: '',
  role: 'MEMBER'
});

const roleForm = reactive({
  role: 'MEMBER'
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入家庭空间名称', trigger: 'blur' }]
};

const inviteRules: FormRules = {
  account: [{ required: true, message: '请输入成员用户名或邮箱', trigger: 'blur' }],
  role: [{ required: true, message: '请选择成员角色', trigger: 'change' }]
};

const roleRules: FormRules = {
  role: [{ required: true, message: '请选择成员角色', trigger: 'change' }]
};

async function loadData() {
  await auth.loadFamilies();
  if (!familyId.value) {
    members.value = [];
    operationLogs.value = [];
    return;
  }
  await Promise.all([loadMembers(), loadOperationLogs()]);
}

async function loadMembers() {
  if (!familyId.value) return;
  memberLoading.value = true;
  try {
    members.value = await getFamilyMembers(familyId.value);
  } finally {
    memberLoading.value = false;
  }
}

async function loadOperationLogs() {
  if (!familyId.value) return;
  logLoading.value = true;
  try {
    const page = await getOperationLogs({
      familyId: familyId.value,
      module: 'FAMILY',
      pageNum: 1,
      pageSize: 8
    });
    operationLogs.value = page.records;
  } finally {
    logLoading.value = false;
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

function openInvite() {
  Object.assign(inviteForm, { account: '', role: 'MEMBER' });
  inviteDialogVisible.value = true;
}

function openRoleDialog(member: FamilyMemberResponse) {
  editingMember.value = member;
  roleForm.role = member.role;
  roleDialogVisible.value = true;
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

async function submitInvite() {
  await inviteFormRef.value?.validate();
  if (!familyId.value) return;
  loading.value = true;
  try {
    await inviteFamilyMember(familyId.value, inviteForm);
    ElMessage.success('成员已加入家庭空间');
    inviteDialogVisible.value = false;
    await loadData();
  } finally {
    loading.value = false;
  }
}

async function submitRole() {
  await roleFormRef.value?.validate();
  if (!familyId.value || !editingMember.value) return;
  loading.value = true;
  try {
    await updateFamilyMemberRole(familyId.value, editingMember.value.id, roleForm);
    ElMessage.success('成员角色已更新');
    roleDialogVisible.value = false;
    await loadData();
  } finally {
    loading.value = false;
  }
}

async function handleRemoveMember(member: FamilyMemberResponse) {
  if (!familyId.value) return;
  await ElMessageBox.confirm(
    `确认将「${member.nickname || member.username}」移出当前家庭吗？`,
    '移除成员',
    { type: 'warning' }
  );
  await removeFamilyMember(familyId.value, member.id);
  ElMessage.success('成员已移除');
  await loadData();
}

function roleLabel(role: string) {
  return roleOptions.find((item) => item.value === role)?.label ?? role;
}

function logActionLabel(action: string) {
  const labels: Record<string, string> = {
    INVITE_MEMBER: '邀请成员',
    UPDATE_MEMBER_ROLE: '调整角色',
    REMOVE_MEMBER: '移除成员'
  };
  return labels[action] ?? action;
}

onMounted(loadData);
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">家庭设置</h1>
        <p class="page-subtitle">家庭空间是数据隔离边界，也是一家人共同维护设备档案的协作入口。</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="loadData">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">创建家庭空间</el-button>
      </div>
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
              <el-tag effect="plain">{{ roleLabel(family.role) }}</el-tag>
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
        <template #header>
          <div class="card-header-row">
            <span>成员协作</span>
            <el-button
              v-if="canManageMembers"
              type="primary"
              plain
              :icon="UserFilled"
              @click="openInvite"
            >
              邀请成员
            </el-button>
          </div>
        </template>
        <el-alert
          v-if="!canManageMembers"
          class="member-tip"
          title="当前账号不是该家庭所有者，只能查看成员列表。"
          type="info"
          :closable="false"
          show-icon
        />
        <el-table v-loading="memberLoading" :data="members" class="desktop-data-table">
          <el-table-column prop="username" label="账号" min-width="120" />
          <el-table-column prop="nickname" label="昵称" min-width="120" />
          <el-table-column label="角色" width="120">
            <template #default="{ row }">
              <el-tag :type="row.role === 'OWNER' ? 'warning' : 'info'" effect="light">
                {{ roleLabel(row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="joinedAt" label="加入时间" width="170" />
          <el-table-column v-if="canManageMembers" label="操作" width="190" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openRoleDialog(row)">角色</el-button>
              <el-button link type="danger" :icon="Delete" @click="handleRemoveMember(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-loading="memberLoading" class="mobile-data-list" aria-label="家庭成员列表">
          <el-empty v-if="members.length === 0" description="暂无家庭成员" />
          <article v-for="member in members" :key="member.id" class="mobile-data-card">
            <div class="mobile-data-head">
              <div>
                <strong>{{ member.nickname || member.username }}</strong>
                <p>@{{ member.username }}</p>
              </div>
              <el-tag :type="member.role === 'OWNER' ? 'warning' : 'info'" effect="light">
                {{ roleLabel(member.role) }}
              </el-tag>
            </div>
            <div class="mobile-data-meta">
              <div class="mobile-data-field">
                <small>加入时间</small>
                <span>{{ member.joinedAt || '-' }}</span>
              </div>
              <div class="mobile-data-field">
                <small>协作权限</small>
                <span>{{ member.role === 'OWNER' ? '管理家庭与成员' : '维护家庭设备档案' }}</span>
              </div>
            </div>
            <div v-if="canManageMembers" class="mobile-data-actions">
              <el-button type="primary" plain @click="openRoleDialog(member)">调整角色</el-button>
              <el-button type="danger" plain :icon="Delete" @click="handleRemoveMember(member)">移除成员</el-button>
            </div>
          </article>
        </div>
      </el-card>
    </div>

    <el-card class="glass-card" shadow="never">
      <template #header>最近协作日志</template>
      <el-table v-loading="logLoading" :data="operationLogs" class="desktop-data-table">
        <el-table-column label="动作" width="140">
          <template #default="{ row }">{{ logActionLabel(row.action) }}</template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="方法" width="92" />
        <el-table-column prop="requestUri" label="接口" min-width="220" />
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>
      <div v-loading="logLoading" class="mobile-data-list" aria-label="最近协作日志">
        <el-empty v-if="operationLogs.length === 0" description="暂无协作日志" />
        <article v-for="log in operationLogs" :key="log.id" class="mobile-data-card">
          <div class="mobile-data-head">
            <strong>{{ logActionLabel(log.action) }}</strong>
            <el-tag effect="plain">{{ log.requestMethod }}</el-tag>
          </div>
          <div class="mobile-data-meta">
            <div class="mobile-data-field">
              <small>时间</small>
              <span>{{ log.createdAt }}</span>
            </div>
            <div class="mobile-data-field mobile-log-uri">
              <small>接口</small>
              <span>{{ log.requestUri }}</span>
            </div>
          </div>
        </article>
      </div>
    </el-card>

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

    <el-dialog v-model="inviteDialogVisible" title="邀请家庭成员" width="480px">
      <el-form ref="inviteFormRef" :model="inviteForm" :rules="inviteRules" label-position="top">
        <el-form-item label="用户名或邮箱" prop="account">
          <el-input v-model="inviteForm.account" placeholder="输入已注册账号或邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="inviteForm.role">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inviteDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submitInvite">邀请加入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="调整成员角色" width="420px">
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-position="top">
        <el-form-item label="角色" prop="role">
          <el-select v-model="roleForm.role">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submitRole">保存角色</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.header-actions,
.card-header-row,
.family-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header-row {
  justify-content: space-between;
}

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
  border: 1px solid rgba(255, 255, 255, 0.66);
  border-radius: var(--fl-radius-md);
  background: var(--fl-glass-chip);
  box-shadow: var(--fl-shadow-sm);
  backdrop-filter: blur(18px) saturate(170%);
  -webkit-backdrop-filter: blur(18px) saturate(170%);
}

.family-card.active {
  border-color: rgba(255, 105, 0, 0.38);
  box-shadow: inset 0 0 0 1px rgba(255, 105, 0, 0.16), 0 16px 34px rgba(255, 105, 0, 0.1);
}

.family-card h3 {
  margin: 0 0 8px;
  color: var(--fl-ink);
}

.family-card p {
  margin: 0 0 10px;
  color: var(--fl-muted);
}

.member-tip {
  margin-bottom: 14px;
  border-radius: var(--fl-radius-md);
}

@media (max-width: 760px) {
  .family-card,
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-actions,
  .family-actions {
    width: 100%;
  }

  .header-actions .el-button,
  .family-actions .el-button {
    min-height: 44px;
    flex: 1;
    margin-left: 0;
  }

  .card-header-row {
    align-items: center;
  }

  .card-header-row .el-button {
    min-height: 44px;
    margin-left: 0;
  }

  .mobile-log-uri {
    grid-column: 1 / -1;
  }
}
</style>
