package org.openhab.binding.honeywellhome.client;

import static org.openhab.binding.honeywellhome.client.HoneywellClientConstants.HONEYWELL_REFRESH_TOKEN_URI;

import java.util.Base64;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.honeywellhome.client.api.response.GetTokenResponse;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoneywellAuthProvider {
    private final Logger logger = LoggerFactory.getLogger(HoneywellAuthProvider.class);

    private static final int DEFAULT_TOKEN_EXPIRES_SEC = 60 * 5;
    private static final int MAX_BACKOFF_SEC = 60 * 60; // never wait more than 1h between refresh retries
    // After this many consecutive refresh failures we treat the refresh token itself as invalid/revoked
    // (rather than a transient network blip) and stop making Honeywell API calls until reconfigured.
    private static final int MAX_CONSECUTIVE_FAILURES_BEFORE_BROKEN = 3;

    private final HttpClient httpClient;
    final String consumerKey;
    final String consumerSecret;
    private final ScheduledExecutorService scheduler;
    private final Gson gson;
    private final AtomicInteger consecutiveFailureCount = new AtomicInteger(0);

    private volatile String refreshToken;
    private volatile @Nullable String accessToken;
    private volatile long tokenExpiresInSec = DEFAULT_TOKEN_EXPIRES_SEC;

    private @Nullable ScheduledFuture<?> refreshTask;

    public HoneywellAuthProvider(ScheduledExecutorService scheduler, HttpClient httpClient, String consumerKey,
            String consumerSecret, String token, String refreshToken) {
        this.scheduler = scheduler;
        this.httpClient = httpClient;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = token != null && !token.isEmpty() ? token : null;
        this.refreshToken = refreshToken;
        this.gson = new Gson();
    }

    public boolean init() {
        return this.pullHoneywellCredentials().isValid();
    }

    private synchronized HoneywellCredentials pullHoneywellCredentials() {
        Fields fields = new Fields();
        fields.put("grant_type", "refresh_token");
        fields.put("refresh_token", this.refreshToken);
        String basicAuth = Base64.getEncoder()
                .encodeToString((this.consumerKey + ":" + this.consumerSecret).getBytes());
        boolean success = false;
        try {
            ContentResponse contentResponse = this.httpClient.POST(HONEYWELL_REFRESH_TOKEN_URI)
                    .content(new FormContentProvider(fields)).header("Authorization", "Basic " + basicAuth).send();
            if (contentResponse.getStatus() == 200 && contentResponse.getContentAsString() != null) {
                GetTokenResponse getTokenResponse = gson.fromJson(contentResponse.getContentAsString(),
                        GetTokenResponse.class);
                this.accessToken = getTokenResponse.getAccessToken();
                this.refreshToken = getTokenResponse.getRefreshToken();
                String expiresInSecAsString = getTokenResponse.getExpiresIn();
                int expiresInSecAsInt = DEFAULT_TOKEN_EXPIRES_SEC;
                try {
                    // -60 for some grace time.
                    expiresInSecAsInt = expiresInSecAsString != null ? Integer.parseInt(expiresInSecAsString) - 60
                            : expiresInSecAsInt;
                } catch (NumberFormatException e) {
                    logger.warn("Could not parse expiresIn '{}' from Honeywell token response, using default {}s",
                            expiresInSecAsString, expiresInSecAsInt);
                }
                this.tokenExpiresInSec = expiresInSecAsInt;
                success = true;
            } else {
                logAuthFailure("Got status " + contentResponse.getStatus() + " while refreshing Honeywell token",
                        null);
            }
        } catch (Exception e) {
            logAuthFailure("Got error while refreshing Honeywell token", e);
        }

        if (success) {
            consecutiveFailureCount.set(0);
            scheduleNextRefresh(this.tokenExpiresInSec);
            return new HoneywellCredentials(true, this.consumerKey, this.consumerSecret, this.accessToken);
        }
        int failures = consecutiveFailureCount.incrementAndGet();
        long backoffSec = Math.min(DEFAULT_TOKEN_EXPIRES_SEC * (1L << Math.min(failures, 10)), MAX_BACKOFF_SEC);
        scheduleNextRefresh(backoffSec);
        return new HoneywellCredentials(false);
    }

    // Intentionally never logs consumerSecret/refreshToken/accessToken - they're secrets, not diagnostic
    // data, and this used to end up verbatim in openhab.log. Also logs at ERROR only on the transition
    // into a failing state and DEBUG for repeats, so a revoked/expired refresh token doesn't spam the log
    // forever - it already backs off and will keep retrying at a growing interval regardless.
    private void logAuthFailure(String reason, @Nullable Exception e) {
        boolean firstFailure = consecutiveFailureCount.get() == 0;
        if (firstFailure) {
            logger.error("{} (consumerKey: {}) - further repeats will be logged at DEBUG until it recovers", reason,
                    consumerKey, e);
        } else {
            logger.debug("{} (consumerKey: {})", reason, consumerKey, e);
        }
    }

    public void refreshToken() {
        pullHoneywellCredentials();
    }

    private void scheduleNextRefresh(long delaySec) {
        disposeRefreshTask();
        refreshTask = scheduler.schedule(this::refreshToken, delaySec, TimeUnit.SECONDS);
    }

    private void disposeRefreshTask() {
        ScheduledFuture<?> localRefreshTask = refreshTask;
        if (localRefreshTask != null) {
            localRefreshTask.cancel(true);
            this.refreshTask = null;
        }
    }

    public void dispose() {
        disposeRefreshTask();
    }

    /**
     * True once refresh has failed {@value #MAX_CONSECUTIVE_FAILURES_BEFORE_BROKEN}+ times in a row - a strong
     * signal the refresh token itself is invalid/revoked rather than a transient network issue. Callers should
     * stop making Honeywell API calls (and stop triggering on-demand refreshes on 401) until the Bridge is
     * reconfigured with a fresh token/refreshToken and reinitialized.
     */
    public boolean isAuthBroken() {
        return consecutiveFailureCount.get() >= MAX_CONSECUTIVE_FAILURES_BEFORE_BROKEN;
    }

    public HoneywellCredentials getHoneywellCredentials() {
        String currentAccessToken = this.accessToken;
        if (currentAccessToken != null) {
            return new HoneywellCredentials(true, this.consumerKey, this.consumerSecret, currentAccessToken);
        }
        return pullHoneywellCredentials();
    }
}
