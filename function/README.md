# PocketSoftware Engine

One lean Appwrite Function. No frameworks, no models, no channels code.

- `function/Index.java` — the entire product: catch → decide (Cloudflare Clef) → lookup → fetch & send
- `function/dependencies.gradle` — function dependencies (Appwrite SDK)
- `function/provision.py` — idempotent setup script for the data layer (run on the server)

## Architecture

1. **CATCH** — normalizes any inbound payload (Telegram, Meta WhatsApp, GOWA, custom)
2. **DECIDE** — Cloudflare Clef (clef-flash) picks one intent token from the bot_actions registry; keywords fallback
3. **LOOKUP** — `platform.bot_actions` maps intent → database/collection/field
4. **FETCH & SEND** — reads the caller's row, renders the tenant's stored template, delivers via Telegram/Meta APIs

Multi-step wizards live in `platform.sessions`. Timed reminders use Appwrite's
scheduled executions — no cron code. All text comes from `bot_templates` — nothing hardcoded.

## Deploy

1. Enable runtime: add `java-25` to `_APP_FUNCTIONS_RUNTIMES`, `docker compose up -d`
2. Create function (runtime `java-25`, entrypoint `Index.java`, execute `["any"]`)
3. Upload `Index.java` + `dependencies.gradle` as a deployment
4. Set variables: `DECISION_URL`, `DECISION_MODEL`, `DECISION_API_KEY`, `TELEGRAM_BOT_TOKEN`
5. Run `provision.py` on the server to create `bot_actions` + templates
