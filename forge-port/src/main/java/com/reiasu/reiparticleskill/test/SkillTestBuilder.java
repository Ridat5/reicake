package com.reiasu.reiparticleskill.test;

import com.reiasu.reiparticlesapi.test.SimpleTestGroup;
import com.reiasu.reiparticlesapi.test.SimpleTestOption;
import com.reiasu.reiparticlesapi.test.api.TestGroup;
import com.reiasu.reiparticlesapi.test.api.TestGroupBuilder;
import com.reiasu.reiparticlesapi.test.api.TestOption;
import com.reiasu.reiparticleskill.particles.core.emitters.p2.formation.SwordFormationEmitters;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Skill test builder that creates a test group with sword formation emitter tests.
 * Server-side port of the Fabric original, adapted to Forge test API.
 */
public final class SkillTestBuilder implements TestGroupBuilder {
    public static final String ID = "skill-test-builder";

    private final ServerPlayer player;

    public SkillTestBuilder(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    @Override
    public String groupID() {
        return ID;
    }

    @Override
    public TestGroup build() {
        SimpleTestGroup group = new SimpleTestGroup(groupID(), player);
        group.appendOption(() -> createSwordFormationTest());
        return group;
    }

    private TestOption createSwordFormationTest() {
        Vec3 eyePos = player.getEyePosition();
        SwordFormationEmitters emitter = new SwordFormationEmitters(eyePos, player.level());
        emitter.setMaxTick(-1);
        return new SimpleTestOption("sword-formation");
    }
}
