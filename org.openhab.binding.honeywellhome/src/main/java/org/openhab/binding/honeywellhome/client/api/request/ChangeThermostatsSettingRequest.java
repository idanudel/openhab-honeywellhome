package org.openhab.binding.honeywellhome.client.api.request;

import org.openhab.binding.honeywellhome.client.api.pojo.ChangeableValues;

public class ChangeThermostatsSettingRequest {

    String mode;
    int heatSetpoint;
    int coolSetpoint;
    String thermostatSetpointStatus;

    public ChangeThermostatsSettingRequest(ChangeableValues changeableValues) {
        this.mode = changeableValues.mode;
        this.heatSetpoint = changeableValues.heatSetpoint;
        this.coolSetpoint = changeableValues.coolSetpoint;
        this.thermostatSetpointStatus = changeableValues.thermostatSetpointStatus;
    }
}
