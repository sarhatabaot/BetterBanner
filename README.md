# BetterBanner

![GitHub release](https://img.shields.io/github/v/release/sarhatabaot/betterbanner)
![License](https://img.shields.io/github/license/sarhatabaot/betterbanner)

Unlimited banner patterns through the **vanilla loom** — no commands, no custom GUIs, no client mods.

## Features

- Apply more than 6 patterns (up to unlimited) to banners using the normal loom
- Zero commands required — completely transparent to players
- **Seamless** experience when ProtocolLib is installed
- Falls back gracefully without ProtocolLib (minor visual flicker on 6+ pattern banners)
- Works on Spigot/Paper 1.14 through latest (tested on 1.14.4–1.26.x; version-agnostic adapter should work on any version with the Loom block)
- For 1.13, use version 0.1.8.0
- For 1.8-1.12 use the original plugin https://www.spigotmc.org/resources/better-banner.16432/

## Requirements

- Spigot or Paper **1.14+**
- (Optional, recommended) [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) — **use 4.8.0 on 1.14.x**; newer versions on 1.15+

## Installation

1. Drop `BetterBanner.jar` into `plugins/`
2. (Optional) Drop `ProtocolLib.jar` for the best experience
3. Restart the server or run `/reload`

## Permissions

| Node                        | Default cap                |
|-----------------------------|----------------------------|
| *(none)*                    | 6 patterns (vanilla limit) |
| `betterbanner.basic`        | 8 patterns                 |
| `betterbanner.intermediate` | 11 patterns                |
| `betterbanner.advanced`     | 15 patterns                |
| `betterbanner.unlimited`    | No cap                     |

Cap values are configurable in `plugins/BetterBanner/config.yml`.

## Commands

| Command                        | Description                      | Permission                  |
|--------------------------------|----------------------------------|-----------------------------|
| `/betterbanner version`        | Show plugin version              | `betterbanner.version`      |
| `/betterbanner debug`          | Toggle debug logging             | `betterbanner.debug`        |
| `/betterbanner debug nms`      | Dump adapter diagnostics         | `betterbanner.debug.nms`    |
| `/betterbanner debug adapter`  | Show active adapter info         | `betterbanner.debug.nms`    |
| `/betterbanner reload`         | Reload config                    | `betterbanner.reload`       |

### Command permissions

| Permission                    | Default             | Controls                           |
|-------------------------------|---------------------|------------------------------------|
| `betterbanner.version`        | `true` (everyone)   | `/betterbanner version`            |
| `betterbanner.debug`          | `op`                | `/betterbanner debug`              |
| `betterbanner.debug.nms`      | `op`                | `/betterbanner debug nms|adapter`  |
| `betterbanner.reload`         | `op`                | `/betterbanner reload`             |

## Configuration

```yaml
# plugins/BetterBanner/config.yml
default: 6
basic: 8
intermediate: 11
advanced: 15
disable-metrics: false
```

## How it works

When a banner reaches 6 patterns, the vanilla client hides the pattern-selection grid. BetterBanner writes a temporary 5-pattern copy of the banner to the loom input slot, causing the client to show the grid again. The server computes the next pattern normally, BetterBanner extracts it, restores the original banner, appends the new pattern, and places it in the output slot.

With ProtocolLib, outgoing packets are rewritten so the client **never** sees the temporary swap — the experience is completely identical to normal loom usage.

## Building

```bash
./gradlew build
```

Outputs to `build/libs/BetterBanner-<version>.jar`.

## License

GNU Affero General Public License v3.0 — see [LICENSE](LICENSE).

© 2019–2026 [sarhatabaot](https://github.com/sarhatabaot)

---

## Docker test server

```bash
docker compose up -d
docker compose run --rm rcon
```

