/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring.bus;

import com.sun.org.apache.bcel.internal.generic.MethodGen;
import de.neemann.digital.core.BurnException;
import de.neemann.digital.core.NodeInterface;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.util.HashSet;

/**
 * Handler to merge a number of inputs to form a common net value
 */
public class MergedOutputHandler extends ObservableValue implements NodeInterface, CheckBurn {
    private final BusModelStateObserver obs;
    private final ObservableValue[] inputs;
    private int addedVersion = -1;
    private long error;
    private HashSet<File> origin;
    private boolean burnCheck=true;

    /**
     * Creates a new instance.
     *
     * @param bits   the number of bits
     * @param inputs the inputs which are to merge
     */
    public MergedOutputHandler(int bits, BusModelStateObserver obs, ObservableValue... inputs) {
        super("commonBusValue", bits);
        this.obs = obs;
        this.inputs = inputs;
        for (ObservableValue input : inputs)
            input.addObserver(this);
    }

    public MergedOutputHandler noBurnCheck() {
        burnCheck=false;
        return this;
    }

    @Override
    public ObservableValues getOutputs() {
        return asList();
    }

    @Override
    public void hasChanged() {
        long s0 = 0;
        long z0 = -1;
        long v0 = 0;
        long e0 = 0;

        for (ObservableValue input : inputs) {
            long s1 = input.getStrong();
            long z1 = input.getHighZ();
            long v1 = input.getValue();

            e0 |= (~s0 & ~s1 & v0 & ~v1 & ~z1) | (~s0 & ~s1 & ~v0 & v1 & ~z0) | (s0 & s1 & ~v0 & v1) | (s0 & s1 & v0 & ~v1);
            v0 = (s1 & v1) | (~s1 & v0) | (v1 & z0);
            s0 = s0 | s1;
            z0 = z0 & z1;
        }

        set(v0, z0, s0);
        error = e0;

        if (burnCheck)
            doBurnCheck();
    }

    void doBurnCheck() {
        // if burn condition and not yet added for post step check add for post step check
        if (error != 0 && (obs.getVersion() != addedVersion)) {
            addedVersion = obs.getVersion();
            obs.addCheck(this);
        }
    }

    /**
     * @return true if there was a merge error
     */
    public boolean isError() {
        return error != 0;
    }

    @Override
    public void checkBurn() {
        if (isError())
            throw new BurnException(Lang.get("err_burnError"), new ObservableValues(inputs)).addOrigin(origin);
    }

    /**
     * Sets the origin of this {@link MergedOutputHandler}
     *
     * @param file the origin
     * @return this for chained calls
     */
    MergedOutputHandler addOrigin(File file) {
        if (origin == null)
            origin = new HashSet<>();
        origin.add(file);
        return this;
    }

    /**
     * Returns true if this net is a constant
     *
     * @return the constant if this is a constant, null otherwise
     */
    public ObservableValue searchConstant() {
        for (ObservableValue i : inputs)
            if (i.isConstant())
                return i;
        return null;
    }

}
