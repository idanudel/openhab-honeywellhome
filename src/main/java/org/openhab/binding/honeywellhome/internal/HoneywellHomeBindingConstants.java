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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HoneywellHomeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Idan - Initial contribution
 */
@NonNullByDefault
public class HoneywellHomeBindingConstants {

    private static final String BINDING_ID = "honeywellhome";

    public static final String LOCATION_ID = "locationId";
    public static final String DEVICE_ID = "deviceId";


    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HONEYWELLHOME_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    // List of all Channel ids
    public static final String COOL_SET_POINT = "coolSetPoint";
    public static final String HEAT_SET_POINT = "heatSetpoint";
    public static final String THERMOSTAT_SET_POINT_STATUS = "thermostatSetpointStatus";
    public static final String HEAT_COOL_MODE = "heatCoolMode";
    public static final String MODE = "mode";
}
