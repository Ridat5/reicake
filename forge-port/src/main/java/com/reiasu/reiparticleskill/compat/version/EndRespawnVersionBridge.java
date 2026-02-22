package com.reiasu.reiparticleskill.compat.version;

import com.reiasu.reiparticleskill.end.respawn.EndRespawnPhase;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public interface EndRespawnVersionBridge {
    Optional<EndRespawnPhase> detectPhase(EndDragonFight fight);

    Vec3 portalCenter(EndDragonFight fight);
}
