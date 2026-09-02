package org.openhab.binding.honeywellhome.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.honeywellhome.client.api.pojo.ChangeableValues;
import org.openhab.binding.honeywellhome.client.api.request.ChangeThermostatsSettingRequest;
import org.openhab.binding.honeywellhome.client.api.response.GetAllLocationsResponse;
import org.openhab.binding.honeywellhome.client.api.response.GetThermostatsFanStatusResponse;
import org.openhab.binding.honeywellhome.client.api.response.GetThermostatsStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.openhab.binding.honeywellhome.client.HoneywellClientConstants.*;

public class HoneywellClient {
    private final Logger logger = LoggerFactory.getLogger(HoneywellClient.class);
    private final HttpClient httpClient;
    private final HoneywellAuthProvider honeywellAuthProvider;
    private final Gson gson = new Gson();
    // Tracks whether the *last* API call failed, so repeated failures (e.g. every 15s poll while auth
    // is broken) log at DEBUG instead of ERROR after the first one - only the state transition matters.
    private final AtomicBoolean lastCallFailed = new AtomicBoolean(false);

    public HoneywellClient(ScheduledExecutorService scheduler, HttpClient httpClient, String consumerKey, String consumerSecret, String token, String refreshToken) {
        this.httpClient = httpClient;
        this.honeywellAuthProvider = new HoneywellAuthProvider(scheduler, httpClient, consumerKey, consumerSecret, token, refreshToken);
        this.honeywellAuthProvider.init();
    }

    public boolean isValid() {
        return this.honeywellAuthProvider.getHoneywellCredentials().isValid();
    }

    /**
     * True once the refresh token has been rejected enough times in a row that it's treated as
     * permanently invalid rather than a transient failure - see {@link HoneywellAuthProvider#isAuthBroken()}.
     */
    public boolean isAuthBroken() {
        return this.honeywellAuthProvider.isAuthBroken();
    }

    public List<GetAllLocationsResponse> getAllLocations() {
        return getAllLocations(false);
    }
    private List<GetAllLocationsResponse> getAllLocations(boolean isRetry) {
        if (bailIfAuthBroken("get Honeywell All locations")) {
            return null;
        }
        try {
            String accessToken = this.honeywellAuthProvider.getHoneywellCredentials().getAccessToken();
            String url = String.format(HONEYWELL_GET_ALL_LOCATIONS, this.honeywellAuthProvider.consumerKey);
            ContentResponse contentResponse = this.httpClient.newRequest(url).
                    method(HttpMethod.GET).header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .send();
            if(contentResponse.getStatus() == 200) {
                String contentAsString = contentResponse.getContentAsString();
                logCallSuccess();
                logger.debug("Got AllLocations by consumer id: {} response: {}", getConsumeId(), contentAsString);
                return gson.fromJson(contentResponse.getContentAsString(), new TypeToken<ArrayList<GetAllLocationsResponse>>() {}.getType());
            }
            if(isRefreshTokenNeeded(contentResponse.getStatus()) && isRetry == false) {
                this.honeywellAuthProvider.refreshToken();
                return getAllLocations(true);
            }
            else {
                logCallFailure("Got error response: " + contentResponse.getStatus() + " while trying to get Honeywell All locations " + contentResponse.getContentAsString());
            }
        } catch (Exception e) {
            logCallFailure("Got error while trying to get Honeywell All locations", e);
        }
        return null;
    }
    public GetThermostatsStatusResponse getThermostatsDevice(String thermostatId, String locationId) {
        return getThermostatsDevice(thermostatId, locationId, false);
    }

    private GetThermostatsStatusResponse getThermostatsDevice(String thermostatId, String locationId, boolean isRetry) {
        if (bailIfAuthBroken("get Honeywell Thermostats Device id: " + thermostatId)) {
            return null;
        }
        try {
            String accessToken = this.honeywellAuthProvider.getHoneywellCredentials().getAccessToken();
            String url = String.format(HONEYWELL_GET_THERMOSTAT_STATUS, thermostatId, this.honeywellAuthProvider.consumerKey, locationId);
            ContentResponse contentResponse = this.httpClient.newRequest(url)
                    .method(HttpMethod.GET).header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .send();
            if(contentResponse.getStatus() == 200) {
                String contentAsString = contentResponse.getContentAsString();
                logCallSuccess();
                logger.debug("Got device by id: {} location id: {} with response: {}", thermostatId, locationId, contentAsString);
                return gson.fromJson(contentAsString, GetThermostatsStatusResponse.class);
            }
            if(isRefreshTokenNeeded(contentResponse.getStatus()) && isRetry == false) {
                this.honeywellAuthProvider.refreshToken();
                return getThermostatsDevice(thermostatId, locationId, true);
            }
            else {
                logCallFailure("Got error response: " + contentResponse.getStatus() + " while trying to get Honeywell Thermostats Device id: " + thermostatId + " in location: " + locationId);
            }
        } catch (Exception e) {
            logCallFailure("Got error while trying to get Thermostats Device", e);
        }
        return null;
    }

    public boolean changeThermostatsSetting (String thermostatId, String locationId, ChangeableValues changeableValues) {
        return changeThermostatsSetting(thermostatId, locationId, changeableValues, false);
    }

    private boolean changeThermostatsSetting (String thermostatId, String locationId, ChangeableValues changeableValues, boolean isRetry) {
        if (bailIfAuthBroken("change Honeywell Thermostats Setting for id: " + thermostatId)) {
            return false;
        }
        try {
            String accessToken = this.honeywellAuthProvider.getHoneywellCredentials().getAccessToken();
            String url = String.format(HONEYWELL_POST_THERMOSTAT_STATUS, thermostatId, this.honeywellAuthProvider.consumerKey, locationId);
            StringContentProvider contentProvider = new StringContentProvider(gson.toJson(new ChangeThermostatsSettingRequest(changeableValues)));
            ContentResponse contentResponse = this.httpClient.newRequest(url)
                    .method(HttpMethod.POST)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .content(contentProvider)
                    .send();
            if(contentResponse.getStatus() == 200) {
                logCallSuccess();
                logger.debug("Change Thermostats Setting by id: {} location id: {} with response: {}", thermostatId, locationId, 200);
                return true;
            }
            if(isRefreshTokenNeeded(contentResponse.getStatus()) && isRetry == false) {
                this.honeywellAuthProvider.refreshToken();
                return changeThermostatsSetting(thermostatId, locationId, changeableValues, true);
            }
            else {
                logCallFailure("Got error response: " + contentResponse.getStatus() + " while trying to Change Honeywell Thermostats Setting by Device id: " + thermostatId + " in location: " + locationId);
            }
        } catch (Exception e) {
            logCallFailure("Got error while trying to Change Honeywell Thermostats DeviceId: " + thermostatId + " locationId: " + locationId, e);
        }
        return false;
    }

    public boolean changeThermostatsFanSetting (String thermostatId, String locationId, String mode) {
        return changeThermostatsFanSetting(thermostatId, locationId, mode, false);
    }

    private boolean changeThermostatsFanSetting (String thermostatId, String locationId, String mode, boolean isRetry) {
        if (bailIfAuthBroken("change Honeywell Thermostats Fan Setting for id: " + thermostatId)) {
            return false;
        }
        try {
            String accessToken = this.honeywellAuthProvider.getHoneywellCredentials().getAccessToken();
            String url = String.format(HONEYWELL_POST_THERMOSTAT_FAN_STATUS, thermostatId, this.honeywellAuthProvider.consumerKey, locationId);
            StringContentProvider contentProvider = new StringContentProvider(gson.toJson(new GetThermostatsFanStatusResponse(mode)));
            ContentResponse contentResponse = this.httpClient.newRequest(url)
                    .method(HttpMethod.POST)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .content(contentProvider)
                    .send();
            if(contentResponse.getStatus() == 200) {
                logCallSuccess();
                logger.debug("Change Thermostats Fan Setting by id: {} location id: {} with response: {}", thermostatId, locationId, 200);
                return true;
            }
            if(isRefreshTokenNeeded(contentResponse.getStatus()) && isRetry == false) {
                this.honeywellAuthProvider.refreshToken();
                return changeThermostatsFanSetting(thermostatId, locationId, mode, true);
            }
            else {
                logCallFailure("Got error response: " + contentResponse.getStatus() + " while trying to Change Thermostats Fan Setting by Device id: " + thermostatId + " in location: " + locationId);
            }
        } catch (Exception e) {
            logCallFailure("Got error while trying to Change Thermostats Fan Setting by DeviceId: " + thermostatId + " locationId: " + locationId, e);
        }
        return false;
    }

    public String getConsumeId () {
        return this.honeywellAuthProvider != null ? this.honeywellAuthProvider.consumerKey : null;
    }

    private boolean isRefreshTokenNeeded(int status) {
        return status == 401;
    }

    private boolean bailIfAuthBroken(String action) {
        if (this.honeywellAuthProvider.isAuthBroken()) {
            logger.debug("Skipping {} - Honeywell auth is broken, re-authorize the Bridge to resume", action);
            return true;
        }
        return false;
    }

    private void logCallSuccess() {
        lastCallFailed.set(false);
    }

    private void logCallFailure(String message) {
        if (!lastCallFailed.getAndSet(true)) {
            logger.error(message);
        } else {
            logger.debug(message);
        }
    }

    private void logCallFailure(String message, Exception e) {
        if (!lastCallFailed.getAndSet(true)) {
            logger.error(message, e);
        } else {
            logger.debug(message, e);
        }
    }
}
