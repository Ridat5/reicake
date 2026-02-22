package com.reiasu.reiparticlesapi.utils;

/**
 * Bit manipulation utility for compact status storage in int/long containers.
 */
public final class MathDataUtil {
    public static final MathDataUtil INSTANCE = new MathDataUtil();

    private MathDataUtil() {
    }

    public int setStatusInt(int container, int bit, boolean status) {
        if (bit > 32 || bit <= 0) {
            return container;
        }
        int move = bit - 1;
        return status ? container | (1 << move) : (container ^ (1 << move)) & container;
    }

    public int getStatusInt(int container, int bit) {
        if (bit < 1 || bit > 32) {
            return -1;
        }
        int move = bit - 1;
        return (container & (1 << move)) >>> move;
    }

    public long setStatusLong(long container, int bit, boolean status) {
        if (bit < 1 || bit > 64) {
            return container;
        }
        int move = bit - 1;
        return status ? container | (1L << move) : (container ^ (1L << move)) & container;
    }

    public int getStatusLong(long container, int bit) {
        if (bit > 64 || bit <= 0) {
            return -1;
        }
        int move = bit - 1;
        return (int) ((container & (1L << move)) >>> move);
    }

    public int getStoragePageInt(int index) {
        return index / 32;
    }

    public int getStorageWithBitInt(int index) {
        return index - getStoragePageInt(index) * 32;
    }

    public int getStoragePageLong(int index) {
        return index / 64;
    }

    public int getStorageWithBitLong(int index) {
        return index - getStoragePageLong(index) * 64;
    }
}
