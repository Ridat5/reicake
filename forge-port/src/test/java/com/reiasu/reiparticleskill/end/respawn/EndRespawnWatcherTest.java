package com.reiasu.reiparticleskill.end.respawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndRespawnWatcherTest {
    @Test
    void shouldMapSyntheticTicksToExpectedPhases() {
        assertEquals(EndRespawnPhase.START, EndRespawnWatcher.syntheticPhaseForTick(0));
        assertEquals(EndRespawnPhase.START, EndRespawnWatcher.syntheticPhaseForTick(149));

        assertEquals(EndRespawnPhase.SUMMON_PILLARS, EndRespawnWatcher.syntheticPhaseForTick(150));
        assertEquals(EndRespawnPhase.SUMMON_PILLARS, EndRespawnWatcher.syntheticPhaseForTick(649));

        assertEquals(EndRespawnPhase.SUMMONING_DRAGON, EndRespawnWatcher.syntheticPhaseForTick(650));
        assertEquals(EndRespawnPhase.SUMMONING_DRAGON, EndRespawnWatcher.syntheticPhaseForTick(749));

        assertEquals(EndRespawnPhase.BEFORE_END_WAITING, EndRespawnWatcher.syntheticPhaseForTick(750));
        assertEquals(EndRespawnPhase.BEFORE_END_WAITING, EndRespawnWatcher.syntheticPhaseForTick(779));

        assertEquals(EndRespawnPhase.END, EndRespawnWatcher.syntheticPhaseForTick(780));
        assertEquals(EndRespawnPhase.END, EndRespawnWatcher.syntheticPhaseForTick(1200));
    }
}
