<script setup lang="ts">
import {
  Collection,
  Delete,
  Download,
  Files,
  Picture,
  Search,
  UploadFilled,
  View
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';

import { getDevicePage } from '@/api/device';
import {
  deleteFile,
  downloadFile,
  getCredentialBox,
  previewFile,
  searchManuals,
  uploadFile
} from '@/api/file';
import { useAuthStore } from '@/stores/auth';
import type {
  CredentialBox,
  CredentialBoxGroup,
  CredentialFileResource,
  CredentialTarget,
  ManualSearchResult
} from '@/types/business';
import type { DeviceListItem } from '@/types/device';
import { formatFileSize } from '@/utils/dicts';

interface DefaultCredentialGroup extends Omit<CredentialBoxGroup, 'targets' | 'files'> {
  emptyText: string;
}

interface DisplayCredentialGroup extends CredentialBoxGroup {
  emptyText: string;
}

const defaultCredentialGroups: DefaultCredentialGroup[] = [
  {
    bizType: 'DEVICE',
    title: '购买凭证',
    shortTitle: '发票',
    description: '购买发票、订单截图、付款凭证等基础设备凭证。',
    emptyText: '还没有上传发票或购买凭证'
  },
  {
    bizType: 'MANUAL',
    title: '说明书',
    shortTitle: '说明书',
    description: '说明书 PDF、安装指南、二维码截图等长期查阅资料。',
    emptyText: '还没有归档说明书'
  },
  {
    bizType: 'WARRANTY',
    title: '保修卡与售后凭证',
    shortTitle: '保修',
    description: '保修卡、延保证明、售后政策截图等保修材料。',
    emptyText: '还没有上传保修凭证'
  },
  {
    bizType: 'MAINTENANCE',
    title: '维修单与售后截图',
    shortTitle: '维修',
    description: '维修工单、报价单、售后聊天截图和处理结果。',
    emptyText: '还没有上传维修相关凭证'
  },
  {
    bizType: 'CONSUMABLE',
    title: '耗材与更换凭证',
    shortTitle: '耗材',
    description: '滤芯、尘袋、电池等耗材购买和更换凭证。',
    emptyText: '还没有上传耗材凭证'
  }
];

const auth = useAuthStore();
const familyId = computed(() => auth.currentFamilyId);
const loading = ref(false);
const deviceLoading = ref(false);
const credentialBox = ref<CredentialBox>();
const devices = ref<DeviceListItem[]>([]);
const selectedDeviceId = ref<number>();
const selectedTargetId = ref<number>();
const activeBizType = ref('DEVICE');
const previewVisible = ref(false);
const previewUrl = ref('');
const previewTarget = ref<CredentialFileResource>();
const manualSearchKeyword = ref('');
const manualSearchLoading = ref(false);
const manualSearchSearched = ref(false);
const manualSearchResults = ref<ManualSearchResult[]>([]);

const query = reactive({
  keyword: ''
});

const credentialGroups = computed<DisplayCredentialGroup[]>(() => {
  const groupMap = new Map((credentialBox.value?.groups ?? []).map((item) => [item.bizType, item]));
  return defaultCredentialGroups.map((item) => {
    const group = groupMap.get(item.bizType);
    return {
      bizType: item.bizType,
      title: group?.title ?? item.title,
      shortTitle: group?.shortTitle ?? item.shortTitle,
      description: group?.description ?? item.description,
      emptyText: item.emptyText,
      targets: group?.targets ?? [],
      files: group?.files ?? []
    };
  });
});
const activeType = computed(
  () => credentialGroups.value.find((item) => item.bizType === activeBizType.value) ?? credentialGroups.value[0]
);
const selectedDevice = computed(() => devices.value.find((item) => item.id === selectedDeviceId.value));
const currentFiles = computed(() => activeType.value?.files ?? []);
const archivedTypeCount = computed(() => credentialBox.value?.archivedTypeCount ?? 0);
const totalTypeCount = computed(() => credentialBox.value?.totalTypeCount ?? defaultCredentialGroups.length);
const completionPercent = computed(() => credentialBox.value?.completionPercent ?? 0);
const totalFileSize = computed(() => credentialBox.value?.totalFileSize ?? 0);
const isDeviceBoundCredential = computed(() => ['DEVICE', 'MANUAL'].includes(activeBizType.value));
const targetOptions = computed<CredentialTarget[]>(() => activeType.value?.targets ?? []);
const targetLabelMap = computed(() => {
  const map = new Map<string, string>();
  for (const group of credentialGroups.value) {
    for (const target of group.targets) {
      map.set(`${group.bizType}:${target.bizId}`, target.label);
    }
  }
  return map;
});
const activeBizId = computed(() =>
  isDeviceBoundCredential.value ? selectedDeviceId.value : selectedTargetId.value
);
const canUpload = computed(() => Boolean(familyId.value && activeBizId.value));
const activeEmptyText = computed(() => {
  if (!isDeviceBoundCredential.value && targetOptions.value.length === 0) {
    return `该设备暂无可挂载的${activeType.value.shortTitle}记录`;
  }
  return activeType.value.emptyText;
});
const previewSupported = computed(() => {
  const contentType = previewTarget.value?.contentType ?? '';
  return contentType.startsWith('image/') || contentType === 'application/pdf';
});

function hasCredential(bizType: string) {
  return credentialGroups.value.some(
    (group) => group.bizType === bizType && group.files.length > 0
  );
}

function isPreviewable(file: CredentialFileResource) {
  return file.contentType.startsWith('image/') || file.contentType === 'application/pdf';
}

function credentialState(type: DisplayCredentialGroup) {
  return hasCredential(type.bizType) ? '已归档' : '待补充';
}

function credentialHint(type: DisplayCredentialGroup) {
  const count = type.files.length;
  return count > 0 ? `${count} 份凭证` : type.emptyText;
}

function selectedDeviceLabel(device: DeviceListItem) {
  return [device.name, device.location, device.brand].filter(Boolean).join(' · ');
}

function targetLabel(file: CredentialFileResource) {
  return file.targetLabel ?? targetLabelMap.value.get(`${file.bizType}:${file.bizId}`) ?? `业务 ID ${file.bizId}`;
}

function syncSelectedTarget() {
  if (isDeviceBoundCredential.value) {
    selectedTargetId.value = selectedDeviceId.value;
    return;
  }
  const exists = targetOptions.value.some((item) => item.bizId === selectedTargetId.value);
  selectedTargetId.value = exists ? selectedTargetId.value : targetOptions.value[0]?.bizId;
}

async function loadDevices() {
  if (!familyId.value) return false;
  deviceLoading.value = true;
  const previousDeviceId = selectedDeviceId.value;
  try {
    const page = await getDevicePage(familyId.value, {
      pageNum: 1,
      pageSize: 100,
      keyword: query.keyword || undefined
    });
    devices.value = page.records;
    if (!selectedDeviceId.value && devices.value.length > 0) {
      selectedDeviceId.value = devices.value[0].id;
    }
    if (selectedDeviceId.value && !devices.value.some((item) => item.id === selectedDeviceId.value)) {
      selectedDeviceId.value = devices.value[0]?.id;
    }
    return previousDeviceId !== selectedDeviceId.value;
  } finally {
    deviceLoading.value = false;
  }
}

async function loadData() {
  if (!familyId.value || !selectedDeviceId.value) {
    credentialBox.value = undefined;
    return;
  }
  loading.value = true;
  try {
    credentialBox.value = await getCredentialBox(familyId.value, selectedDeviceId.value);
    syncSelectedTarget();
  } finally {
    loading.value = false;
  }
}

async function handleSearchDevices() {
  const selectionChanged = await loadDevices();
  if (!selectionChanged) {
    await loadData();
  }
}

async function handleUpload(options: { file: File }) {
  if (!familyId.value || !activeBizId.value) {
    ElMessage.warning('请先选择设备和可挂载的凭证对象。');
    return;
  }
  await uploadFile(familyId.value, activeBizType.value, activeBizId.value, options.file);
  ElMessage.success('凭证已归档');
  await loadData();
}

async function handleSearchManuals() {
  if (!familyId.value || !selectedDeviceId.value) return;
  const keyword = manualSearchKeyword.value.trim();
  if (!keyword) {
    ElMessage.warning('请输入说明书关键词。');
    return;
  }
  manualSearchLoading.value = true;
  manualSearchSearched.value = true;
  try {
    manualSearchResults.value = await searchManuals(familyId.value, selectedDeviceId.value, keyword);
  } finally {
    manualSearchLoading.value = false;
  }
}

async function handlePreview(file: CredentialFileResource) {
  if (!familyId.value || !isPreviewable(file)) return;
  revokePreviewUrl();
  previewTarget.value = file;
  previewUrl.value = await previewFile(familyId.value, file);
  previewVisible.value = true;
}

async function handleDelete(row: CredentialFileResource) {
  await ElMessageBox.confirm(`确认删除凭证「${row.originalName}」吗？`, '删除凭证', { type: 'warning' });
  await deleteFile(familyId.value!, row.id);
  ElMessage.success('凭证已删除');
  await loadData();
}

async function handlePreviewManualResult(result: ManualSearchResult) {
  await handlePreview(manualResultFile(result));
}

async function handleDownloadManualResult(result: ManualSearchResult) {
  if (!familyId.value) return;
  await downloadFile(familyId.value, manualResultFile(result));
}

function manualResultFile(result: ManualSearchResult): CredentialFileResource {
  return {
    id: result.fileId,
    originalName: result.fileName,
    contentType: result.contentType,
    fileSize: result.fileSize,
    bizType: 'MANUAL',
    bizId: selectedDeviceId.value ?? 0,
    targetLabel: selectedDevice.value?.name
  };
}

function resetManualSearch() {
  manualSearchKeyword.value = '';
  manualSearchResults.value = [];
  manualSearchSearched.value = false;
}

function revokePreviewUrl() {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value);
    previewUrl.value = '';
  }
}

function closePreview() {
  previewVisible.value = false;
  previewTarget.value = undefined;
  revokePreviewUrl();
}

watch(selectedDeviceId, () => {
  resetManualSearch();
  void loadData();
});

watch(activeBizType, syncSelectedTarget);

watch(familyId, async () => {
  selectedDeviceId.value = undefined;
  selectedTargetId.value = undefined;
  credentialBox.value = undefined;
  const selectionChanged = await loadDevices();
  if (!selectionChanged) {
    await loadData();
  }
});

onMounted(async () => {
  const selectionChanged = await loadDevices();
  if (!selectionChanged) {
    await loadData();
  }
});

onBeforeUnmount(revokePreviewUrl);
</script>

<template>
  <div class="page-shell file-box-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">凭证盒</h1>
        <p class="page-subtitle">按设备整理发票、说明书、保修卡、维修单和耗材凭证；预览和下载仍由后端鉴权。</p>
      </div>
      <el-upload :http-request="handleUpload" :show-file-list="false" :disabled="!canUpload">
        <el-button type="primary" :icon="UploadFilled" :disabled="!canUpload">上传当前凭证</el-button>
      </el-upload>
    </div>

    <section class="file-hero">
      <div>
        <p class="file-kicker">Home Credential Box</p>
        <h2>{{ selectedDevice?.name || '选择一台设备开始整理凭证' }}</h2>
        <p>{{ selectedDevice ? selectedDeviceLabel(selectedDevice) : '设备护照、凭证盒和家庭日历会围绕同一台设备串起来。' }}</p>
      </div>
      <div class="file-score">
        <span>凭证完整度</span>
        <strong>{{ completionPercent }}%</strong>
        <small>{{ archivedTypeCount }}/{{ totalTypeCount }} 类已归档 · {{ formatFileSize(totalFileSize) }}</small>
      </div>
    </section>

    <el-card class="glass-card file-toolbar" shadow="never">
      <div class="device-picker">
        <el-select
          v-model="selectedDeviceId"
          filterable
          :loading="deviceLoading"
          placeholder="选择设备"
        >
          <el-option
            v-for="device in devices"
            :key="device.id"
            :label="selectedDeviceLabel(device)"
            :value="device.id"
          />
        </el-select>
        <el-input v-model="query.keyword" clearable placeholder="搜索设备名称、品牌或房间" @keyup.enter="handleSearchDevices">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button :icon="Search" @click="handleSearchDevices">搜索设备</el-button>
      </div>
    </el-card>

    <div class="credential-grid">
      <button
        v-for="item in credentialGroups"
        :key="item.bizType"
        class="credential-card"
        :class="{ active: activeBizType === item.bizType, archived: hasCredential(item.bizType) }"
        type="button"
        @click="activeBizType = item.bizType"
      >
        <span class="credential-icon"><el-icon><Collection /></el-icon></span>
        <strong>{{ item.shortTitle }}</strong>
        <small>{{ credentialState(item) }}</small>
        <em>{{ credentialHint(item) }}</em>
      </button>
    </div>

    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="file-card-header">
          <div>
            <strong>{{ activeType.title }}</strong>
            <span>{{ activeType.description }}</span>
          </div>
          <el-tag :type="currentFiles.length > 0 ? 'success' : 'warning'" effect="light">
            {{ currentFiles.length > 0 ? `${currentFiles.length} 份已归档` : '待补充' }}
          </el-tag>
        </div>
      </template>

      <el-alert
        v-if="!isDeviceBoundCredential"
        class="tip-alert"
        title="当前类型绑定到具体保修、维修或耗材记录。请选择下方挂载对象后上传凭证。"
        type="info"
        :closable="false"
        show-icon
      />

      <div v-if="!isDeviceBoundCredential" class="target-picker">
        <span>挂载对象</span>
        <el-select
          v-model="selectedTargetId"
          :disabled="targetOptions.length === 0"
          placeholder="暂无可挂载记录"
        >
          <el-option
            v-for="target in targetOptions"
            :key="`${activeBizType}-${target.bizId}`"
            :label="target.label"
            :value="target.bizId"
          />
        </el-select>
      </div>

      <section v-if="activeBizType === 'MANUAL'" class="manual-search-panel">
        <div class="manual-search-copy">
          <strong>说明书搜索</strong>
        </div>
        <div class="manual-search-controls">
          <el-input
            v-model="manualSearchKeyword"
            clearable
            maxlength="64"
            placeholder="关键词"
            @clear="resetManualSearch"
            @keyup.enter="handleSearchManuals"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button
            type="primary"
            :icon="Search"
            :loading="manualSearchLoading"
            @click="handleSearchManuals"
          >
            搜索说明书
          </el-button>
        </div>
        <div
          v-if="manualSearchSearched"
          v-loading="manualSearchLoading"
          class="manual-search-results"
        >
          <article
            v-for="result in manualSearchResults"
            :key="result.fileId"
            class="manual-result-item"
          >
            <div>
              <strong>{{ result.fileName }}</strong>
              <p>{{ result.snippet || '命中文件名，说明书文本暂未提取到可展示片段。' }}</p>
              <span>{{ result.contentType }} · {{ formatFileSize(result.fileSize) }}</span>
            </div>
            <div class="file-actions">
              <el-button
                circle
                :icon="View"
                :disabled="result.contentType !== 'application/pdf'"
                title="预览"
                @click="handlePreviewManualResult(result)"
              />
              <el-button
                circle
                :icon="Download"
                title="下载"
                @click="handleDownloadManualResult(result)"
              />
            </div>
          </article>
          <el-empty
            v-if="manualSearchResults.length === 0 && !manualSearchLoading"
            description="没有命中已索引的说明书"
          />
        </div>
      </section>

      <div v-loading="loading" class="file-list">
        <article v-for="file in currentFiles" :key="file.id" class="file-item">
          <div class="file-thumb">
            <el-icon><Picture v-if="file.contentType.startsWith('image/')" /><Files v-else /></el-icon>
          </div>
          <div class="file-meta">
            <strong>{{ file.originalName }}</strong>
            <span>{{ targetLabel(file) }} · {{ file.contentType }} · {{ formatFileSize(file.fileSize) }}</span>
          </div>
          <div class="file-actions">
            <el-button
              circle
              :icon="View"
              :disabled="!isPreviewable(file)"
              title="预览"
              @click="handlePreview(file)"
            />
            <el-button circle :icon="Download" title="下载" @click="downloadFile(familyId!, file)" />
            <el-button circle type="danger" :icon="Delete" title="删除" @click="handleDelete(file)" />
          </div>
        </article>

        <el-empty v-if="currentFiles.length === 0" :description="activeEmptyText">
          <el-upload :http-request="handleUpload" :show-file-list="false" :disabled="!canUpload">
            <el-button type="primary" :icon="UploadFilled" :disabled="!canUpload">
              上传 {{ activeType.shortTitle }}
            </el-button>
          </el-upload>
        </el-empty>
      </div>
    </el-card>

    <el-drawer v-model="previewVisible" size="60%" title="凭证预览" @closed="closePreview">
      <div class="preview-shell">
        <h3>{{ previewTarget?.originalName }}</h3>
        <img
          v-if="previewTarget?.contentType.startsWith('image/')"
          class="preview-image"
          :src="previewUrl"
          :alt="previewTarget.originalName"
        >
        <iframe
          v-else-if="previewSupported"
          class="preview-frame"
          :src="previewUrl"
          title="PDF 凭证预览"
        />
        <el-empty v-else description="该类型暂不支持在线预览，请下载后查看" />
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.file-box-page {
  gap: 22px;
}

.file-hero {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 20px;
  padding: clamp(24px, 4vw, 42px);
  border: 1px solid rgba(255, 255, 255, 0.78);
  border-radius: 32px;
  background:
    radial-gradient(circle at 80% 18%, rgba(255, 194, 122, 0.34), transparent 34%),
    linear-gradient(145deg, #fffdf8 0%, #f6efe1 100%);
  box-shadow: var(--fl-shadow-md);
}

.file-kicker {
  margin: 0 0 8px;
  color: var(--fl-mi-orange-dark);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.file-hero h2 {
  max-width: 680px;
  margin: 0;
  color: var(--fl-ink);
  font-size: clamp(28px, 4vw, 46px);
  font-weight: 950;
  letter-spacing: -0.05em;
}

.file-hero p {
  max-width: 640px;
  margin: 12px 0 0;
  color: var(--fl-muted);
  line-height: 1.8;
}

.file-score {
  display: grid;
  min-width: 210px;
  padding: 22px;
  place-items: center;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.72);
  text-align: center;
}

.file-score span,
.file-score small,
.file-meta span,
.file-card-header span,
.credential-card small,
.credential-card em {
  color: var(--fl-muted);
}

.file-score strong {
  color: var(--fl-ink);
  font-size: 44px;
  font-weight: 950;
  letter-spacing: -0.06em;
}

.file-toolbar {
  padding: 16px;
}

.device-picker {
  display: grid;
  grid-template-columns: minmax(220px, 0.7fr) minmax(220px, 1fr) auto;
  gap: 12px;
}

.credential-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
}

.credential-card {
  display: flex;
  min-height: 156px;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.78);
  border-radius: 26px;
  background: rgba(255, 253, 248, 0.78);
  box-shadow: 0 12px 28px rgba(88, 72, 49, 0.07);
  color: inherit;
  cursor: pointer;
  text-align: left;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.credential-card:hover,
.credential-card.active {
  border-color: rgba(255, 138, 31, 0.34);
  box-shadow: 0 18px 42px rgba(88, 72, 49, 0.1);
  transform: translateY(-2px);
}

.credential-card.archived .credential-icon {
  background: rgba(77, 143, 115, 0.12);
  color: var(--fl-green-dark);
}

.credential-card.active {
  background:
    radial-gradient(circle at 84% 16%, rgba(255, 216, 157, 0.36), transparent 32%),
    rgba(255, 252, 246, 0.94);
}

.credential-icon {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 16px;
  background: rgba(255, 138, 31, 0.12);
  color: var(--fl-mi-orange-dark);
}

.credential-card strong {
  color: var(--fl-ink);
  font-size: 18px;
  font-weight: 900;
}

.credential-card em {
  min-height: 40px;
  font-style: normal;
  line-height: 1.5;
}

.file-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.file-card-header strong,
.file-card-header span {
  display: block;
}

.file-card-header span {
  margin-top: 4px;
  font-weight: 600;
}

.tip-alert {
  margin-bottom: 16px;
  border-radius: 14px;
}

.target-picker {
  display: grid;
  grid-template-columns: auto minmax(220px, 420px);
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  color: var(--fl-muted);
  font-weight: 800;
}

.manual-search-panel {
  display: grid;
  gap: 14px;
  margin-bottom: 18px;
  padding: 16px;
  border: 1px solid rgba(255, 138, 31, 0.16);
  border-radius: 20px;
  background: rgba(255, 248, 238, 0.72);
}

.manual-search-copy strong,
.manual-search-copy span {
  display: block;
}

.manual-search-copy strong {
  color: var(--fl-ink);
  font-size: 16px;
  font-weight: 900;
}

.manual-search-copy span,
.manual-result-item p,
.manual-result-item span {
  color: var(--fl-muted);
}

.manual-search-copy span {
  margin-top: 4px;
  line-height: 1.6;
}

.manual-search-controls {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) auto;
  gap: 12px;
}

.manual-search-results {
  display: grid;
  gap: 10px;
  min-height: 82px;
}

.manual-result-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid rgba(39, 46, 42, 0.07);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.76);
}

.manual-result-item strong,
.manual-result-item p,
.manual-result-item span {
  display: block;
}

.manual-result-item strong {
  overflow: hidden;
  color: var(--fl-ink);
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.manual-result-item p {
  margin: 6px 0;
  line-height: 1.6;
}

.file-list {
  display: grid;
  gap: 12px;
  min-height: 180px;
}

.file-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border: 1px solid rgba(39, 46, 42, 0.07);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.68);
}

.file-thumb {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: 18px;
  background: #fff4e6;
  color: var(--fl-mi-orange-dark);
  font-size: 22px;
}

.file-meta {
  min-width: 0;
}

.file-meta strong,
.file-meta span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta strong {
  color: var(--fl-ink);
  font-weight: 900;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.preview-shell {
  display: flex;
  height: 100%;
  flex-direction: column;
  gap: 16px;
}

.preview-shell h3 {
  margin: 0;
  color: var(--fl-ink);
}

.preview-image,
.preview-frame {
  width: 100%;
  flex: 1;
  min-height: 60vh;
  border: none;
  border-radius: 20px;
  background: #fff;
  object-fit: contain;
}

@media (max-width: 1180px) {
  .credential-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .file-hero,
  .file-card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .device-picker,
  .credential-grid,
  .manual-search-controls,
  .manual-result-item,
  .target-picker {
    grid-template-columns: 1fr;
  }

  .file-score {
    width: 100%;
  }

  .file-item {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .file-actions {
    grid-column: 1 / -1;
  }
}
</style>
