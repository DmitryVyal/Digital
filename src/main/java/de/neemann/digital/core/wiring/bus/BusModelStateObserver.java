/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserverTyped;
import de.neemann.digital.core.switching.PlainSwitch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Checks if a temporary burn condition is still present after the step is completed.
 * If so, an exception is thrown.
 * Handles also the reconfiguration of the nets if a switch has changed.
 */
public final class BusModelStateObserver implements ModelStateObserverTyped {
    private final ArrayList<CheckBurn> busList;
    private final HashSet<PlainSwitch.RealSwitch> closedSwitches;
    private int version;

    BusModelStateObserver() {
        busList = new ArrayList<>();
        closedSwitches = new HashSet<>();
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (event == ModelEvent.STEP && !busList.isEmpty()) {
            for (CheckBurn bus : busList)
                bus.checkBurn();

            busList.clear();
            version++;
        }
    }

    @Override
    public ModelEvent[] getEvents() {
        return new ModelEvent[]{ModelEvent.STEP};
    }

    /**
     * @return the version used to avoid double additions of nets in a burn condition
     */
    public int getVersion() {
        return version;
    }

    /**
     * Adds a net in a burn condition
     *
     * @param commonBusValue the value in burn condition
     */
    public void addCheck(CheckBurn commonBusValue) {
        busList.add(commonBusValue);
    }
}
