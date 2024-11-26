package org.nrnr.neverdies.util;

public class Timer2 {

    public Timer2() {
        reset();
    }
    private long time = -1L;

    public Timer2 reset() {
        time = System.nanoTime();
        return this;
    }

    public boolean passedTicks(int tick) {
        return passedMs(tick * 50L);
    }
    public boolean passedS(double s) {
        return passedMs((long) s * 1000L);
    }

    public boolean passedMs(long ms) {
        return passedNS(convertToNS(ms));
    }

    public boolean passedMs(double ms) {
        return passedMs((long) ms);
    }
    public boolean passed(long ms) {
        return passedNS(convertToNS(ms));
    }
    public boolean passed(double ms) {
        return passedMs((long) ms);
    }
    public void setMs(long ms) {
        time = System.nanoTime() - convertToNS(ms);
    }

    public boolean passedNS(long ns) {
        return System.nanoTime() - time >= ns;
    }

    public long getPassedTimeMs() {
        return getMs(System.nanoTime() - time);
    }

    public long getMs(long time) {
        return time / 1000000L;
    }

    public long convertToNS(long time) {
        return time * 1000000L;
    }
}