package org.openhab.binding.honeywellhome.client;

import static org.openhab.binding.honeywellhome.client.HoneywellClientConstants.HONEYWELL_REFRESH_TOKEN_URI;

import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;
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
    private final Logger logger = LoggerFactory.getLogger(HoneywellClient.class);

    HttpClient httpClient;
    String consumerKey;
    String consumerSecret;
    String token;
    String refreshToken;
    String accessToken;
    Date tokenExpiresDate;
    long tokenExpiresInSec;
    Gson gson;
    ScheduledExecutorService scheduler;
    ScheduledFuture<?> refreshTask;

    public HoneywellAuthProvider(ScheduledExecutorService scheduler, HttpClient httpClient, String consumerKey, String consumerSecret, String token,
                                 String refreshToken) {
        this.scheduler = scheduler;
        this.httpClient = httpClient;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.token = token;
        this.refreshToken = refreshToken;
        this.gson = new Gson();
    }

    public boolean init() throws Exception {
        HoneywellCredentials honeywellCredentials = this.pullHoneywellCredentials();
        return honeywellCredentials.isValid;
    }

    private HoneywellCredentials pullHoneywellCredentials() throws Exception {
        Fields fields = new Fields();
        fields.put("grant_type", "refresh_token");
        fields.put("refresh_token", this.refreshToken);
        String basicAuth = Base64.getEncoder()
                .encodeToString((this.consumerKey + ":" + this.consumerSecret).getBytes());
        int expiresInSecAsInt = 60 * 5; //todo move to config
        try {
            ContentResponse contentResponse = this.httpClient.POST(HONEYWELL_REFRESH_TOKEN_URI)
                    .content(new FormContentProvider(fields)).header("Authorization", "Basic " + basicAuth).send();
            if (contentResponse.getStatus() == 200 && contentResponse.getContentAsString() != null) {
                String getTokenJsonResponse = contentResponse.getContentAsString();
                GetTokenResponse getTokenResponse = gson.fromJson(getTokenJsonResponse, GetTokenResponse.class);
                this.accessToken = getTokenResponse.getAccessToken();
                this.refreshToken = getTokenResponse.getRefreshToken();
                String expiresInSecAsString = getTokenResponse.getExpiresIn();
                try {
                    expiresInSecAsInt = expiresInSecAsString != null ? Integer.parseInt(expiresInSecAsString) - 60 : expiresInSecAsInt; // -60 for some grace time.
                } catch (Exception e) {
                    logger.error("Got error while trying convert expiresIn: {} as String to int - will use default: {}", expiresInSecAsString, expiresInSecAsInt, e);
                }
                this.tokenExpiresDate = DateUtils.addSeconds(new Date(), expiresInSecAsInt);
                this.tokenExpiresInSec = expiresInSecAsInt;

                return new HoneywellCredentials(true, this.consumerKey, this.consumerSecret,
                        getTokenResponse.getAccessToken());
            } else {
                return new HoneywellCredentials(false);
            }
        } catch (Exception e) {
            logger.error("Got error while trying pullHoneywellCredentials with consumerKey: {}, consumerSecret: {}, refreshToken: {}", this.consumerKey, this.consumerSecret, this.refreshToken, e);
        } finally {
            this.tokenExpiresInSec = expiresInSecAsInt;
            startTokenRefresher();
        }
        return new HoneywellCredentials(false);
    }

    public void refreshToken() {
        try {
            pullHoneywellCredentials();
        } catch (Exception e) {
            logger.error("Got error while trying refreshToken", e);
        }
    }

    private void startTokenRefresher () {
        disposeRefreshTask();
        refreshTask = scheduler.schedule(this::refreshToken, this.tokenExpiresInSec, TimeUnit.SECONDS);
    }

    private void disposeRefreshTask() {
        ScheduledFuture<?> localRefreshTask = refreshTask;
        if (localRefreshTask != null) {
            localRefreshTask.cancel(true);
            this.refreshTask = null;
        }
    }

    public HoneywellCredentials getHoneywellCredentials() throws Exception {
        if (this.accessToken != null) {
            return new HoneywellCredentials(true, this.consumerKey, this.consumerSecret, this.accessToken);
        }
        return pullHoneywellCredentials();
    }
}
