# 个人知识博客前端

基于 Vue 3、TypeScript 和 Vite。项目使用 Vue Router 管理页面路由、Pinia 管理共享状态，并通过 Axios 统一请求后端 API。公开页面支持文章/作品浏览、文章搜索及分类/标签筛选；`/admin/login` 为管理员入口。

## 启动开发环境

需要 Node.js 20.19.0 或更高版本。先按 [`vs_blog_backend/README.md`](../vs_blog_backend/README.md) 配置并启动后端，再启动前端开发服务器。Vite 会将 `/api` 同源请求代理到 `http://localhost:8080`。

```sh
npm install
npm run dev
```

Windows PowerShell 若执行策略阻止 `npm.ps1`，使用 `npm.cmd` 替代上述 `npm` 命令。

## 检查、测试与构建

```sh
npm run typecheck
npm test
npm run build
npm run preview
```

## 环境变量

默认 Axios API 前缀为 `/api`，与 Vite 代理配合使用。只有部署到不同 API 地址时，才在 `.env.local` 设置：

```dotenv
VITE_API_BASE_URL=/api
```

管理端使用服务端 Session Cookie。写入请求会先读取 `/api/admin/csrf`，并按响应的 `headerName` 携带令牌；不要在客户端硬编码管理员密码或 CSRF 令牌。

文章和作品正文以 Markdown 编辑，预览和公开详情使用 DOMPurify 清理 HTML。

## 目录结构

- `src/views/` 页面级视图
- `src/router/` 路由配置
- `src/stores/` Pinia 状态管理
- `src/api/` 后端请求封装
- `src/styles.css` 全局样式