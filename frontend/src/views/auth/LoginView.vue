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
      <div class="hero-badge">FixLedger MVP</div>
      <h1>每台设备都有一份清晰的家庭档案</h1>
      <p>保修、发票、耗材、维修记录和提醒统一管理，面试演示时可以完整跑通家庭设备生命周期。</p>
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
  display: grid;
  min-height: 100vh;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 40px;
  align-items: center;
  padding: 48px clamp(24px, 6vw, 84px);
}

.auth-hero {
  position: relative;
  overflow: hidden;
  min-height: 620px;
  padding: 56px;
  border-radius: 36px;
  background:
    linear-gradient(145deg, rgba(47, 125, 104, 0.94), rgba(31, 93, 77, 0.92)),
    radial-gradient(circle at 20% 20%, rgba(242, 166, 90, 0.48), transparent 32%);
  color: #fff;
  box-shadow: 0 28px 80px rgba(31, 93, 77, 0.35);
}

.auth-hero::after {
  position: absolute;
  right: -80px;
  bottom: -90px;
  width: 280px;
  height: 280px;
  border: 34px solid rgba(255, 255, 255, 0.16);
  border-radius: 999px;
  content: '';
}

.hero-badge {
  display: inline-flex;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-weight: 800;
}

.auth-hero h1 {
  max-width: 620px;
  margin: 34px 0 20px;
  font-size: clamp(42px, 6vw, 72px);
  line-height: 0.96;
  letter-spacing: -0.07em;
}

.auth-hero p {
  max-width: 560px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 18px;
  line-height: 1.8;
}

.hero-cards {
  display: grid;
  max-width: 520px;
  margin-top: 56px;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.hero-cards div {
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.12);
  font-weight: 800;
}

.auth-card {
  border: none;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 24px 70px rgba(36, 49, 47, 0.14);
}

.auth-card h2 {
  margin: 0 0 6px;
  color: var(--fl-ink);
  font-size: 28px;
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
  padding: 14px;
  border: 1px dashed rgba(47, 125, 104, 0.32);
  border-radius: 18px;
  background: rgba(220, 238, 230, 0.5);
}

.demo-account-box strong,
.demo-account-box span {
  display: block;
}

.demo-account-box strong {
  color: var(--fl-green-dark);
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
  color: var(--fl-green);
  font-weight: 800;
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
    padding: 32px;
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
