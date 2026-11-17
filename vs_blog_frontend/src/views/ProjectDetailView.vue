<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { apiErrorMessage, getPublicProject } from '@/api/content'
import type { Project } from '@/api/types'
import { renderMarkdown } from '@/utils/markdown'

const route = useRoute()
const project = ref<Project | null>(null)
const loading = ref(true)
const error = ref('')

async function loadProject() {
  loading.value = true
  error.value = ''
  try {
    project.value = await getPublicProject(String(route.params.slug))
  } catch (cause) {
    project.value = null
    error.value = apiErrorMessage(cause, '作品加载失败。')
  } finally {
    loading.value = false
  }
}

onMounted(loadProject)
watch(() => route.params.slug, loadProject)
</script>

<template>
  <article class="page detail-page">
    <p v-if="loading" class="status-message" role="status">正在加载作品…</p>
    <p v-else-if="error" class="error-message" role="alert">{{ error }}</p>
    <template v-else-if="project">
      <RouterLink class="back-link" to="/projects">← 返回作品列表</RouterLink>
      <p class="eyebrow">PROJECT</p>
      <h1>{{ project.title }}</h1>
      <p class="intro">{{ project.summary }}</p>
      <p v-if="project.techStack.length" class="muted">技术栈：{{ project.techStack.join(' · ') }}</p>
      <div class="project-links">
        <a v-if="project.projectUrl" :href="project.projectUrl" target="_blank" rel="noopener noreferrer">项目地址 ↗</a>
        <a v-if="project.repositoryUrl" :href="project.repositoryUrl" target="_blank" rel="noopener noreferrer">源代码 ↗</a>
      </div>
      <div class="markdown-body" v-html="renderMarkdown(project.descriptionMarkdown)" />
    </template>
  </article>
</template>
