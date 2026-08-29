# Changelog

All notable changes to BetterBanner.

## [1.0.0] — 2026-08-29

### Added
- **Loom support** — apply 7+ patterns through the normal loom interface
- **ProtocolLib integration** — optional packet-level interception for a seamless  
  experience with zero visual flicker. Auto-detected at startup; falls back to a  
  pure Bukkit adapter when ProtocolLib is not installed
- **Version-agnostic Bukkit adapter** — works on Spigot/Paper 1.14 through latest  
  using only the public `Inventory` API
- Per-tier pattern cap configuration (`betterbanner.basic`, `.intermediate`,  
  `.advanced`, `.unlimited`)
- Configurable bStats metrics opt-out (`disable-metrics`)
- AGPL-3.0 license

### Changed
- Package renamed from `com.netrust.betterbanner` to `net.sarhatabaot.betterbanner`
- Config cleaned up — removed dead pre-1.14 copy-system settings
- Build toolchain updated to JDK 17 (targets Java 8 bytecode)

### Removed
- Pre-1.14 crafting-table workaround — no longer relevant

---

## [0.1.8.0] — 2022
- Minor optimizations for 1.13.x
- Added sub-command permissions:
  - `betterbanner.reload`
  - `betterbanner.version`
  - `betterbanner.debug`

## [0.1.7.1] — 2019
- Added bStats metrics support

## [0.1.7.0] — 2019
- Initial public release with 1.13.x support

---

[1.0.0]: https://github.com/sarhatabaot/BetterBanner/releases/tag/v1.0.0
[0.1.8.0]: https://github.com/sarhatabaot/BetterBanner/releases/tag/v0.1.8.0
[0.1.7.1]: https://github.com/sarhatabaot/BetterBanner/releases/tag/v0.1.7.1
[0.1.7.0]: https://github.com/sarhatabaot/BetterBanner/releases/tag/v0.1.7.0