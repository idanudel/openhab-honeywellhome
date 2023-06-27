package org.openhab.binding.honeywellhome.client.api.response;

import com.google.gson.annotations.SerializedName;

public class GetTokenResponse {

    @SerializedName("access_token")
    String accessToken;
    @SerializedName("refresh_token")
    String refreshToken;
    @SerializedName("expires_in")
    String expiresIn;
    @SerializedName("token_type")
    String tokenType;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }
}
