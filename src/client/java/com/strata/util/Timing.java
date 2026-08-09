package com.strata.util;

import java.util.Random;

/**
 * Jittered delays.
 *
 * <p>A macro that acts on an exact interval produces a timing histogram that is a
 * single spike, which is detectable purely from packet arrival times without any
 * client inspection. Jitter spreads that out.
 *
 * <p>The distribution is deliberately not uniform: human reaction times are roughly
 * log-normal -- a floor, a peak, and a long right tail of occasional slow responses.
 * Flat random noise is itself a recognisable signature.
 */
public final class Timing {

    private static final Random RANDOM = new Random();

    private Timing() {
    }

    /**
     * @param baseMs   centre of the distribution
     * @param jitterMs rough spread; 0 disables jitter entirely
     * @return a delay in milliseconds, never negative
     */
    public static long delay(double baseMs, double jitterMs) {
        if (jitterMs <= 0) {
            return Math.max(0, Math.round(baseMs));
        }
        // Gaussian core for the common case, plus an occasional long tail so the
        // distribution has the asymmetry a human's does.
        double value = baseMs + RANDOM.nextGaussian() * (jitterMs / 2.0);
        if (RANDOM.nextFloat() < 0.06F) {
            value += RANDOM.nextDouble() * jitterMs * 2.0;
        }
        return Math.max(0, Math.round(value));
    }

    /** Simple stopwatch, in milliseconds. */
    public static final class Stopwatch {
        private long mark = System.currentTimeMillis();

        public void reset() {
            mark = System.currentTimeMillis();
        }

        public long elapsed() {
            return System.currentTimeMillis() - mark;
        }

        public boolean passed(long millis) {
            return elapsed() >= millis;
        }
    }
}
