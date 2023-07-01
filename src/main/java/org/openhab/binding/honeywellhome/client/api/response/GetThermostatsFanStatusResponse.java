package org.openhab.binding.honeywellhome.client.api.response;

public class GetThermostatsFanStatusResponse {
    public GetThermostatsFanStatusResponse(String mode) {
        this.mode = mode;
    }

    public String mode;
}
