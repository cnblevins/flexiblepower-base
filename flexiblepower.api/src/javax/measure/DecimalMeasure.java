/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2007 - JScience (http://jscience.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure;

import java.math.BigDecimal;
import java.math.MathContext;

import javax.measure.converter.AddConverter;
import javax.measure.converter.RationalConverter;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

/**
 * <p>
 * This class represents a measure whose value is an arbitrary-precision decimal number.
 * </p>
 *
 * <p>
 * When converting, applications may supply the <code>java.math.Context</code>:
 *
 * <pre>
 * DecimalMeasure&lt;Velocity&gt; c = DecimalMeasure.valueOf("299792458 m/s");
 * DecimalMeasure&lt;Velocity&gt; milesPerHour = c.to(MILES_PER_HOUR, MathContext.DECIMAL128);
 * System.out.println(milesPerHour);
 *
 * &gt; 670616629.3843951324266284896206156 mph
 * </pre>
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.3, October 3, 2007
 * @param <Q>
 *            The quantity
 */
public class DecimalMeasure<Q extends Quantity> extends Measure<BigDecimal, Q> {

    /**
     * Holds the BigDecimal value.
     */
    private final BigDecimal value;

    /**
     * Holds the unit.
     */
    private final Unit<Q> unit;

    /**
     * Creates a decimal measure for the specified number stated in the specified unit.
     *
     * @param value
     *            The value
     * @param unit
     *            The unit
     */
    public DecimalMeasure(BigDecimal value, Unit<Q> unit) {
        this.value = value;
        this.unit = unit;
    }

    /**
     * Returns the decimal measure for the specified number stated in the specified unit.
     *
     * @param decimal
     *            the measurement value.
     * @param unit
     *            the measurement unit.
     * @param <Q>
     *            The quantity
     */
    public static <Q extends Quantity> DecimalMeasure<Q> valueOf(BigDecimal decimal, Unit<Q> unit) {
        return new DecimalMeasure<Q>(decimal, unit);
    }

    /**
     * @param csq
     *            the decimal measure representation (including unit if any).
     * @param <Q>
     *            The quantity
     * @return the decimal measure for the specified textual representation. This method first reads the
     *         <code>BigDecimal</code> value, then the unit if any (value and unit should be separated by white spaces).
     * @throws NumberFormatException
     *             if the specified character sequence is not a valid representation of decimal measure.
     */
    @SuppressWarnings("unchecked")
    public static <Q extends Quantity> DecimalMeasure<Q> valueOf(CharSequence csq) {
        String str = csq.toString();
        int numberLength = str.length();
        int unitStartIndex = -1;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                for (int j = i + 1; j < str.length(); j++) {
                    if (!Character.isWhitespace(str.charAt(j))) {
                        unitStartIndex = j;
                        break;
                    }
                }
                numberLength = i;
                break;
            }
        }
        BigDecimal decimal = new BigDecimal(str.substring(0, numberLength));
        Unit<?> unit = Unit.ONE;
        if (unitStartIndex > 0) {
            unit = Unit.valueOf(str.substring(unitStartIndex));
        }
        return new DecimalMeasure<Q>(decimal, (Unit<Q>) unit);
    }

    @Override
    public Unit<Q> getUnit() {
        return this.unit;
    }

    @Override
    public BigDecimal getValue() {
        return this.value;
    }

    /**
     * Returns the decimal measure equivalent to this measure but stated in the specified unit. This method will raise
     * an ArithmeticException if the resulting measure does not have a terminating decimal expansion.
     *
     * @param unit
     *            the new measurement unit.
     * @return the measure stated in the specified unit.
     * @throws ArithmeticException
     *             if the converted measure value does not have a terminating decimal expansion
     * @see #to(Unit, MathContext)
     */
    @Override
    public DecimalMeasure<Q> to(Unit<Q> unit) {
        return to(unit, null);
    }

    /**
     * Returns the decimal measure equivalent to this measure but stated in the specified unit, the conversion is
     * performed using the specified math context.
     *
     * @param unit
     *            the new measurement unit.
     * @param mathContext
     *            the mathContext used to convert <code>BigDecimal</code> values or <code>null</code> if none.
     * @return the measure stated in the specified unit.
     * @throws ArithmeticException
     *             if the result is inexact but the rounding mode is <code>MathContext.UNNECESSARY</code> or
     *             <code>mathContext.precision == 0</code> and the quotient has a non-terminating decimal expansion.
     */
    public DecimalMeasure<Q> to(Unit<Q> unit, MathContext mathContext) {
        if ((unit == this.unit) || (unit.equals(this.unit))) {
            return this;
        }
        UnitConverter cvtr = this.unit.getConverterTo(unit);
        if (cvtr instanceof RationalConverter) {
            RationalConverter factor = (RationalConverter) cvtr;
            BigDecimal dividend = BigDecimal.valueOf(factor.getDividend());
            BigDecimal divisor = BigDecimal.valueOf(factor.getDivisor());
            BigDecimal result = mathContext == null ? this.value.multiply(dividend).divide(divisor)
                                                   : this.value.multiply(dividend, mathContext).divide(divisor,
                                                                                                       mathContext);
            return new DecimalMeasure<Q>(result, unit);
        } else if (cvtr.isLinear()) {
            BigDecimal factor = BigDecimal.valueOf(cvtr.convert(1.0));
            BigDecimal result = mathContext == null ? this.value.multiply(factor) : this.value.multiply(factor,
                                                                                                        mathContext);
            return new DecimalMeasure<Q>(result, unit);
        } else if (cvtr instanceof AddConverter) {
            BigDecimal offset = BigDecimal.valueOf(((AddConverter) cvtr).getOffset());
            BigDecimal result = mathContext == null ? this.value.add(offset) : this.value.add(offset, mathContext);
            return new DecimalMeasure<Q>(result, unit);
        } else { // Non-linear and not an offset, convert the double value.
            BigDecimal result = BigDecimal.valueOf(cvtr.convert(this.value.doubleValue()));
            return new DecimalMeasure<Q>(result, unit);
        }
    }

    @Override
    public double doubleValue(Unit<Q> unit) {
        if ((unit == this.unit) || (unit.equals(this.unit))) {
            return this.value.doubleValue();
        }
        return this.unit.getConverterTo(unit).convert(this.value.doubleValue());
    }

    @Override
    public Measurable<Q> add(Measurable<Q> other) {
        if (other instanceof DecimalMeasure) {
            DecimalMeasure<Q> otherDec = (DecimalMeasure<Q>) other;
            return new DecimalMeasure<Q>(value.add(otherDec.to(unit).value), unit);
        } else {
            return new DecimalMeasure<Q>(value.add(BigDecimal.valueOf(other.doubleValue(unit))), unit);
        }
    }

    private static final long serialVersionUID = 1L;
}
