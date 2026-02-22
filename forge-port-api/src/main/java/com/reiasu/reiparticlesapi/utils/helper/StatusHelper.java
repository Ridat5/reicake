package com.reiasu.reiparticlesapi.utils.helper;

import com.reiasu.reiparticlesapi.network.buffer.ParticleControlerDataBuffer;
import com.reiasu.reiparticlesapi.network.buffer.ParticleControlerDataBuffers;

import java.util.List;
import java.util.Map;

public abstract class StatusHelper implements ParticleHelper {
    private int displayStatus = Status.ENABLE.id();
    private int closedInternal;
    private int current;

    public final int getDisplayStatus() {
        return displayStatus;
    }

    public final int getClosedInternal() {
        return closedInternal;
    }

    public final void setClosedInternal(int closedInternal) {
        this.closedInternal = Math.max(0, closedInternal);
    }

    public final int getCurrent() {
        return current;
    }

    protected final void setCurrent(int current) {
        this.current = Math.max(0, current);
    }

    public final void setStatus(int status) {
        this.displayStatus = status == Status.DISABLE.id() ? Status.DISABLE.id() : Status.ENABLE.id();
        changeStatus(this.displayStatus);
    }

    public final void setStatus(Status status) {
        setStatus(status.id());
    }

    public final Status getCurrentStatus() {
        return displayStatus == Status.DISABLE.id() ? Status.DISABLE : Status.ENABLE;
    }

    public final List<ArgPair> toArgsPairs() {
        return List.of(
                new ArgPair("display_status", ParticleControlerDataBuffers.INSTANCE.intValue(displayStatus)),
                new ArgPair("display_time", ParticleControlerDataBuffers.INSTANCE.intValue(current))
        );
    }

    public final void readFromServer(Map<String, ? extends ParticleControlerDataBuffer<?>> args) {
        ParticleControlerDataBuffer<?> statusBuffer = args.get("display_status");
        if (statusBuffer != null && statusBuffer.getLoadedValue() instanceof Number number) {
            setStatus(number.intValue());
        }
        ParticleControlerDataBuffer<?> timeBuffer = args.get("display_time");
        if (timeBuffer != null && timeBuffer.getLoadedValue() instanceof Number number) {
            current = Math.max(0, number.intValue());
        }
    }

    public abstract void changeStatus(int status);

    public enum Status {
        ENABLE(1),
        DISABLE(2);

        private final int id;

        Status(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public static Status fromId(int id) {
            return id == DISABLE.id ? DISABLE : ENABLE;
        }
    }

    public record ArgPair(String name, ParticleControlerDataBuffer<?> value) {
    }
}
