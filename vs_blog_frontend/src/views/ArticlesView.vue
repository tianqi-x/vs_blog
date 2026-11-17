<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

import {
  apiErrorMessage,
  listPublicArticles,
  listPublicCategories,
  listPublicTags,
  searchPublicArticles,
} from '@/api/content'
import type { Article, Category, Tag } from '@/api/types'

const route = useRoute()
const articles = ref<Article[]>([])
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const query = ref(typeof route.query.q === 'string' ? route.query.q : '')
const appliedQuery = ref(query.value)
const categoryId = ref('')
const tagId = ref('')
const loading = ref(true)
const error = ref('')

const filteredArticles = computed(() => {
  const search = appliedQuery.value.trim().toLocaleLowerCase()
  return articles.value.filter((article) => {
    const matchesText = !search
      || `${article.title} ${article.summary || ''} ${article.contentMarkdown}`.toLocaleLowerCase().includes(search)
    return matchesText
      && (!categoryId.value || article.categoryId === categoryId.value)
      && (!tagId.value || article.tagIds.includes(tagId.value))
  })
})

onMounted(async () => {
  await loadPage()
})

async function loadPage() {
  loading.value = true
  error.value = ''
  try {
    const [articleData, categoryData, tagData] = await Promise.all([
      appliedQuery.value.trim()
        ? searchPublicArticles(appliedQuery.value.trim(), 0, 100).then((page) => page.content)
        : listPublicArticles(),
      listPublicCategories(),
      listPublicTags(),
    ])
    articles.value = articleData
    categories.value = categoryData
    tags.value = tagData
  } catch (cause) {
    error.value = apiErrorMessage(cause, '文章列表加载失败。')
  } finally {
    loading.value = false
  }
}

async function submitSearch() {
  appliedQuery.value = query.value
  await loadPage()
}
</script>

<template>
  <section class="page">
    <p class="eyebrow">KNOWLEDGE NOTES</p>
    <h1>文章</h1>
    <p class="intro">按关键词、分类或标签查找公开文章。</p>
    <form class="filters" @submit.prevent="submitSearch">
      <label class="sr-only" for="article-search">搜索文章</label>
      <input id="article-search" v-model="query" type="search" placeholder="搜索标题、正文或标签">
      <button type="submit" :disabled="loading">搜索</button>
      <label class="sr-only" for="category-filter">按分类筛选</label>
      <select id="category-filter" v-model="categoryId">
        <option value="">全部分类</option>
        <option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option>
      </select>
      <label class="sr-only" for="tag-filter">按标签筛选</label>
      <select id="tag-filter" v-model="tagId">
        <option value="">全部标签</option>
        <option v-for="tag in tags" :key="tag.id" :value="tag.id">{{ tag.name }}</option>
      </select>
    </form>
    <p v-if="loading" class="status-message" role="status">正在加载文章…</p>
    <p v-else-if="error" class="error-message" role="alert">{{ error }}</p>
    <p v-else-if="!filteredArticles.length" class="empty-state">没有符合条件的文章。</p>
    <ul v-else class="content-list article-list">
      <li v-for="article in filteredArticles" :key="article.id">
        <RouterLink :to="`/articles/${article.slug}`">
          <strong>{{ article.title }}</strong>
          <span>{{ article.summary || '打开文章阅读' }}</span>
          <small v-if="article.publishedAt">{{ new Date(article.publishedAt).toLocaleDateString() }}</small>
        </RouterLink>
      </li>
    </ul>
  </section>
</template>
