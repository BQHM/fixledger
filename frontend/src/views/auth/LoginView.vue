<script setup lang="ts">
import { Lock, User } from '@element-plus/icons-vue';
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

function fillDemoAccount() {
  form.account = 'demo';
  form.password = 'fixledger123';
  ElMessage.info('已填入本地演示账号，请确认后登录');
}

async function handleLogin() {
  await formRef.value?.validate();
  loading.value = true;
  try {
    await auth.login(form);
    ElMessage.success('欢迎回来，继续整理家庭设备档案');
    router.push((route.query.redirect as string) || '/dashboard');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="auth-page">
    <section class="auth-hero">
      <div class="hero-badge">FixLedger Home</div>
      <h1>像管理智能家一样整理每台设备</h1>
      <p>把保修、发票、耗材、维修和提醒收进一个温暖的家庭设备空间。</p>
      <div class="hero-cards">
        <div>设备护照</div>
        <div>家庭日历</div>
        <div>凭证盒</div>
        <div>AI 辅助</div>
      </div>
    </section>

    <el-card class="auth-card" shadow="never">
      <template #header>
        <div>
          <h2>登录 FixLedger</h2>
          <span>表单默认留空；本地演示时可手动填入演示账号，避免把演示密码误认为生产密钥。</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleLogin">
        <el-form-item label="账号" prop="account">
          <el-input v-model="form.account" size="large" placeholder="用户名或邮箱" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            size="large"
            type="password"
            show-password
            placeholder="请输入密码"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <div class="demo-account-box">
          <div>
            <strong>本地演示账号</strong>
            <span>仅用于 Docker 演示数据：demo / fixledger123</span>
          </div>
          <el-button plain @click="fillDemoAccount">填入演示账号</el-button>
        </div>
        <el-button type="primary" size="large" :loading="loading" class="auth-submit" @click="handleLogin">
          登录并进入首页
        </el-button>
      </el-form>
      <div class="auth-switch">
        还没有账号？<RouterLink to="/register">注册一个家庭档案</RouterLink>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  position: relative;
  display: grid;
  min-height: 100vh;
  grid-template-columns: minmax(0, 1.08fr) minmax(390px, 0.78fr);
  gap: clamp(28px, 5vw, 56px);
  align-items: center;
  padding: 48px clamp(22px, 6vw, 86px);
}

.auth-page::before {
  position: fixed;
  inset: 0;
  z-index: -1;
  background:
    radial-gradient(circle at 14% 14%, rgba(255, 188, 103, 0.34), transparent 30%),
    radial-gradient(circle at 72% 74%, rgba(255, 255, 255, 0.9), transparent 34%),
    linear-gradient(145deg, #f5f1e8 0%, #fbfaf5 58%, #ecefe8 100%);
  content: '';
}

.auth-hero {
  position: relative;
  overflow: hidden;
  min-height: 640px;
  padding: clamp(36px, 5vw, 64px);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 42px;
  background:
    radial-gradient(circle at 18% 18%, rgba(255, 209, 135, 0.5), transparent 28%),
    radial-gradient(circle at 82% 18%, rgba(255, 255, 255, 0.42), transparent 26%),
    linear-gradient(145deg, #fffdf7 0%, #f6ead9 48%, #e9eee6 100%);
  color: var(--fl-ink);
  box-shadow: 0 34px 90px rgba(94, 78, 54, 0.14);
}

.auth-hero::before {
  position: absolute;
  right: 8%;
  bottom: 10%;
  width: 220px;
  height: 220px;
  border-radius: 48px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.82), rgba(255, 238, 210, 0.8));
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.8), 0 26px 58px rgba(94, 78, 54, 0.12);
  content: '';
  transform: rotate(-8deg);
}

.auth-hero::after {
  position: absolute;
  right: -96px;
  bottom: -110px;
  width: 300px;
  height: 300px;
  border: 36px solid rgba(255, 138, 31, 0.14);
  border-radius: 999px;
  content: '';
}

.hero-badge {
  position: relative;
  z-index: 1;
  display: inline-flex;
  padding: 9px 15px;
  border-radius: 999px;
  background: rgba(255, 138, 31, 0.12);
  color: var(--fl-mi-orange-dark);
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 0.16em;
}

.auth-hero h1,
.auth-hero p,
.hero-cards {
  position: relative;
  z-index: 1;
}

.auth-hero h1 {
  max-width: 650px;
  margin: 34px 0 20px;
  color: var(--fl-ink);
  font-size: clamp(44px, 6vw, 78px);
  line-height: 0.96;
  letter-spacing: -0.08em;
}

.auth-hero p {
  max-width: 540px;
  color: var(--fl-muted);
  font-size: 18px;
  line-height: 1.85;
}

.hero-cards {
  display: grid;
  max-width: 540px;
  margin-top: 58px;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.hero-cards div {
  min-height: 94px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.62);
  box-shadow: 0 14px 32px rgba(88, 72, 49, 0.08);
  color: var(--fl-ink);
  font-weight: 900;
}

.auth-card {
  border: 1px solid rgba(255, 255, 255, 0.78);
  border-radius: 32px;
  background: rgba(255, 253, 248, 0.86);
  box-shadow: 0 26px 76px rgba(88, 72, 49, 0.14);
  backdrop-filter: blur(18px);
}

.auth-card h2 {
  margin: 0 0 6px;
  color: var(--fl-ink);
  font-size: 30px;
  font-weight: 950;
  letter-spacing: -0.04em;
}

.auth-card span,
.auth-switch {
  color: var(--fl-muted);
}

.demo-account-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin: 6px 0 16px;
  padding: 15px;
  border: 1px dashed rgba(255, 138, 31, 0.36);
  border-radius: 20px;
  background: rgba(255, 244, 229, 0.76);
}

.demo-account-box strong,
.demo-account-box span {
  display: block;
}

.demo-account-box strong {
  color: var(--fl-mi-orange-dark);
  font-size: 14px;
}

.demo-account-box span {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.6;
}

.auth-submit {
  width: 100%;
  margin-top: 8px;
}

.auth-switch {
  margin-top: 20px;
  text-align: center;
}

.auth-switch a {
  color: var(--fl-mi-orange-dark);
  font-weight: 900;
  text-decoration: none;
}

@media (max-width: 960px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-hero {
    min-height: auto;
  }
}

@media (max-width: 560px) {
  .auth-page {
    padding: 28px 16px;
  }

  .auth-hero {
    padding: 30px;
    border-radius: 30px;
  }

  .hero-cards,
  .demo-account-box {
    grid-template-columns: 1fr;
  }

  .demo-account-box {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>