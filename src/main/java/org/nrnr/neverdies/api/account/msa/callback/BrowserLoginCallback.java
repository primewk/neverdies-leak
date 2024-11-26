package org.nrnr.neverdies.api.account.msa.callback;

/**
 * @author xgraza
 * @since 03/31/24
 */
@FunctionalInterface
public interface BrowserLoginCallback {
    void callback(final String accessToken);
}
