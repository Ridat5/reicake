// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeReiParticlesNetworkTest {
    @Test
    void shouldRequireExactProtocolVersionDuringHandshake() {
        assertTrue(ForgeReiParticlesProtocol.isAcceptedProtocolVersion(ForgeReiParticlesProtocol.protocolVersion()));
        assertFalse(ForgeReiParticlesProtocol.isAcceptedProtocolVersion("1"));
        assertFalse(ForgeReiParticlesProtocol.isAcceptedProtocolVersion("vanilla"));
    }
}
