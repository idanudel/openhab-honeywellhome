package org.openhab.binding.honeywellhome.client.api.response;

import java.util.ArrayList;

import org.openhab.binding.honeywellhome.client.api.pojo.Configuration;
import org.openhab.binding.honeywellhome.client.api.pojo.Device;
import org.openhab.binding.honeywellhome.client.api.pojo.User;

public class GetAllLocationsResponse {
    public String locationID;
    public String name;
    public String country;
    public String zipcode;
    public ArrayList<Device> devices;
    public ArrayList<User> users;
    public String timeZoneId;
    public String timeZone;
    public String ianaTimeZone;
    public boolean daylightSavingTimeEnabled;
    public boolean geoFenceEnabled;
    public boolean predictiveAIREnabled;
    public int comfortLevel;
    public boolean geoFenceNotificationEnabled;
    public int geoFenceNotificationTypeId;
    public Configuration configuration;
}
