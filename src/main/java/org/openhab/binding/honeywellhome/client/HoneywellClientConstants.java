package org.openhab.binding.honeywellhome.client;

public class HoneywellClientConstants {
    public static final String DEFAULT_HONEYWELL_DOMAIN = "api.honeywell.com"; //todo add option to pull it from config
    public static final String HONEYWELL_GET_TOKEN_URI = "https://" + DEFAULT_HONEYWELL_DOMAIN + "/oauth2/token";
    public static final String HONEYWELL_REFRESH_TOKEN_URI = HONEYWELL_GET_TOKEN_URI;
    public static final String HONEYWELL_GET_ALL_LOCATIONS = "https://" + DEFAULT_HONEYWELL_DOMAIN + "/v2/locations?apikey=%s";
    public static final String HONEYWELL_GET_THERMOSTAT_STATUS = "https://" + DEFAULT_HONEYWELL_DOMAIN + "/v2/devices/thermostats/%s?apikey=%s&locationId=%s";
    public static final String HONEYWELL_POST_THERMOSTAT_STATUS = HONEYWELL_GET_THERMOSTAT_STATUS;
    public static final String HONEYWELL_GET_THERMOSTAT_FAN_STATUS = "https://" + DEFAULT_HONEYWELL_DOMAIN + "/v2/devices/thermostats/%s/fan?apikey=%s&locationId=%s";
    public static final String HONEYWELL_POST_THERMOSTAT_FAN_STATUS = HONEYWELL_GET_THERMOSTAT_FAN_STATUS;
}
