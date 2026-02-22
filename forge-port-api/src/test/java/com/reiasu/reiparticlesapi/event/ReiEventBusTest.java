package com.reiasu.reiparticlesapi.event;

import com.reiasu.reiparticlesapi.annotations.events.EventHandler;
import com.reiasu.reiparticlesapi.event.api.ReiEvent;
import com.reiasu.reiparticlesapi.event.api.EventInterruptible;
import com.reiasu.reiparticlesapi.event.api.EventPriority;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReiEventBusTest {
    @AfterEach
    void clean() {
        ReiEventBus.INSTANCE.clear();
    }

    @Test
    void shouldDispatchHandlersByPriorityOrder() {
        StringBuilder order = new StringBuilder();
        ReiEventBus.INSTANCE.registerListenerInstance("test", new PriorityListener(order));

        ReiEventBus.call(new BaseEvent());

        assertEquals("HIGHEST,HIGH,NORMAL,LOW,LOWEST", order.toString());
    }

    @Test
    void shouldStopWhenEventIsInterrupted() {
        AtomicInteger calls = new AtomicInteger();
        ReiEventBus.INSTANCE.registerListenerInstance("test", new InterruptingListener(calls));

        ReiEventBus.call(new InterruptEvent());

        assertEquals(1, calls.get());
    }

    @Test
    void shouldDispatchChildThenParentEventHandlers() {
        StringBuilder order = new StringBuilder();
        ReiEventBus.INSTANCE.registerListenerInstance("test", new HierarchyListener(order));

        ReiEventBus.call(new ChildEvent());

        assertEquals("child,parent", order.toString());
    }

    private static class BaseEvent extends ReiEvent {
    }

    private static final class ChildEvent extends BaseEvent {
    }

    private static final class InterruptEvent extends ReiEvent implements EventInterruptible {
        private boolean interrupted;

        @Override
        public boolean isInterrupted() {
            return interrupted;
        }

        @Override
        public void setInterrupted(boolean interrupted) {
            this.interrupted = interrupted;
        }
    }

    private static final class PriorityListener {
        private final StringBuilder order;

        private PriorityListener(StringBuilder order) {
            this.order = order;
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void highest(BaseEvent event) {
            append("HIGHEST");
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void high(BaseEvent event) {
            append("HIGH");
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void normal(BaseEvent event) {
            append("NORMAL");
        }

        @EventHandler(priority = EventPriority.LOW)
        public void low(BaseEvent event) {
            append("LOW");
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void lowest(BaseEvent event) {
            append("LOWEST");
        }

        private void append(String value) {
            if (order.length() > 0) {
                order.append(',');
            }
            order.append(value);
        }
    }

    private static final class InterruptingListener {
        private final AtomicInteger calls;

        private InterruptingListener(AtomicInteger calls) {
            this.calls = calls;
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void first(InterruptEvent event) {
            calls.incrementAndGet();
            event.setInterrupted(true);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void second(InterruptEvent event) {
            calls.incrementAndGet();
        }
    }

    private static final class HierarchyListener {
        private final StringBuilder order;

        private HierarchyListener(StringBuilder order) {
            this.order = order;
        }

        @EventHandler
        public void onChild(ChildEvent event) {
            append("child");
        }

        @EventHandler
        public void onParent(BaseEvent event) {
            append("parent");
        }

        private void append(String value) {
            if (order.length() > 0) {
                order.append(',');
            }
            order.append(value);
        }
    }
}

