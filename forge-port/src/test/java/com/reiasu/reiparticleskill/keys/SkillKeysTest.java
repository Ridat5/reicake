package com.reiasu.reiparticleskill.keys;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SkillKeysTest {
    @Test
    void formationIdsAreStable() {
        assertEquals("reiparticleskill", SkillKeys.FORMATION_1.getNamespace());
        assertEquals("formation1", SkillKeys.FORMATION_1.getPath());
        assertEquals("reiparticleskill", SkillKeys.FORMATION_2.getNamespace());
        assertEquals("formation2", SkillKeys.FORMATION_2.getPath());
    }

    @Test
    void helperBuildsModScopedId() {
        assertEquals("reiparticleskill:test_id", SkillKeys.id("test_id").toString());
    }
}
