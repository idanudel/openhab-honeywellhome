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
import org.openhab.binding.honeywellhome.client.api.response.GetThermostatsStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.openhab.binding.honeywellhome.client.HoneywellClientConstants.*;

public class HoneywellClient {
    private final Logger logger = LoggerFactory.getLogger(HoneywellClient.class);
    private final HttpClient httpClient;
    private final HoneywellAuthProvider honeywellAuthProvider;
    private final Gson gson = new Gson();

    public HoneywellClient(ScheduledExecutorService scheduler, HttpClient httpClient, String consumerKey, String consumerSecret, String token, String refreshToken) throws Exception {
        this.httpClient = httpClient;
        this.honeywellAuthProvider = new HoneywellAuthProvider(scheduler, httpClient, consumerKey, consumerSecret, refreshToken, refreshToken);
        this.honeywellAuthProvider.init();
    }

    public boolean isValid() throws Exception {
        return this.honeywellAuthProvider.getHoneywellCredentials().isValid;
    }
    public List<GetAllLocationsResponse> getAllLocations() {
        return getAllLocations(false);
    }
    private List<GetAllLocationsResponse> getAllLocations(boolean isRetry) {
        try {
            String accessToken = this.honeywellAuthProvider.getHoneywellCredentials().accessToken;
            String url = String.format(HONEYWELL_GET_ALL_LOCATIONS, this.honeywellAuthProvider.consumerKey);
            ContentResponse contentResponse = this.httpClient.newRequest(url).
                    method(HttpMethod.GET).header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .send();
            if(contentResponse.getStatus() == 200) {
                String contentAsString = contentResponse.getContentAsString();
                logger.debug("Got AllLocations by consumer id: {} response: {}", getConsumeId(), contentAsString);
                return gson.fromJson(contentResponse.getContentAsString(), new TypeToken<ArrayList<GetAllLocationsResponse>>() {}.getType());
            }
            if(contentResponse.getStatus() == 401 && isRetry == false) {
                this.honeywellAuthProvider.refreshToken();
                return getAllLocations(true);
            }
            else {
                logger.error("Got error response: {} while trying to get Honeywell All locations {}", contentResponse.getStatus(), contentResponse.getContentAsString());
            }
        } catch (Exception e) {
            logger.error("Got error while trying to get Honeywell All locations", e);
        }
        return null;
    }
    public GetThermostatsStatusResponse getThermostatsDevice(String thermostatId, String locationId) {
        return getThermostatsDevice(thermostatId, locationId, false);
    }

    private GetThermostatsStatusResponse getThermostatsDevice(String thermostatId, String locationId, boolean isRetry) {
        try {
            String accessToken = this.honeywellAuthProvider.getHoneywellCredentials().accessToken;
            String url = String.format(HONEYWELL_GET_THERMOSTAT_STATUS, thermostatId, this.honeywellAuthProvider.consumerKey, locationId);
            ContentResponse contentResponse = this.httpClient.newRequest(url)
                    .method(HttpMethod.GET).header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .send();
            if(contentResponse.getStatus() == 200) {
                String contentAsString = contentResponse.getContentAsString();
                logger.debug("Got device by id: {} location id: {} with response: {}", thermostatId, locationId, contentAsString);
                return gson.fromJson(contentAsString, GetThermostatsStatusResponse.class);
            }
            if(contentResponse.getStatus() == 401 && isRetry == false) {
                this.honeywellAuthProvider.refreshToken();
                return getThermostatsDevice(thermostatId, locationId, true);
            }
            else {
                logger.error("Got error response: {} while trying to get Honeywell Thermostats Device id: {} in location: {}", contentResponse.getStatus(), thermostatId, locationId);
            }
        } catch (Exception e) {
            logger.error("Got error while trying to get Thermostats Device", e);
        }
        return null;
    }

    public boolean changeThermostatsSetting (String thermostatId, String locationId, ChangeableValues changeableValues) {
        return changeThermostatsSetting(thermostatId, locationId, changeableValues, false);
    }

    private boolean changeThermostatsSetting (String thermostatId, String locationId, ChangeableValues changeableValues, boolean isRetry) {
        try {
            String accessToken = this.honeywellAuthProvider.getHoneywellCredentials().accessToken;
            String url = String.format(HONEYWELL_POST_THERMOSTAT_STATUS, thermostatId, this.honeywellAuthProvider.consumerKey, locationId);
            StringContentProvider contentProvider = new StringContentProvider(gson.toJson(new ChangeThermostatsSettingRequest(changeableValues)));
            ContentResponse contentResponse = this.httpClient.newRequest(url)
                    .method(HttpMethod.POST)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .content(contentProvider)
                    .send();
            if(contentResponse.getStatus() == 200) {
                logger.debug("Change Thermostats Setting by id: {} location id: {} with response: {}", thermostatId, locationId, 200);
                return true;
            }
            if(contentResponse.getStatus() == 401 && isRetry == false) {
                this.honeywellAuthProvider.refreshToken();
                return changeThermostatsSetting(thermostatId, locationId, changeableValues, true);
            }
            else {
                logger.error("Got error response: {} while trying to Change Honeywell Thermostats Setting by Device id: {} in location: {}", contentResponse.getStatus(), thermostatId, locationId);
            }
        } catch (Exception e) {
            logger.error("Got error while trying to Change Honeywell Thermostats DeviceId: {} locationId: {}", thermostatId, locationId, e);
        }
        return false;
    }

    public String getConsumeId () {
        return this.honeywellAuthProvider != null ? this.honeywellAuthProvider.consumerKey : null;
    }
}
