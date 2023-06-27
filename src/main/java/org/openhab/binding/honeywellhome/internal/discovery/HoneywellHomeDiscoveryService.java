package org.openhab.binding.honeywellhome.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.honeywellhome.client.HoneywellClient;
import org.openhab.binding.honeywellhome.client.api.response.GetAllLocationsResponse;
import org.openhab.binding.honeywellhome.client.api.pojo.Device;
import org.openhab.binding.honeywellhome.internal.HoneywellHomeBindingConstants;
import org.openhab.binding.honeywellhome.internal.HoneywellHomeHandlerFactory;
import org.openhab.binding.honeywellhome.internal.handler.HoneywellHomeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoneywellHomeDiscoveryService extends AbstractDiscoveryService {

    private static final int TIMEOUT = 10;

    HoneywellHomeHandler honeywellHomeHandler;

    private final Logger logger = LoggerFactory.getLogger(HoneywellHomeDiscoveryService.class);

    public HoneywellHomeDiscoveryService(HoneywellHomeHandler honeywellHomeHandler) throws IllegalArgumentException {
        super(HoneywellHomeHandlerFactory.SUPPORTED_THING_TYPES_UIDS, TIMEOUT);
        this.honeywellHomeHandler = honeywellHomeHandler;
    }

    @Override
    protected void startScan() {
        HoneywellClient honeywellClient = this.honeywellHomeHandler.getHoneywellClient();
        try {
            if (honeywellClient.isValid()) {
                List<GetAllLocationsResponse> getAllLocationsResponseList = honeywellClient.getAllLocations();
                if (getAllLocationsResponseList != null && !getAllLocationsResponseList.isEmpty()) {
                    for (GetAllLocationsResponse getAllLocationsResponse : getAllLocationsResponseList) {
                        String locationId = getAllLocationsResponse.locationID;
                        if (getAllLocationsResponse.devices != null && !getAllLocationsResponse.devices.isEmpty()) {
                            for (Device device : getAllLocationsResponse.devices) {
                                addDeviceResult(locationId, device.deviceID, device.userDefinedDeviceName);
                            }
                        } else {
                            logger.debug("Got empty devices in getAllLocations List for ConsumeId: {}", honeywellClient.getConsumeId());
                        }
                    }
                } else{
                    logger.debug("Got empty results in getAllLocations List for ConsumeId: {}", honeywellClient.getConsumeId());
                }
            } else {
                logger.warn("Honeywell Client is not Valid");
            }
        } catch (Exception e) {
            logger.error("Got error while trying scanning honeywell things", e);
        }
    }

    @Override
    public void deactivate() {
    }

    private void addDeviceResult(String locationId, String deviceId, String userDefinedDeviceName) {
        logger.debug("Adding new honeywell locationId: {} deviceId: {} userDefinedDeviceName: {}", locationId, deviceId, userDefinedDeviceName);
        String name = userDefinedDeviceName;
        ThingUID thingUID = new ThingUID(HoneywellHomeBindingConstants.THING_TYPE_THERMOSTAT, this.honeywellHomeHandler.getThing().getUID(), deviceId);
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(HoneywellHomeBindingConstants.LOCATION_ID, locationId);
        properties.put(HoneywellHomeBindingConstants.DEVICE_ID, deviceId);

        addDiscoveredThing(thingUID, properties, name);
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withLabel(displayLabel).withBridge(this.honeywellHomeHandler.getThing().getUID()).build();
        thingDiscovered(discoveryResult);
    }
}
