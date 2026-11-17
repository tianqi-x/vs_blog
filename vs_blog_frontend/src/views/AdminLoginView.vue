<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { apiErrorMessage, loginAdmin } from '@/api/content'

const route = useRoute()
const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref(
  route.query.error === 'unavailable'
    ? '暂时无法验证登录状态，请确认后端服务正常后重试。'
    : route.query.error === 'session-expired'
      ? '登录会话已失效，请重新登录。'
    : '',
)

async function submitLogin() {
  loading.value = true
  error.value = ''
  try {
    await loginAdmin(username.value, password.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    const target = redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/admin'
    await router.replace(target)
  } catch (cause) {
    error.value = apiErrorMessage(cause, '登录失败，请检查账号密码后重试。')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="page narrow-page">
    <p class="eyebrow">ADMIN</p>
    <h1>管理员登录</h1>
    <form class="stack-form login-form" @submit.prevent="submitLogin">
      <label>
        用户名
        <input v-model="username" autocomplete="username" required>
      </label>
      <label>
        密码
        <input v-model="password" type="password" autocomplete="current-password" required>
      </label>
      <p v-if="error" class="error-message" role="alert">{{ error }}</p>
      <button type="submit" :disabled="loading">{{ loading ? '正在登录…' : '登录' }}</button>
    </form>
  </section>
</template>
