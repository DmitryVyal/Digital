/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.NodeInterface;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;

public class MergedOutputHandler extends ObservableValue implements NodeInterface {
    private final ObservableValue[] inputs;

    /**
     * Creates a new instance.
     *
     * @param name the name of this value
     * @param bits the number of bits
     */
    public MergedOutputHandler(String name, int bits, ObservableValue... inputs) {
        super(name, bits);
        this.inputs = inputs;
        for (ObservableValue input : inputs)
            input.addObserver(this);
    }

    @Override
    public ObservableValues getOutputs() {
        return asList();
    }

    @Override
    public void hasChanged() {
        long strong = 0;

        for (ObservableValue input : inputs) {
            long s = input.getStrong();
            long z = input.getHighZ();
            long v = input.getValue();


        }


    }

    public boolean isError() {
        return true;
    }
}
