<script setup lang="ts">
import { onMounted, ref } from 'vue'

import { apiErrorMessage, listPublicArticles, listPublicProjects } from '@/api/content'
import type { Article, Project } from '@/api/types'

const articles = ref<Article[]>([])
const projects = ref<Project[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    ;[articles.value, projects.value] = await Promise.all([
      listPublicArticles(),
      listPublicProjects(),
    ])
  } catch (cause) {
    error.value = apiErrorMessage(cause, '首页内容加载失败。')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page home-page">
    <p class="eyebrow">PERSONAL KNOWLEDGE BASE</p>
    <h1>把学到的，整理成自己的。</h1>
    <p class="intro">知识笔记、实践记录与作品集，持续归档在这里。</p>
    <div class="home-links">
      <RouterLink to="/articles">浏览文章 <span aria-hidden="true">→</span></RouterLink>
      <RouterLink to="/projects">查看作品 <span aria-hidden="true">→</span></RouterLink>
    </div>
    <p v-if="loading" class="status-message" role="status">正在加载最新内容…</p>
    <p v-else-if="error" class="error-message" role="alert">{{ error }}</p>
    <div v-else class="home-content">
      <section>
        <div class="section-heading">
          <h2>最新文章</h2>
          <RouterLink to="/articles">全部文章 →</RouterLink>
        </div>
        <ul v-if="articles.length" class="content-list">
          <li v-for="article in articles.slice(0, 3)" :key="article.id">
            <RouterLink :to="`/articles/${article.slug}`">
              <strong>{{ article.title }}</strong>
              <span>{{ article.summary || '查看文章' }}</span>
            </RouterLink>
          </li>
        </ul>
        <p v-else class="muted">目前还没有公开文章。</p>
      </section>
      <section>
        <div class="section-heading">
          <h2>近期作品</h2>
          <RouterLink to="/projects">全部作品 →</RouterLink>
        </div>
        <ul v-if="projects.length" class="content-list">
          <li v-for="project in projects.slice(0, 3)" :key="project.id">
            <RouterLink :to="`/projects/${project.slug}`">
              <strong>{{ project.title }}</strong>
              <span>{{ project.summary }}</span>
            </RouterLink>
          </li>
        </ul>
        <p v-else class="muted">目前还没有公开作品。</p>
      </section>
    </div>
  </section>
</template>
