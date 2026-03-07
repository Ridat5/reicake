// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.reflect;

import com.reiasu.reiparticlesapi.annotations.ReiAutoRegister;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReiAPIScannerTest {
    @AfterEach
    void cleanup() {
        ReiAPIScanner.INSTANCE.clear();
    }

    @Test
    void shouldReturnAnnotatedClassesInStableTypeOrder() {
        ReiAPIScanner.INSTANCE.inputScanResult(info("com.example.ZetaPort"));
        ReiAPIScanner.INSTANCE.inputScanResult(info("com.example.AlphaPort"));
        ReiAPIScanner.INSTANCE.inputScanResult(info("com.example.BetaPort"));

        List<String> discovered = ReiAPIScanner.INSTANCE.getWithAnnotation(ReiAutoRegister.class).stream()
                .map(SimpleClassInfo::getType)
                .toList();

        assertEquals(List.of(
                "com.example.AlphaPort",
                "com.example.BetaPort",
                "com.example.ZetaPort"
        ), discovered);
    }

    private static SimpleClassInfo info(String typeName) {
        return new SimpleClassInfo(typeName, new HashSet<>(Set.of(ReiAutoRegister.class.getName())));
    }
}
