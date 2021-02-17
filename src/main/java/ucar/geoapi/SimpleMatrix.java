/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import javax.vecmath.GMatrix;
import org.opengis.referencing.operation.Matrix;


/**
 * A {@link Matrix} built on top of Java3D {@code vecmath} library.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class SimpleMatrix extends GMatrix implements Matrix {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -9053188309751616226L;

    /**
     * Creates a matrix of size {@code size}&nbsp;×&nbsp;{@code size}.
     * Elements on the diagonal (<var>j</var> == <var>i</var>) are set to 1.
     */
    public SimpleMatrix(final int size) {
        super(size, size);
    }

    /**
     * Returns {@code true} if this matrix is an identity matrix.
     */
    @Override
    public boolean isIdentity() {
        final int numRow = getNumRow();
        final int numCol = getNumCol();
        if (numRow != numCol) {
            return false;
        }
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                if (getElement(j,i) != (i==j ? 1 : 0)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a clone of this matrix.
     */
    @Override
    public SimpleMatrix clone() {
        return (SimpleMatrix) super.clone();
    }
}
