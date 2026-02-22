# ReiParticlesAPI Forge Module

Minecraft Forge 1.20.1 particle API library providing runtime support for ReiParticleSkill.

## Features

- Controllable particle system (ControlableParticle)
- Particle group styles with network synchronization
- Custom particle rendering (additive blending, etc.)
- Server-side RenderEntity management

## Building

```bash
.\gradlew build
```

Output: `build/libs/reiparticlesapi-1.0-SNAPSHOT-forge-port.jar`

## Usage

Place alongside `forge-port` (ReiParticleSkill) in the `mods/` folder.
