# Chat Tabs

A client-side Fabric mod for Minecraft that adds Tibia-style chat tabs to the vanilla chat screen.

Private messages get their own tab. Built for multi-version production with the same architecture used by advanced multi-version Fabric mods.

## Multi-version build system

- Shared source under `src/` with preprocess conditionals
- Per-version projects in `versions/`
- `fabricWrapper` packs all version jars into one distribution
- Gradle 9.5 + Loom 1.17.21 + preprocess
- CI/CD via GitHub Actions (build + release workflows)
- Property-driven versioning, Mod Menu support, publishing ready

### Supported versions

| Minecraft | Notes |
|-----------|-------|
| 1.21.11   | Fabric |
| 26.1      | Fabric |
| 26.2      | Fabric (main / develop against this) |

### Build

```bash
./gradlew build                  # all versions
./gradlew :26.2:build            # single version
./gradlew :fabricWrapper:build   # multi-version pack
```

Target specific versions via environment:

```bash
TARGET_MC_VERSIONS=26.2,1.21.11 ./gradlew build
```

### License

MIT
