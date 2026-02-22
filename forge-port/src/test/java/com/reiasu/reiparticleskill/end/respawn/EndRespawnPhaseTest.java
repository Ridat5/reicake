package com.reiasu.reiparticleskill.end.respawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndRespawnPhaseTest {
    @Test
    void shouldMapKnownIdsIgnoringCaseAndSpaces() {
        assertEquals(EndRespawnPhase.START, EndRespawnPhase.fromId("START").orElseThrow());
        assertEquals(EndRespawnPhase.SUMMON_PILLARS, EndRespawnPhase.fromId("  summon_pillars ").orElseThrow());
        assertEquals(EndRespawnPhase.SUMMONING_DRAGON, EndRespawnPhase.fromId("summoning_dragon").orElseThrow());
        assertEquals(EndRespawnPhase.BEFORE_END_WAITING, EndRespawnPhase.fromId("before_end_waiting").orElseThrow());
        assertEquals(EndRespawnPhase.END, EndRespawnPhase.fromId("end").orElseThrow());
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        assertTrue(EndRespawnPhase.fromId("unknown").isEmpty());
    }

    @Test
    void shouldMapKnownRespawnStageNames() {
        assertEquals(EndRespawnPhase.START, EndRespawnPhase.fromStageName("START").orElseThrow());
        assertEquals(EndRespawnPhase.SUMMON_PILLARS, EndRespawnPhase.fromStageName("PREPARING_TO_SUMMON_PILLARS").orElseThrow());
        assertEquals(EndRespawnPhase.SUMMON_PILLARS, EndRespawnPhase.fromStageName("summoning pillars").orElseThrow());
        assertEquals(EndRespawnPhase.SUMMONING_DRAGON, EndRespawnPhase.fromStageName("SUMMONING_DRAGON").orElseThrow());
        assertEquals(EndRespawnPhase.BEFORE_END_WAITING, EndRespawnPhase.fromStageName("WAITING").orElseThrow());
        assertEquals(EndRespawnPhase.END, EndRespawnPhase.fromStageName("END").orElseThrow());
        assertTrue(EndRespawnPhase.fromStageName("SOMETHING_ELSE").isEmpty());
    }
}
