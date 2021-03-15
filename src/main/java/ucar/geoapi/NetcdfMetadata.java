/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import java.net.URI;
import java.net.URISyntaxException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Attribute;
import ucar.nc2.time.Calendar;
import ucar.nc2.time.CalendarDateFormatter;
import ucar.nc2.constants.ACDD;

import org.opengis.metadata.*;
import org.opengis.metadata.acquisition.AcquisitionInformation;
import org.opengis.metadata.extent.*;
import org.opengis.metadata.spatial.*;
import org.opengis.metadata.citation.*;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.content.ContentInformation;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.maintenance.*;
import org.opengis.metadata.identification.*;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.ReferenceSystem;
import org.opengis.util.InternationalString;


/**
 * A {@link Metadata} implementation backed by a netCDF {@link NetcdfFile} object.
 * All getter methods fetch their values from the netCDF file, so change to the netCDF
 * file content will be immediately reflected in this class.
 *
 * <p>Unless otherwise noted in the javadoc, this implementation defines a one-to-one relationship
 * between the metadata attributes and netCDF attributes. This simple model allows us to implement
 * relevant interfaces in a single class. Note that this simplification may not be appropriate for
 * all usages, but this class can be used as a starting point for customized implementations.</p>
 *
 * <p>Some interfaces implemented by this class or its component objects:</p>
 * <ul>
 *   <li>{@link Metadata} is the root interface.</li>
 *   <li>{@link Identifier} implements the value returned by {@link Citation#getIdentifiers()}.
 *          <ul><li>NetCDF attributes: {@code id}, {@code naming_authority}.
 *          </li></ul></li>
 *   <li>{@link DataIdentification} implements the value returned by {@link Metadata#getIdentificationInfo()}.
 *          <ul><li>NetCDF attributes: {@code summary}, {@code purpose}, {@code topic_category}, {@code cdm_data_type},
 *          {@code comment}, {@code acknowledgment}.
 *          </li></ul></li>
 *   <li>{@link CitationDate} implements the value returned by {@link Metadata#getDateInfo()}.
 *          <ul><li>NetCDF attributes: {@code metadata_creation}.
 *          </li></ul></li>
 *   <li>{@link Citation} implements the value returned by {@link Identification#getCitation()},
 *          which contain only the creator. This simple implementation ignores the publisher and contributors.
 *          <ul><li>NetCDF attributes: {@code title}.
 *          </li></ul></li>
 *   <li>{@link Responsibility} implements the value returned by {@link Citation#getCitedResponsibleParties()},
 *          which contain only the creator and the institution.
 *          <ul><li>NetCDF attributes: {@code creator_name}, {@code institution}.
 *          </li></ul></li>
 *   <li>{@link Individual} implements the value returned by {@link Responsibility#getParties()},
 *          which contain only the creator. This simple implementation ignores the publisher and contributors.
 *          <ul><li>NetCDF attributes: {@code creator_name}.
 *          </li></ul></li>
 *   <li>{@link Address} implements the value returned by {@link Contact#getAddress()}.
 *          <ul><li>NetCDF attributes: {@code creator_email}.
 *          </li></ul></li>
 *   <li>{@link GeographicBoundingBox} implements the value returned by {@link Extent#getGeographicElements()}.
 *          <ul><li>NetCDF attributes: {@code geospatial_lon_min}, {@code geospatial_lon_max},
 *          {@code geospatial_lat_min}, {@code geospatial_lat_max}.
 *          </li></ul></li>
 * </ul>
 *
 * <p><b>Javadoc convention:</b> all public non-deprecated methods in this class can be grouped in 3 categories,
 * identifiable by the first words in their Javadoc:</p>
 *
 * <ul>
 *   <li>Each method documented as “<cite><b>Returns</b> foo…</cite>” maps directly to a netCDF attribute
 *       or hard-coded non-empty value.</li>
 *   <li>Each method documented as “<cite><b>Encapsulates</b> foo…</cite>” returns an object (often {@code this})
 *       providing the getter methods for a group of attributes. This encapsulation is done for compliance with
 *       the ISO 19115 model.</li>
 *   <li>Each method documented as “<cite><b>Default to</b> foo…</cite>” is an unimplemented property.</li>
 * </ul>
 *
 * @author  Martin Desruisseaux (Geomatys)
 *
 * @see <a href="http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/metadata/DataDiscoveryAttConvention.html">NetCDF Attribute Convention for Dataset Discovery</a>
 */
final class NetcdfMetadata extends Wrapper implements Metadata, DataIdentification, ReferenceIdentifier, Citation, CitationDate,
        OnlineResource, Address, Extent, GeographicBoundingBox     // Do not implement Party because can be Individual or Organisation.
{
    /**
     * The netCDF file given to the constructor.
     */
    protected final NetcdfFile file;

    /**
     * Creates a new metadata object as a wrapper around the given netCDF file.
     *
     * @param file  the netCDF file.
     */
    public NetcdfMetadata(final NetcdfFile file) {
        Objects.requireNonNull(file);
        this.file = file;
    }

    /**
     * Returns the netCDF object where all getter methods get information.
     *
     * @return the file from which metadata are read.
     */
    @Override
    public NetcdfFile delegate() {
        return file;
    }

    /**
     * Returns {@code this} wrapped in a singleton.
     */
    private Collection<NetcdfMetadata> self() {
        return Collections.singleton(this);
    }

    /**
     * Returns {@code this} wrapped in a singleton if the given flag is {@code true}, or an empty set otherwise.
     */
    private Collection<NetcdfMetadata> self(final boolean flag) {
        return flag ? Collections.singleton(this) : Collections.<NetcdfMetadata>emptySet();
    }

    /**
     * Returns {@code true} if the netCDF file contains an attribute of the given name.
     */
    private boolean hasAttribute(final String name) {
        return file.findGlobalAttributeIgnoreCase(name) != null;
    }

    /**
     * Returns the value of the given attribute as a string. The attribute name is case-insensitive.
     * If non-null, the returned string is {@linkplain String#trim() trimmed} and never
     * {@linkplain String#isEmpty() empty}.
     *
     * @param  name  the case-insensitive attribute name.
     * @return the non-empty attribute value, or {@code null} if none.
     */
    private String getString(final String name) {
        final Attribute attribute = file.findGlobalAttributeIgnoreCase(name);
        if (attribute != null && attribute.isString()) {
            String value = attribute.getStringValue();
            if (value != null && !(value = value.trim()).isEmpty()) {
                return value;
            }
        }
        return null;
    }

    /**
     * Returns the value of the given attribute as an upper case string.
     * This method invokes {@link #getString(String)}, then change the case of the result (if non null).
     *
     * @param  name  the case-insensitive attribute name.
     * @return the non-empty attribute value in upper-case, or {@code null} if none.
     */
    private String getUpperCase(final String name) {
        final String value = getString(name);
        return (value != null) ? value.toUpperCase() : null;
    }

    /**
     * Returns the value of the given attribute as an international string.
     * This method invokes {@link #getString(String)}, then wraps the result
     * (if non null) in an {@link InternationalString} implementation.
     *
     * @param  name  the case-insensitive attribute name.
     * @return the non-empty attribute value, or {@code null} if none.
     */
    private InternationalString getInternationalString(final String name) {
        final String value = getString(name);
        return (value != null) ? new SimpleCitation(value) : null;
    }

    /**
     * Returns the value of the given attribute as a floating point value.
     *
     * @param  name  the case-insensitive attribute name.
     * @return the attribute value, or {@code NaN} if none.
     * @throws NumberFormatException if the number can not be parsed.
     */
    private double getDouble(final String name) throws NumberFormatException {
        final Attribute attribute = file.findGlobalAttributeIgnoreCase(name);
        if (attribute != null) {
            if (attribute.isString()) {
                final String value = attribute.getStringValue();
                if (value != null) {
                    return Double.parseDouble(value);
                }
            } else {
                final Number value = attribute.getNumericValue();
                if (value != null) {
                    return value.doubleValue();
                }
            }
        }
        return Double.NaN;
    }

    /**
     * Returns the value of the given attribute as a date.
     * This method invokes {@link #getString(String)}, then parses the value.
     *
     * @param  name  the case-insensitive attribute name.
     * @return the attribute value, or {@code null} if none or can not be parsed.
     */
    private Date getDate(final String name) {
        final String value = getString(name);
        if (value != null) {
            return parseDate(value);
        }
        return null;
    }

    /**
     * Parses the given ISO date, assuming proleptic Gregorian calendar and UTC time zone.
     *
     * @param  value  the date in ISO format.
     * @return the parsed date.
     * @throws IllegalArgumentException if the given date can not be parsed.
     */
    private static Date parseDate(final String value) throws IllegalArgumentException {
        return new Date(CalendarDateFormatter.isoStringToCalendarDate(Calendar.proleptic_gregorian, value).getMillis());
    }

    /**
     * Returns the given value in a collection if non-null, or an empty collection if the
     * given value is {@code null}.
     *
     * @param <T>   The type of the element to be wrapped in a collection.
     * @param value The value to wrap in a collection.
     * @return      A collection containing the given value, or an empty collection
     *              if the value was null.
     */
    private static <T> Collection<T> singleton(final T value) {
        return (value != null) ? Collections.singleton(value) : Collections.<T>emptySet();
    }




    // ┌─────────────────────────────────────────────────────────────────────────────────────────┐
    // │    Methods that return directly an attribute value                                      │
    // └─────────────────────────────────────────────────────────────────────────────────────────┘

    /**
     * Encapsulates the {@linkplain #getCodeSpace() code space} in a citation.
     *
     * @return the authority for the dataset described by this metadata, or {@code null}.
     */
    @Override
    public Citation getAuthority() {
        final String naming = getCodeSpace();
        return (naming != null) ? new SimpleCitation(naming) : null;
    }

    /**
     * Returns the netCDF {@value ACDD#naming_authority} attribute value, or {@code null} if none.
     *
     * @see #getMetadataIdentifier()
     */
    @Override
    public String getCodeSpace() {
        return getString(ACDD.naming_authority);
    }

    /**
     * Returns the netCDF {@value ACDD#id} attribute value,
     * or the {@linkplain NetcdfFile#getId() file identifier},
     * or {@code null} if none.
     *
     * @see NetcdfFile#getId()
     * @see #getMetadataIdentifier()
     */
    @Override
    public String getCode() {
        final String id = getString(ACDD.id);
        return (id != null) ? id : file.getId();
    }

    /**
     * Returns the version of the code.
     *
     * @return the code version, or {@code null} if none.
     *
     * @hidden
     */
    @Override
    public String getVersion() {
        return null;
    }

    /**
     * Returns the name of the resource, as the filename without path and suffix.
     *
     * @return the name of the resource, or {@code null} if none.
     */
    @Override
    public String getName() {
        String name = file.getLocation();
        if (name == null) {
            return null;
        }
        int end = name.lastIndexOf('.');
        if (end < 0) end = name.length();
        final int start = name.lastIndexOf('/', end);
        if (start >= 0) {
            name = name.substring(start + 1, end);
        } else {
            name = new File(name.substring(0, end)).getName();
        }
        return name.replace('_', ' ');
    }

    /**
     * Returns the netCDF {@value ACDD#title} attribute value, or the
     * {@linkplain NetcdfFile#getTitle() file title}, or {@code null} if none.
     *
     * @return the title, or {@code null} if none.
     *
     * @see NetcdfFile#getTitle()
     */
    @Override
    public InternationalString getTitle() {
        String title = getString(ACDD.title);
        if (title == null) {
            title = file.getTitle();
            if (title == null) {
                return null;
            }
        }
        return new SimpleCitation(title);
    }

    /**
     * Returns the netCDF {@value ACDD#summary} attribute value, or {@code null} if none.
     */
    @Override
    public InternationalString getAbstract() {
        return getInternationalString(ACDD.summary);
    }

    /**
     * Returns the netCDF {@code "purpose"} attribute value, or {@code null} if none.
     */
    @Override
    public InternationalString getPurpose() {
        return getInternationalString("purpose");
    }

    /**
     * Returns the netCDF {@code "topic_category"} attribute value, or an empty set if none.
     */
    @Override
    public Collection<TopicCategory> getTopicCategories() {
        final String value = getUpperCase("topic_category");
        if (value == null) return Collections.emptySet();
        final Set<TopicCategory> categories = new HashSet<>();
        for (final String element : value.split(",")) {
            categories.add(TopicCategory.valueOf(element.replace(' ', '_').trim()));
        }
        return categories;
    }

    /**
     * Returns the netCDF {@value ACDD#cdm_data_type} attribute value, or an empty set if none.
     */
    @Override
    public Collection<SpatialRepresentationType> getSpatialRepresentationTypes() {
        return singleton(SpatialRepresentationType.valueOf(getUpperCase(ACDD.cdm_data_type)));
    }

    /**
     * Returns the netCDF {@value ACDD#creator_email} attribute value, or {@code null} if none.
     */
    @Override
    public Collection<String> getElectronicMailAddresses() {
        return singleton(getString(ACDD.creator_email));
    }

    /**
     * Returns the netCDF {@code "acknowledgment"} attribute value, or an empty set if none.
     */
    @Override
    public Collection<String> getCredits() {
        String value = getString("acknowledgment");
        if (value == null) {
            value = getString(ACDD.acknowledgement);
        }
        return singleton(value);
    }

    /**
     * Returns {@code true} if this metadata has a date.
     */
    private boolean hasDate(final boolean data) {
        return hasAttribute(data ? ACDD.date_created : "metadata_creation");
    }

    /**
     * Encapsulates the netCDF {@code "metadata_creation"} attribute value, or {@code null} if none.
     * This is the time when metadata have been created (not necessarily the time when data have been collected).
     */
    @Override
    public Date getDateStamp() {
        return getDate("metadata_creation");
    }

    /**
     * Encapsulates the netCDF {@value ACDD#date_created} attribute value.
     */
    @Override
    public Collection<? extends CitationDate> getDates() {
        return self(hasDate(true));
    }

    /**
     * Returns the netCDF {@value ACDD#date_created} attribute value, or {@code null} if none.
     * This is the creation date of the actual dataset, not necessarily the same that the
     * metadata creation time.
     *
     * @return the creation date, or {@code null} if none.
     */
    @Override
    public Date getDate() {
        return getDate(ACDD.date_created);
    }

    /**
     * Returns {@link DateType#CREATION}.
     * Note that the citation encapsulated by this implementation is only for the creator.
     * The contributors and publishers are not supported by this simple implementation.
     */
    @Override
    public DateType getDateType() {
        return DateType.CREATION;
    }

    /**
     * Returns the netCDF {@value ACDD#LON_MIN} attribute value, or {@linkplain Double#NaN NaN} if none.
     */
    @Override
    public double getWestBoundLongitude() {
        return getDouble(ACDD.LON_MIN);
    }

    /**
     * Returns the netCDF {@value ACDD#LON_MAX} attribute value, or {@linkplain Double#NaN NaN} if none.
     */
    @Override
    public double getEastBoundLongitude() {
        return getDouble(ACDD.LON_MAX);
    }

    /**
     * Returns the netCDF {@code ACDD#LAT_MIN} attribute value, or {@linkplain Double#NaN NaN} if none.
     */
    @Override
    public double getSouthBoundLatitude() {
        return getDouble(ACDD.LAT_MIN);
    }

    /**
     * Returns the netCDF {@value ACDD#LAT_MAX} attribute value, or {@linkplain Double#NaN NaN} if none.
     */
    @Override
    public double getNorthBoundLatitude() {
        return getDouble(ACDD.LAT_MAX);
    }

    /**
     * Returns {@link Boolean#TRUE} since the geographic bounding box is inclusive.
     */
    @Override
    public Boolean getInclusion() {
        return Boolean.TRUE;
    }

    /**
     * Returns the netCDF {@linkplain NetcdfFile#getLocation() file location}, or {@code null} if none.
     *
     * @return the file path, or {@code null} if none.
     */
    @Override
    public URI getLinkage() {
        final String location = file.getLocation();
        if (location != null) try {
            return new URI(location);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        return null;
    }

    /**
     * Returns the {@code getLinkage()} purpose, which is to access the file.
     *
     * @return {@link OnLineFunction#FILE_ACCESS}.
     */
    @Override
    public OnLineFunction getFunction() {
        return OnLineFunction.valueOf("FILE_ACCESS");
    }

    /**
     * Returns the netCDF {@value ACDD#comment} attribute value, or {@code null} if none.
     */
    @Override
    public InternationalString getSupplementalInformation() {
        return getInternationalString(ACDD.comment);
    }




    // ┌─────────────────────────────────────────────────────────────────────────────────────────┐
    // │    Indirection levels to the above attributes                                           │
    // └─────────────────────────────────────────────────────────────────────────────────────────┘

    /**
     * Encapsulates the {@linkplain #getAuthority() naming authority} together with the
     * {@linkplain #getCode() identifier code} for this data.
     *
     * @return the authority and the code for this data.
     *
     * @see #getAuthority()
     * @see #getCode()
     */
    @Override
    public Collection<? extends Identifier> getIdentifiers() {
        return self();
    }

    /**
     * Encapsulates the data identifiers,
     * {@linkplain #getCitation() creator} and other information.
     */
    @Override
    public Collection<? extends Identification> getIdentificationInfo() {
        return self();
    }

    /**
     * Encapsulates the {@linkplain #getTitle() title}, {@linkplain #getPointOfContacts() creator}
     * and {@linkplain #getDatasetDate() data creation time}.
     *
     * @return {@code this}.
     */
    @Override
    public Citation getCitation() {
        return this;
    }

    /**
     * Encapsulates the role, name, contact and position information for individuals or organisations.
     */
    @Override
    public Collection<? extends ResponsibleParty> getCitedResponsibleParties() {
        return Collections.singleton(new Creator());
    }

    /**
     * Encapsulates the {@linkplain #getName() creator name} and
     * {@linkplain #getElectronicMailAddresses() email address}.
     * The responsible party represented by this class is associated to {@link Role#ORIGINATOR} instead than
     * {@link Role#POINT_OF_CONTACT} because this simple implementation can not be associated to more than
     * one responsible party. However library vendors are encouraged to provide more accurate implementations.
     *
     * <p>Defined in a separated class because of methods clash:</p>
     * <ul>
     *   <li>{@link Party#getName()} not the same name than {@link OnlineResource#getName()}.</li>
     *   <li>{@link Contact#getOnlineResources()} not the same link than {@link Citation#getOnlineResources()}.</li>
     *   <li>{@link Responsibility#getExtents()} not the same than {@link DataIdentification#getExtents()}.</li>
     * </ul>
     */
    private final class Creator implements ResponsibleParty, Contact {
        /**
         * Returns email address at which the individual may be contacted.
         */
        @Override
        public Address getAddress() {
            return hasAttribute(ACDD.creator_email) ? NetcdfMetadata.this : null;
        }

        /**
         * Returns {@link Role#ORIGINATOR}, because the citation encapsulated by this implementation is only
         * for the creator. The contributors and publishers are not supported by this simple implementation.
         */
        @Override
        public Role getRole() {
            return Role.ORIGINATOR;
        }

        @Override
        public InternationalString getPositionName() {
            return null;
        }

        @Override
        public InternationalString getOrganisationName() {
            return getInternationalString("institution");
        }

        /**
         * Returns the netCDF {@value ACDD#creator_name} attribute value, or {@code null} if none.
         */
        @Override
        public String getIndividualName() {
            return getString(ACDD.creator_name);
        }

        /**
         * Encapsulates the creator {@linkplain #getElectronicMailAddresses() email address}.
         */
        @Override
        public Contact getContactInfo() {
            return hasAttribute(ACDD.creator_email) ? this : null;
        }

        @Override public Telephone           getPhone()               {return null;}
        @Override public OnlineResource      getOnlineResource()      {return null;}
        @Override public InternationalString getHoursOfService()      {return null;}
        @Override public InternationalString getContactInstructions() {return null;}
    }

    /**
     * Defaults to a synonymous for the {@linkplain #getPointOfContacts() point of contacts}
     * in this simple implementation. Note that in theory, those two methods are not strictly
     * synonymous since {@code getContacts()} shall return the contact for the <em>metadata</em>,
     * while {@code getPointOfContacts()} shall return the contact for the <em>data</em>.
     * However the attributes in netCDF files usually don't make this distinction.
     */
    @Override
    public Collection<? extends ResponsibleParty> getContacts() {
        return getPointOfContacts();
    }

    /**
     * Encapsulates the {@linkplain #getGeographicElements() geographic bounding box}.
     */
    @Override
    public Collection<? extends Extent> getExtents() {
        return self(hasAttribute(ACDD.LON_MIN) || hasAttribute(ACDD.LON_MAX)
                 || hasAttribute(ACDD.LAT_MIN) || hasAttribute(ACDD.LAT_MAX));
    }

    /**
     * Encapsulates the geographic bounding box.
     */
    @Override
    public Collection<? extends GeographicExtent> getGeographicElements() {
        return self();
    }




    // ┌─────────────────────────────────────────────────────────────────────────────────────────┐
    // │    Non-implemented methods                                                              │
    // └─────────────────────────────────────────────────────────────────────────────────────────┘

    /** @hidden */ @Override public InternationalString getDescription() {return null;}
    /** @hidden */ @Override public String getFileIdentifier() {return null;}
    /** @hidden */ @Override public Locale getLanguage() {return null;}
    /** @hidden */ @Override public CharacterSet getCharacterSet() {return null;}
    /** @hidden */ @Override public String getParentIdentifier() {return null;}
    /** @hidden */ @Override public Collection<ScopeCode> getHierarchyLevels() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<String> getHierarchyLevelNames() {return Collections.emptyList();}
    /** @hidden */ @Override public String getMetadataStandardName() {return "ISO 19115-2:2009(E)";}
    /** @hidden */ @Override public String getMetadataStandardVersion() {return null;}
    /** @hidden */ @Override public String getDataSetUri() {return null;}
    /** @hidden */ @Override public Collection<Locale> getLocales() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends SpatialRepresentation> getSpatialRepresentationInfo() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends ReferenceSystem> getReferenceSystemInfo() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends MetadataExtensionInformation> getMetadataExtensionInfo() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends ContentInformation> getContentInfo() {return Collections.emptyList();}
    /** @hidden */ @Override public Distribution getDistributionInfo() {return null;}
    /** @hidden */ @Override public Collection<? extends DataQuality> getDataQualityInfo() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends PortrayalCatalogueReference> getPortrayalCatalogueInfo() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends Constraints> getMetadataConstraints() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends ApplicationSchemaInformation> getApplicationSchemaInfo() {return Collections.emptyList();}
    /** @hidden */ @Override public MaintenanceInformation getMetadataMaintenance() {return null;}
    /** @hidden */ @Override public Collection<? extends AcquisitionInformation> getAcquisitionInformation() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends Resolution> getSpatialResolutions() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<Locale> getLanguages() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<CharacterSet> getCharacterSets() {return Collections.emptyList();}
    /** @hidden */ @Override public InternationalString getEnvironmentDescription() {return null;}
    /** @hidden */ @Override public Collection<Progress> getStatus() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends ResponsibleParty> getPointOfContacts() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends MaintenanceInformation> getResourceMaintenances() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends BrowseGraphic> getGraphicOverviews() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends Format> getResourceFormats() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends Keywords> getDescriptiveKeywords() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends Usage> getResourceSpecificUsages() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends Constraints> getResourceConstraints() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends AggregateInformation> getAggregationInfo() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends InternationalString> getAlternateTitles() {return Collections.emptyList();}
    /** @hidden */ @Override public InternationalString getEdition() {return null;}
    /** @hidden */ @Override public Date getEditionDate() {return null;}
    /** @hidden */ @Override public Collection<PresentationForm> getPresentationForms() {return Collections.emptyList();}
    /** @hidden */ @Override public Series getSeries() {return null;}
    /** @hidden */ @Override public InternationalString getOtherCitationDetails() {return null;}
    /** @hidden */ @Override public InternationalString getCollectiveTitle() {return null;}
    /** @hidden */ @Override public String getISBN() {return null;}
    /** @hidden */ @Override public String getISSN() {return null;}
    /** @hidden */ @Override public String getProtocol() {return null;}
    /** @hidden */ @Override public String getApplicationProfile() {return null;}
    /** @hidden */ @Override public Collection<String> getDeliveryPoints() {return Collections.emptyList();}
    /** @hidden */ @Override public InternationalString getCity() {return null;}
    /** @hidden */ @Override public InternationalString getAdministrativeArea() {return null;}
    /** @hidden */ @Override public String getPostalCode() {return null;}
    /** @hidden */ @Override public InternationalString getCountry() {return null;}
    /** @hidden */ @Override public Collection<? extends TemporalExtent> getTemporalElements() {return Collections.emptyList();}
    /** @hidden */ @Override public Collection<? extends VerticalExtent> getVerticalElements() {return Collections.emptyList();}


    // ┌─────────────────────────────────────────────────────────────────────────────────────────┐
    // │    Other methods                                                                        │
    // └─────────────────────────────────────────────────────────────────────────────────────────┘

    /**
     * Returns the concatenation of {@linkplain #getAuthority() naming authority},
     * the {@code ':'} character and the {@linkplain #getCode() identifier code}.
     * One or both of the authority and the code can be null.
     *
     * @return the identifier code in the naming authority space, or {@code null} if null.
     */
    @Override
    public String toString() {
        final String code = getCode();
        final String codeSpace = getCodeSpace();
        if (codeSpace == null) {
            return code;
        }
        return (code != null) ? codeSpace + ':' + code : codeSpace;
    }
}
