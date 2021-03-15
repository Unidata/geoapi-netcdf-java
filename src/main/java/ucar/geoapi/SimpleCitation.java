/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import java.util.Locale;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Series;
import org.opengis.util.InternationalString;
import org.opengis.util.GenericName;
import org.opengis.util.NameSpace;


/**
 * A simple {@link Citation} implementation, which is also its own international string.
 * In this netCDF package, citations are used either as a cheap {@link InternationalString}
 * implementation, or for specifying the {@linkplain Identifier#getAuthority() authority}
 * of identifier codes. For this later purpose, it is convenient to also implement the
 * {@link NameSpace} interface in order to allow usage of the {@link #NETCDF} constant
 * for {@linkplain org.opengis.referencing.IdentifiedObject#getAlias() aliases} scope.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class SimpleCitation implements Citation, NameSpace, InternationalString, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -8466770625990964435L;

    /**
     * The root (or global) namespace.
     */
    static final SimpleCitation GLOBAL = new SimpleCitation("");

    /**
     * The netCDF authority citation.
     */
    static final SimpleCitation NETCDF = new SimpleCitation("NetCDF");

    /**
     * The OGC authority citation, also used as a namespace for aliases.
     */
    static final SimpleCitation OGC = new SimpleCitation("OGC");

    /**
     * The EPSG authority citation, also used as a namespace for aliases.
     */
    static final SimpleCitation EPSG = new SimpleCitation("EPSG");

    /**
     * The citation title to be returned by {@link #getTitle()}.
     */
    private final String title;

    /**
     * The citation edition to be returned by {@link #getEdition()}.
     */
    private final InternationalString edition;

    /**
     * Creates a new citation having the given title.
     *
     * @param title  the citation title to be returned by {@link #getTitle()}.
     */
    SimpleCitation(final String title) {
        this.title   = title;
        this.edition = null;
    }

    /**
     * Creates a new citation having the given title and edition.
     *
     * @param title    the citation title to be returned by {@link #getTitle()}.
     * @param edition  the citation edition to be returned by {@link #getEdition()}.
     */
    SimpleCitation(final String title, final InternationalString edition) {
        this.title   = title;
        this.edition = edition;
    }

    /*
     * Citation implementations.
     */
    @Override public InternationalString getTitle()   {return this;}
    @Override public InternationalString getEdition() {return edition;}

    /*
     * Global NameSpace implementations.
     */
    @Override public boolean isGlobal() {return true;}
    @Override public GenericName name() {return new SimpleName(GLOBAL, title);}

    /*
     * InternationalString implementations.
     */
    @Override public int     length()                              {return title.length();}
    @Override public char    charAt(int index)                     {return title.charAt(index);}
    @Override public String  subSequence(int start, int end)       {return title.substring(start, end);}
    @Override public String  toString()                            {return title;}
    @Override public String  toString(Locale locale)               {return title;}
    @Override public int     compareTo(InternationalString object) {return title.compareTo(object.toString());}
    @Override public int     hashCode()                            {return title.hashCode() ^ (int) serialVersionUID;}
    @Override public boolean equals(final Object object) {
        return (object instanceof SimpleCitation) && title.equals(((SimpleCitation) object).title);
    }

    /** Not yet implemented. */
    @Override public Collection<? extends InternationalString> getAlternateTitles() {
        return Collections.emptyList();
    }

    /** Not yet implemented. */
    @Override public Collection<? extends CitationDate> getDates() {
        return Collections.emptyList();
    }

    /** Not yet implemented. */
    @Override public Date getEditionDate() {
        return null;
    }

    /** Not yet implemented. */
    @Override public Collection<? extends Identifier> getIdentifiers() {
        return Collections.emptyList();
    }

    /** Not yet implemented. */
    @Override public Collection<? extends ResponsibleParty> getCitedResponsibleParties() {
        return Collections.emptyList();
    }

    /** Not yet implemented. */
    @Override public Collection<PresentationForm> getPresentationForms() {
        return Collections.emptyList();
    }

    /** Not yet implemented. */
    @Override public Series getSeries() {
        return null;
    }

    /** Not yet implemented. */
    @Override public InternationalString getOtherCitationDetails() {
        return null;
    }

    /** Not yet implemented. */
    @Override public InternationalString getCollectiveTitle() {
        return null;
    }

    /** Not yet implemented. */
    @Override public String getISBN() {
        return null;
    }

    /** Not yet implemented. */
    @Override public String getISSN() {
        return null;
    }
}
