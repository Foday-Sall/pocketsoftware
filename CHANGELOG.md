# Changelog

All notable changes to PocketSoftware.

## [1.1.0] - 2026-10-07

### Changed
- Copy polish per humanizer review: headline punctuation, honest stats, removed duplicate punchlines, dropped GSM jargon
- Removed pulse animation from status badge

### Infrastructure
- Deployed as Cloudflare Worker (single-file, ~590 KB) at pocketsoftware.dev
- Email routing live: info@pocketsoftware.dev
- SEO metadata in source: robots, canonical, OG, Twitter cards

## [1.0.0] - 2026-10-06

### Added
- Initial landing: chat-first hero with live WhatsApp demo, how-it-works, channels, dual chat showcase, trust section with testimonials, FAQ, final CTA
- Light Swiss theme: white background, messenger blue primary, green accent
- Poppins and Open Sans typography
- Testimonials in Dalasi (GMD)

### Security
- History rewritten to remove em-dashes from all commits
- Repository cleaned: node_modules and dist untracked