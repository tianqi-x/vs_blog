import axios from 'axios'

import { http } from './http'
import type {
  AdminSession,
  Article,
  ArticleInput,
  ApiErrorResponse,
  Category,
  CategoryInput,
  Page,
  Project,
  ProjectInput,
  Tag,
  TagInput,
} from './types'

export async function listPublicArticles(): Promise<Article[]> {
  return (await http.get<Article[]>('/articles')).data
}

export async function getPublicArticle(slug: string): Promise<Article> {
  return (await http.get<Article>(`/articles/${encodeURIComponent(slug)}`)).data
}

export async function searchPublicArticles(query: string, page = 0, size = 10): Promise<Page<Article>> {
  return (await http.get<Page<Article>>('/search', { params: { q: query, page, size } })).data
}

export async function listPublicProjects(): Promise<Project[]> {
  return (await http.get<Project[]>('/projects')).data
}

export async function getPublicProject(slug: string): Promise<Project> {
  return (await http.get<Project>(`/projects/${encodeURIComponent(slug)}`)).data
}

export async function listPublicCategories(): Promise<Category[]> {
  return (await http.get<Category[]>('/categories')).data
}

export async function listPublicTags(): Promise<Tag[]> {
  return (await http.get<Tag[]>('/tags')).data
}

export async function getAdminSession(): Promise<AdminSession> {
  try {
    return (await http.get<AdminSession>('/admin/session')).data
  } catch (error) {
    if (
      axios.isAxiosError(error)
      && (error.response?.status === 401 || error.response?.status === 403)
    ) {
      return { authenticated: false }
    }
    throw error
  }
}

export async function loginAdmin(username: string, password: string): Promise<void> {
  await http.post('/admin/login', { username, password })
}

export async function logoutAdmin(): Promise<void> {
  await http.post('/admin/logout')
}

export async function listAdminArticles(): Promise<Article[]> {
  return (await http.get<Article[]>('/admin/articles')).data
}

export async function saveArticle(input: ArticleInput, id?: string): Promise<Article> {
  const response = id
    ? await http.put<Article>(`/admin/articles/${id}`, input)
    : await http.post<Article>('/admin/articles', input)
  return response.data
}

export async function deleteArticle(id: string): Promise<void> {
  await http.delete(`/admin/articles/${id}`)
}

export async function listAdminProjects(): Promise<Project[]> {
  return (await http.get<Project[]>('/admin/projects')).data
}

export async function saveProject(input: ProjectInput, id?: string): Promise<Project> {
  const response = id
    ? await http.put<Project>(`/admin/projects/${id}`, input)
    : await http.post<Project>('/admin/projects', input)
  return response.data
}

export async function deleteProject(id: string): Promise<void> {
  await http.delete(`/admin/projects/${id}`)
}

export async function listAdminCategories(): Promise<Category[]> {
  return (await http.get<Category[]>('/admin/categories')).data
}

export async function saveCategory(input: CategoryInput, id?: string): Promise<Category> {
  const response = id
    ? await http.put<Category>(`/admin/categories/${id}`, input)
    : await http.post<Category>('/admin/categories', input)
  return response.data
}

export async function deleteCategory(id: string): Promise<void> {
  await http.delete(`/admin/categories/${id}`)
}

export async function listAdminTags(): Promise<Tag[]> {
  return (await http.get<Tag[]>('/admin/tags')).data
}

export async function saveTag(input: TagInput, id?: string): Promise<Tag> {
  const response = id
    ? await http.put<Tag>(`/admin/tags/${id}`, input)
    : await http.post<Tag>('/admin/tags', input)
  return response.data
}

export async function deleteTag(id: string): Promise<void> {
  await http.delete(`/admin/tags/${id}`)
}

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    if (
      error.config?.url?.endsWith('/admin/login')
      && (error.response?.status === 401 || error.response?.data?.message === 'invalid_credentials')
    ) {
      return '用户名或密码错误。'
    }
    if (error.response?.data?.message) return error.response.data.message
    if (error.response?.status === 401 || error.response?.status === 403) {
      return '登录状态已失效或权限不足，请重新登录后重试。'
    }
    if (!error.response) return '无法连接后端服务，请检查后端是否启动。'
  }
  return fallback
}
