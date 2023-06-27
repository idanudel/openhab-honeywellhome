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


import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.honeywellhome.client.HoneywellClient;
import org.openhab.binding.honeywellhome.internal.HoneywellHomeConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.honeywellhome.internal.HoneywellHomeBindingConstants.COOL_SET_POINT;

/**
 * The {@link HoneywellHomeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Idan - Initial contribution
 */
public class HoneywellHomeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HoneywellHomeHandler.class);

    private @Nullable HoneywellHomeConfiguration config;

    private HoneywellClient honeywellClient;

    private final HttpClient httpClient;

    public HoneywellHomeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void initialize() {
        config = getConfigAs(HoneywellHomeConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            String consumerKey = config.consumerKey;
            String consumerSecret = config.consumerSecret;
            String token = config.token;
            String refreshToken = config.refreshToken;

            try {
                logger.debug("Initializing HoneywellClient with consumerKey: {}, consumerSecret: {}, token: {}, refreshToken: {}", consumerKey, consumerSecret, token,
                        refreshToken);
                this.honeywellClient = new HoneywellClient(scheduler, this.httpClient, consumerKey, consumerSecret, token,
                        refreshToken);
                if (this.honeywellClient.isValid()) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Can not access Honeywell, please check consumerKey, consumerSecret, code, redirect_uri");
            }
        });
    }

    public HoneywellClient getHoneywellClient() {
        return honeywellClient;
    }
}
