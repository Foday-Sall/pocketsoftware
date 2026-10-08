# Contributing to PocketSoftware

Thanks for your interest in contributing!

## Development setup

```bash
git clone https://github.com/Foday-Sall/pocketsoftware.git
cd pocketsoftware
npm install
npm run dev
```

The site runs at `http://localhost:5173`.

## Guidelines

- Keep the Light Swiss design language: white background, messenger blue primary, flat surfaces. No gradients, no glow effects, no animated status dots
- Write copy in plain human language. No AI-slop vocabulary ("next-gen", "seamless", "elevate", "game-changer", "unlock", "revolutionize")
- No em-dashes anywhere in code or copy
- Testimonials must use Dalasi (GMD) with real Gambian names
- Test your changes at mobile width (390px) before submitting

## Commit style

Use conventional commits: `feat:`, `fix:`, `docs:`, `chore:`, `style:`

## Deployment

Deployment is handled by the maintainer. Changes merged to `main` are built and deployed to pocketsoftware.dev as a Cloudflare Worker.