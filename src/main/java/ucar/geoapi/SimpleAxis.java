/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import javax.measure.Unit;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.RangeMeaning;


/**
 * A {@link CoordinateSystemAxis} for longitude or latitude that are not backed by a netCDF object.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class SimpleAxis extends NetcdfIdentifiedObject implements CoordinateSystemAxis {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 9016839231124704236L;

    /**
     * The <cite>geodetic longitude</cite> axis.
     * Values are increasing toward North, in decimal degrees.
     */
    static final CoordinateSystemAxis LONGITUDE = new SimpleAxis('λ');

    /**
     * The <cite>geodetic latitude</cite> axis.
     * Values are increasing toward East, in decimal degrees.
     */
    static final CoordinateSystemAxis LATITUDE = new SimpleAxis('φ');

    /**
     * The abbreviation used for this coordinate system axes. This abbreviation is also
     * used to identify the ordinates in coordinate tuple. Examples are "λ" and
     * "φ".
     *
     * @see #getAbbreviation()
     */
    private final char abbreviation;

    /**
     * Creates a new axis for the given authority, name and abbreviation. The axis direction
     * and units are inferred from the abbreviation.
     *
     * @param abbreviation  the abbreviation used for this coordinate system axes.
     */
    private SimpleAxis(final char abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * Returns the abbreviation, since this is the criterion used for identifying the axis.
     */
    @Override
    public Character delegate() {
        return abbreviation;
    }

    /**
     * The abbreviation used for this coordinate system axes. This abbreviation is also
     * used to identify the ordinates in coordinate tuple. Examples are "λ" and "φ".
     */
    @Override
    public String getAbbreviation() {
        return String.valueOf(abbreviation);
    }

    /**
     * Name of this coordinate system axis.
     */
    @Override
    public String getCode() {
        switch (abbreviation) {
            case 'λ': return "geodetic longitude";
            case 'φ': return "geodetic latitude";
            default:  return null;                  // Should never happen.
        }
    }

    /**
     * Direction of this coordinate system axis.
     */
    @Override
    public AxisDirection getDirection() {
        switch (abbreviation) {
            case 'λ': return AxisDirection.EAST;
            case 'φ': return AxisDirection.NORTH;
            default:  return null;                  // Should never happen.
        }
    }

    /**
     * Returns the minimum value normally allowed for this axis, in the {@linkplain #getUnit unit
     * of measure for the axis}. The current implementation infers the value from the abbreviation
     * symbol.
     */
    @Override
    public double getMinimumValue() {
        switch (abbreviation) {
            case 'λ': return -180;
            case 'φ': return  -90;
            default:  return Double.NEGATIVE_INFINITY;
        }
    }

    /**
     * Returns the maximum value normally allowed for this axis, in the {@linkplain #getUnit unit
     * of measure for the axis}. The current implementation infers the value from the abbreviation
     * symbol.
     */
    @Override
    public double getMaximumValue() {
        switch (abbreviation) {
            case 'λ': return 180;
            case 'φ': return  90;
            default:  return Double.POSITIVE_INFINITY;
        }
    }

    /**
     * Returns the meaning of axis value range specified by the {@linkplain #getMinimumValue
     * minimum} and {@linkplain #getMaximumValue maximum} values. The current implementation
     * infers the value from the abbreviation symbol.
     */
    @Override
    public RangeMeaning getRangeMeaning() {
        switch (abbreviation) {
            case 'λ': return RangeMeaning.WRAPAROUND;
            case 'φ': return RangeMeaning.EXACT;
            default:  return null;
        }
    }

    /**
     * The unit of measure used for this coordinate system axis.
     */
    @Override
    public Unit<?> getUnit() {
        return Units.DEGREE;
    }

    // Do not override `hashCode()` and `equals(Object)` — implementation in parent class is sufficient.
}
