package com.reiasu.reiparticleskill.barrages;

import com.reiasu.reiparticlesapi.barrages.Barrage;
import com.reiasu.reiparticlesapi.barrages.BarrageHitResult;
import com.reiasu.reiparticlesapi.barrages.BarrageOption;
import com.reiasu.reiparticlesapi.barrages.HitBox;
import com.reiasu.reiparticlesapi.network.particle.ServerControler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkillBarrageManagerTest {
    @AfterEach
    void tearDown() {
        SkillBarrageManager.INSTANCE.clear();
    }

    @Test
    void invalidBarragesArePrunedAfterTick() {
        SkillBarrageManager.INSTANCE.spawn(new DummyBarrage(1));
        SkillBarrageManager.INSTANCE.spawn(new DummyBarrage(3));
        assertEquals(2, SkillBarrageManager.INSTANCE.activeCount());

        SkillBarrageManager.INSTANCE.tickAll();
        assertEquals(1, SkillBarrageManager.INSTANCE.activeCount());

        SkillBarrageManager.INSTANCE.tickAll();
        assertEquals(1, SkillBarrageManager.INSTANCE.activeCount());

        SkillBarrageManager.INSTANCE.tickAll();
        assertEquals(0, SkillBarrageManager.INSTANCE.activeCount());
    }

    @Test
    void clearCancelsAndRemovesAll() {
        SkillBarrageManager.INSTANCE.spawn(new DummyBarrage(8));
        SkillBarrageManager.INSTANCE.spawn(new DummyBarrage(8));
        assertEquals(2, SkillBarrageManager.INSTANCE.activeCount());
        SkillBarrageManager.INSTANCE.clear();
        assertEquals(0, SkillBarrageManager.INSTANCE.activeCount());
    }

    private static final class DummyBarrage implements Barrage {
        private final int maxTick;
        private final UUID uuid = UUID.randomUUID();
        private final BarrageOption option = new BarrageOption();
        private final HitBox hitBox = HitBox.of(1.0, 1.0, 1.0);
        private final DummyControler controler = new DummyControler();
        private Vec3 loc = Vec3.ZERO;
        private Vec3 direction = new Vec3(0.0, 0.0, 1.0);
        private boolean lunch = true;
        private boolean valid = true;
        private int tick;
        private LivingEntity shooter;

        private DummyBarrage(int maxTick) {
            this.maxTick = maxTick;
        }

        @Override
        public Vec3 getLoc() {
            return loc;
        }

        @Override
        public void setLoc(Vec3 loc) {
            this.loc = loc;
        }

        @Override
        public ServerLevel getWorld() {
            return null;
        }

        @Override
        public HitBox getHitBox() {
            return hitBox;
        }

        @Override
        public void setHitBox(HitBox hitBox) {
        }

        @Override
        public LivingEntity getShooter() {
            return shooter;
        }

        @Override
        public void setShooter(LivingEntity shooter) {
            this.shooter = shooter;
        }

        @Override
        public Vec3 getDirection() {
            return direction;
        }

        @Override
        public void setDirection(Vec3 direction) {
            this.direction = direction;
        }

        @Override
        public boolean getLunch() {
            return lunch;
        }

        @Override
        public void setLunch(boolean lunch) {
            this.lunch = lunch;
        }

        @Override
        public boolean getValid() {
            return valid;
        }

        @Override
        public BarrageOption getOptions() {
            return option;
        }

        @Override
        public UUID getUuid() {
            return uuid;
        }

        @Override
        public ServerControler<?> getBindControl() {
            return controler;
        }

        @Override
        public void hit(BarrageHitResult result) {
            valid = false;
            controler.cancel();
        }

        @Override
        public void onHit(BarrageHitResult result) {
        }

        @Override
        public boolean noclip() {
            return false;
        }

        @Override
        public void tick() {
            if (!lunch || !valid) {
                return;
            }
            tick++;
            if (tick >= maxTick) {
                valid = false;
                controler.cancel();
            }
        }
    }

    private static final class DummyControler implements ServerControler<DummyControler> {
        private boolean canceled;

        @Override
        public boolean getCanceled() {
            return canceled;
        }

        @Override
        public void cancel() {
            canceled = true;
        }
    }
}
