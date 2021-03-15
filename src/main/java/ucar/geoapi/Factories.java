/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import java.util.Map;
import java.util.HashMap;
import java.util.ServiceLoader;
import org.opengis.util.Factory;
import org.opengis.util.FactoryException;


/**
 * The factories needed for {@code geoapi-netcdf} working.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class Factories {
    /**
     * The factories, created when first needed.
     */
    private static final Map<Class<? extends Factory>, Factory> FACTORIES = new HashMap<>();

    /**
     * Do now allow instantiation.
     */
    private Factories() {
    }

    /**
     * Returns an instance of the factory of the given type.
     *
     * @param  type  the factory type.
     * @return an instance of the factory of the given type, or {@code null}.
     * @throws FactoryException if no factory can be found for the given type.
     */
    static <T extends Factory> T getFactory(final Class<T> type) throws FactoryException {
        synchronized (FACTORIES) {
            final T factory = type.cast(FACTORIES.get(type));
            if (factory != null) {
                return factory;
            }
            for (final T candidate : ServiceLoader.load(type)) {
                // TODO: If we want to apply some filtering, do it here.
                FACTORIES.put(type, candidate);
                return candidate;
            }
        }
        throw new FactoryException("No " + type.getSimpleName() + " found.");
    }
}
