<script setup lang="ts">
import { onMounted, ref } from 'vue'

import { apiErrorMessage, listPublicProjects } from '@/api/content'
import type { Project } from '@/api/types'

const projects = ref<Project[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    projects.value = await listPublicProjects()
  } catch (cause) {
    error.value = apiErrorMessage(cause, '作品列表加载失败。')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page">
    <p class="eyebrow">SELECTED WORK</p>
    <h1>作品</h1>
    <p class="intro">项目实践与作品记录。</p>
    <p v-if="loading" class="status-message" role="status">正在加载作品…</p>
    <p v-else-if="error" class="error-message" role="alert">{{ error }}</p>
    <p v-else-if="!projects.length" class="empty-state">目前还没有公开作品。</p>
    <ul v-else class="content-list">
      <li v-for="project in projects" :key="project.id">
        <RouterLink :to="`/projects/${project.slug}`">
          <strong>{{ project.title }}</strong>
          <span>{{ project.summary }}</span>
          <small v-if="project.techStack.length">{{ project.techStack.join(' · ') }}</small>
        </RouterLink>
      </li>
    </ul>
  </section>
</template>
