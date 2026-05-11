<script setup lang="ts">
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
  await formRef.value?.validate();
  loading.value = true;
  try {
    await auth.registerAndLogin({
      username: form.username,
      email: form.email || undefined,
      nickname: form.nickname || undefined,
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
  <div class="register-page">
    <el-card class="register-card" shadow="never">
      <template #header>
        <div class="page-header compact">
          <div>
            <h1 class="page-title">创建家庭设备档案</h1>
            <p class="page-subtitle">注册后系统会自动创建默认家庭空间。</p>
          </div>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" size="large" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" size="large" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" size="large" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" size="large" type="password" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="register-submit" @click="handleRegister">
          注册并进入首页
        </el-button>
      </el-form>
      <div class="login-link">
        已有账号？<RouterLink to="/login">返回登录</RouterLink>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.register-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 32px;
}

.register-card {
  width: min(560px, 100%);
  border: none;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 70px rgba(36, 49, 47, 0.14);
}

.compact {
  margin-bottom: 0;
}

.register-submit {
  width: 100%;
}

.login-link {
  margin-top: 18px;
  color: var(--fl-muted);
  text-align: center;
}

.login-link a {
  color: var(--fl-green);
  font-weight: 800;
  text-decoration: none;
}
</style>