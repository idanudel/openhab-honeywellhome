package org.openhab.binding.honeywellhome.client.api.pojo;

import java.util.ArrayList;
import java.util.Date;

public class Device {
    public int displayedOutdoorHumidity;
    public VacationHold vacationHold;
    public CurrentSchedulePeriod currentSchedulePeriod;
    public ScheduleCapabilities scheduleCapabilities;
    public ScheduleType scheduleType;
    public ChangeSource changeSource;
    public String scheduleStatus;
    public int allowedTimeIncrements;
    public Settings settings;
    public String deviceOsVersion;
    public String deviceClass;
    public String deviceType;
    public String deviceID;
    public int deviceInternalID;
    public String userDefinedDeviceName;
    public String name;
    public boolean isAlive;
    public boolean isUpgrading;
    public boolean isProvisioned;
    public String macID;
    public DeviceSettings deviceSettings;
    public Service service;
    public String deviceRegistrationDate;
    public String dataSyncStatus;
    public String deviceSerialNo;
    public String units;
    public int indoorTemperature;
    public double outdoorTemperature;
    public ArrayList<String> allowedModes;
    public int deadband;
    public boolean hasDualSetpointStatus;
    public int minHeatSetpoint;
    public int maxHeatSetpoint;
    public int minCoolSetpoint;
    public int maxCoolSetpoint;
    public ChangeableValues changeableValues;
    public OperationStatus operationStatus;
    public String deviceModel;
}
