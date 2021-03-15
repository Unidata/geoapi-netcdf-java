/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import org.opengis.referencing.datum.PrimeMeridian;


/**
 * The Greenwich prime meridian, implemented as a separated class because ISO 19111
 * requires the name to be <cite>"Greenwich"</cite>.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class SimplePrimeMeridian extends NetcdfIdentifiedObject implements PrimeMeridian {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 7605367861806200503L;

    /**
     * The unique instance of the Greenwich class.
     */
    static final SimplePrimeMeridian GREENWICH = new SimplePrimeMeridian();

    /**
     * Creates the unique instance of the Greenwich class.
     */
    private SimplePrimeMeridian() {
        super();
    }

    /**
     * Returns the prime meridian name.
     */
    @Override
    public String delegate() {
        return getCode();
    }

    /**
     * Returns the prime meridian name.
     */
    @Override
    public String getCode() {
        return "Greenwich";
    }

    /**
     * Returns the Greenwich longitude, which is 0°.
     */
    @Override
    public double getGreenwichLongitude() {
        return 0;
    }

    /**
     * Returns the units of the {@link #getGreenwichLongitude()} measurement.
     */
    @Override
    public Unit<Angle> getAngularUnit() {
        return Units.DEGREE;
    }
}
