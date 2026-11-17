CREATE TABLE blog_admin_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT blog_admin_users_username_uk UNIQUE (username)
);

CREATE TABLE blog_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(160) NOT NULL,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT blog_categories_slug_uk UNIQUE (slug)
);

CREATE TABLE blog_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(160) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT blog_tags_slug_uk UNIQUE (slug)
);

CREATE TABLE blog_articles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    summary TEXT,
    content_markdown TEXT NOT NULL,
    category_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    visibility VARCHAR(20) NOT NULL DEFAULT 'private',
    cover_image_url TEXT,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    seo_title VARCHAR(255),
    seo_description TEXT,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT blog_articles_slug_uk UNIQUE (slug),
    CONSTRAINT blog_articles_status_ck CHECK (status IN ('draft', 'published', 'archived')),
    CONSTRAINT blog_articles_visibility_ck CHECK (visibility IN ('private', 'public')),
    CONSTRAINT blog_articles_category_fk
        FOREIGN KEY (category_id) REFERENCES blog_categories (id) ON DELETE SET NULL
);

CREATE TABLE blog_tags_articles (
    article_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    CONSTRAINT blog_tags_articles_article_fk
        FOREIGN KEY (article_id) REFERENCES blog_articles (id) ON DELETE CASCADE,
    CONSTRAINT blog_tags_articles_tag_fk
        FOREIGN KEY (tag_id) REFERENCES blog_tags (id) ON DELETE CASCADE
);

CREATE TABLE blog_projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    summary TEXT NOT NULL,
    description_markdown TEXT NOT NULL,
    tech_stack JSONB NOT NULL DEFAULT '[]'::JSONB,
    project_url TEXT,
    repository_url TEXT,
    cover_image_url TEXT,
    gallery JSONB NOT NULL DEFAULT '[]'::JSONB,
    role VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    visibility VARCHAR(20) NOT NULL DEFAULT 'private',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    started_at DATE,
    completed_at DATE,
    seo_title VARCHAR(255),
    seo_description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT blog_projects_slug_uk UNIQUE (slug),
    CONSTRAINT blog_projects_status_ck CHECK (status IN ('draft', 'published', 'archived')),
    CONSTRAINT blog_projects_visibility_ck CHECK (visibility IN ('private', 'public')),
    CONSTRAINT blog_projects_tech_stack_array_ck CHECK (JSONB_TYPEOF(tech_stack) = 'array'),
    CONSTRAINT blog_projects_gallery_array_ck CHECK (JSONB_TYPEOF(gallery) = 'array'),
    CONSTRAINT blog_projects_dates_ck CHECK (
        started_at IS NULL OR completed_at IS NULL OR completed_at >= started_at
    )
);

CREATE TABLE blog_projects_articles (
    project_id UUID NOT NULL,
    article_id UUID NOT NULL,
    PRIMARY KEY (project_id, article_id),
    CONSTRAINT blog_projects_articles_project_fk
        FOREIGN KEY (project_id) REFERENCES blog_projects (id) ON DELETE CASCADE,
    CONSTRAINT blog_projects_articles_article_fk
        FOREIGN KEY (article_id) REFERENCES blog_articles (id) ON DELETE CASCADE
);

CREATE TABLE blog_article_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT blog_article_notes_article_fk
        FOREIGN KEY (article_id) REFERENCES blog_articles (id) ON DELETE CASCADE
);

CREATE TABLE blog_media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    url TEXT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    alt TEXT NOT NULL,
    caption TEXT,
    width INTEGER,
    height INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT blog_media_width_ck CHECK (width IS NULL OR width > 0),
    CONSTRAINT blog_media_height_ck CHECK (height IS NULL OR height > 0)
);

CREATE INDEX blog_articles_category_id_idx ON blog_articles (category_id);
CREATE INDEX blog_articles_listing_idx ON blog_articles (status, visibility, published_at DESC);
CREATE INDEX blog_tags_articles_tag_id_idx ON blog_tags_articles (tag_id);
CREATE INDEX blog_projects_listing_idx ON blog_projects (status, visibility, is_featured, sort_order);
CREATE INDEX blog_projects_articles_article_id_idx ON blog_projects_articles (article_id);
CREATE INDEX blog_article_notes_article_id_created_at_idx ON blog_article_notes (article_id, created_at DESC);

CREATE OR REPLACE FUNCTION blog_set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

CREATE TRIGGER blog_admin_users_updated_at_trg
    BEFORE UPDATE ON blog_admin_users
    FOR EACH ROW EXECUTE FUNCTION blog_set_updated_at();
CREATE TRIGGER blog_categories_updated_at_trg
    BEFORE UPDATE ON blog_categories
    FOR EACH ROW EXECUTE FUNCTION blog_set_updated_at();
CREATE TRIGGER blog_tags_updated_at_trg
    BEFORE UPDATE ON blog_tags
    FOR EACH ROW EXECUTE FUNCTION blog_set_updated_at();
CREATE TRIGGER blog_articles_updated_at_trg
    BEFORE UPDATE ON blog_articles
    FOR EACH ROW EXECUTE FUNCTION blog_set_updated_at();
CREATE TRIGGER blog_projects_updated_at_trg
    BEFORE UPDATE ON blog_projects
    FOR EACH ROW EXECUTE FUNCTION blog_set_updated_at();
CREATE TRIGGER blog_article_notes_updated_at_trg
    BEFORE UPDATE ON blog_article_notes
    FOR EACH ROW EXECUTE FUNCTION blog_set_updated_at();