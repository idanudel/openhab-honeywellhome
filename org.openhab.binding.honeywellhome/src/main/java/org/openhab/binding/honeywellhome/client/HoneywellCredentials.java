package org.openhab.binding.honeywellhome.client;

public class HoneywellCredentials {

    boolean isValid;
    String consumerKey;
    String consumerSecret;
    String accessToken;

    public HoneywellCredentials(boolean isValid, String consumerKey, String consumerSecret, String accessToken) {
        this.isValid = isValid;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
    }
    public HoneywellCredentials(boolean isValid) {
        this.isValid = isValid;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
