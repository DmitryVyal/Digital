package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.gui.components.table.TableDialog;
import junit.framework.TestCase;

import java.util.ArrayList;

public class MergedOutputHandlerTest extends TestCase {

    public void testHandler() {
        ObservableValue in1 = new ObservableValue("i1", 3);
        ObservableValue in2 = new ObservableValue("i2", 3);
        MergedOutputHandler h = new MergedOutputHandler(3, new BusModelStateObserver(), in1, in2);
        ObservableValue expected = new ObservableValue("test", 3);

        for (SixValue i1 : allValues) {
            for (SixValue i2 : allValues) {
                for (SixValue i3 : allValues) {
                    for (SixValue i4 : allValues) {
                        for (SixValue i5 : allValues) {
                            for (SixValue i6 : allValues) {
                                SixValue.set(in1, i1, i2, i3);
                                SixValue.set(in2, i4, i5, i6);

                                final SixValue exp1 = i1.add(i4);
                                final SixValue exp2 = i2.add(i5);
                                final SixValue exp3 = i3.add(i6);
                                if (exp1 != SixValue.error && exp2 != SixValue.error && exp3 != SixValue.error) {
                                    SixValue.set(expected, exp1, exp2, exp3);
                                    assertTrue(valEquals(expected, h));
                                    assertFalse("should be no error", h.isError());
                                } else {
                                    assertTrue("should be an error", h.isError());
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    public void testHandler2() {
        ObservableValue in1 = new ObservableValue("i1", 1);
        ObservableValue in2 = new ObservableValue("i2", 1);
        ObservableValue in3 = new ObservableValue("i3", 1);
        MergedOutputHandler h = new MergedOutputHandler( 1, new BusModelStateObserver(), in1, in2, in3);
        ObservableValue expected = new ObservableValue("test", 1);

        for (SixValue i1 : allValues) {
            for (SixValue i2 : allValues) {
                for (SixValue i3 : allValues) {
                    SixValue.set(in1, i1);
                    SixValue.set(in2, i2);
                    SixValue.set(in3, i3);

                    final SixValue exp = i1.add(i2).add(i3);
                    if (exp != SixValue.error) {
                        SixValue.set(expected, exp);
                        assertTrue(valEquals(expected, h));
                        assertFalse("should be no error", h.isError());
                    } else {
                        assertTrue("should be an error", h.isError());
                    }
                }
            }

        }
    }


    private boolean valEquals(ObservableValue a, ObservableValue b) {
        return a.getHighZ() == b.getHighZ() && a.getValue() == b.getValue() && a.getStrong() == b.getStrong();
    }

    /*
    public void testSynthesises() throws InterruptedException {

        ArrayList<Variable> vars = new ArrayList<>();
        vars.add(new Variable("s_0"));
        vars.add(new Variable("z_0"));
        vars.add(new Variable("v_0"));
        vars.add(new Variable("s_1"));
        vars.add(new Variable("z_1"));
        vars.add(new Variable("v_1"));
        TruthTable t = new TruthTable(vars).addResult("s").addResult("z").addResult("v").addResult("e");
        for (int r = 0; r < t.getRows(); r++) {
            t.setValue(r, 6, 2);
            t.setValue(r, 7, 2);
            t.setValue(r, 8, 2);
            t.setValue(r, 9, 2);
        }

        for (SixValue i1 : allValues) {
            for (SixValue i2 : allValues) {

                final SixValue exp = i1.add(i2);
                int row = (i1.getInt() << 3) | i2.getInt();

                if (exp != SixValue.error) {
                    t.setValue(row, 6, exp.getStrong() ? 1 : 0);
                    t.setValue(row, 7, exp.getHighZ() ? 1 : 0);
                    t.setValue(row, 8, exp.getValue() ? 1 : 0);
                }
                t.setValue(row, 9, exp == SixValue.error ? 1 : 0);
            }
        }

        new TableDialog(null, t, null, null, null).setVisible(true);
        Thread.sleep(100000);

    }*/

    private SixValue[] allValues = new SixValue[]{SixValue.highZ, SixValue.weak_high, SixValue.weak_low, SixValue.strong_high, SixValue.strong_low};

    enum SixValue {
        highZ, weak_high, weak_low, strong_high, strong_low, error;


        public SixValue add(SixValue v) {
            switch (this) {
                case strong_high:
                    if (v == strong_low || v == error) return error;
                    return strong_high;
                case strong_low:
                    if (v == strong_high || v == error) return error;
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

        public int getInt() {
            return ((getStrong() ? 1 : 0) << 2) | ((getHighZ() ? 1 : 0) << 1) | (getValue() ? 1 : 0);
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
        test(SixValue.strong_high, SixValue.error, SixValue.error);
        test(SixValue.strong_low, SixValue.error, SixValue.error);

        test(SixValue.weak_high, SixValue.weak_high, SixValue.weak_high);
        test(SixValue.weak_low, SixValue.weak_low, SixValue.weak_low);
        test(SixValue.weak_low, SixValue.weak_high, SixValue.error);
        test(SixValue.weak_high, SixValue.highZ, SixValue.weak_high);
        test(SixValue.weak_low, SixValue.highZ, SixValue.weak_low);
        test(SixValue.weak_high, SixValue.error, SixValue.error);
        test(SixValue.weak_low, SixValue.error, SixValue.error);

        test(SixValue.highZ, SixValue.highZ, SixValue.highZ);
        test(SixValue.highZ, SixValue.error, SixValue.error);
    }

    private void test(SixValue in1, SixValue in2, SixValue result) {
        assertEquals(result, in1.add(in2));
        assertEquals(result, in2.add(in1));
    }

}