export type ContentStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
export type ContentVisibility = 'PRIVATE' | 'PUBLIC'

export interface Article {
  id: string
  title: string
  slug: string
  summary: string | null
  contentMarkdown: string
  categoryId: string | null
  status: ContentStatus
  visibility: ContentVisibility
  coverImageUrl: string | null
  pinned: boolean
  seoTitle: string | null
  seoDescription: string | null
  publishedAt: string | null
  createdAt: string | null
  updatedAt: string | null
  tagIds: string[]
}

export interface Project {
  id: string
  title: string
  slug: string
  summary: string
  descriptionMarkdown: string
  techStack: string[]
  projectUrl: string | null
  repositoryUrl: string | null
  coverImageUrl: string | null
  gallery: string[]
  role: string | null
  status: ContentStatus
  visibility: ContentVisibility
  featured: boolean
  sortOrder: number
  startedAt: string | null
  completedAt: string | null
  seoTitle: string | null
  seoDescription: string | null
  articleIds: string[]
  createdAt: string | null
  updatedAt: string | null
}

export interface Category {
  id: string
  name: string
  slug: string
  description: string | null
  sortOrder: number
  createdAt: string | null
  updatedAt: string | null
}

export interface Tag {
  id: string
  name: string
  slug: string
  createdAt: string | null
  updatedAt: string | null
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

export interface AdminSession {
  authenticated: boolean
  username?: string
}

export interface ArticleInput {
  title: string
  slug: string
  summary: string
  contentMarkdown: string
  categoryId: string | null
  status: ContentStatus
  visibility: ContentVisibility
  coverImageUrl: string
  pinned: boolean
  seoTitle: string
  seoDescription: string
  tagIds: string[]
}

export interface ProjectInput {
  title: string
  slug: string
  summary: string
  descriptionMarkdown: string
  techStack: string[]
  projectUrl: string
  repositoryUrl: string
  coverImageUrl: string
  gallery: string[]
  role: string
  status: ContentStatus
  visibility: ContentVisibility
  featured: boolean
  sortOrder: number
  startedAt: string | null
  completedAt: string | null
  seoTitle: string
  seoDescription: string
  articleIds: string[]
}

export interface CategoryInput {
  name: string
  slug: string
  description: string
  sortOrder: number
}

export interface TagInput {
  name: string
  slug: string
}

export interface ApiErrorResponse {
  error?: string
  message?: string
}
