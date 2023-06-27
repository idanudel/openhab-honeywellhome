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
package org.openhab.binding.honeywellhome.internal;

import static org.openhab.binding.honeywellhome.internal.HoneywellHomeBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.honeywellhome.client.HoneywellClient;
import org.openhab.binding.honeywellhome.internal.discovery.HoneywellHomeDiscoveryService;
import org.openhab.binding.honeywellhome.internal.handler.HoneywellHomeHandler;
import org.openhab.binding.honeywellhome.internal.handler.HoneywellThermostatHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HoneywellHomeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Idan - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.honeywellhome", service = ThingHandlerFactory.class)
public class HoneywellHomeHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_THERMOSTAT, THING_TYPE_HONEYWELLHOME_ACCOUNT);
    private Map<ThingUID, ServiceRegistration<DiscoveryService>> discoveryServiceRegistrations = new HashMap<>();
    private final HttpClient httpClient;
    @Activate
    public HoneywellHomeHandlerFactory(@Reference final HttpClientFactory httpClientFactory) {
        super();
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_HONEYWELLHOME_ACCOUNT.equals(thingTypeUID)) {
            HoneywellHomeHandler honeywellHomeHandler = new HoneywellHomeHandler((Bridge)thing, this.httpClient);
            HoneywellHomeDiscoveryService discoveryService = new HoneywellHomeDiscoveryService(honeywellHomeHandler);

            ServiceRegistration<DiscoveryService> serviceRegistration = this.bundleContext
                    .registerService(DiscoveryService.class, discoveryService, null);

            discoveryServiceRegistrations.put(honeywellHomeHandler.getThing().getUID(), serviceRegistration);
            return honeywellHomeHandler;
        }
        if (THING_TYPE_THERMOSTAT.equals(thingTypeUID)) {
            return  new HoneywellThermostatHandler(thing);
        }

        return null;
    }
}
