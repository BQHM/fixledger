<script setup lang="ts">
import { House } from '@element-plus/icons-vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const auth = useAuthStore();
const formRef = ref<FormInstance>();
const loading = ref(false);
const form = reactive({
  username: '',
  email: '',
  nickname: '',
  password: '',
  confirmPassword: ''
});

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 8, message: '密码至少 8 位', trigger: 'blur' }],
  confirmPassword: [
    {
      validator: (_rule, value, callback) => {
        value === form.password ? callback() : callback(new Error('两次密码不一致'));
      },
      trigger: 'blur'
    }
  ]
};

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    await auth.registerAndLogin({
      username: form.username.trim(),
      email: form.email.trim() || undefined,
      nickname: form.nickname.trim() || undefined,
      password: form.password
    });
    ElMessage.success('注册成功，默认家庭空间已创建');
    router.push('/dashboard');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="register-page">
    <section class="register-shell" aria-labelledby="register-title">
      <button class="brand" type="button" aria-label="返回 FixLedger 首页" @click="router.push('/')">
        <span class="brand-mark"><el-icon><House /></el-icon></span>
        <span>
          <strong>FixLedger</strong>
          <small>家庭设备档案</small>
        </span>
      </button>

      <header class="register-header">
        <h1 id="register-title">创建家庭档案</h1>
        <p>注册后会自动创建默认家庭空间，后续可邀请家庭成员共同维护。</p>
      </header>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleRegister">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model.trim="form.email" type="email" autocomplete="email" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model.trim="form.nickname" autocomplete="name" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" autocomplete="new-password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            autocomplete="new-password"
            show-password
          />
        </el-form-item>
        <el-button type="primary" size="large" native-type="submit" :loading="loading" class="register-submit">
          注册并进入总览
        </el-button>
      </el-form>

      <div class="login-link">
        已有账号？<RouterLink to="/login">返回登录</RouterLink>
      </div>
    </section>
  </main>
</template>

<style scoped>
.register-page {
  display: grid;
  min-height: 100dvh;
  padding: 32px 16px;
  place-items: center;
  --auth-accent: #ff6900;
  --auth-text: #1d1d1f;
  --auth-muted: rgba(29, 29, 31, 0.58);
  --auth-line: rgba(17, 24, 39, 0.08);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(246, 248, 252, 0.9)),
    linear-gradient(180deg, #fbfbfb 0%, #f3f5f9 100%);
}

.register-shell {
  position: relative;
  overflow: hidden;
  isolation: isolate;
  width: min(100%, 520px);
  padding: 28px;
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 26px 70px rgba(31, 41, 55, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(24px) saturate(170%);
  -webkit-backdrop-filter: blur(24px) saturate(170%);
}

.register-shell::before {
  position: absolute;
  inset: 1px 1px auto;
  z-index: 0;
  height: 38%;
  border-radius: inherit;
  background:
    linear-gradient(120deg, rgba(255, 255, 255, 0.78), rgba(255, 255, 255, 0.1)),
    linear-gradient(90deg, rgba(255, 105, 0, 0.08), transparent 36%);
  content: '';
  pointer-events: none;
}

.register-shell::after {
  display: none;
}

.register-shell > * {
  position: relative;
  z-index: 1;
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
.register-header p,
.login-link {
  color: var(--auth-muted);
}

.brand small {
  font-size: 12px;
}

.register-header {
  margin: 30px 0 22px;
}

.register-header h1 {
  margin: 0;
  color: var(--auth-text);
  font-size: 24px;
  font-weight: 800;
}

.register-header p {
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.7;
}

.register-submit {
  width: 100%;
  min-height: 46px;
}

.register-page :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 0 0 1px var(--auth-line);
}

.register-page :deep(.el-button--primary) {
  border-color: var(--auth-accent);
  background: var(--auth-accent);
  box-shadow: 0 12px 26px rgba(255, 105, 0, 0.18), inset 0 1px 0 rgba(255, 255, 255, 0.36);
}

.login-link {
  margin-top: 18px;
  text-align: center;
}

.login-link a {
  color: var(--auth-accent);
  font-weight: 800;
  text-decoration: none;
}

@media (max-width: 520px) {
  .register-shell {
    padding: 24px;
  }
}
</style>
