package org.nrnr.neverdies.api;

/**
 * @param <T> The argument type
 * @author chronos
 * @since 1.0
 */
public interface Invokable<T> {
    /**
     * @param arg The argument
     */
    void invoke(T arg);
}
