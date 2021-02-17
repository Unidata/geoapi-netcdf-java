/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import java.util.Arrays;
import java.io.Serializable;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * A trivial implementation of {@link DirectPosition}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class SimpleDirectPosition implements DirectPosition, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -6629745469199237817L;

    /**
     * The coordinate values.
     */
    final double[] coordinates;

    /**
     * Creates a new direct position of the given dimension.
     *
     * @param  dimension  the number of dimensions.
     */
    SimpleDirectPosition(final int dimension) {
        coordinates = new double[dimension];
    }

    /**
     * Returns always {@code null}, which is allowed by the specification.
     */
    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return null;
    }

    /**
     * The length of coordinate sequence (the number of entries).
     */
    @Override
    public int getDimension() {
        return coordinates.length;
    }

    /**
     * A copy of the coordinates presented as an array of double values.
     */
    @Override
    public double[] getCoordinate() {
        return coordinates.clone();
    }

    /**
     * Returns the coordinate at the specified dimension.
     */
    @Override
    public double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        return coordinates[dimension];
    }

    /**
     * Sets the coordinate value along the specified dimension.
     */
    @Override
    public void setOrdinate(int dimension, double value) throws IndexOutOfBoundsException {
        coordinates[dimension] = value;
    }

    /**
     * Returns the direct position, which is {@code this} in this implementation.
     */
    @Override
    public DirectPosition getDirectPosition() {
        return this;
    }

    /**
     * Returns {@code true} if this direct position is equals to the given object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof DirectPosition) {
            final DirectPosition other = (DirectPosition) object;
            if (other.getCoordinateReferenceSystem() == null) {
                return Arrays.equals(coordinates, other.getCoordinate());
            }
        }
        return false;
    }

    /**
     * Returns a hash code value for this direct position.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(coordinates) ^ (int) serialVersionUID;
    }

    /**
     * Returns a string representation of this direct position in <cite>Well-Known Text</cite> (WKT) format.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Well-known_text_representation_of_geometry">Well-known text on Wikipedia</a>
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder("POINT");
        char separator = '(';
        for (final double ordinate : coordinates) {
            buffer.append(separator).append(ordinate);
            separator = ' ';
        }
        return buffer.append(')').toString();
    }
}
