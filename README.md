# ReiParticleSkill Forge (1.20.1)

[![License: LGPL-3.0-only](https://img.shields.io/badge/License-LGPL--3.0--only-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)

A Minecraft Forge 1.20.1 particle effects mod featuring custom Ender Dragon respawn animations and visual effects.

## Project Layout

The workspace now builds a single Gradle module in `forge-port/`.
That jar bundles the former `ReiParticlesAPI` runtime, so only one output jar is produced.

## Requirements

- Minecraft 1.20.1
- Forge 47.2.0
- Java 17

## Building

```bash
cd forge-port
.\gradlew build
```

Or run `build.cmd` from the repository root.

Output jar:
`forge-port/build/libs/reiparticleskill-1.0-SNAPSHOT-forge-port.jar`

Place that single jar in the `mods/` folder.

## Development

```bash
cd forge-port
.\gradlew runClient
```

## License

This project is licensed under **LGPL-3.0-only**. See `LICENSE`, `ATTRIBUTION.md`, and the `LICENSES/` directory for details.

### For Modpack Authors / Players

- Keep the original `LICENSE` and `NOTICE` files bundled with the jar intact.
- If you redistribute the jar, also provide a link to the corresponding source code.
- You do not need to open-source your modpack or other mods just because you include this one.

### For Mod Developers

- `ReiParticlesAPI` is now bundled into the same source module and jar as `ReiParticleSkill`.
- If you modify and redistribute this project's source code, publish those changes under LGPL-3.0 and mark them clearly.

*The above is a simplified summary, not legal advice. See the full license texts for authoritative terms.*
