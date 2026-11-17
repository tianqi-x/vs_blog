<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import {
  apiErrorMessage,
  deleteArticle,
  deleteCategory,
  deleteProject,
  deleteTag,
  listAdminArticles,
  listAdminCategories,
  listAdminProjects,
  listAdminTags,
  logoutAdmin,
  saveArticle,
  saveCategory,
  saveProject,
  saveTag,
} from '@/api/content'
import type {
  Article,
  ArticleInput,
  Category,
  CategoryInput,
  ContentStatus,
  ContentVisibility,
  Project,
  ProjectInput,
  Tag,
  TagInput,
} from '@/api/types'
import { renderMarkdown } from '@/utils/markdown'

type AdminSection = 'articles' | 'projects' | 'categories' | 'tags'

const router = useRouter()
const section = ref<AdminSection>('articles')
const articles = ref<Article[]>([])
const projects = ref<Project[]>([])
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const notice = ref('')
const editingId = ref<string | null>(null)

const articleForm = reactive({
  title: '',
  slug: '',
  summary: '',
  contentMarkdown: '',
  categoryId: '',
  status: 'DRAFT' as ContentStatus,
  visibility: 'PRIVATE' as ContentVisibility,
  coverImageUrl: '',
  pinned: false,
  seoTitle: '',
  seoDescription: '',
  tagIds: [] as string[],
})

const projectForm = reactive({
  title: '',
  slug: '',
  summary: '',
  descriptionMarkdown: '',
  techStackText: '',
  projectUrl: '',
  repositoryUrl: '',
  coverImageUrl: '',
  galleryText: '',
  role: '',
  status: 'DRAFT' as ContentStatus,
  visibility: 'PRIVATE' as ContentVisibility,
  featured: false,
  sortOrder: 0,
  startedAt: '',
  completedAt: '',
  seoTitle: '',
  seoDescription: '',
  articleIdsText: '',
})

const categoryForm = reactive({ name: '', slug: '', description: '', sortOrder: 0 })
const tagForm = reactive({ name: '', slug: '' })
const sectionTitle = computed(() => ({
  articles: '文章',
  projects: '作品',
  categories: '分类',
  tags: '标签',
}[section.value]))

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const [articleData, projectData, categoryData, tagData] = await Promise.all([
      listAdminArticles(),
      listAdminProjects(),
      listAdminCategories(),
      listAdminTags(),
    ])
    articles.value = articleData
    projects.value = projectData
    categories.value = categoryData
    tags.value = tagData
  } catch (cause) {
    error.value = apiErrorMessage(cause, '管理数据加载失败。')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingId.value = null
  Object.assign(articleForm, {
    title: '', slug: '', summary: '', contentMarkdown: '', categoryId: '',
    status: 'DRAFT', visibility: 'PRIVATE', coverImageUrl: '', pinned: false,
    seoTitle: '', seoDescription: '', tagIds: [],
  })
  Object.assign(projectForm, {
    title: '', slug: '', summary: '', descriptionMarkdown: '', techStackText: '',
    projectUrl: '', repositoryUrl: '', coverImageUrl: '', galleryText: '', role: '',
    status: 'DRAFT', visibility: 'PRIVATE', featured: false, sortOrder: 0,
    startedAt: '', completedAt: '', seoTitle: '', seoDescription: '', articleIdsText: '',
  })
  Object.assign(categoryForm, { name: '', slug: '', description: '', sortOrder: 0 })
  Object.assign(tagForm, { name: '', slug: '' })
  error.value = ''
  notice.value = ''
}

function editArticle(article: Article) {
  resetForm()
  section.value = 'articles'
  editingId.value = article.id
  Object.assign(articleForm, {
    title: article.title,
    slug: article.slug,
    summary: article.summary || '',
    contentMarkdown: article.contentMarkdown,
    categoryId: article.categoryId || '',
    status: article.status,
    visibility: article.visibility,
    coverImageUrl: article.coverImageUrl || '',
    pinned: article.pinned,
    seoTitle: article.seoTitle || '',
    seoDescription: article.seoDescription || '',
    tagIds: [...article.tagIds],
  })
}

function editProject(project: Project) {
  resetForm()
  section.value = 'projects'
  editingId.value = project.id
  Object.assign(projectForm, {
    title: project.title,
    slug: project.slug,
    summary: project.summary,
    descriptionMarkdown: project.descriptionMarkdown,
    techStackText: project.techStack.join(', '),
    projectUrl: project.projectUrl || '',
    repositoryUrl: project.repositoryUrl || '',
    coverImageUrl: project.coverImageUrl || '',
    galleryText: project.gallery.join(', '),
    role: project.role || '',
    status: project.status,
    visibility: project.visibility,
    featured: project.featured,
    sortOrder: project.sortOrder,
    startedAt: project.startedAt || '',
    completedAt: project.completedAt || '',
    seoTitle: project.seoTitle || '',
    seoDescription: project.seoDescription || '',
    articleIdsText: project.articleIds.join(', '),
  })
}

function editCategory(category: Category) {
  resetForm()
  section.value = 'categories'
  editingId.value = category.id
  Object.assign(categoryForm, {
    name: category.name,
    slug: category.slug,
    description: category.description || '',
    sortOrder: category.sortOrder || 0,
  })
}

function editTag(tag: Tag) {
  resetForm()
  section.value = 'tags'
  editingId.value = tag.id
  Object.assign(tagForm, { name: tag.name, slug: tag.slug })
}

function csvValues(text: string): string[] {
  return text.split(',').map((value) => value.trim()).filter(Boolean)
}

async function submitArticle() {
  const input: ArticleInput = { ...articleForm, categoryId: articleForm.categoryId || null }
  await save(async () => saveArticle(input, editingId.value || undefined), '文章已保存。')
}

async function submitProject() {
  const input: ProjectInput = {
    title: projectForm.title,
    slug: projectForm.slug,
    summary: projectForm.summary,
    descriptionMarkdown: projectForm.descriptionMarkdown,
    techStack: csvValues(projectForm.techStackText),
    projectUrl: projectForm.projectUrl,
    repositoryUrl: projectForm.repositoryUrl,
    coverImageUrl: projectForm.coverImageUrl,
    gallery: csvValues(projectForm.galleryText),
    role: projectForm.role,
    status: projectForm.status,
    visibility: projectForm.visibility,
    featured: projectForm.featured,
    sortOrder: Number(projectForm.sortOrder),
    startedAt: projectForm.startedAt || null,
    completedAt: projectForm.completedAt || null,
    seoTitle: projectForm.seoTitle,
    seoDescription: projectForm.seoDescription,
    articleIds: csvValues(projectForm.articleIdsText),
  }
  await save(async () => saveProject(input, editingId.value || undefined), '作品已保存。')
}

async function submitCategory() {
  const input: CategoryInput = { ...categoryForm, sortOrder: Number(categoryForm.sortOrder) }
  await save(async () => saveCategory(input, editingId.value || undefined), '分类已保存。')
}

async function submitTag() {
  const input: TagInput = { ...tagForm }
  await save(async () => saveTag(input, editingId.value || undefined), '标签已保存。')
}

async function save(action: () => Promise<unknown>, successMessage: string) {
  saving.value = true
  error.value = ''
  notice.value = ''
  try {
    await action()
    resetForm()
    notice.value = successMessage
    await loadData()
  } catch (cause) {
    error.value = apiErrorMessage(cause, '保存失败，请检查输入后重试。')
  } finally {
    saving.value = false
  }
}

async function removeItem(action: () => Promise<void>, label: string) {
  if (!window.confirm(`确定删除此${label}？此操作不可撤销。`)) return
  error.value = ''
  notice.value = ''
  try {
    await action()
    notice.value = `${label}已删除。`
    await loadData()
  } catch (cause) {
    error.value = apiErrorMessage(cause, `删除${label}失败。`)
  }
}

async function signOut() {
  try {
    await logoutAdmin()
    await router.replace('/admin/login')
  } catch (cause) {
    error.value = apiErrorMessage(cause, '退出登录失败。')
  }
}

onMounted(loadData)
</script>

<template>
  <section class="page admin-page">
    <div class="section-heading">
      <div>
        <p class="eyebrow">ADMIN CONSOLE</p>
        <h1>内容管理</h1>
      </div>
      <button class="button-secondary" type="button" @click="signOut">退出登录</button>
    </div>

    <nav class="admin-tabs" aria-label="管理内容类型">
      <button v-for="item in ['articles', 'projects', 'categories', 'tags'] as const"
        :key="item" type="button" :aria-pressed="section === item"
        @click="section = item; resetForm()">
        {{ { articles: '文章', projects: '作品', categories: '分类', tags: '标签' }[item] }}
      </button>
    </nav>
    <p v-if="error" class="error-message" role="alert">{{ error }}</p>
    <p v-if="notice" class="success-message" role="status">{{ notice }}</p>
    <p v-if="loading" class="status-message" role="status">正在加载管理数据…</p>

    <template v-else>
      <section v-if="section === 'articles'">
        <h2>{{ sectionTitle }}列表</h2>
        <div class="admin-items">
          <div v-for="article in articles" :key="article.id" class="admin-item">
            <div><strong>{{ article.title }}</strong><small>{{ article.status }} · {{ article.visibility }}</small></div>
            <div class="button-row">
              <button class="button-secondary" type="button" @click="editArticle(article)">编辑</button>
              <button class="button-danger" type="button" @click="removeItem(() => deleteArticle(article.id), '文章')">删除</button>
            </div>
          </div>
          <p v-if="!articles.length" class="muted">暂无文章。</p>
        </div>
        <h2>{{ editingId ? '编辑文章' : '新建文章' }}</h2>
        <form class="stack-form" @submit.prevent="submitArticle">
          <div class="form-grid">
            <label>标题<input v-model="articleForm.title" required maxlength="255"></label>
            <label>Slug<input v-model="articleForm.slug" placeholder="留空则自动生成"></label>
            <label>分类
              <select v-model="articleForm.categoryId">
                <option value="">未分类</option>
                <option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option>
              </select>
            </label>
            <label>状态<select v-model="articleForm.status"><option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option><option value="ARCHIVED">归档</option></select></label>
            <label>可见性<select v-model="articleForm.visibility"><option value="PRIVATE">私有</option><option value="PUBLIC">公开</option></select></label>
            <label>封面图片 URL<input v-model="articleForm.coverImageUrl" type="url"></label>
            <label>SEO 标题<input v-model="articleForm.seoTitle"></label>
            <label>SEO 描述<input v-model="articleForm.seoDescription"></label>
          </div>
          <label>摘要<textarea v-model="articleForm.summary" rows="2" maxlength="1000"></textarea></label>
          <label>正文（Markdown）<textarea v-model="articleForm.contentMarkdown" rows="14" required></textarea></label>
          <details class="markdown-preview">
            <summary>预览正文</summary>
            <div class="markdown-body" v-html="renderMarkdown(articleForm.contentMarkdown)" />
          </details>
          <fieldset><legend>标签</legend>
            <label v-for="tag in tags" :key="tag.id" class="check-label">
              <input v-model="articleForm.tagIds" type="checkbox" :value="tag.id">{{ tag.name }}
            </label>
            <span v-if="!tags.length" class="muted">尚无标签</span>
          </fieldset>
          <label class="check-label"><input v-model="articleForm.pinned" type="checkbox">置顶文章</label>
          <div class="button-row">
            <button type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存文章' }}</button>
            <button v-if="editingId" class="button-secondary" type="button" @click="resetForm">取消编辑</button>
          </div>
        </form>
      </section>

      <section v-else-if="section === 'projects'">
        <h2>{{ sectionTitle }}列表</h2>
        <div class="admin-items">
          <div v-for="project in projects" :key="project.id" class="admin-item">
            <div><strong>{{ project.title }}</strong><small>{{ project.status }} · {{ project.visibility }}</small></div>
            <div class="button-row">
              <button class="button-secondary" type="button" @click="editProject(project)">编辑</button>
              <button class="button-danger" type="button" @click="removeItem(() => deleteProject(project.id), '作品')">删除</button>
            </div>
          </div>
          <p v-if="!projects.length" class="muted">暂无作品。</p>
        </div>
        <h2>{{ editingId ? '编辑作品' : '新建作品' }}</h2>
        <form class="stack-form" @submit.prevent="submitProject">
          <div class="form-grid">
            <label>标题<input v-model="projectForm.title" required maxlength="255"></label>
            <label>Slug<input v-model="projectForm.slug" placeholder="留空则自动生成"></label>
            <label>状态<select v-model="projectForm.status"><option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option><option value="ARCHIVED">归档</option></select></label>
            <label>可见性<select v-model="projectForm.visibility"><option value="PRIVATE">私有</option><option value="PUBLIC">公开</option></select></label>
            <label>项目地址<input v-model="projectForm.projectUrl" type="url"></label>
            <label>仓库地址<input v-model="projectForm.repositoryUrl" type="url"></label>
            <label>封面图片 URL<input v-model="projectForm.coverImageUrl" type="url"></label>
            <label>角色<input v-model="projectForm.role"></label>
            <label>开始日期<input v-model="projectForm.startedAt" type="date"></label>
            <label>完成日期<input v-model="projectForm.completedAt" type="date"></label>
            <label>排序<input v-model.number="projectForm.sortOrder" type="number"></label>
            <label>SEO 标题<input v-model="projectForm.seoTitle"></label>
            <label>SEO 描述<input v-model="projectForm.seoDescription"></label>
          </div>
          <label>摘要<textarea v-model="projectForm.summary" rows="2" required></textarea></label>
          <label>项目介绍（Markdown）<textarea v-model="projectForm.descriptionMarkdown" rows="12" required></textarea></label>
          <details class="markdown-preview">
            <summary>预览介绍</summary>
            <div class="markdown-body" v-html="renderMarkdown(projectForm.descriptionMarkdown)" />
          </details>
          <label>技术栈（逗号分隔）<input v-model="projectForm.techStackText"></label>
          <label>图片集 URL（逗号分隔）<input v-model="projectForm.galleryText"></label>
          <label>关联文章 ID（逗号分隔）<input v-model="projectForm.articleIdsText"></label>
          <label class="check-label"><input v-model="projectForm.featured" type="checkbox">精选作品</label>
          <div class="button-row">
            <button type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存作品' }}</button>
            <button v-if="editingId" class="button-secondary" type="button" @click="resetForm">取消编辑</button>
          </div>
        </form>
      </section>

      <section v-else-if="section === 'categories'">
        <h2>分类列表</h2>
        <div class="admin-items">
          <div v-for="category in categories" :key="category.id" class="admin-item">
            <div><strong>{{ category.name }}</strong><small>{{ category.slug }}</small></div>
            <div class="button-row">
              <button class="button-secondary" type="button" @click="editCategory(category)">编辑</button>
              <button class="button-danger" type="button" @click="removeItem(() => deleteCategory(category.id), '分类')">删除</button>
            </div>
          </div>
          <p v-if="!categories.length" class="muted">暂无分类。</p>
        </div>
        <h2>{{ editingId ? '编辑分类' : '新建分类' }}</h2>
        <form class="stack-form" @submit.prevent="submitCategory">
          <div class="form-grid">
            <label>名称<input v-model="categoryForm.name" required maxlength="120"></label>
            <label>Slug<input v-model="categoryForm.slug" placeholder="留空则自动生成"></label>
            <label>排序<input v-model.number="categoryForm.sortOrder" type="number"></label>
          </div>
          <label>描述<textarea v-model="categoryForm.description" rows="3" maxlength="1000"></textarea></label>
          <div class="button-row">
            <button type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存分类' }}</button>
            <button v-if="editingId" class="button-secondary" type="button" @click="resetForm">取消编辑</button>
          </div>
        </form>
      </section>

      <section v-else>
        <h2>标签列表</h2>
        <div class="admin-items">
          <div v-for="tag in tags" :key="tag.id" class="admin-item">
            <div><strong>{{ tag.name }}</strong><small>{{ tag.slug }}</small></div>
            <div class="button-row">
              <button class="button-secondary" type="button" @click="editTag(tag)">编辑</button>
              <button class="button-danger" type="button" @click="removeItem(() => deleteTag(tag.id), '标签')">删除</button>
            </div>
          </div>
          <p v-if="!tags.length" class="muted">暂无标签。</p>
        </div>
        <h2>{{ editingId ? '编辑标签' : '新建标签' }}</h2>
        <form class="stack-form" @submit.prevent="submitTag">
          <div class="form-grid">
            <label>名称<input v-model="tagForm.name" required maxlength="120"></label>
            <label>Slug<input v-model="tagForm.slug" placeholder="留空则自动生成"></label>
          </div>
          <div class="button-row">
            <button type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存标签' }}</button>
            <button v-if="editingId" class="button-secondary" type="button" @click="resetForm">取消编辑</button>
          </div>
        </form>
      </section>
    </template>
  </section>
</template>
