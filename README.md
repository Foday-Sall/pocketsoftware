# PocketSoftware 💬

**Run your business from your pocket.** No dashboards. No logins. No training week.

Text your AI agent on **WhatsApp**, **Telegram** or plain **SMS** and manage customers,
records and school operations built for small businesses and schools.

🌐 **Live site: [pocketsoftware.dev](https://pocketsoftware.dev)**

## What is this?

This repository contains the **marketing/landing site** for PocketSoftware.

PocketSoftware itself is a chat-first business tool: instead of learning yet another
dashboard, you message an AI agent the same way you message a customer. It handles:

- 👤 Customers & records
- 🏫 School operations (fees, attendance, communication)
- 📱 Channels: WhatsApp, Telegram, SMS
- 🤖 AI agent that understands natural language

## Tech stack

- [React 18](https://react.dev) + [TypeScript](https://www.typescriptlang.org/)
- [Vite](https://vitejs.dev) build tool
- [Tailwind CSS](https://tailwindcss.com) styling
- [Framer Motion](https://www.framer.com/motion/) animations

## Local development

```bash
npm install
npm run dev      # start dev server
npm run build    # production build to dist/
```

## Deployment

The site is served via a Cloudflare Worker at [pocketsoftware.dev](https://pocketsoftware.dev).

## License

[MIT](LICENSE) © Foday-Sall
