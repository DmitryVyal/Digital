package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.ObservableValue;
import junit.framework.TestCase;

public class MergedOutputHandlerTest extends TestCase {

    public void testHandler() {

        ObservableValue in1 = new ObservableValue("i1", 2);
        ObservableValue in2 = new ObservableValue("i2", 2);
        MergedOutputHandler h = new MergedOutputHandler("t", 2, in1, in2);

        ObservableValue expected = new ObservableValue("test", 2);


        for (SixValue i1 : allValues) {
            for (SixValue i2 : allValues) {
                for (SixValue i3 : allValues) {
                    for (SixValue i4 : allValues) {
                        SixValue.set(in1, i1, i2);
                        SixValue.set(in2, i3, i4);

                        final SixValue exp1 = i1.add(i3);
                        final SixValue exp2 = i2.add(i4);
                        if (exp1 != SixValue.error && exp2 != SixValue.error) {
                            SixValue.set(expected, exp1, exp2);
                            System.out.println(in1 + " and " + in1 + " gives " + expected);
                            //assertEquals(i1 + "<->" + i2, expected, h);
                            //assertFalse(h.isError());
                        } else {
                            assertTrue(h.isError());
                        }

                    }
                }
            }
        }

    }

    SixValue[] allValues = new SixValue[]{SixValue.highZ, SixValue.weak_high, SixValue.weak_low, SixValue.strong_high, SixValue.strong_low};

    enum SixValue {
        highZ, weak_high, weak_low, strong_high, strong_low, error;


        public SixValue add(SixValue v) {
            switch (this) {
                case strong_high:
                    if (v == strong_low) return error;
                    return strong_high;
                case strong_low:
                    if (v == strong_high) return error;
                    return strong_low;
                case weak_high:
                    switch (v) {
                        case strong_high:
                            return strong_high;
                        case strong_low:
                            return strong_low;
                        case weak_high:
                            return weak_high;
                        case weak_low:
                            return error;
                        case highZ:
                            return weak_high;
                    }
                    break;
                case weak_low:
                    switch (v) {
                        case strong_high:
                            return strong_high;
                        case strong_low:
                            return strong_low;
                        case weak_high:
                            return error;
                        case weak_low:
                            return weak_low;
                        case highZ:
                            return weak_low;
                    }
                case highZ:
                    switch (v) {
                        case strong_high:
                            return strong_high;
                        case strong_low:
                            return strong_low;
                        case weak_high:
                            return weak_high;
                        case weak_low:
                            return weak_low;
                        case highZ:
                            return highZ;
                    }
            }
            return error;
        }

        boolean getValue() {
            switch (this) {
                case strong_high:
                    return true;
                case strong_low:
                    return false;
                case weak_high:
                    return true;
                case weak_low:
                    return false;
                case highZ:
                    return false;
                default:
                    throw new RuntimeException("error");
            }
        }

        boolean getStrong() {
            switch (this) {
                case strong_high:
                    return true;
                case strong_low:
                    return true;
                case weak_high:
                    return false;
                case weak_low:
                    return false;
                case highZ:
                    return false;
                default:
                    throw new RuntimeException("error");
            }
        }

        boolean getHighZ() {
            switch (this) {
                case strong_high:
                    return false;
                case strong_low:
                    return false;
                case weak_high:
                    return false;
                case weak_low:
                    return false;
                case highZ:
                    return true;
                default:
                    throw new RuntimeException("error");
            }
        }

        static void set(ObservableValue v, SixValue... values) {
            if (v.getBits() != values.length)
                throw new RuntimeException("wrong size");

            long highZ = 0;
            long value = 0;
            long strong = 0;
            long mask = 1;
            for (SixValue val : values) {
                if (val.getHighZ()) highZ |= mask;
                if (val.getValue()) value |= mask;
                if (val.getStrong()) strong |= mask;
                mask *= 2;
            }

            v.set(value, highZ, strong);
        }
    }

    public void testSixValue() {
        test(SixValue.strong_high, SixValue.strong_high, SixValue.strong_high);
        test(SixValue.strong_low, SixValue.strong_low, SixValue.strong_low);
        test(SixValue.strong_low, SixValue.strong_high, SixValue.error);
        test(SixValue.strong_high, SixValue.weak_high, SixValue.strong_high);
        test(SixValue.strong_low, SixValue.weak_high, SixValue.strong_low);
        test(SixValue.strong_high, SixValue.weak_low, SixValue.strong_high);
        test(SixValue.strong_low, SixValue.weak_low, SixValue.strong_low);
        test(SixValue.strong_high, SixValue.highZ, SixValue.strong_high);
        test(SixValue.strong_low, SixValue.highZ, SixValue.strong_low);

        test(SixValue.weak_high, SixValue.weak_high, SixValue.weak_high);
        test(SixValue.weak_low, SixValue.weak_low, SixValue.weak_low);
        test(SixValue.weak_low, SixValue.weak_high, SixValue.error);
        test(SixValue.weak_high, SixValue.highZ, SixValue.weak_high);
        test(SixValue.weak_low, SixValue.highZ, SixValue.weak_low);

        test(SixValue.highZ, SixValue.highZ, SixValue.highZ);
    }

    private void test(SixValue in1, SixValue in2, SixValue result) {
        assertEquals(result, in1.add(in2));
        assertEquals(result, in2.add(in1));
    }

}