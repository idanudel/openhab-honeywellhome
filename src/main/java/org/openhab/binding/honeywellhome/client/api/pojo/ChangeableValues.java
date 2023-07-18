package org.openhab.binding.honeywellhome.client.api.pojo;

public class ChangeableValues {
    public String mode;
    public Number heatSetpoint;
    public Number coolSetpoint;
    public String thermostatSetpointStatus;
    public String heatCoolMode;

    public ChangeableValues(String mode, int heatSetpoint, int coolSetpoint, String thermostatSetpointStatus, String heatCoolMode) {
        this.mode = mode;
        this.heatSetpoint = heatSetpoint;
        this.coolSetpoint = coolSetpoint;
        this.thermostatSetpointStatus = thermostatSetpointStatus;
        this.heatCoolMode = heatCoolMode;
    }
}
