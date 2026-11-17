import { createRouter, createWebHistory } from 'vue-router'

import AboutView from '@/views/AboutView.vue'
import ArticleDetailView from '@/views/ArticleDetailView.vue'
import ArticlesView from '@/views/ArticlesView.vue'
import AdminLoginView from '@/views/AdminLoginView.vue'
import AdminView from '@/views/AdminView.vue'
import HomeView from '@/views/HomeView.vue'
import NotFoundView from '@/views/NotFoundView.vue'
import ProjectDetailView from '@/views/ProjectDetailView.vue'
import ProjectsView from '@/views/ProjectsView.vue'
import { getAdminSession } from '@/api/content'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAdmin?: boolean
  }
}

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/articles', name: 'articles', component: ArticlesView },
    { path: '/articles/:slug', name: 'article-detail', component: ArticleDetailView },
    { path: '/projects', name: 'projects', component: ProjectsView },
    { path: '/projects/:slug', name: 'project-detail', component: ProjectDetailView },
    { path: '/about', name: 'about', component: AboutView },
    { path: '/admin/login', name: 'admin-login', component: AdminLoginView },
    { path: '/admin', name: 'admin', component: AdminView, meta: { requiresAdmin: true } },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundView },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to) => {
  if (!to.meta.requiresAdmin && to.name !== 'admin-login') return true

  try {
    const session = await getAdminSession()
    if (to.name === 'admin-login' && session.authenticated) return { name: 'admin' }
    if (to.meta.requiresAdmin && !session.authenticated) {
      return { name: 'admin-login', query: { redirect: to.fullPath } }
    }
    return true
  } catch {
    if (to.name === 'admin-login') return true
    return { name: 'admin-login', query: { redirect: to.fullPath, error: 'unavailable' } }
  }
})
