# ReiParticleSkill Forge (1.20.1)

A Minecraft Forge 1.20.1 particle effects mod featuring custom Ender Dragon respawn animations and visual effects.

## Modules

| Module | Description | Output Jar |
|--------|-------------|------------|
| `forge-port/` | ReiParticleSkill — main mod | `reiparticleskill-1.0-SNAPSHOT-forge-port.jar` |
| `forge-port-api/` | ReiParticlesAPI — runtime library | `reiparticlesapi-1.0-SNAPSHOT-forge-port.jar` |

Both jars must be placed in the `mods/` folder.

## Requirements

- Minecraft 1.20.1
- Forge 47.2.0
- Java 17

## Building

```bash
# API module
cd forge-port-api
.\gradlew build

# Main mod
cd forge-port
.\gradlew build
```

Jars are output to `build/libs/` in each module.

## Development

```bash
cd forge-port
.\gradlew runClient
```

## License

This project is licensed under **LGPL-3.0**. See `LICENSE`, `ATTRIBUTION.md`, and the `LICENSES/` directory for details.
