// SPDX-License-Identifier: LGPL-3.0-only
// Copyright (C) 2025 Reiasu
package com.reiasu.reiparticlesapi.test;

import com.reiasu.reiparticlesapi.test.api.TestGroup;
import com.reiasu.reiparticlesapi.test.api.TestGroupBuilder;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class TestManager {
    public static final TestManager INSTANCE = new TestManager();
    private final Map<String, Function<ServerPlayer, TestGroupBuilder>> registry = new ConcurrentHashMap<>();
    private final Set<TestGroup> validGroupsServer = ConcurrentHashMap.newKeySet();
    private final Set<TestGroup> validGroupsClient = ConcurrentHashMap.newKeySet();

    private TestManager() {
    }

    public void register(String id, Function<ServerPlayer, TestGroupBuilder> builder) {
        if (id == null || builder == null) {
            return;
        }
        registry.put(id, builder);
    }

    public Map<String, Function<ServerPlayer, TestGroupBuilder>> registry() {
        return registry;
    }

    public Set<TestGroup> getValidGroupsServer() {
        return validGroupsServer;
    }

    public Set<TestGroup> getValidGroupsClient() {
        return validGroupsClient;
    }

    public TestGroup getTestFromServer(ServerPlayer user) {
        if (user == null) {
            return null;
        }
        UUID id = user.getUUID();
        for (TestGroup group : validGroupsServer) {
            if (id.equals(group.getUser().getUUID())) {
                return group;
            }
        }
        return null;
    }

    public TestGroup getTestFromClient(ServerPlayer user) {
        if (user == null) {
            return null;
        }
        UUID id = user.getUUID();
        for (TestGroup group : validGroupsClient) {
            if (id.equals(group.getUser().getUUID())) {
                return group;
            }
        }
        return null;
    }

    public TestGroup startTest(String id, ServerPlayer user) {
        if (id == null || user == null) {
            return null;
        }
        Function<ServerPlayer, TestGroupBuilder> builderFactory = registry.get(id);
        if (builderFactory == null) {
            return null;
        }
        TestGroupBuilder builder = builderFactory.apply(user);
        if (builder == null) {
            return null;
        }
        TestGroup group = builder.build();
        if (group == null) {
            return null;
        }
        validGroupsServer.add(group);
        group.start();
        return group;
    }

    public void doTickServer() {
        Iterator<TestGroup> iterator = validGroupsServer.iterator();
        while (iterator.hasNext()) {
            TestGroup group = iterator.next();
            if (group.isDone()) {
                iterator.remove();
                continue;
            }
            group.doTick();
        }
    }

    public void doTickClient() {
        Iterator<TestGroup> iterator = validGroupsClient.iterator();
        while (iterator.hasNext()) {
            TestGroup group = iterator.next();
            if (group.isDone()) {
                iterator.remove();
                continue;
            }
            group.doTick();
        }
    }

    public void clear() {
        validGroupsServer.clear();
        validGroupsClient.clear();
    }
}
