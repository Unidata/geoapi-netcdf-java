/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import java.net.URL;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;


/**
 * Copy of an GeoAPI-conformance class added in GeoAPI 3.1 and 4.0.
 * This copy will be removed after we upgraded the dependency from GeoAPI 3.0 to GeoAPI 3.1 or 4.0.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
enum TestData {
    /**
     * A two-dimensional netCDF file using a geographic CRS for global data over the world.
     */
    NETCDF_2D_GEOGRAPHIC("Cube2D_geographic_packed.nc", 12988),

    /**
     * A four-dimensional netCDF file using a projected CRS with elevation and time.
     */
    NETCDF_4D_PROJECTED("Cube4D_projected_float.nc", 14544);

    /**
     * Name of the test file, located in the same directory (after JAR packaging) than the {@code TestData.class} file.
     */
    private final String filename;

    /**
     * Expected length in bytes.
     */
    private final int length;

    /**
     * Creates a new enumeration value.
     */
    private TestData(final String filename, final int length) {
        this.filename = filename;
        this.length = length;
    }

    /**
     * Returns a URL to the test file.
     * The URL is not necessary a file on the default file system; it may be an entry inside a JAR file.
     * If a path on the file system is desired, use {@link #file()} instead.
     *
     * @return a URL to the test file, possibly as an entry inside a JAR file.
     */
    final URL location() {
        final URL location = TestData.class.getResource(filename);
        assertNotNull(filename, location);
        return location;
    }

    /**
     * Returns the full content of the test file as an array of bytes.
     *
     * @return the test file content.
     * @throws IOException if an error occurred while reading the test file.
     */
    final byte[] content() throws IOException {
        final byte[] content = new byte[length];
        try (InputStream stream = TestData.class.getResourceAsStream(filename)) {
            int offset = 0, r, n;
            do {
                r = length - offset;
                n = stream.read(content, offset, r);
                if (n < 0) throw new EOFException(filename);
            } while (r != n);
            if (stream.read() >= 0) {
                throw new IOException("Unexpected file length.");
            }
        }
        return content;
    }
}
