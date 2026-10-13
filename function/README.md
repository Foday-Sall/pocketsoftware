# PocketSoftware Engine

One lean Appwrite Function. No frameworks, no models, no channels code.

- `function/Index.java` — the entire product: catch → decide → lookup → fetch & send
- `function/dependencies.gradle` — function dependencies (Appwrite SDK)
- `function/provision.py` — idempotent setup script for the data layer (run on the server)

## Architecture

1. **CATCH** — normalizes any inbound payload (Telegram, Meta WhatsApp, GOWA, custom), parks attachments in Storage
2. **DECIDE** — three-tier decision chain: primary decision model → fallback router (tool-calling classification) → keyword scoring. Confidence reflects the tier that answered.
3. **LOOKUP** — `platform.bot_actions` maps intent → database/collection/field
4. **FETCH & SEND** — reads the caller's row, renders the tenant's stored template, delivers via Telegram/Meta APIs

Multi-step wizards live in `platform.sessions`. Timed reminders use Appwrite's
scheduled executions — no cron code. All text comes from `bot_templates` — nothing hardcoded.

## Deploy

1. Enable runtime: add `java-25` to `_APP_FUNCTIONS_RUNTIMES`, `docker compose up -d`
2. Create function (runtime `java-25`, entrypoint `Index.java`, execute `["any"]`)
3. Upload `Index.java` + `dependencies.gradle` as a deployment
4. Set variables: `DECISION_URL`, `DECISION_MODEL`, `DECISION_API_KEY`, `TELEGRAM_BOT_TOKEN`
   (optional: `CF_ACCOUNT_ID` + `CF_API_TOKEN` + `CF_DECISION_MODEL` for the fallback router,
   `REMINDER_FUNCTION_ID` for reminder delivery, `POCKET_*` for table names and defaults)
5. Run `provision.py` on the server to create `bot_actions` + templates
