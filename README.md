# PocketSoftware Engine

Telegram-first school management backend for Gambian schools. Parents chat with
a bot: they register students, list or find students, and ask about fees.
Messages are classified with a keyword matcher (optional LLM classifiers are
pluggable) and data is stored in self-hosted Appwrite.

## Build

Requires JDK 25 and Maven 3.9+.

```
mvn -q package      # produces target/pocketsoftware.jar
mvn test            # runs the unit tests
```

## Configure

Copy `config.template.properties` to `config.properties` and fill in:

| key | purpose |
| --- | --- |
| `appwrite.endpoint` | Appwrite REST endpoint (e.g. `https://api.pocketsoftware.dev/v1`) |
| `appwrite.project` | Appwrite project ID |
| `appwrite.key` | Appwrite API key (server-side, never commit it) |
| `telegram.token` | Telegram bot token from @BotFather |
| `classifier` | `keyword` (default), `jev`, or `cloudflare` |
| `jev.*` | OpenRouter chat-completions URL, model, API key |
| `cf.*` | Cloudflare account ID, API token, Workers AI model |

`config.properties` is git-ignored; the repo only carries the template.

## Run

```
java -jar target/pocketsoftware.jar
```

With a custom config location:

```
POCKETSOFTWARE_CONFIG=/etc/pocketsoftware/config.properties java -jar target/pocketsoftware.jar
```

The bot uses Telegram long polling, so no inbound port is needed. If the token
is missing or still a placeholder, the process prints
`waiting for valid telegram token` and exits without touching Telegram.

## Chat commands

- `menu` / `help` / `start` - list commands
- `add student` - guided registration: name, class, guardian phone, guardian name
- `students` - list all student names
- `find <name>` - find students by name
- `fees` - fees info (coming soon)

## Structure

- `Main.java` - config load, wiring, startup
- `Config.java` - properties loading
- `engine/Appwrite.java` - TablesDB REST wrapper
- `engine/Classifier.java` + `KeywordClassifier` / `JevClassifier` / `CloudflareClassifier`
- `engine/Router.java` - conversation state, registration flow, replies
- `channels/TelegramChannel.java` - long-polling Telegram adapter
