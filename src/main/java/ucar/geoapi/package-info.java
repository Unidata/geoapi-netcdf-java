/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */

/**
 * GeoAPI implementation as wrappers around the
 * <a href="https://www.unidata.ucar.edu/software/netcdf-java/">netCDF</a> library.
 * This package provides adapters allowing usage of the following netCDF services
 * as an implementation of GeoAPI interfaces:
 *
 * <ul>
 *   <li>Metadata services, as wrappers around {@link ucar.nc2.NetcdfFile}.</li>
 *   <li>Referencing services, including:
 *     <ul>
 *       <li>Coordinate Reference Systems as wrappers around the netCDF {@link ucar.nc2.dataset.CoordinateSystem} object.</li>
 *       <li>Coordinate Operations as wrappers around the netCDF {@link ucar.unidata.geoloc.Projection} object.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * The GeoAPI objects are created by calls to static methods in {@link ucar.geoapi.Wrapper}.
 *
 * <h2>Limitations</h2>
 * Current implementation has the following restrictions:
 * <ul>
 *   <li>Coordinate Reference Systems wrappers support only coordinate systems with axes of kind
 *       {@link ucar.nc2.dataset.CoordinateAxis1D}.</li>
 *
 *   <li>Geodetic reference frames are assumed spherical.</li>
 * </ul>
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
package ucar.geoapi;
