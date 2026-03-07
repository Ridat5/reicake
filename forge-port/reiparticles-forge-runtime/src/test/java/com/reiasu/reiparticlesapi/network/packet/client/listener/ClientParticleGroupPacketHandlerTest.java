// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.network.packet.client.listener;

import com.reiasu.reiparticlesapi.testutil.RecordingLogger;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientParticleGroupPacketHandlerTest {
    @Test
    void shouldRejectRemoteInvokeRequests() {
        RecordingLogger recorder = new RecordingLogger();
        UUID uuid = UUID.randomUUID();

        ClientParticleGroupPacketHandler.rejectRemoteInvoke("dangerousMethod", uuid, DummyGroup.class, recorder.logger());

        assertTrue(recorder.hasEvent("warn", "Rejected deprecated remote invoke"));
    }

    private static final class DummyGroup {
    }
}
