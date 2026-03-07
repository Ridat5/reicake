# ReiParticleSkill Forge (1.20.1)

[![License: LGPL-3.0-only](https://img.shields.io/badge/License-LGPL--3.0--only-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)

ReiParticleSkill is a Forge 1.20.1 mod for Ender Dragon respawn effects.

## Requirements

- Minecraft 1.20.1
- Forge 47.2.0
- Java 17

## Build

```bash
cd forge-port
.\gradlew build
```

Run Gradle from `forge-port`.
If you prefer the repository root entry point, run `build.cmd`. It builds the Forge runtime module with tests and verification enabled, then copies the distributable jar to your Desktop.

Canonical distributable jar: `forge-port/reiparticles-forge-runtime/build/libs/reiparticleskill-1.0-SNAPSHOT-forge-port.jar`
Verification: `cd forge-port` then `.\gradlew check`

## Development

```bash
cd forge-port
.\gradlew runClient
```

## Distribution

Put the jar in your `mods/` folder.
If you share a build, keep `LICENSE` and `NOTICE` with it and provide the matching source code.

## License

This project is licensed under **LGPL-3.0-only**. See `LICENSE`, `NOTICE`, `ATTRIBUTION.md`, and `LICENSES/`.
