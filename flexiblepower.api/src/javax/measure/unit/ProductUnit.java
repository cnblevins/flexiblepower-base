/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.unit;

import java.io.Serializable;

import javax.measure.converter.ConversionException;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;

/**
 * <p>
 * This class represents units formed by the product of rational powers of existing units.
 * </p>
 * 
 * <p>
 * This class maintains the canonical form of this product (simplest form after factorization). For example:
 * <code>METER.pow(2).divide(METER)</code> returns <code>METER</code>.
 * </p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, April 22, 2006
 * @see Unit#times(Unit)
 * @see Unit#divide(Unit)
 * @see Unit#pow(int)
 * @see Unit#root(int)
 */
public final class ProductUnit<Q extends Quantity> extends DerivedUnit<Q> {

    /**
     * Holds the units composing this product unit.
     */
    private final Element[] elements;

    /**
     * Holds the hashcode (optimization).
     */
    private int hashCode;

    /**
     * Default constructor (used solely to create <code>ONE</code> instance).
     */
    ProductUnit() {
        this.elements = new Element[0];
    }

    /**
     * Copy constructor (allows for parameterization of product units).
     * 
     * @param productUnit
     *            the product unit source.
     * @throws ClassCastException
     *             if the specified unit is not a product unit.
     */
    public ProductUnit(Unit<?> productUnit) {
        this.elements = ((ProductUnit<?>) productUnit).elements;
    }

    /**
     * Product unit constructor.
     * 
     * @param elements
     *            the product elements.
     */
    private ProductUnit(Element[] elements) {
        this.elements = elements;
    }

    /**
     * Returns the unit defined from the product of the specifed elements.
     * 
     * @param leftElems
     *            left multiplicand elements.
     * @param rightElems
     *            right multiplicand elements.
     * @return the corresponding unit.
     */
    @SuppressWarnings("rawtypes")
    private static Unit<? extends Quantity> getInstance(Element[] leftElems, Element[] rightElems) {
        // Merges left elements with right elements.
        Element[] result = new Element[leftElems.length + rightElems.length];
        int resultIndex = 0;
        for (Element leftElem : leftElems) {
            Unit unit = leftElem.unit;
            int p1 = leftElem.pow;
            int r1 = leftElem.root;
            int p2 = 0;
            int r2 = 1;
            for (Element rightElem : rightElems) {
                if (unit.equals(rightElem.unit)) {
                    p2 = rightElem.pow;
                    r2 = rightElem.root;
                    break; // No duplicate.
                }
            }
            int pow = (p1 * r2) + (p2 * r1);
            int root = r1 * r2;
            if (pow != 0) {
                int gcd = gcd(Math.abs(pow), root);
                result[resultIndex++] = new Element(unit, pow / gcd, root / gcd);
            }
        }

        // Appends remaining right elements not merged.
        for (Element rightElem : rightElems) {
            Unit unit = rightElem.unit;
            boolean hasBeenMerged = false;
            for (Element leftElem : leftElems) {
                if (unit.equals(leftElem.unit)) {
                    hasBeenMerged = true;
                    break;
                }
            }
            if (!hasBeenMerged) {
                result[resultIndex++] = rightElem;
            }
        }

        // Returns or creates instance.
        if (resultIndex == 0) {
            return ONE;
        } else if ((resultIndex == 1) && (result[0].pow == result[0].root)) {
            return result[0].unit;
        } else {
            Element[] elems = new Element[resultIndex];
            for (int i = 0; i < resultIndex; i++) {
                elems[i] = result[i];
            }
            return new ProductUnit<Quantity>(elems);
        }
    }

    /**
     * Returns the product of the specified units.
     * 
     * @param left
     *            the left unit operand.
     * @param right
     *            the right unit operand.
     * @return <code>left * right</code>
     */
    static Unit<? extends Quantity> getProductInstance(Unit<?> left, Unit<?> right) {
        Element[] leftElems;
        if (left instanceof ProductUnit) {
            leftElems = ((ProductUnit<?>) left).elements;
        } else {
            leftElems = new Element[] { new Element(left, 1, 1) };
        }
        Element[] rightElems;
        if (right instanceof ProductUnit) {
            rightElems = ((ProductUnit<?>) right).elements;
        } else {
            rightElems = new Element[] { new Element(right, 1, 1) };
        }
        return getInstance(leftElems, rightElems);
    }

    /**
     * Returns the quotient of the specified units.
     * 
     * @param left
     *            the dividend unit operand.
     * @param right
     *            the divisor unit operand.
     * @return <code>dividend / divisor</code>
     */
    static Unit<? extends Quantity> getQuotientInstance(Unit<?> left, Unit<?> right) {
        Element[] leftElems;
        if (left instanceof ProductUnit) {
            leftElems = ((ProductUnit<?>) left).elements;
        } else {
            leftElems = new Element[] { new Element(left, 1, 1) };
        }
        Element[] rightElems;
        if (right instanceof ProductUnit) {
            Element[] elems = ((ProductUnit<?>) right).elements;
            rightElems = new Element[elems.length];
            for (int i = 0; i < elems.length; i++) {
                rightElems[i] = new Element(elems[i].unit, -elems[i].pow, elems[i].root);
            }
        } else {
            rightElems = new Element[] { new Element(right, -1, 1) };
        }
        return getInstance(leftElems, rightElems);
    }

    /**
     * Returns the product unit corresponding to the specified root of the specified unit.
     * 
     * @param unit
     *            the unit.
     * @param n
     *            the root's order (n &gt; 0).
     * @return <code>unit^(1/nn)</code>
     * @throws ArithmeticException
     *             if <code>n == 0</code>.
     */
    static Unit<? extends Quantity> getRootInstance(Unit<?> unit, int n) {
        Element[] unitElems;
        if (unit instanceof ProductUnit) {
            Element[] elems = ((ProductUnit<?>) unit).elements;
            unitElems = new Element[elems.length];
            for (int i = 0; i < elems.length; i++) {
                int gcd = gcd(Math.abs(elems[i].pow), elems[i].root * n);
                unitElems[i] = new Element(elems[i].unit, elems[i].pow / gcd, elems[i].root * n / gcd);
            }
        } else {
            unitElems = new Element[] { new Element(unit, 1, n) };
        }
        return getInstance(unitElems, new Element[0]);
    }

    /**
     * Returns the product unit corresponding to this unit raised to the specified exponent.
     * 
     * @param unit
     *            the unit.
     * @param nn
     *            the exponent (nn &gt; 0).
     * @return <code>unit^n</code>
     */
    static Unit<? extends Quantity> getPowInstance(Unit<?> unit, int n) {
        Element[] unitElems;
        if (unit instanceof ProductUnit) {
            Element[] elems = ((ProductUnit<?>) unit).elements;
            unitElems = new Element[elems.length];
            for (int i = 0; i < elems.length; i++) {
                int gcd = gcd(Math.abs(elems[i].pow * n), elems[i].root);
                unitElems[i] = new Element(elems[i].unit, elems[i].pow * n / gcd, elems[i].root / gcd);
            }
        } else {
            unitElems = new Element[] { new Element(unit, n, 1) };
        }
        return getInstance(unitElems, new Element[0]);
    }

    /**
     * Returns the number of units in this product.
     * 
     * @return the number of units being multiplied.
     */
    public int getUnitCount() {
        return this.elements.length;
    }

    /**
     * Returns the unit at the specified position.
     * 
     * @param index
     *            the index of the unit to return.
     * @return the unit at the specified position.
     * @throws IndexOutOfBoundsException
     *             if index is out of range <code>(index &lt; 0 || index &gt;= size())</code>.
     */
    public Unit<? extends Quantity> getUnit(int index) {
        return this.elements[index].getUnit();
    }

    /**
     * Returns the power exponent of the unit at the specified position.
     * 
     * @param index
     *            the index of the unit to return.
     * @return the unit power exponent at the specified position.
     * @throws IndexOutOfBoundsException
     *             if index is out of range <code>(index &lt; 0 || index &gt;= size())</code>.
     */
    public int getUnitPow(int index) {
        return this.elements[index].getPow();
    }

    /**
     * Returns the root exponent of the unit at the specified position.
     * 
     * @param index
     *            the index of the unit to return.
     * @return the unit root exponent at the specified position.
     * @throws IndexOutOfBoundsException
     *             if index is out of range <code>(index &lt; 0 || index &gt;= size())</code>.
     */
    public int getUnitRoot(int index) {
        return this.elements[index].getRoot();
    }

    /**
     * Indicates if this product unit is considered equals to the specified object.
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
        if (that instanceof ProductUnit) {
            // Two products are equals if they have the same elements
            // regardless of the elements' order.
            Element[] elems = ((ProductUnit<?>) that).elements;
            if (this.elements.length == elems.length) {
                for (Element element : this.elements) {
                    boolean unitFound = false;
                    for (Element elem : elems) {
                        if (element.unit.equals(elem.unit)) {
                            if ((element.pow != elem.pow) || (element.root != elem.root)) {
                                return false;
                            } else {
                                unitFound = true;
                                break;
                            }
                        }
                    }
                    if (!unitFound) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    // Implements abstract method.
            public int
            hashCode() {
        if (this.hashCode != 0) {
            return this.hashCode;
        }
        int code = 0;
        for (Element element : this.elements) {
            code += element.unit.hashCode() * (element.pow * 3 - element.root * 2);
        }
        this.hashCode = code;
        return code;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Unit<? super Q> getStandardUnit() {
        if (hasOnlyStandardUnit()) {
            return this;
        }
        Unit systemUnit = ONE;
        for (Element element : this.elements) {
            Unit unit = element.unit.getStandardUnit();
            unit = unit.pow(element.pow);
            unit = unit.root(element.root);
            systemUnit = systemUnit.times(unit);
        }
        return systemUnit;
    }

    @Override
    public UnitConverter toStandardUnit() {
        if (hasOnlyStandardUnit()) {
            return UnitConverter.IDENTITY;
        }
        UnitConverter converter = UnitConverter.IDENTITY;
        for (Element element : this.elements) {
            UnitConverter cvtr = element.unit.toStandardUnit();
            if (!cvtr.isLinear()) {
                throw new ConversionException(element.unit + " is non-linear, cannot convert");
            }
            if (element.root != 1) {
                throw new ConversionException(element.unit + " holds a base unit with fractional exponent");
            }
            int pow = element.pow;
            if (pow < 0) { // Negative power.
                pow = -pow;
                cvtr = cvtr.inverse();
            }
            for (int j = 0; j < pow; j++) {
                converter = converter.concatenate(cvtr);
            }
        }
        return converter;
    }

    /**
     * Indicates if this product unit is a standard unit.
     * 
     * @return <code>true</code> if all elements are standard units; <code>false</code> otherwise.
     */
    private boolean hasOnlyStandardUnit() {
        for (Element element : this.elements) {
            Unit<?> u = element.unit;
            if (!u.isStandardUnit()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the greatest common divisor (Euclid's algorithm).
     * 
     * @param m
     *            the first number.
     * @param nn
     *            the second number.
     * @return the greatest common divisor.
     */
    private static int gcd(int m, int n) {
        if (n == 0) {
            return m;
        } else {
            return gcd(n, m % n);
        }
    }

    /**
     * Inner product element represents a rational power of a single unit.
     */
    private static final class Element implements Serializable {

        /**
         * Holds the single unit.
         */
        private final Unit<?> unit;

        /**
         * Holds the power exponent.
         */
        private final int pow;

        /**
         * Holds the root exponent.
         */
        private final int root;

        /**
         * Structural constructor.
         * 
         * @param unit
         *            the unit.
         * @param pow
         *            the power exponent.
         * @param root
         *            the root exponent.
         */
        private Element(Unit<?> unit, int pow, int root) {
            this.unit = unit;
            this.pow = pow;
            this.root = root;
        }

        /**
         * Returns this element's unit.
         * 
         * @return the single unit.
         */
        public Unit<?> getUnit() {
            return this.unit;
        }

        /**
         * Returns the power exponent. The power exponent can be negative but is always different from zero.
         * 
         * @return the power exponent of the single unit.
         */
        public int getPow() {
            return this.pow;
        }

        /**
         * Returns the root exponent. The root exponent is always greater than zero.
         * 
         * @return the root exponent of the single unit.
         */
        public int getRoot() {
            return this.root;
        }

        private static final long serialVersionUID = 1L;
    }

    private static final long serialVersionUID = 1L;
}
