package com.reiasu.reiparticlesapi.network.particle.emitters.type;

import com.reiasu.reiparticlesapi.annotations.codec.BufferCodec;
import com.reiasu.reiparticlesapi.utils.math.ExpressionEvaluator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Emitter shoot type that evaluates mathematical expressions (via EvalEx) to
 * compute particle spawn positions and directions.
 * <p>
 * Position expressions may reference variables: {@code i} (particle index),
 * {@code c} (particle count), {@code t} (tick).
 * <p>
 * Direction expressions may reference: {@code t} (tick), {@code x/y/z} (particle pos),
 * {@code ox/oy/oz} (emitter origin).
 */
public final class MathEmittersShootType implements EmittersShootType {

    public static final String ID = "math";

    public static final BufferCodec<EmittersShootType> CODEC = BufferCodec.of(
            (buf, type) -> {
                MathEmittersShootType math = (MathEmittersShootType) type;
                buf.writeUtf(math.x);
                buf.writeUtf(math.y);
                buf.writeUtf(math.z);
                buf.writeUtf(math.dx);
                buf.writeUtf(math.dy);
                buf.writeUtf(math.dz);
            },
            buf -> {
                MathEmittersShootType math = new MathEmittersShootType();
                math.x = buf.readUtf();
                math.y = buf.readUtf();
                math.z = buf.readUtf();
                math.dx = buf.readUtf();
                math.dy = buf.readUtf();
                math.dz = buf.readUtf();
                math.setup();
                return math;
            }
    );

    private String x = "0";
    private String y = "0";
    private String z = "0";
    private String dx = "0";
    private String dy = "0";
    private String dz = "0";

    private ExpressionEvaluator xe;
    private ExpressionEvaluator ye;
    private ExpressionEvaluator ze;
    private ExpressionEvaluator dxe;
    private ExpressionEvaluator dye;
    private ExpressionEvaluator dze;

    public MathEmittersShootType() {
        setup();
    }

    // ---- Getters / Setters ----

    public String getX() { return x; }
    public void setX(String x) { this.x = x; }

    public String getY() { return y; }
    public void setY(String y) { this.y = y; }

    public String getZ() { return z; }
    public void setZ(String z) { this.z = z; }

    public String getDx() { return dx; }
    public void setDx(String dx) { this.dx = dx; }

    public String getDy() { return dy; }
    public void setDy(String dy) { this.dy = dy; }

    public String getDz() { return dz; }
    public void setDz(String dz) { this.dz = dz; }

    // ---- Core logic ----

    /**
     * Re-compile all expression objects from the current string formulas.
     * Must be called after changing any formula field.
     */
    public void setup() {
        xe = new ExpressionEvaluator(x).with("i", 0).with("c", 0).with("t", 0);
        ye = new ExpressionEvaluator(y).with("i", 0).with("c", 0).with("t", 0);
        ze = new ExpressionEvaluator(z).with("i", 0).with("c", 0).with("t", 0);
        dxe = new ExpressionEvaluator(dx).with("t", 0);
        dye = new ExpressionEvaluator(dy).with("t", 0);
        dze = new ExpressionEvaluator(dz).with("t", 0);
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public List<Vec3> getPositions(Vec3 origin, int tick, int count) {
        List<Vec3> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final int idx = i;
            CompletableFuture<Double> taskX = CompletableFuture.supplyAsync(() ->
                    evalPosition(xe, tick, count, idx));
            CompletableFuture<Double> taskY = CompletableFuture.supplyAsync(() ->
                    evalPosition(ye, tick, count, idx));
            CompletableFuture<Double> taskZ = CompletableFuture.supplyAsync(() ->
                    evalPosition(ze, tick, count, idx));
            CompletableFuture.allOf(taskX, taskY, taskZ).join();
            try {
                result.add(origin.add(new Vec3(taskX.get(), taskY.get(), taskZ.get())));
            } catch (Exception e) {
                result.add(origin);
            }
        }
        return result;
    }

    @Override
    public Vec3 getDefaultDirection(Vec3 enter, int tick, Vec3 pos, Vec3 origin) {
        CompletableFuture<Double> fx = CompletableFuture.supplyAsync(() ->
                evalDirection(dxe, tick, pos, origin));
        CompletableFuture<Double> fy = CompletableFuture.supplyAsync(() ->
                evalDirection(dye, tick, pos, origin));
        CompletableFuture<Double> fz = CompletableFuture.supplyAsync(() ->
                evalDirection(dze, tick, pos, origin));
        CompletableFuture.allOf(fx, fy, fz).join();
        try {
            return enter.add(fx.get(), fy.get(), fz.get());
        } catch (Exception e) {
            return enter;
        }
    }

    // ---- Helpers ----

    private static double evalPosition(ExpressionEvaluator expr, int tick, int count, int index) {
        try {
            return expr.with("t", tick).with("c", count).with("i", index)
                    .evaluate();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static double evalDirection(ExpressionEvaluator expr, int tick, Vec3 pos, Vec3 origin) {
        try {
            return expr.with("t", tick)
                    .with("x", pos.x).with("y", pos.y).with("z", pos.z)
                    .with("ox", origin.x).with("oy", origin.y).with("oz", origin.z)
                    .evaluate();
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Encode this shoot type to a buffer.
     */
    public void encode(FriendlyByteBuf buf) {
        CODEC.encode(buf, this);
    }

    /**
     * Decode a new instance from a buffer.
     */
    public static MathEmittersShootType decode(FriendlyByteBuf buf) {
        return (MathEmittersShootType) CODEC.decode(buf);
    }
}
