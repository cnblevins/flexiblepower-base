/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.unit;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;

/**
 * This class represents the multi-radix units (such as "hour:min:sec"). Instances of this class are created using the
 * {@link Unit#compound Unit.compound} method.
 *
 * Examples of compound units:
 *
 * <pre>
 * Unit&lt;Duration&gt; HOUR_MINUTE_SECOND = HOUR.compound(MINUTE).compound(SECOND);
 * Unit&lt;Angle&gt; DEGREE_MINUTE_ANGLE = DEGREE_ANGLE.compound(MINUTE_ANGLE);
 * </pre>
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, April 22, 2006
 */
public final class CompoundUnit<Q extends Quantity> extends DerivedUnit<Q> {

    /**
     * Holds the higher unit.
     */
    private final Unit<Q> high;

    /**
     * Holds the lower unit.
     */
    private final Unit<Q> low;

    /**
     * Creates a compound unit from the specified units.
     *
     * @param high
     *            the high unit.
     * @param low
     *            the lower unit(s)
     * @throws IllegalArgumentException
     *             if both units do not the same system unit.
     */
    CompoundUnit(Unit<Q> high, Unit<Q> low) {
        if (!high.getStandardUnit().equals(low.getStandardUnit())) {
            throw new IllegalArgumentException("Both units do not have the same system unit");
        }
        this.high = high;
        this.low = low;

    }

    /**
     * Returns the lower unit of this compound unit.
     *
     * @return the lower unit.
     */
    public Unit<Q> getLower() {
        return this.low;
    }

    /**
     * Returns the higher unit of this compound unit.
     *
     * @return the higher unit.
     */
    public Unit<Q> getHigher() {
        return this.high;
    }

    /**
     * Indicates if this compound unit is considered equals to the specified object (both are compound units with same
     * composing units in the same order).
     *
     * @param that
     *            the object to compare for equality.
     * @return <code>true</code> if <code>this</code> and <code>that</code> are considered equals; <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof CompoundUnit)) {
            return false;
        }
        CompoundUnit<?> thatUnit = (CompoundUnit<?>) that;
        return this.high.equals(thatUnit.high) && this.low.equals(thatUnit.low);
    }

    @Override
    public int hashCode() {
        return this.high.hashCode() ^ this.low.hashCode();
    }

    @Override
    public Unit<? super Q> getStandardUnit() {
        return this.low.getStandardUnit();
    }

    @Override
    public UnitConverter toStandardUnit() {
        return this.low.toStandardUnit();
    }

    private static final long serialVersionUID = 1L;
}
