package com.reiasu.reiparticlesapi.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Animate {
    private final List<NodeSlot> nodes = new ArrayList<>();
    private final List<Predicate<Animate>> cancelPredicates = new ArrayList<>();
    private AnimateNode currentNode;
    private int currentInterval;
    private int currentIndex;
    private int timestarp;
    private boolean done;
    private boolean display;

    public Animate addNode(AnimateNode node) {
        return addNode(node, 0);
    }

    public Animate addNode(AnimateNode node, int interval) {
        if (node != null) {
            nodes.add(new NodeSlot(node, Math.max(0, interval)));
        }
        return this;
    }

    public Animate addCancelPredicate(Predicate<Animate> predicate) {
        if (predicate != null) {
            cancelPredicates.add(predicate);
        }
        return this;
    }

    public void skip() {
        if (!display || done) {
            return;
        }

        if (currentNode != null && !currentNode.checkDone()) {
            currentNode.cancel();
        }

        if (currentIndex >= nodes.size()) {
            done = true;
            return;
        }

        NodeSlot slot = nodes.get(currentIndex++);
        currentNode = slot.node();
        currentNode.onStart();
        currentInterval = slot.interval();
        timestarp = 0;
    }

    public void start() {
        if (display || done) {
            return;
        }
        display = true;
        if (!nodes.isEmpty()) {
            skip();
        }
    }

    public void tick() {
        if (!display || done) {
            return;
        }

        if (currentNode == null) {
            skip();
            if (done) {
                return;
            }
        }

        if (currentNode != null && currentNode.checkDone()) {
            if (currentInterval < timestarp) {
                skip();
            }
        } else if (currentNode != null) {
            currentNode.tick();
        }

        boolean shouldCancel = false;
        if (!cancelPredicates.isEmpty()) {
            for (Predicate<Animate> predicate : cancelPredicates) {
                if (predicate.test(this)) {
                    shouldCancel = true;
                    break;
                }
            }
        }
        if (shouldCancel) {
            cancel();
            return;
        }

        timestarp++;
    }

    public void cancel() {
        done = true;
        display = false;
        for (NodeSlot slot : nodes) {
            AnimateNode node = slot.node();
            if (!node.checkDone()) {
                node.cancel();
            }
        }
    }

    public int getTimestarp() {
        return timestarp;
    }

    public int getCurrentInterval() {
        return currentInterval;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean getDone() {
        return done;
    }

    public boolean getDisplay() {
        return display;
    }

    private record NodeSlot(AnimateNode node, int interval) {
    }
}
