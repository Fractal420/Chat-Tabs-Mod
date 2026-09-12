# Chat Tabs

Client-side Fabric mod that adds conversation tabs to the Minecraft chat screen (designed for 6b6t / anarchy-style servers).

## Supported versions

| Minecraft | Java | Loom |
|-----------|------|------|
| 1.21.11   | 21   | fabric-loom-remap 1.17.20 |
| 26.1      | 25   | fabric-loom 1.17-SNAPSHOT |
| 26.2      | 25   | fabric-loom 1.17-SNAPSHOT |

- **1.21.11** uses shared sources in `src/`
- **26.1 / 26.2** use version-specific sources under `versions/<mc>/src/` (ported for GuiGraphicsExtractor, KeyMappingHelper, addMessage signature, screen access)

## Building

```bash
./gradlew build -Pmc=1.21.11
./gradlew build -Pmc=26.1
./gradlew build -Pmc=26.2
./gradlew buildAll
```

Or target subprojects:

```bash
./gradlew :1.21.11:build
./gradlew :26.1:build
./gradlew :26.2:build
```

Jars land in `versions/<mc>/build/libs/`.

### Clean build

```bash
./gradlew --stop
rm -rf .gradle versions/*/build build
./gradlew buildAll
```

**Note:** 26.x requires **JDK 25**. 1.21.11 requires **JDK 21**.

## License

MIT — see [LICENSE](LICENSE).
