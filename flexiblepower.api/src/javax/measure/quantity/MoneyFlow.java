/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.quantity;

import javax.measure.unit.NonSI;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.Unit;

/**
 * This interface represents MoneyFlow value. The default unit for this quantity is euros per hour.
 */
public interface MoneyFlow extends Quantity {

    /**
     * Default value for money
     */
    Unit<MoneyFlow> UNIT = new ProductUnit<MoneyFlow>(NonSI.EUR_PER_HOUR);

}
