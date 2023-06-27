/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.honeywellhome.internal.handler;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.honeywellhome.client.HoneywellClient;
import org.openhab.binding.honeywellhome.client.api.pojo.ChangeableValues;
import org.openhab.binding.honeywellhome.client.api.response.GetThermostatsStatusResponse;
import org.openhab.binding.honeywellhome.internal.HoneywellHomeThermostatConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.honeywellhome.internal.HoneywellHomeBindingConstants.*;

/**
 * The {@link HoneywellThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Idan - Initial contribution
 */

public class HoneywellThermostatHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HoneywellThermostatHandler.class);
    private final int MIN_REFRESH_INTERVAL = 15;
    private final int DEFAULT_REFRESH_INTERVAL = 20;
    private ChangeableValues changeableValuesState;

    private @Nullable HoneywellHomeThermostatConfiguration config;

    protected @Nullable ScheduledFuture<?> refreshTask;

    public HoneywellThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        //todo put command on Q and fire them one after another to avoid data overwrite
        //todo make sure this handler is only for T5-T6 thermostat type
        String locationId = config.locationId;
        String deviceId = config.deviceId;
        if (command instanceof RefreshType) {
            startRefreshTask();
            return;
        }
        if (command instanceof QuantityType) {
            if (COOL_SET_POINT.equals(channelUID.getId())) {
                this.changeableValuesState.coolSetpoint = ((QuantityType<?>) command).intValue();
                this.getHoneywellClient().changeThermostatsSetting(deviceId, locationId, this.changeableValuesState);
            }
            if (HEAT_SET_POINT.equals(channelUID.getId())) {
                this.changeableValuesState.heatSetpoint = ((QuantityType<?>) command).intValue();
                this.getHoneywellClient().changeThermostatsSetting(deviceId, locationId, this.changeableValuesState);
            }
        }
        if (HEAT_COOL_MODE.equals(channelUID.getId())) {
            this.changeableValuesState.heatCoolMode = command.toString();
            this.getHoneywellClient().changeThermostatsSetting(deviceId, locationId, this.changeableValuesState);
        }
        if (MODE.equals(channelUID.getId())) {
            this.changeableValuesState.mode = command.toString();
            this.getHoneywellClient().changeThermostatsSetting(deviceId, locationId, this.changeableValuesState);
        }

    }

    @Override
    public void initialize() {
        config = getConfigAs(HoneywellHomeThermostatConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        startRefreshTask();

    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
    }

    private void startRefreshTask() {
        disposeRefreshTask();
        if(config.refreshInterval != -1) { // -1 to disable update
            int currentRefreshInterval = config.refreshInterval < MIN_REFRESH_INTERVAL ? DEFAULT_REFRESH_INTERVAL : config.refreshInterval;
            logger.info("Starting Honeywell Thermostat Refresh Task with refresh interval: {}", currentRefreshInterval);
            refreshTask = scheduler.scheduleWithFixedDelay(this::update, 0, currentRefreshInterval, TimeUnit.SECONDS);
        }
    }

    private void disposeRefreshTask() {
        ScheduledFuture<?> localRefreshTask = refreshTask;
        if (localRefreshTask != null) {
            localRefreshTask.cancel(true);
            this.refreshTask = null;
        }
    }

    @Override
    public void dispose() {
        logger.info("Stopping Honeywell Thermostat Refresh Task");
        super.dispose();
        this.disposeRefreshTask();
    }

    private void update() {
        try {
            String locationId = config.locationId;
            String deviceId = config.deviceId;
            if (getHoneywellClient() != null) {
                GetThermostatsStatusResponse getThermostatsStatusResponse = getHoneywellClient().getThermostatsDevice(deviceId, locationId);
                if(getThermostatsStatusResponse!=null) {
                    updateStatus(ThingStatus.ONLINE);
                    this.changeableValuesState = getThermostatsStatusResponse.changeableValues; // we need to store this data because in order to change one of the values we need to provide all of them;
                    updateState(COOL_SET_POINT, new DecimalType(getThermostatsStatusResponse.changeableValues.coolSetpoint));
                    updateState(HEAT_SET_POINT, new DecimalType(getThermostatsStatusResponse.changeableValues.heatSetpoint));
                    updateState(THERMOSTAT_SET_POINT_STATUS, new StringType(getThermostatsStatusResponse.changeableValues.thermostatSetpointStatus));
                    updateState(HEAT_COOL_MODE, new StringType(getThermostatsStatusResponse.changeableValues.heatCoolMode));
                    updateState(MODE, new StringType(getThermostatsStatusResponse.changeableValues.mode));
                }
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE);
            logger.error("Got error on Thermostat Handler update method", e);
        }

    }

    private HoneywellClient getHoneywellClient() {
        HoneywellHomeHandler honeywellHomeHandler = (HoneywellHomeHandler) getBridge().getHandler(); // todo make it safe
        return honeywellHomeHandler.getHoneywellClient();
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        this.startRefreshTask();
    }
}
