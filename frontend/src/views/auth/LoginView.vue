<script setup lang="ts">
import { House, Lock, User } from '@element-plus/icons-vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();
const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  account: '',
  password: ''
});

const rules: FormRules = {
  account: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const demoScopes = ['设备档案', '保修提醒', '耗材更换', '凭证归档'];

function fillDemoAccount() {
  form.account = 'demo';
  form.password = 'fixledger123';
  ElMessage.info('已填入本地演示账号，请确认后登录');
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    await auth.login({ account: form.account.trim(), password: form.password });
    ElMessage.success('欢迎回来');
    router.push((route.query.redirect as string) || '/dashboard');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="auth-page">
    <section class="auth-shell" aria-labelledby="login-title">
      <div class="auth-form-panel">
        <button class="brand" type="button" aria-label="返回 FixLedger 首页" @click="router.push('/')">
          <span class="brand-mark"><el-icon><House /></el-icon></span>
          <span>
            <strong>FixLedger</strong>
            <small>家庭设备档案</small>
          </span>
        </button>

        <header class="auth-header">
          <h1 id="login-title">欢迎回来</h1>
          <p>进入家庭空间，查看设备状态、待办提醒和凭证归档。</p>
        </header>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
          <el-form-item label="账号" prop="account">
            <el-input
              v-model.trim="form.account"
              name="username"
              autocomplete="username"
              placeholder="用户名或邮箱"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              name="password"
              type="password"
              autocomplete="current-password"
              show-password
              placeholder="请输入密码"
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" class="auth-submit">
            登录
          </el-button>
        </el-form>

        <div class="auth-switch">
          没有账号？<RouterLink to="/register">注册家庭档案</RouterLink>
        </div>
      </div>

      <aside class="demo-panel" aria-label="演示环境说明">
        <div>
          <span class="panel-label">本地演示</span>
          <h2>demo / fixledger123</h2>
          <p>演示账号只用于本地体验。点击下方按钮填入，再由你确认登录。</p>
        </div>
        <el-button class="demo-fill-button" plain @click="fillDemoAccount">填入演示账号</el-button>
        <div class="scope-list">
          <span v-for="item in demoScopes" :key="item">{{ item }}</span>
        </div>
      </aside>
    </section>
  </main>
</template>

<style scoped>
.auth-page {
  display: grid;
  min-height: 100dvh;
  padding: 24px;
  place-items: center;
  --auth-accent: #ff6900;
  --auth-accent-soft: rgba(255, 105, 0, 0.12);
  --auth-text: #1d1d1f;
  --auth-muted: rgba(29, 29, 31, 0.58);
  --auth-line: rgba(17, 24, 39, 0.08);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(246, 248, 252, 0.9)),
    linear-gradient(180deg, #fbfbfb 0%, #f3f5f9 100%);
}

.auth-shell {
  position: relative;
  overflow: hidden;
  isolation: isolate;
  display: grid;
  width: min(100%, 860px);
  grid-template-columns: minmax(0, 1fr) 300px;
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 26px 70px rgba(31, 41, 55, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(24px) saturate(170%);
  -webkit-backdrop-filter: blur(24px) saturate(170%);
}

.auth-shell::before {
  position: absolute;
  inset: 1px 1px auto;
  z-index: 0;
  height: 42%;
  border-radius: inherit;
  background:
    linear-gradient(120deg, rgba(255, 255, 255, 0.78), rgba(255, 255, 255, 0.1)),
    linear-gradient(90deg, rgba(255, 105, 0, 0.08), transparent 36%);
  content: '';
  pointer-events: none;
}

.auth-shell::after {
  display: none;
}

.auth-form-panel,
.demo-panel {
  position: relative;
  z-index: 1;
  padding: 26px;
}

.auth-form-panel {
  border-right: 1px solid var(--auth-line);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  text-align: left;
}

.brand-mark {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 12px;
  background: var(--auth-accent);
  color: #fff;
  font-size: 18px;
  box-shadow: 0 12px 24px rgba(255, 105, 0, 0.16);
}

.brand strong,
.brand small {
  display: block;
}

.brand strong {
  color: var(--auth-text);
  font-size: 16px;
  font-weight: 800;
}

.brand small,
.auth-header p,
.demo-panel p,
.panel-label,
.auth-switch {
  color: var(--auth-muted);
}

.brand small,
.panel-label,
.scope-list span {
  font-size: 12px;
}

.auth-header {
  margin: 30px 0 22px;
}

.auth-header h1 {
  margin: 0;
  color: var(--auth-text);
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 0;
}

.auth-header p,
.demo-panel p {
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.7;
}

.auth-submit {
  width: 100%;
  min-height: 46px;
  margin-top: 4px;
}

.auth-page :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 0 0 1px var(--auth-line);
}

.auth-page :deep(.el-button--primary) {
  border-color: var(--auth-accent);
  background: var(--auth-accent);
  box-shadow: 0 12px 26px rgba(255, 105, 0, 0.18), inset 0 1px 0 rgba(255, 255, 255, 0.36);
}

.auth-switch {
  margin-top: 18px;
  text-align: center;
}

.auth-switch a {
  color: var(--auth-accent);
  font-weight: 800;
  text-decoration: none;
}

.demo-panel {
  overflow: hidden;
  isolation: isolate;
  display: flex;
  flex-direction: column;
  gap: 18px;
  border-left: 1px solid rgba(255, 255, 255, 0.34);
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.74), rgba(247, 248, 251, 0.56)),
    rgba(255, 255, 255, 0.58);
}

.demo-panel::before {
  display: none;
}

.demo-panel::after {
  display: none;
}

.demo-panel > * {
  position: relative;
  z-index: 1;
}

.panel-label {
  font-weight: 800;
}

.demo-panel h2 {
  margin: 10px 0 0;
  color: var(--auth-text);
  font-size: 18px;
  font-weight: 800;
}

.demo-fill-button {
  min-height: 44px;
  border-color: rgba(255, 105, 0, 0.2);
  background: rgba(255, 255, 255, 0.74);
  color: var(--auth-accent);
  font-weight: 800;
}

.scope-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: auto;
}

.scope-list span {
  position: relative;
  padding: 6px 9px;
  border: 1px solid rgba(17, 24, 39, 0.06);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.74);
  color: var(--auth-text);
  font-weight: 700;
}

@media (max-width: 760px) {
  .auth-page {
    padding: 16px;
  }

  .auth-shell {
    grid-template-columns: 1fr;
  }

  .auth-form-panel {
    border-right: 0;
    border-bottom: 1px solid var(--auth-line);
  }

  .demo-panel {
    border-left: 0;
    border-top: 1px solid rgba(255, 255, 255, 0.34);
  }

  .auth-form-panel,
  .demo-panel {
    padding: 24px;
  }
}
</style>
