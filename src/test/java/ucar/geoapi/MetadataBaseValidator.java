/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import java.util.Date;
import java.util.Collection;
import org.opengis.metadata.*;
import org.opengis.metadata.identification.*;
import org.opengis.test.ValidatorContainer;
import org.opengis.test.metadata.MetadataValidator;


/**
 * Copy of an GeoAPI-conformance class added in GeoAPI 3.1 and 4.0.
 * This copy will be removed after we upgraded the dependency from GeoAPI 3.0 to GeoAPI 3.1 or 4.0.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class MetadataBaseValidator extends MetadataValidator {
    /**
     * Creates a new validator instance.
     *
     * @param container  the set of validators to use for validating other kinds of objects
     *                   (see {@linkplain #container field javadoc}).
     */
    public MetadataBaseValidator(final ValidatorContainer container) {
        super(container, "org.opengis.metadata");
    }

    /**
     * Validates the given metadata.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final Metadata object) {
        if (object == null) {
            return;
        }
        Date creationDate = object.getDateStamp();
        mandatory("Metadata: shall have a creation date.", creationDate);

        final Collection<? extends Identification> identifications = object.getIdentificationInfo();
        mandatory("Metadata: shall have an identification information.",
                (identifications != null && identifications.isEmpty()) ? null : identifications);
    }
}
