package com.reiasu.reiparticlesapi.network.particle.emitters.impl;

import com.reiasu.reiparticlesapi.network.particle.emitters.ClassParticleEmitters;
import com.reiasu.reiparticlesapi.network.particle.emitters.ControlableParticleData;
import com.reiasu.reiparticlesapi.network.particle.emitters.ParticleEmitters;
import com.reiasu.reiparticlesapi.network.particle.data.SerializableData;
import com.reiasu.reiparticlesapi.particles.Controlable;
import com.reiasu.reiparticlesapi.utils.Math3DUtil;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Lightning-bolt particle emitter. Uses {@link Math3DUtil#getLightningEffectPoints}
 * to generate branching bolt geometry each tick.
 */
public final class LightningClassParticleEmitters extends ClassParticleEmitters {

    public static final String ID = "lightning-class-particle-emitters";

    private ControlableParticleData templateData = new ControlableParticleData();
    private final Random random = new Random(System.currentTimeMillis());

    public LightningClassParticleEmitters(Vec3 pos, Level world) {
        super(pos, world);
    }

    public ControlableParticleData getTemplateData() {
        return templateData;
    }

    public void setTemplateData(ControlableParticleData templateData) {
        this.templateData = templateData;
    }

    @Override
    public void doTick() {
    }

    @Override
    public List<Map.Entry<SerializableData, RelativeLocation>> genParticles(float lerpProgress) {
        RelativeLocation target = new RelativeLocation(
                random.nextDouble() * 100.0 - 50.0,
                random.nextDouble() * 20.0 - 10.0,
                random.nextDouble() * 100.0 - 50.0);
        List<RelativeLocation> points = Math3DUtil.INSTANCE.getLightningEffectPoints(target, 10, 3);
        List<Map.Entry<SerializableData, RelativeLocation>> result = new ArrayList<>();
        for (RelativeLocation pt : points) {
            result.add(new AbstractMap.SimpleEntry<>(templateData.clone(), pt));
        }
        return result;
    }

    @Override
    public void singleParticleAction(Controlable<?> controler, SerializableData data,
                                      RelativeLocation spawnPos, Level spawnWorld,
                                      float particleLerpProgress, float posLerpProgress) {
    }

    @Override
    public String getEmittersID() {
        return ID;
    }

    @Override
    protected void writePayload(FriendlyByteBuf buf) {
        ClassParticleEmitters.Companion.encodeBase(this, buf);
        templateData.writeToBuf(buf);
    }

    @Override
    protected void readPayload(FriendlyByteBuf buf) {
        ClassParticleEmitters.Companion.decodeBase(this, buf);
        templateData.readFromBuf(buf);
    }

    @Override
    public void update(ParticleEmitters emitters) {
        super.update(emitters);
        if (emitters instanceof LightningClassParticleEmitters other) {
            this.templateData = other.templateData;
        }
    }
}
