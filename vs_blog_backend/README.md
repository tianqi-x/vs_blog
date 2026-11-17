# VS Blog Backend

Personal knowledge blog REST API built with Java 21, Spring Boot, Maven, and PostgreSQL.

## Run locally

Create an empty PostgreSQL database named `vs_blog`; Flyway applies the initial schema from `src/main/resources/db/migration/V1__initial_schema.sql` when the application starts. The application connects to `jdbc:postgresql://localhost:5432/vs_blog` by default. Set `DB_USERNAME` and `DB_PASSWORD` in the shell before starting; optional settings are `DB_URL` and `SERVER_PORT` (default `8080`).

```powershell
$env:DB_USERNAME = "postgres"
$secure = Read-Host "PostgreSQL password" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $secure).Password
.\mvnw.cmd spring-boot:run
```
<!-- 命令行启动：mvn spring-boot:run -->

These variables apply only to the current PowerShell window. Spring Boot reads them through `${DB_USERNAME}` and `${DB_PASSWORD}` in `application.yml`; no credentials are stored in the repository. Do not manually apply the root-level `blog_schema.sql` to a database managed by this application.

The API is available at `http://localhost:8080/api`. `GET /api/health` is public and returns `{"status":"UP"}`. Public article, project, search, tag, and media-image reads are unauthenticated; admin endpoints require an admin session. A new empty database runs migration version `1`; an existing non-empty database without Flyway history is baselined at version `1` and skips that migration. Before starting against an existing database, verify its schema matches V1; future migration files should start at version `2`. Hibernate validates entity mappings on startup and does not create or alter tables.

## Search and media

`GET /api/search?q=postgres&page=0&size=10` searches article titles, Markdown bodies, and tag names/slugs using PostgreSQL case-insensitive substring matching. Results are limited to `published + public` articles. This supports literal Chinese substring searches but does not perform language segmentation or ranking.

Media upload and metadata changes require an admin session and CSRF token:

- `POST /api/admin/media` as multipart form data with required `file` and `alt`, and optional `caption`.
- `GET /api/admin/media` lists uploaded media.
- `PUT /api/admin/media/{id}` updates `alt` and `caption`; `DELETE /api/admin/media/{id}` removes metadata and the stored file.
- `GET /api/media/{id}` serves an uploaded image at its stable URL.

Uploads accept PNG, JPEG, or GIF images up to 10 MB. Image dimensions are read from the image header and bounded before accepting the upload. Binary files are stored outside PostgreSQL under `MEDIA_STORAGE_DIR` (default `./var/media`, relative to the backend working directory); metadata is stored in the existing `blog_media` table. The image URL is public to anyone who knows it, so do not upload confidential images. Use a persistent volume for deployments that restart or move the application; multi-instance deployments need shared storage or an object-storage implementation rather than per-instance local disks.

```powershell
$env:MEDIA_STORAGE_DIR = "E:\vs-blog-media"
```

`mvnw test` verifies unit and MockMvc behavior. Database migration history and schema should still be checked in the target PostgreSQL environment before deployment.

## Verify

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Database schema changes belong in `src/main/resources/db/migration` as Flyway migration scripts. Hibernate is configured to validate the schema rather than create or modify it automatically.