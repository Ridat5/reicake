package com.reiasu.reiparticleskill.display;

import com.reiasu.reiparticlesapi.display.DisplayEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class SwordLightDisplay extends DisplayEntity implements ServerMovableDisplay {
    private Vec3 pos;
    private Vec3 end;
    private int maxAge = 20;
    private int bloomCount = 2;
    private float thinness = 0.125F;
    private int age;
    private boolean removed;

    public SwordLightDisplay(Vec3 pos) {
        this.pos = pos == null ? Vec3.ZERO : pos;
        this.end = this.pos;
    }

    public Vec3 getPos() {
        return pos;
    }

    public void setPos(Vec3 pos) {
        this.pos = pos == null ? Vec3.ZERO : pos;
    }

    public Vec3 getEnd() {
        return end;
    }

    public void setEnd(Vec3 end) {
        this.end = end == null ? pos : end;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = Math.max(1, maxAge);
    }

    public int getBloomCount() {
        return bloomCount;
    }

    public void setBloomCount(int bloomCount) {
        this.bloomCount = Math.max(0, bloomCount);
    }

    public float getThinness() {
        return thinness;
    }

    public void setThinness(float thinness) {
        this.thinness = Math.max(0.01F, thinness);
    }

    public int getAge() {
        return age;
    }

    @Override
    public void teleportTo(Vec3 pos) {
        this.pos = pos == null ? Vec3.ZERO : pos;
    }

    @Override
    public void tick() {
        age++;
        if (age > maxAge) {
            if (!removed) {
                removed = true;
                age = 0;
            } else {
                cancel();
                return;
            }
        }

        ServerLevel level = level();
        if (level == null) {
            return;
        }

        Vec3 diff = end.subtract(pos);
        int samples = Math.max(6, (int) (diff.length() * 6.0));
        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;
            Vec3 p = pos.add(diff.scale(t));
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
            if (i % 4 == 0 && bloomCount > 0) {
                level.sendParticles(ParticleTypes.ENCHANT, p.x, p.y, p.z, bloomCount, thinness, thinness, thinness, 0.0);
            }
        }
    }
}
