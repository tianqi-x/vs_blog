<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const onSessionExpired = () => {
  void router.replace({ name: 'admin-login', query: { error: 'session-expired' } })
}

onMounted(() => window.addEventListener('vsblog:session-expired', onSessionExpired))
onBeforeUnmount(() => window.removeEventListener('vsblog:session-expired', onSessionExpired))
</script>

<template>
  <div class="site-shell">
    <header class="site-header">
      <RouterLink class="brand" to="/">序章知识小栈</RouterLink>
      <nav aria-label="主导航">
        <RouterLink to="/articles">文章</RouterLink>
        <RouterLink to="/projects">作品</RouterLink>
        <RouterLink to="/about">关于</RouterLink>
        <RouterLink to="/admin/login">管理</RouterLink>
      </nav>
    </header>
    <main>
      <RouterView />
    </main>
  </div>
</template>