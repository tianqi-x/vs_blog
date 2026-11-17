// Runtime validation coverage for safe Markdown rendering.
// @vitest-environment jsdom
import { describe, expect, it } from 'vitest'

import { renderMarkdown } from './markdown'

describe('renderMarkdown', () => {
  it('renders Markdown while removing executable HTML', () => {
    const html = renderMarkdown('# Notes\n\n<script>alert(1)</script>\n\n**safe**')

    expect(html).toContain('<h1>Notes</h1>')
    expect(html).toContain('<strong>safe</strong>')
    expect(html).not.toContain('<script>')
  })
})
