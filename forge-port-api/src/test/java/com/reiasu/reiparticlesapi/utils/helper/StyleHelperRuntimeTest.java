package com.reiasu.reiparticlesapi.utils.helper;

import com.reiasu.reiparticlesapi.network.particle.style.ParticleGroupStyle;
import com.reiasu.reiparticlesapi.utils.RelativeLocation;
import com.reiasu.reiparticlesapi.utils.helper.impl.StyleStatusHelper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class StyleHelperRuntimeTest {
    @Test
    void statusHelperRemovesStyleAfterClosedInterval() {
        ParticleGroupStyle style = new ParticleGroupStyle() {
            @Override public Map<StyleData, RelativeLocation> getCurrentFrames() { return Collections.emptyMap(); }
            @Override public void onDisplay() {}
        };
        StyleStatusHelper statusHelper = HelperUtil.INSTANCE.styleStatus(3);
        statusHelper.loadControler(style);
        statusHelper.setStatus(StatusHelper.Status.DISABLE);

        style.tick();
        style.tick();
        style.tick();

        assertTrue(style.getCanceled());
    }

    @Test
    void styleScaleHelperMovesScaleForward() {
        ParticleGroupStyle style = new ParticleGroupStyle() {
            @Override public Map<StyleData, RelativeLocation> getCurrentFrames() { return Collections.emptyMap(); }
            @Override public void onDisplay() {}
        };
        ScaleHelper helper = HelperUtil.INSTANCE.scaleStyle(0.1, 1.0, 10);
        helper.loadControler(style);
        helper.doScale();
        helper.doScale();
        helper.doScale();

        assertTrue(style.getScale() > 0.1);
        assertTrue(style.getScale() <= 1.0);
    }

    @Test
    void bezierScaleHelperProducesBoundedScale() {
        ParticleGroupStyle style = new ParticleGroupStyle() {
            @Override public Map<StyleData, RelativeLocation> getCurrentFrames() { return Collections.emptyMap(); }
            @Override public void onDisplay() {}
        };
        BezierValueScaleHelper helper = HelperUtil.INSTANCE.bezierValueScaleStyle(
                0.01,
                1.0,
                20,
                new RelativeLocation(17.0, 1.0, 0.0),
                new RelativeLocation(-3.0, 0.0, 0.0)
        );
        helper.loadControler(style);
        for (int i = 0; i < 8; i++) {
            helper.doScale();
        }

        assertTrue(style.getScale() >= 0.01);
        assertTrue(style.getScale() <= 1.0);
    }
}
