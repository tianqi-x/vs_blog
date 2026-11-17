<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { apiErrorMessage, getPublicArticle } from '@/api/content'
import type { Article } from '@/api/types'
import { renderMarkdown } from '@/utils/markdown'

const route = useRoute()
const article = ref<Article | null>(null)
const loading = ref(true)
const error = ref('')

async function loadArticle() {
  const slug = String(route.params.slug)
  loading.value = true
  error.value = ''
  try {
    article.value = await getPublicArticle(slug)
  } catch (cause) {
    article.value = null
    error.value = apiErrorMessage(cause, '文章加载失败。')
  } finally {
    loading.value = false
  }
}

onMounted(loadArticle)
watch(() => route.params.slug, loadArticle)
</script>

<template>
  <article class="page detail-page">
    <p v-if="loading" class="status-message" role="status">正在加载文章…</p>
    <p v-else-if="error" class="error-message" role="alert">{{ error }}</p>
    <template v-else-if="article">
      <RouterLink class="back-link" to="/articles">← 返回文章列表</RouterLink>
      <p class="eyebrow">ARTICLE</p>
      <h1>{{ article.title }}</h1>
      <p v-if="article.publishedAt" class="muted">{{ new Date(article.publishedAt).toLocaleDateString() }}</p>
      <p v-if="article.summary" class="intro">{{ article.summary }}</p>
      <div class="markdown-body" v-html="renderMarkdown(article.contentMarkdown)" />
    </template>
  </article>
</template>
